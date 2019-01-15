package no.nav.syfo.service;

import no.nav.syfo.domain.Kommentar;
import no.nav.syfo.domain.Oppfoelgingsdialog;
import no.nav.syfo.domain.Tiltak;
import no.nav.syfo.repository.dao.GodkjenningerDAO;
import no.nav.syfo.repository.dao.KommentarDAO;
import no.nav.syfo.repository.dao.OppfoelingsdialogDAO;
import no.nav.syfo.repository.dao.TiltakDAO;
import no.nav.syfo.util.ConflictException;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.ws.rs.ForbiddenException;

public class KommentarService {

    @Inject
    private OppfoelingsdialogDAO oppfoelingsdialogDAO;
    @Inject
    private TiltakDAO tiltakDAO;
    @Inject
    private AktoerService aktoerService;
    @Inject
    private KommentarDAO kommentarDAO;
    @Inject
    private GodkjenningerDAO godkjenningerDAO;

    @Transactional
    public Long lagreKommentar(Long tiltakId, Kommentar kommentar, String fnr) {
        Tiltak tiltak = tiltakDAO.finnTiltakById(tiltakId);
        String innloggetAktoerId = aktoerService.hentAktoerIdForFnr(fnr);

        Oppfoelgingsdialog oppfoelgingsdialog = oppfoelingsdialogDAO.finnOppfoelgingsdialogMedId(tiltak.oppfoelgingsdialogId);
        if (kommentarenErIkkeOpprettetAvNoenAndre(kommentar, innloggetAktoerId, oppfoelgingsdialog)) {
            throw new ForbiddenException("Ikke tilgang");
        }

        if (godkjenningerDAO.godkjenningerByOppfoelgingsdialogId(tiltak.oppfoelgingsdialogId).stream().anyMatch(pGodkjenning -> pGodkjenning.godkjent)) {
            throw new ConflictException();
        }

        oppfoelingsdialogDAO.sistEndretAv(tiltak.oppfoelgingsdialogId, innloggetAktoerId);
        if (kommentar.id == null) {
            return kommentarDAO.create(kommentar)
                    .tiltakId(tiltakId)
                    .opprettetAvAktoerId(innloggetAktoerId)
                    .sistEndretAvAktoerId(innloggetAktoerId).id;
        } else {
            return kommentarDAO.update(kommentar).id;
        }
    }

    private boolean kommentarenErIkkeOpprettetAvNoenAndre(Kommentar kommentar, String innloggetAktoerId, Oppfoelgingsdialog oppfoelgingsdialog) {
        return kommentar.id != null && oppfoelgingsdialog.tiltakListe.stream().noneMatch(pTiltak -> pTiltak.opprettetAvAktoerId.equals(innloggetAktoerId));
    }

    @Transactional
    public void slettKommentar(Long kommentarId, String fnr) {
        String innloggetAktoerId = aktoerService.hentAktoerIdForFnr(fnr);
        Kommentar kommentar = kommentarDAO.finnKommentar(kommentarId);
        Oppfoelgingsdialog oppfoelgingsdialog = oppfoelingsdialogDAO.oppfoelgingsdialogByTiltakId(kommentar.tiltakId);

        if (!kommentar.opprettetAvAktoerId.equals(innloggetAktoerId)) {
            throw new ForbiddenException("Ikke tilgang");
        }

        if (godkjenningerDAO.godkjenningerByOppfoelgingsdialogId(oppfoelgingsdialog.id).stream().anyMatch(godkjenning -> godkjenning.godkjent)) {
            throw new ConflictException();
        }

        oppfoelingsdialogDAO.sistEndretAv(oppfoelgingsdialog.id, innloggetAktoerId);
        kommentarDAO.delete(kommentarId);
    }
}
