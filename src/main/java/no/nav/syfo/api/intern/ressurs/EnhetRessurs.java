package no.nav.syfo.api.intern.ressurs;

import no.nav.syfo.api.intern.domain.RSBrukerPaaEnhet;
import no.nav.syfo.service.*;
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

    private AktoerService aktoerService;
    private EgenAnsattService egenAnsattService;
    private PersonService personService;
    private TilgangsKontroll tilgangsKontroll;
    private VeilederBehandlingService veilederBehandlingService;

    @Inject
    public EnhetRessurs(
            final AktoerService aktoerService,
            final EgenAnsattService egenAnsattService,
            final PersonService personService,
            final TilgangsKontroll tilgangsKontroll,
            final VeilederBehandlingService veilederBehandlingService
    ) {
        this.aktoerService = aktoerService;
        this.egenAnsattService = egenAnsattService;
        this.personService = personService;
        this.tilgangsKontroll = tilgangsKontroll;
        this.veilederBehandlingService = veilederBehandlingService;
    }

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
