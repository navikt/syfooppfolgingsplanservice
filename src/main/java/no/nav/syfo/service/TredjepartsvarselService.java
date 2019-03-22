package no.nav.syfo.service;

import no.nav.melding.virksomhet.servicemeldingmedkontaktinformasjon.v1.servicemeldingmedkontaktinformasjon.*;
import no.nav.syfo.model.Naermesteleder;
import no.nav.syfo.model.Varseltype;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.lang.System.getProperty;
import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static no.nav.syfo.util.JAXB.marshallTredjepartsServiceMelding;
import static no.nav.syfo.util.JmsUtil.messageCreator;

@Service
public class TredjepartsvarselService {

    private JmsTemplate tredjepartsvarselqueue;

    @Autowired
    public TredjepartsvarselService(@Qualifier("tredjepartsvarselqueue") JmsTemplate tredjepartsvarselqueue) {
        this.tredjepartsvarselqueue = tredjepartsvarselqueue;
    }

    public void sendVarselTilNaermesteLeder(Varseltype varseltype, Naermesteleder naermesteleder, Long oppfoelgingsdialogId) {
        List<Parameter> parametere = asList(
                createParameter("url", getProperty("TJENESTER_URL") + "/sykefravaerarbeidsgiver/"),
                createParameter("dittnavUrl", getProperty("TJENESTER_URL") + "/sykefravaerarbeidsgiver/" + naermesteleder.naermesteLederAktoerId + "/oppfolgingsplaner/" + oppfoelgingsdialogId)
        );
        ServicemeldingMedKontaktinformasjon melding = new ServicemeldingMedKontaktinformasjon();
        populerServiceMelding(melding, kontaktinformasjon(naermesteleder), naermesteleder, varseltype, parametere);


        String xml = marshallTredjepartsServiceMelding(new ObjectFactory().createServicemelding(melding));
        tredjepartsvarselqueue.send(messageCreator(xml, randomUUID().toString()));
    }

    private List<Kontaktinformasjon> kontaktinformasjon(Naermesteleder tredjepartsKontaktinfo) {
        return asList(
                opprettKontaktinformasjon(tredjepartsKontaktinfo.epost, "EPOST"),
                opprettKontaktinformasjon(tredjepartsKontaktinfo.mobil, "SMS")
        );
    }

    private void populerServiceMelding(ServicemeldingMedKontaktinformasjon servicemeldingMedKontaktinformasjon,
                                       List<Kontaktinformasjon> kontaktinformasjon,
                                       Naermesteleder naermesteleder,
                                       Varseltype varseltype,
                                       List<Parameter> parametere) {
        servicemeldingMedKontaktinformasjon.setMottaker(aktoer(naermesteleder.naermesteLederAktoerId));
        servicemeldingMedKontaktinformasjon.setTilhoerendeOrganisasjon(organisasjon(naermesteleder.orgnummer));
        servicemeldingMedKontaktinformasjon.setVarseltypeId(varseltype.name());
        servicemeldingMedKontaktinformasjon.getParameterListe().addAll(parametere);
        servicemeldingMedKontaktinformasjon.getKontaktinformasjonListe().addAll(kontaktinformasjon);
    }

    private Kontaktinformasjon opprettKontaktinformasjon(String kontaktinfo, String type) {
        Kommunikasjonskanaler kanal = new Kommunikasjonskanaler();
        kanal.setValue(type);

        Kontaktinformasjon kontaktinformasjon = new Kontaktinformasjon();
        kontaktinformasjon.setKanal(kanal);
        kontaktinformasjon.setKontaktinformasjon(kontaktinfo);

        return kontaktinformasjon;
    }

    private Organisasjon organisasjon(String orgnummer) {
        Organisasjon organisasjon = new Organisasjon();
        organisasjon.setOrgnummer(orgnummer);

        return organisasjon;
    }

    private Aktoer aktoer(String aktoerId) {
        AktoerId aktoer = new AktoerId();
        aktoer.setAktoerId(aktoerId);

        return aktoer;
    }

    private Parameter createParameter(String key, String value) {
        Parameter urlParameter = new Parameter();
        urlParameter.setKey(key);
        urlParameter.setValue(value);
        return urlParameter;
    }
}
