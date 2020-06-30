package no.nav.syfo.ws;

import no.altinn.schemas.services.intermediary.receipt._2009._10.ReceiptExternal;
import no.altinn.schemas.services.intermediary.receipt._2009._10.ReceiptStatusEnum;
import no.altinn.services.serviceengine.correspondence._2009._10.ICorrespondenceAgencyExternalBasic;
import no.altinn.services.serviceengine.correspondence._2009._10.ICorrespondenceAgencyExternalBasicInsertCorrespondenceBasicV2AltinnFaultFaultFaultMessage;
import no.nav.syfo.domain.OppfolgingsplanAltinn;
import no.nav.syfo.pdl.PdlConsumer;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.xml.ws.soap.SOAPFaultException;
import java.util.Optional;

import static no.nav.syfo.ws.mappers.OppfoelgingsdialogAltinnMapper.oppfoelgingsdialogTilCorrespondence;
import static org.slf4j.LoggerFactory.getLogger;

@Service
public class AltinnConsumer {

    private static final Logger log = getLogger(AltinnConsumer.class);

    @Value("${altinnuser.username}")
    private String alltinnUsername;
    @Value("${altinnuser.password}")
    private String altinnPassword;

    public static final String SYSTEM_USER_CODE = "NAV_DIGISYFO";
    private static final String FEIL_VED_SENDING_AV_OPPFOELGINGSPLAN_TIL_ALTINN = "Feil ved sending av oppfølgingsplan til Altinn";

    private final ICorrespondenceAgencyExternalBasic insertCorrespondenceBasic;
    private final PdlConsumer pdlConsumer;

    @Inject
    public AltinnConsumer(
            ICorrespondenceAgencyExternalBasic insertCorrespondenceBasic,
            PdlConsumer pdlConsumer

    ) {
        this.insertCorrespondenceBasic = insertCorrespondenceBasic;
        this.pdlConsumer = pdlConsumer;
    }

    public Integer sendOppfolgingsplanTilArbeidsgiver(OppfolgingsplanAltinn oppfolgingplanAltinn) {
        Optional<String> brukersNavn = Optional.ofNullable(pdlConsumer.personName(oppfolgingplanAltinn.oppfolgingsplan.arbeidstaker.fnr));
        try {
            ReceiptExternal receiptExternal = insertCorrespondenceBasic.insertCorrespondenceBasicV2(
                    alltinnUsername,
                    altinnPassword,
                    SYSTEM_USER_CODE,
                    oppfolgingplanAltinn.oppfolgingsplan.uuid,
                    oppfoelgingsdialogTilCorrespondence(oppfolgingplanAltinn, brukersNavn.orElseThrow(() -> {
                        log.error("Fikk uventet feil fra TPS");
                        return new RuntimeException(FEIL_VED_SENDING_AV_OPPFOELGINGSPLAN_TIL_ALTINN);
                    }))
            );
            if (receiptExternal.getReceiptStatusCode() != ReceiptStatusEnum.OK) {
                log.error("Fikk uventet statuskode fra Altinn {}", receiptExternal.getReceiptStatusCode());
                throw new RuntimeException(FEIL_VED_SENDING_AV_OPPFOELGINGSPLAN_TIL_ALTINN);
            }
            return receiptExternal.getReceiptId();
        } catch (ICorrespondenceAgencyExternalBasicInsertCorrespondenceBasicV2AltinnFaultFaultFaultMessage e) {
            log.error(FEIL_VED_SENDING_AV_OPPFOELGINGSPLAN_TIL_ALTINN, e);
            throw new RuntimeException(FEIL_VED_SENDING_AV_OPPFOELGINGSPLAN_TIL_ALTINN, e);
        } catch (SOAPFaultException e) {
            log.error(FEIL_VED_SENDING_AV_OPPFOELGINGSPLAN_TIL_ALTINN, e);
            throw e;
        }
    }
}
