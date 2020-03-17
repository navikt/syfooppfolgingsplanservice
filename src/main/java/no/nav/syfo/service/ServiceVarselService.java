package no.nav.syfo.service;

import no.nav.melding.virksomhet.varsel.v1.varsel.*;
import no.nav.syfo.dkif.DigitalKontaktinfo;
import no.nav.syfo.dkif.DkifConsumer;
import no.nav.syfo.model.Varseltype;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.*;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static java.util.UUID.randomUUID;
import static no.nav.syfo.util.JAXB.marshallVarsel;
import static no.nav.syfo.util.JmsUtil.messageCreator;
import static org.slf4j.LoggerFactory.getLogger;

@Service
@Transactional
public class ServiceVarselService {

    private static final Logger log = getLogger(ServiceVarselService.class);

    @Value("${tjenester.url}")
    private String tjenesterUrl;

    private JmsTemplate servicevarselqueue;
    private DkifConsumer dkifConsumer;

    @Autowired
    public ServiceVarselService(
            @Qualifier("servicevarselqueue") JmsTemplate servicevarselqueue,
            DkifConsumer dkifConsumer
    ) {
        this.servicevarselqueue = servicevarselqueue;
        this.dkifConsumer = dkifConsumer;
    }

    public void sendServiceVarsel(String aktoerId, Varseltype varseltype, Long oppfoelgingsdialogId) {
        DigitalKontaktinfo digitalKontaktinfo = dkifConsumer.kontaktinformasjon(aktoerId);
        if (!digitalKontaktinfo.getKanVarsles()) {
            log.warn("Bruker {} skal ikke ha varsel", aktoerId);
            return;
        }
        XMLVarsel xmlVarsel = new XMLVarsel()
                .withMottaker(new XMLAktoerId(aktoerId))
                .withVarslingstype(new XMLVarslingstyper(varseltype.name(), null, null))
                .withParameterListes(
                        new XMLParameter("dittnavUrl", tjenesterUrl + "/sykefravaer/oppfolgingsplaner/" + oppfoelgingsdialogId),
                        new XMLParameter("url", tjenesterUrl + "/sykefravaer")
                );

        String xml = marshallVarsel(xmlVarsel);
        servicevarselqueue.send(messageCreator(xml, randomUUID().toString()));
    }


}
