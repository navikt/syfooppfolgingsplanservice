package no.nav.syfo.service;

import no.nav.syfo.domain.Arbeidsoppgave;
import no.nav.syfo.domain.Oppfoelgingsdialog;
import no.nav.syfo.repository.dao.ArbeidsoppgaveDAO;
import no.nav.syfo.repository.dao.GodkjenningerDAO;
import no.nav.syfo.repository.dao.OppfoelingsdialogDAO;
import no.nav.syfo.util.ConflictException;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.ws.rs.ForbiddenException;

import static no.nav.metrics.MetricsFactory.createEvent;
import static no.nav.syfo.util.OppfoelgingsdialogUtil.eksisterendeArbeidsoppgaveHoererTilDialog;

public class ArbeidsoppgaveService {

    @Inject
    private OppfoelingsdialogDAO oppfoelingsdialogDAO;
    @Inject
    private TilgangskontrollService tilgangskontrollService;
    @Inject
    private AktoerService aktoerService;
    @Inject
    private ArbeidsoppgaveDAO arbeidsoppgaveDAO;
    @Inject
    private GodkjenningerDAO godkjenningerDAO;

    @Transactional
    public Long lagreArbeidsoppgave(Long oppfoelgingsdialogId, Arbeidsoppgave arbeidsoppgave, String fnr) throws ConflictException {
        Oppfoelgingsdialog oppfoelgingsdialog = oppfoelingsdialogDAO.finnOppfoelgingsdialogMedId(oppfoelgingsdialogId);
        String innloggetAktoerId = aktoerService.hentAktoerIdForFnr(fnr);

        if (!eksisterendeArbeidsoppgaveHoererTilDialog(arbeidsoppgave.id, arbeidsoppgaveDAO.arbeidsoppgaverByOppfoelgingsdialogId(oppfoelgingsdialogId))
                || !tilgangskontrollService.aktoerTilhoererDialogen(innloggetAktoerId, oppfoelgingsdialog)) {
            throw new ForbiddenException("Ikke tilgang");
        }

        if (godkjenningerDAO.godkjenningerByOppfoelgingsdialogId(oppfoelgingsdialogId).stream().anyMatch(pGodkjenning -> pGodkjenning.godkjent)) {
            throw new ConflictException();
        }

        oppfoelingsdialogDAO.sistEndretAv(oppfoelgingsdialogId, innloggetAktoerId);
        if (arbeidsoppgave.id == null) {
            createEvent("nyArbeidsoppgave").report();
            return arbeidsoppgaveDAO.create(arbeidsoppgave
                    .oppfoelgingsdialogId(oppfoelgingsdialogId)
                    .erVurdertAvSykmeldt(oppfoelgingsdialog.arbeidstaker.aktoerId.equals(innloggetAktoerId))
                    .opprettetAvAktoerId(innloggetAktoerId)
                    .sistEndretAvAktoerId(innloggetAktoerId)).id;
        } else {
            return arbeidsoppgaveDAO.update(arbeidsoppgave
                    .oppfoelgingsdialogId(oppfoelgingsdialogId)
                    .erVurdertAvSykmeldt(oppfoelgingsdialog.arbeidstaker.aktoerId.equals(innloggetAktoerId) || arbeidsoppgaveDAO.finnArbeidsoppgave(arbeidsoppgave.id).erVurdertAvSykmeldt)
                    .sistEndretAvAktoerId(innloggetAktoerId)).id;
        }
    }

    @Transactional
    public void slettArbeidsoppgave(Long arbeidsoppgaveId, String fnr) throws ConflictException {
        String innloggetAktoerId = aktoerService.hentAktoerIdForFnr(fnr);
        Arbeidsoppgave arbeidsoppgave = arbeidsoppgaveDAO.finnArbeidsoppgave(arbeidsoppgaveId);

        if (!arbeidsoppgave.opprettetAvAktoerId.equals(innloggetAktoerId)) {
            throw new ForbiddenException("Ikke tilgang");
        }
        if (godkjenningerDAO.godkjenningerByOppfoelgingsdialogId(arbeidsoppgave.oppfoelgingsdialogId).stream().anyMatch(pGodkjenning -> pGodkjenning.godkjent)) {
            throw new ConflictException();
        }

        oppfoelingsdialogDAO.sistEndretAv(arbeidsoppgave.oppfoelgingsdialogId, innloggetAktoerId);
        arbeidsoppgaveDAO.delete(arbeidsoppgave.id);
    }
}
