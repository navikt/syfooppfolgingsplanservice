package no.nav.syfo.service;

import no.nav.syfo.domain.*;
import no.nav.syfo.metric.Metrikk;
import no.nav.syfo.pdl.PdlConsumer;
import no.nav.syfo.repository.dao.*;
import no.nav.syfo.util.ConflictException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import jakarta.ws.rs.ForbiddenException;

import static no.nav.syfo.util.OppfoelgingsdialogUtil.eksisterendeArbeidsoppgaveHoererTilDialog;
import static no.nav.syfo.util.OppfoelgingsdialogUtil.kanEndreElement;

@Service
public class ArbeidsoppgaveService {

    private PdlConsumer pdlConsumer;
    private ArbeidsoppgaveDAO arbeidsoppgaveDAO;
    private GodkjenningerDAO godkjenningerDAO;
    private Metrikk metrikk;
    private OppfolgingsplanDAO oppfolgingsplanDAO;
    private TilgangskontrollService tilgangskontrollService;

    @Inject
    public ArbeidsoppgaveService(
            PdlConsumer pdlConsumer,
            ArbeidsoppgaveDAO arbeidsoppgaveDAO,
            GodkjenningerDAO godkjenningerDAO,
            Metrikk metrikk,
            OppfolgingsplanDAO oppfolgingsplanDAO,
            TilgangskontrollService tilgangskontrollService
    ) {
        this.pdlConsumer = pdlConsumer;
        this.arbeidsoppgaveDAO = arbeidsoppgaveDAO;
        this.godkjenningerDAO = godkjenningerDAO;
        this.metrikk = metrikk;
        this.oppfolgingsplanDAO = oppfolgingsplanDAO;
        this.tilgangskontrollService = tilgangskontrollService;
    }

    @Transactional
    public Long lagreArbeidsoppgave(Long oppfoelgingsdialogId, Arbeidsoppgave arbeidsoppgave, String fnr) throws ConflictException {
        Oppfolgingsplan oppfolgingsplan = oppfolgingsplanDAO.finnOppfolgingsplanMedId(oppfoelgingsdialogId);
        String innloggetAktoerId = pdlConsumer.aktorid(fnr);

        if (!eksisterendeArbeidsoppgaveHoererTilDialog(arbeidsoppgave.id, arbeidsoppgaveDAO.arbeidsoppgaverByOppfoelgingsdialogId(oppfoelgingsdialogId))
                || !tilgangskontrollService.brukerTilhorerOppfolgingsplan(fnr, oppfolgingsplan)) {
            throw new ForbiddenException("Ikke tilgang");
        }

        if (godkjenningerDAO.godkjenningerByOppfoelgingsdialogId(oppfoelgingsdialogId).stream().anyMatch(pGodkjenning -> pGodkjenning.godkjent)) {
            throw new ConflictException();
        }

        oppfolgingsplanDAO.sistEndretAv(oppfoelgingsdialogId, innloggetAktoerId);
        if (arbeidsoppgave.id == null) {
            metrikk.tellHendelse("lagre_arbeidsoppgave_ny");
            return arbeidsoppgaveDAO.create(arbeidsoppgave
                    .oppfoelgingsdialogId(oppfoelgingsdialogId)
                    .erVurdertAvSykmeldt(oppfolgingsplan.arbeidstaker.aktoerId.equals(innloggetAktoerId))
                    .opprettetAvAktoerId(innloggetAktoerId)
                    .sistEndretAvAktoerId(innloggetAktoerId)).id;
        } else {
            metrikk.tellHendelse("lagre_arbeidsoppgave_eksisterende");
            return arbeidsoppgaveDAO.update(arbeidsoppgave
                    .oppfoelgingsdialogId(oppfoelgingsdialogId)
                    .erVurdertAvSykmeldt(oppfolgingsplan.arbeidstaker.aktoerId.equals(innloggetAktoerId) || arbeidsoppgaveDAO.finnArbeidsoppgave(arbeidsoppgave.id).erVurdertAvSykmeldt)
                    .sistEndretAvAktoerId(innloggetAktoerId)).id;
        }
    }

    @Transactional
    public void slettArbeidsoppgave(Long arbeidsoppgaveId, String fnr) throws ConflictException {
        String innloggetAktoerId = pdlConsumer.aktorid(fnr);
        Arbeidsoppgave arbeidsoppgave = arbeidsoppgaveDAO.finnArbeidsoppgave(arbeidsoppgaveId);
        Oppfolgingsplan oppfolgingsplan = oppfolgingsplanDAO.finnOppfolgingsplanMedId(arbeidsoppgave.oppfoelgingsdialogId);

        if (!tilgangskontrollService.brukerTilhorerOppfolgingsplan(fnr, oppfolgingsplan) || !kanEndreElement(innloggetAktoerId, oppfolgingsplan.arbeidstaker.aktoerId, arbeidsoppgave.opprettetAvAktoerId)) {
            throw new ForbiddenException("Ikke tilgang");
        }
        if (godkjenningerDAO.godkjenningerByOppfoelgingsdialogId(arbeidsoppgave.oppfoelgingsdialogId).stream().anyMatch(pGodkjenning -> pGodkjenning.godkjent)) {
            throw new ConflictException();
        }

        oppfolgingsplanDAO.sistEndretAv(arbeidsoppgave.oppfoelgingsdialogId, innloggetAktoerId);
        arbeidsoppgaveDAO.delete(arbeidsoppgave.id);
    }
}
