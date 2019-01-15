package no.nav.syfo.service;

import no.nav.tjeneste.virksomhet.brukerprofil.v3.BrukerprofilV3;
import no.nav.tjeneste.virksomhet.brukerprofil.v3.HentKontaktinformasjonOgPreferanserPersonIdentErUtgaatt;
import no.nav.tjeneste.virksomhet.brukerprofil.v3.HentKontaktinformasjonOgPreferanserPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.brukerprofil.v3.HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.brukerprofil.v3.informasjon.WSNorskIdent;
import no.nav.tjeneste.virksomhet.brukerprofil.v3.informasjon.WSPerson;
import no.nav.tjeneste.virksomhet.brukerprofil.v3.informasjon.WSPersonidenter;
import no.nav.tjeneste.virksomhet.brukerprofil.v3.meldinger.WSHentKontaktinformasjonOgPreferanserRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;

import javax.inject.Inject;
import javax.ws.rs.ForbiddenException;
import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.text.WordUtils.capitalize;

public class BrukerprofilService {
    private static final Logger LOG = LoggerFactory.getLogger(BrukerprofilService.class);
    @Inject
    private BrukerprofilV3 brukerprofilV3;
    @Inject
    private AktoerService aktoerService;

    @Cacheable(value = "tps", keyGenerator = "userkeygenerator")
    public String hentNavnByFnr(String fnr) {
        if (isBlank(fnr) || !fnr.matches("\\d{11}$")) {
            LOG.error("Prøvde å hente navn med fnr {}", fnr);
            throw new RuntimeException();
        }
        try {
            WSPerson wsPerson = brukerprofilV3.hentKontaktinformasjonOgPreferanser(new WSHentKontaktinformasjonOgPreferanserRequest()
                    .withIdent(new WSNorskIdent()
                            .withType(new WSPersonidenter()
                                    .withKodeRef("http://nav.no/kodeverk/Term/Personidenter/FNR/nb/F_c3_b8dselnummer?v=1")
                                    .withValue("FNR")
                            )
                            .withIdent(fnr))).getBruker();
            String mellomnavn = wsPerson.getPersonnavn().getMellomnavn() == null ? "" : wsPerson.getPersonnavn().getMellomnavn();
            if (!"".equals(mellomnavn)) {
                mellomnavn = mellomnavn + " ";
            }
            final String navnFraTps = wsPerson.getPersonnavn().getFornavn() + " " + mellomnavn + wsPerson.getPersonnavn().getEtternavn();
            return capitalize(navnFraTps.toLowerCase(), '-', ' ');
        } catch (HentKontaktinformasjonOgPreferanserPersonIdentErUtgaatt e) {
            LOG.error("HentKontaktinformasjonOgPreferanserPersonIdentErUtgaatt ved oppslag mot TPS", e);
            throw new RuntimeException();
        } catch (HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning e) {
            LOG.error("Sikkerhetsbegrensning ved henting av navn fra TPS", e);
            throw new ForbiddenException();
        } catch (HentKontaktinformasjonOgPreferanserPersonIkkeFunnet e) {
            LOG.error("HentKontaktinformasjonOgPreferanserPersonIkkeFunnet ved oppslap mot TPS", e);
            throw new RuntimeException();
        } catch (RuntimeException e) {
            LOG.error("Fikk RuntimeException ved oppslag mot TPS", e);
            return "Vi fant ikke navnet";
        }
    }

    public Optional<String> hentNavnByFnrForAsynkOppgave(String fnr) {
        String brukersNavn = hentNavnByFnr(fnr);
        if (brukersNavn.equals("Vi fant ikke navnet")) {
            return empty();
        }
        return of(brukersNavn);
    }

    @Cacheable(value = "tps", keyGenerator = "userkeygenerator")
    public String hentNavnByAktoerId(String aktoerId) {
        if (!aktoerId.matches("\\d{13}$")) {
            throw new RuntimeException();
        }
        return hentNavnByFnr(aktoerService.hentFnrForAktoer(aktoerId));
    }

    @Cacheable(value = "tps", keyGenerator = "userkeygenerator")
    public boolean erKode6eller7(String fnr) {
        if (!fnr.matches("\\d{11}$")) {
            throw new RuntimeException();
        }
        try {
            WSPerson wsPerson = brukerprofilV3.hentKontaktinformasjonOgPreferanser(new WSHentKontaktinformasjonOgPreferanserRequest()
                    .withIdent(new WSNorskIdent()
                            .withIdent(fnr)
                            .withType(new WSPersonidenter()
                                    .withKodeRef("http://nav.no/kodeverk/Term/Personidenter/FNR/nb/F_c3_b8dselnummer?v=1")
                                    .withValue("FNR")
                            ))).getBruker();
            return ((wsPerson.getDiskresjonskode() != null) && "6".equals(wsPerson.getDiskresjonskode().getValue())) || ((wsPerson.getDiskresjonskode() != null) && "7".equals(wsPerson.getDiskresjonskode().getValue()));
        } catch (HentKontaktinformasjonOgPreferanserPersonIdentErUtgaatt e) {
            LOG.error("HentKontaktinformasjonOgPreferanserPersonIdentErUtgaatt ved oppslag mot TPS", e);
            throw new RuntimeException();
        } catch (HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning e) {
            LOG.error("Sikkerhetsbegrensning ved henting av person fra TPS", e);
            throw new ForbiddenException();
        } catch (HentKontaktinformasjonOgPreferanserPersonIkkeFunnet e) {
            LOG.error("HentKontaktinformasjonOgPreferanserPersonIkkeFunnet ved oppslag mot TPS", e);
            return true;
        } catch (RuntimeException e) {
            LOG.error("Fikk RuntimeException ved oppslag mot TPS", e);
            throw e;
        }
    }

}
