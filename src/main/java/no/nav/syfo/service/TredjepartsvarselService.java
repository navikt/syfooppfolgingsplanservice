package no.nav.syfo.service;

import no.nav.melding.virksomhet.servicemeldingmedkontaktinformasjon.v1.servicemeldingmedkontaktinformasjon.*;

import no.nav.syfo.aktorregister.AktorregisterConsumer;
import no.nav.syfo.model.*;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.*;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static no.nav.syfo.util.JAXB.marshallTredjepartsServiceMelding;
import static no.nav.syfo.util.JmsUtil.messageCreator;
import static org.slf4j.LoggerFactory.getLogger;

@Service
@Transactional
public class TredjepartsvarselService {

    private static final Logger log = getLogger(TredjepartsvarselService.class);

    @Value("${tjenester.url}")
    private String tjenesterUrl;

    private JmsTemplate tredjepartsvarselqueue;

    AktorregisterConsumer aktorregisterConsumer;

    @Autowired
    public TredjepartsvarselService(@Qualifier("tredjepartsvarselqueue") JmsTemplate tredjepartsvarselqueue, AktorregisterConsumer aktorregisterConsumer) {
        this.tredjepartsvarselqueue = tredjepartsvarselqueue;
        this.aktorregisterConsumer = aktorregisterConsumer;
    }

    public void sendVarselTilNaermesteLeder(Varseltype varseltype, Naermesteleder naermesteleder) {
        List<WSParameter> parametere = Collections.singletonList(
                createParameter("url", tjenesterUrl + "/sykefravaerarbeidsgiver/")
        );
        WSServicemeldingMedKontaktinformasjon melding = new WSServicemeldingMedKontaktinformasjon();

        String narmesteLederAktorId = aktorregisterConsumer.hentAktorIdForFnr(naermesteleder.naermesteLederFnr);

        populerServiceMelding(melding, kontaktinformasjon(naermesteleder), narmesteLederAktorId, naermesteleder.orgnummer, varseltype, parametere);
        log.info("Melding: " + melding);

        String xml = marshallTredjepartsServiceMelding(new ObjectFactory().createServicemelding(melding));
        tredjepartsvarselqueue.send(messageCreator(xml, randomUUID().toString()));
    }

    private List<WSKontaktinformasjon> kontaktinformasjon(Naermesteleder tredjepartsKontaktinfo) {
        return asList(
                opprettKontaktinformasjon(tredjepartsKontaktinfo.epost, "EPOST"),
                opprettKontaktinformasjon(tredjepartsKontaktinfo.mobil, "SMS")
        );
    }

    private void populerServiceMelding(WSServicemeldingMedKontaktinformasjon servicemeldingMedKontaktinformasjon,
                                       List<WSKontaktinformasjon> kontaktinformasjon,
                                       String narmesteLederAktorId,
                                       String orgnummer,
                                       Varseltype varseltype,
                                       List<WSParameter> parametere) {
        servicemeldingMedKontaktinformasjon.setMottaker(aktoer(narmesteLederAktorId));
        servicemeldingMedKontaktinformasjon.setTilhoerendeOrganisasjon(organisasjon(orgnummer));
        servicemeldingMedKontaktinformasjon.setVarseltypeId(varseltype.name());
        servicemeldingMedKontaktinformasjon.getParameterListe().addAll(parametere);
        servicemeldingMedKontaktinformasjon.getKontaktinformasjonListe().addAll(kontaktinformasjon);
    }

    private WSKontaktinformasjon opprettKontaktinformasjon(String kontaktinfo, String type) {
        WSKommunikasjonskanaler kanal = new WSKommunikasjonskanaler();
        kanal.setValue(type);

        WSKontaktinformasjon kontaktinformasjon = new WSKontaktinformasjon();
        kontaktinformasjon.setKanal(kanal);
        kontaktinformasjon.setKontaktinformasjon(kontaktinfo);

        return kontaktinformasjon;
    }

    private WSOrganisasjon organisasjon(String orgnummer) {
        WSOrganisasjon organisasjon = new WSOrganisasjon();
        organisasjon.setOrgnummer(orgnummer);

        return organisasjon;
    }

    private WSAktoer aktoer(String aktoerId) {
        WSAktoerId aktoer = new WSAktoerId();
        aktoer.setAktoerId(aktoerId);

        return aktoer;
    }

    private WSParameter createParameter(String key, String value) {
        WSParameter urlParameter = new WSParameter();
        urlParameter.setKey(key);
        urlParameter.setValue(value);
        return urlParameter;
    }
}
