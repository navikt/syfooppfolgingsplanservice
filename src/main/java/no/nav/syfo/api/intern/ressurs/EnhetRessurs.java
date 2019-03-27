package no.nav.syfo.api.intern.ressurs;

import no.nav.security.spring.oidc.validation.api.ProtectedWithClaims;
import no.nav.syfo.api.intern.domain.RSBrukerPaaEnhet;
import no.nav.syfo.service.*;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static no.nav.syfo.oidc.OIDCIssuer.INTERN;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "/api/enhet")
@ProtectedWithClaims(issuer = INTERN)
public class EnhetRessurs {

    private AktoerService aktoerService;
    private EgenAnsattService egenAnsattService;
    private PersonService personService;
    private VeilederTilgangService veilederTilgangService;
    private VeilederBehandlingService veilederBehandlingService;

    @Inject
    public EnhetRessurs(
            final AktoerService aktoerService,
            final EgenAnsattService egenAnsattService,
            final PersonService personService,
            final VeilederTilgangService veilederTilgangService,
            final VeilederBehandlingService veilederBehandlingService
    ) {
        this.aktoerService = aktoerService;
        this.egenAnsattService = egenAnsattService;
        this.personService = personService;
        this.veilederTilgangService = veilederTilgangService;
        this.veilederBehandlingService = veilederBehandlingService;
    }

    @GetMapping(produces = APPLICATION_JSON_VALUE)
    @RequestMapping(value = "/{enhet}/oppfolgingsplaner/brukere")
    public List<RSBrukerPaaEnhet> hentSykmeldteMedUlesteOppfolgingsdialogerPaaEnhet(@PathVariable("enhet") String enhet) {
        veilederTilgangService.kastExceptionHvisIkkeVeilederHarTilgangTilEnhet(enhet);
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
