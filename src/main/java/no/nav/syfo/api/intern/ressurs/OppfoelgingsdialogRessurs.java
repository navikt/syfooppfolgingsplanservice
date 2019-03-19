package no.nav.syfo.api.intern.ressurs;

import no.nav.syfo.api.intern.domain.RSHistorikk;
import no.nav.syfo.api.intern.domain.RSOppfoelgingsdialog;
import no.nav.syfo.domain.Oppfoelgingsdialog;
import no.nav.syfo.model.VeilederOppgave.OppgaveStatus;
import no.nav.syfo.model.VeilederOppgave.OppgaveType;
import no.nav.syfo.repository.dao.OppfoelingsdialogDAO;
import no.nav.syfo.service.*;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.*;
import java.util.List;

import static java.lang.System.getProperty;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.syfo.api.intern.mappers.OppfoelgingsdialogRestMapper.oppfoelgingsdialog2rs;
import static no.nav.syfo.mockdata.MockData.mockedOppfoelgingsdialoger;
import static no.nav.syfo.util.MapUtil.mapListe;
import static no.nav.syfo.util.PropertyUtil.LOCAL_MOCK;

@Component
@Path("/oppfoelgingsdialog/v1/{fnr}")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
public class OppfoelgingsdialogRessurs {

    private AktoerService aktoerService;
    private BrukerprofilService brukerprofilService;
    private OppfoelingsdialogDAO oppfoelingsdialogDAO;
    private OrganisasjonService organisasjonService;
    private TilgangsKontroll tilgangsKontroll;
    private VeilederOppgaverService veilederOppgaverService;

    @Inject
    public OppfoelgingsdialogRessurs(
            final AktoerService aktoerService,
            final BrukerprofilService brukerprofilService,
            final OppfoelingsdialogDAO oppfoelingsdialogDAO,
            final OrganisasjonService organisasjonService,
            final TilgangsKontroll tilgangsKontroll,
            final VeilederOppgaverService veilederOppgaverService
    ) {
        this.aktoerService = aktoerService;
        this.brukerprofilService = brukerprofilService;
        this.oppfoelingsdialogDAO = oppfoelingsdialogDAO;
        this.organisasjonService = organisasjonService;
        this.tilgangsKontroll = tilgangsKontroll;
        this.veilederOppgaverService = veilederOppgaverService;
    }

    private String finnDeltAvNavn(Oppfoelgingsdialog oppfoelgingsdialog) {
        if (oppfoelgingsdialog.sistEndretAvAktoerId.equals(oppfoelgingsdialog.arbeidstaker.aktoerId)) {
            return brukerprofilService.hentNavnByAktoerId(oppfoelgingsdialog.sistEndretAvAktoerId);
        }
        return organisasjonService.finnVirksomhetsnavn(oppfoelgingsdialog.virksomhet.virksomhetsnummer);
    }

    @GET
    @Path("/historikk")
    public List<RSHistorikk> historikk(@PathParam("fnr") String fnr) {
        tilgangsKontroll.sjekkTilgangTilPerson(fnr);

        List<Oppfoelgingsdialog> oppfoelgingsplaner = oppfoelingsdialogDAO.oppfoelgingsdialogerKnyttetTilSykmeldt(aktoerService.hentAktoerIdForFnr(fnr))
                .stream()
                .map(oppfoelgingsdialog -> oppfoelingsdialogDAO.populate(oppfoelgingsdialog))
                .filter(oppfoelgingsdialog -> oppfoelgingsdialog.godkjentPlan.isPresent())
                .filter(oppfoelgingsdialog -> oppfoelgingsdialog.godkjentPlan.get().deltMedNAV)
                .collect(toList());

        List<RSHistorikk> utfoertHistorikk = veilederOppgaverService.get(fnr).stream()
                .filter(veilederOppgave -> veilederOppgave.type.equals(OppgaveType.SE_OPPFOLGINGSPLAN.name())
                        && veilederOppgave.status.equals(OppgaveStatus.FERDIG.name()))
                .map(veilederOppgave -> new RSHistorikk()
                        .tekst("Oppfølgingsplanen ble lest av " + veilederOppgave.sistEndretAv)
                        .tidspunkt(veilederOppgave.getSistEndret())
                )
                .collect(toList());
        List<RSHistorikk> opprettetHistorikk = mapListe(
                oppfoelgingsplaner,
                oppfoelgingsdialog -> new RSHistorikk()
                        .tekst("Oppfølgingsplanen ble delt med NAV av " + finnDeltAvNavn(oppfoelgingsdialog) + ".")
                        .tidspunkt(oppfoelgingsdialog.godkjentPlan.get().deltMedNAVTidspunkt)
        );
        return concat(opprettetHistorikk.stream(), utfoertHistorikk.stream()).collect(toList());
    }

    @GET
    public List<RSOppfoelgingsdialog> hentDialoger(@PathParam("fnr") String fnr) {
        tilgangsKontroll.sjekkTilgangTilPerson(fnr);

        if ("true".equals(getProperty(LOCAL_MOCK))) {
            return mockedOppfoelgingsdialoger();
        }
        return mapListe(oppfoelingsdialogDAO.oppfoelgingsdialogerKnyttetTilSykmeldt(aktoerService.hentAktoerIdForFnr(fnr))
                        .stream()
                        .map(oppfoelgingsdialog -> oppfoelingsdialogDAO.populate(oppfoelgingsdialog))
                        .filter(oppfoelgingsdialog -> oppfoelgingsdialog.godkjentPlan.isPresent())
                        .filter(oppfoelgingsdialog -> oppfoelgingsdialog.godkjentPlan.get().deltMedNAV)
                        .collect(toList()),
                oppfoelgingsdialog2rs);
    }

}
