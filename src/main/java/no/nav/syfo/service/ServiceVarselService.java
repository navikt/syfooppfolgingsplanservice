package no.nav.syfo.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.melding.virksomhet.varsel.v1.varsel.*;
import no.nav.syfo.model.Kontaktinfo;
import no.nav.syfo.model.Varseltype;
import org.springframework.beans.factory.annotation.*;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static java.util.UUID.randomUUID;
import static no.nav.syfo.util.JAXB.marshallVarsel;
import static no.nav.syfo.util.JmsUtil.messageCreator;

@Slf4j
@Service
@Transactional
public class ServiceVarselService {

    @Value("${tjenester.url}")
    private String tjenesterUrl;

    private JmsTemplate servicevarselqueue;
    private DkifService dkifService;

    @Autowired
    public ServiceVarselService(
            @Qualifier("servicevarselqueue") JmsTemplate servicevarselqueue,
            DkifService dkifService
    ) {
        this.servicevarselqueue = servicevarselqueue;
        this.dkifService = dkifService;
    }

    public void sendServiceVarsel(String aktoerId, Varseltype varseltype, Long oppfoelgingsdialogId) {
        Kontaktinfo kontaktinfo = dkifService.hentKontaktinfoAktoerId(aktoerId);
        if (!kontaktinfo.skalHaVarsel) {
            log.warn("Bruker {} skal ikke ha varsel pga {}", aktoerId, kontaktinfo.feilAarsak.name());
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
