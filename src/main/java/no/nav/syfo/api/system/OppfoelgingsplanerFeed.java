package no.nav.syfo.api.system;

import no.nav.security.spring.oidc.validation.api.ProtectedWithClaims;
import no.nav.security.spring.oidc.validation.api.Unprotected;
import no.nav.syfo.api.system.domain.VeilederOppgaveFeedItem;
import no.nav.syfo.domain.Oppfoelgingsdialog;
import no.nav.syfo.repository.dao.GodkjentplanDAO;
import no.nav.syfo.repository.dao.OppfoelingsdialogDAO;
import no.nav.syfo.aktorregister.AktorregisterConsumer;
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
    private OppfoelingsdialogDAO oppfoelingsdialogDAO;

    @Inject
    public OppfoelgingsplanerFeed(
            AktorregisterConsumer aktorregisterConsumer,
            GodkjentplanDAO godkjentplanDAO,
            OppfoelingsdialogDAO oppfoelingsdialogDAO
    ) {
        this.aktorregisterConsumer = aktorregisterConsumer;
        this.godkjentplanDAO = godkjentplanDAO;
        this.oppfoelingsdialogDAO = oppfoelingsdialogDAO;
    }

    @Unprotected
    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public List<VeilederOppgaveFeedItem> oppfoelgingsplanOppgaverFeed(@RequestParam(value = "timestamp") String timestamp) {
        return godkjentplanDAO.godkjentePlanerSiden(LocalDateTime.parse(timestamp))
                .stream()
                .map(godkjentPlan -> {
                    Oppfoelgingsdialog oppfoelgingsdialog = oppfoelingsdialogDAO.finnOppfolgingsplanMedId(godkjentPlan.oppfoelgingsdialogId);
                    String fnr = aktorregisterConsumer.hentFnrForAktor(oppfoelgingsdialog.arbeidstaker.aktoerId);
                    return new VeilederOppgaveFeedItem()
                            .uuid(oppfoelgingsdialog.uuid)
                            .fnr(fnr)
                            .lenke(baseUrl() + "/sykefravaer/" + fnr + "/oppfoelgingsplaner/" + oppfoelgingsdialog.id)
                            .type("SE_OPPFOLGINGSPLAN")
                            .created(godkjentPlan.deltMedNAVTidspunkt)
                            .status("IKKE_STARTET")
                            .virksomhetsnummer(oppfoelgingsdialog.virksomhet.virksomhetsnummer);
                })
                .filter(veilederOppgaveFeedItem -> veilederOppgaveFeedItem.uuid != null)
                .collect(toList());
    }
}
