package no.nav.syfo.mocks;

import no.nav.tjeneste.virksomhet.organisasjon.v4.*;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.WSOrganisasjon;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.WSUstrukturertNavn;
import no.nav.tjeneste.virksomhet.organisasjon.v4.meldinger.*;

public class OrganisasjonMock implements OrganisasjonV4 {
    @Override
    public WSHentOrganisasjonResponse hentOrganisasjon(WSHentOrganisasjonRequest wsHentOrganisasjonRequest) throws HentOrganisasjonOrganisasjonIkkeFunnet, HentOrganisasjonUgyldigInput {
        return new WSHentOrganisasjonResponse().withOrganisasjon(new WSOrganisasjon().withNavn(new WSUstrukturertNavn().withNavnelinje("BEKK Consulting AS")));
    }

    @Override
    public void ping() {

    }

    @Override
    public WSHentOrganisasjonsnavnBolkResponse hentOrganisasjonsnavnBolk(WSHentOrganisasjonsnavnBolkRequest wsHentOrganisasjonsnavnBolkRequest) {
        return null;
    }

    @Override
    public WSFinnOrganisasjonsendringerListeResponse finnOrganisasjonsendringerListe(WSFinnOrganisasjonsendringerListeRequest wsFinnOrganisasjonsendringerListeRequest) throws FinnOrganisasjonsendringerListeUgyldigInput {
        return null;
    }

    @Override
    public WSFinnOrganisasjonResponse finnOrganisasjon(WSFinnOrganisasjonRequest wsFinnOrganisasjonRequest) throws FinnOrganisasjonUgyldigInput, FinnOrganisasjonForMangeForekomster {
        return null;
    }

    @Override
    public WSHentNoekkelinfoOrganisasjonResponse hentNoekkelinfoOrganisasjon(WSHentNoekkelinfoOrganisasjonRequest wsHentNoekkelinfoOrganisasjonRequest) throws HentNoekkelinfoOrganisasjonOrganisasjonIkkeFunnet, HentNoekkelinfoOrganisasjonUgyldigInput {
        return null;
    }

    @Override
    public WSHentVirksomhetsOrgnrForJuridiskOrgnrBolkResponse hentVirksomhetsOrgnrForJuridiskOrgnrBolk(WSHentVirksomhetsOrgnrForJuridiskOrgnrBolkRequest wsHentVirksomhetsOrgnrForJuridiskOrgnrBolkRequest) {
        return null;
    }

    @Override
    public WSValiderOrganisasjonResponse validerOrganisasjon(WSValiderOrganisasjonRequest wsValiderOrganisasjonRequest) throws ValiderOrganisasjonOrganisasjonIkkeFunnet, ValiderOrganisasjonUgyldigInput {
        return null;
    }
}
