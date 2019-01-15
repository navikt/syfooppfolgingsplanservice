package no.nav.syfo.ws;

import no.altinn.schemas.services.intermediary.receipt._2009._10.ReceiptExternal;
import no.altinn.schemas.services.intermediary.receipt._2009._10.ReceiptStatusEnum;
import no.altinn.services.serviceengine.correspondence._2009._10.ICorrespondenceAgencyExternalBasic;
import no.altinn.services.serviceengine.correspondence._2009._10.ICorrespondenceAgencyExternalBasicInsertCorrespondenceBasicV2AltinnFaultFaultFaultMessage;
import no.nav.metrics.aspects.Timed;
import no.nav.syfo.domain.OppfoelgingsdialogAltinn;
import no.nav.syfo.service.BrukerprofilService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.xml.ws.soap.SOAPFaultException;
import java.util.Optional;

import static java.lang.System.getProperty;
import static no.nav.syfo.ws.mappers.OppfoelgingsdialogAltinnMapper.oppfoelgingsdialogTilCorrespondence;

@Service
public class AltinnConsumer {

    public static final Logger LOG = LoggerFactory.getLogger(AltinnConsumer.class);

    public static final String SYSTEM_USER_CODE = "NAV_DIGISYFO";
    private static final String FEIL_VED_SENDING_AV_OPPFOELGINGSPLAN_TIL_ALTINN = "Feil ved sending av oppfølgingsplan til Altinn";

    private ICorrespondenceAgencyExternalBasic insertCorrespondenceBasic;
    private BrukerprofilService brukerprofilService;

    private String brukernavn() {
        return getProperty("altinnUser.username");
    }

    private String passord() {
        return getProperty("altinnUser.password");
    }

    @Timed(name = "oppfoelgingsplanTilAltinn")
    public Integer sendOppfoelgingsplanTilArbeidsgiver(OppfoelgingsdialogAltinn oppfoelgingsdialogAltinn) {
        Optional<String> brukersNavn = brukerprofilService.hentNavnByFnrForAsynkOppgave(oppfoelgingsdialogAltinn.oppfoelgingsdialog.arbeidstaker.fnr);
        try {
            ReceiptExternal receiptExternal = insertCorrespondenceBasic.insertCorrespondenceBasicV2(
                    brukernavn(),
                    passord(),
                    SYSTEM_USER_CODE,
                    oppfoelgingsdialogAltinn.oppfoelgingsdialog.uuid,
                    oppfoelgingsdialogTilCorrespondence(oppfoelgingsdialogAltinn, brukersNavn.orElseThrow(() -> {
                        LOG.error("Fikk uventet feil fra TPS");
                        return new RuntimeException(FEIL_VED_SENDING_AV_OPPFOELGINGSPLAN_TIL_ALTINN);
                    }))
            );
            if (receiptExternal.getReceiptStatusCode() != ReceiptStatusEnum.OK) {
                LOG.error("Fikk uventet statuskode fra Altinn {}", receiptExternal.getReceiptStatusCode());
                throw new RuntimeException(FEIL_VED_SENDING_AV_OPPFOELGINGSPLAN_TIL_ALTINN);
            }
            return receiptExternal.getReceiptId();
        } catch (ICorrespondenceAgencyExternalBasicInsertCorrespondenceBasicV2AltinnFaultFaultFaultMessage e) {
            LOG.error(FEIL_VED_SENDING_AV_OPPFOELGINGSPLAN_TIL_ALTINN, e);
            throw new RuntimeException(FEIL_VED_SENDING_AV_OPPFOELGINGSPLAN_TIL_ALTINN, e);
        } catch (SOAPFaultException e) {
            LOG.error(FEIL_VED_SENDING_AV_OPPFOELGINGSPLAN_TIL_ALTINN, e);
            throw e;
        }
    }

    @Inject
    public void setInsertCorrespondenceV2(ICorrespondenceAgencyExternalBasic insertCorrespondenceBasic) {
        this.insertCorrespondenceBasic = insertCorrespondenceBasic;
    }

    @Inject
    public void setBrukerprofilService(BrukerprofilService brukerprofilService) {
        this.brukerprofilService = brukerprofilService;
    }
}
