package no.nav.syfo.api.intern.ressurs;

import no.nav.syfo.api.intern.domain.RSBrukerPaaEnhet;
import no.nav.syfo.service.TilgangsKontroll;
import no.nav.syfo.service.AktoerService;
import no.nav.syfo.service.EgenAnsattService;
import no.nav.syfo.service.PersonService;
import no.nav.syfo.service.VeilederBehandlingService;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.*;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Component
@Path("/enhet")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
public class EnhetRessurs {

    @Inject
    private VeilederBehandlingService veilederBehandlingService;

    @Inject
    private AktoerService aktoerService;

    @Inject
    private TilgangsKontroll tilgangsKontroll;

    @Inject
    private PersonService personService;

    @Inject
    private EgenAnsattService egenAnsattService;

    @GET
    @Path("/{enhet}/oppfolgingsplaner/brukere")
    public List<RSBrukerPaaEnhet> hentSykmeldteMedUlesteOppfolgingsdialogerPaaEnhet(@PathParam("enhet") String enhet) {
        tilgangsKontroll.sjekkTilgangTilEnhet(enhet);
        return veilederBehandlingService.hentSykmeldteMedUlesteOppfolgingsplaner(enhet)
                .stream()
                .map(aktorId -> aktoerService.hentFnrForAktoer(aktorId))
                .map(fnr -> new RSBrukerPaaEnhet()
                    .fnr(fnr)
                    .skjermetEllerEgenAnsatt(sykmeldtErDiskresjonsmerketEllerEgenAnsatt(fnr)))
                .collect(toList());
    }


    private boolean sykmeldtErDiskresjonsmerketEllerEgenAnsatt(String fnr) {
        return personService.erDiskresjonsmerket(fnr) || egenAnsattService.erEgenAnsatt(fnr);
    }

}
