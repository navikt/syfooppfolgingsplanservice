package no.nav.syfo.api.system;

import no.nav.security.oidc.api.ProtectedWithClaims;
import no.nav.security.oidc.api.Unprotected;
import no.nav.syfo.aktorregister.AktorregisterConsumer;
import no.nav.syfo.api.system.domain.VeilederOppgaveFeedItem;
import no.nav.syfo.domain.Oppfolgingsplan;
import no.nav.syfo.repository.dao.GodkjentplanDAO;
import no.nav.syfo.repository.dao.OppfolgingsplanDAO;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static no.nav.syfo.oidc.OIDCIssuer.INTERN;
import static no.nav.syfo.util.RestUtils.baseUrl;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@ProtectedWithClaims(issuer = INTERN, claimMap = {"sub=srvsyfoveilederoppgaver"})
@RequestMapping(value = "/api/system/feed/oppfoelgingsdialoger")
public class OppfoelgingsplanerFeed {

    private AktorregisterConsumer aktorregisterConsumer;
    private GodkjentplanDAO godkjentplanDAO;
    private OppfolgingsplanDAO oppfolgingsplanDAO;

    @Inject
    public OppfoelgingsplanerFeed(
            AktorregisterConsumer aktorregisterConsumer,
            GodkjentplanDAO godkjentplanDAO,
            OppfolgingsplanDAO oppfolgingsplanDAO
    ) {
        this.aktorregisterConsumer = aktorregisterConsumer;
        this.godkjentplanDAO = godkjentplanDAO;
        this.oppfolgingsplanDAO = oppfolgingsplanDAO;
    }

    @Unprotected
    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public List<VeilederOppgaveFeedItem> oppfoelgingsplanOppgaverFeed(@RequestParam(value = "timestamp") String timestamp) {
        return godkjentplanDAO.godkjentePlanerSiden(LocalDateTime.parse(timestamp))
                .stream()
                .map(godkjentPlan -> {
                    Oppfolgingsplan oppfolgingsplan = oppfolgingsplanDAO.finnOppfolgingsplanMedId(godkjentPlan.oppfoelgingsdialogId);
                    String fnr = aktorregisterConsumer.hentFnrForAktor(oppfolgingsplan.arbeidstaker.aktoerId);
                    return new VeilederOppgaveFeedItem()
                            .uuid(oppfolgingsplan.uuid)
                            .fnr(fnr)
                            .lenke(baseUrl() + "/sykefravaer/" + fnr + "/oppfoelgingsplaner/" + oppfolgingsplan.id)
                            .type("SE_OPPFOLGINGSPLAN")
                            .created(godkjentPlan.deltMedNAVTidspunkt)
                            .status("IKKE_STARTET")
                            .virksomhetsnummer(oppfolgingsplan.virksomhet.virksomhetsnummer);
                })
                .filter(veilederOppgaveFeedItem -> veilederOppgaveFeedItem.uuid != null)
                .collect(toList());
    }
}
