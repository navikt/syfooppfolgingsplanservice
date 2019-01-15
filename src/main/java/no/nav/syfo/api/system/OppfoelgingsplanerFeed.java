package no.nav.syfo.api.system;

import no.nav.syfo.api.system.domain.VeilederOppgaveFeedItem;
import no.nav.syfo.domain.Oppfoelgingsdialog;
import no.nav.syfo.repository.dao.GodkjentplanDAO;
import no.nav.syfo.repository.dao.OppfoelingsdialogDAO;
import no.nav.syfo.service.AktoerService;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.*;
import java.time.LocalDateTime;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.syfo.util.RestUtils.baseUrl;

@Component
@Path("/system/feed/oppfoelgingsdialoger")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
public class OppfoelgingsplanerFeed {

    @Inject
    private OppfoelingsdialogDAO oppfoelingsdialogDAO;
    @Inject
    private GodkjentplanDAO godkjentplanDAO;
    @Inject
    private AktoerService aktoerService;

    @GET
    public List<VeilederOppgaveFeedItem> oppfoelgingsplanOppgaverFeed(@QueryParam("timestamp") String timestamp) {
        return godkjentplanDAO.godkjentePlanerSiden(LocalDateTime.parse(timestamp))
                .stream()
                .map(godkjentPlan -> {
                    Oppfoelgingsdialog oppfoelgingsdialog = oppfoelingsdialogDAO.finnOppfoelgingsdialogMedId(godkjentPlan.oppfoelgingsdialogId);
                    String fnr = aktoerService.hentFnrForAktoer(oppfoelgingsdialog.arbeidstaker.aktoerId);
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
