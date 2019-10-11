package no.nav.syfo.service;

import no.nav.melding.virksomhet.servicemeldingmedkontaktinformasjon.v1.servicemeldingmedkontaktinformasjon.*;
import no.nav.syfo.model.Naermesteleder;
import no.nav.syfo.model.Varseltype;
import org.springframework.beans.factory.annotation.*;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static java.lang.System.getProperty;
import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static no.nav.syfo.util.JAXB.marshallTredjepartsServiceMelding;
import static no.nav.syfo.util.JmsUtil.messageCreator;

@Service
@Transactional
public class TredjepartsvarselService {

    @Value("${tjenester.url}")
    private String tjenesterUrl;

    private JmsTemplate tredjepartsvarselqueue;

    @Autowired
    public TredjepartsvarselService(@Qualifier("tredjepartsvarselqueue") JmsTemplate tredjepartsvarselqueue) {
        this.tredjepartsvarselqueue = tredjepartsvarselqueue;
    }

    public void sendVarselTilNaermesteLeder(Varseltype varseltype, Naermesteleder naermesteleder, Long oppfoelgingsdialogId) {
        List<WSParameter> parametere = asList(
                createParameter("url", tjenesterUrl + "/sykefravaerarbeidsgiver/"),
                createParameter("dittnavUrl", tjenesterUrl + "/sykefravaerarbeidsgiver/" + naermesteleder.naermesteLederAktoerId + "/oppfolgingsplaner/" + oppfoelgingsdialogId)
        );
        WSServicemeldingMedKontaktinformasjon melding = new WSServicemeldingMedKontaktinformasjon();
        populerServiceMelding(melding, kontaktinformasjon(naermesteleder), naermesteleder, varseltype, parametere);


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
                                       Naermesteleder naermesteleder,
                                       Varseltype varseltype,
                                       List<WSParameter> parametere) {
        servicemeldingMedKontaktinformasjon.setMottaker(aktoer(naermesteleder.naermesteLederAktoerId));
        servicemeldingMedKontaktinformasjon.setTilhoerendeOrganisasjon(organisasjon(naermesteleder.orgnummer));
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
