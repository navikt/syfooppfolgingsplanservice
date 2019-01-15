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
import java.util.List;

import static no.nav.metrics.MetricsFactory.createEvent;
import static no.nav.syfo.util.OppfoelgingsdialogUtil.eksisterendeTiltakHoererTilDialog;

public class TiltakService {

    @Inject
    private OppfoelingsdialogDAO oppfoelingsdialogDAO;
    @Inject
    private AktoerService aktoerService;
    @Inject
    private TiltakDAO tiltakDAO;
    @Inject
    private GodkjenningerDAO godkjenningerDAO;
    @Inject
    private TilgangskontrollService tilgangskontrollService;
    @Inject
    private KommentarDAO kommentarDAO;

    @Transactional
    public Long lagreTiltak(Long oppfoelgingsdialogId, Tiltak tiltak, String fnr) {
        Oppfoelgingsdialog oppfoelgingsdialog = oppfoelingsdialogDAO.finnOppfoelgingsdialogMedId(oppfoelgingsdialogId);
        String innloggetAktoerId = aktoerService.hentAktoerIdForFnr(fnr);

        if (!eksisterendeTiltakHoererTilDialog(tiltak.id, tiltakDAO.finnTiltakByOppfoelgingsdialogId(oppfoelgingsdialogId)) || !tilgangskontrollService.aktoerTilhoererDialogen(innloggetAktoerId, oppfoelgingsdialog)) {
            throw new ForbiddenException("Ikke tilgang");
        }

        if (godkjenningerDAO.godkjenningerByOppfoelgingsdialogId(oppfoelgingsdialogId).stream().anyMatch(pGodkjenning -> pGodkjenning.godkjent)) {
            throw new ConflictException();
        }

        oppfoelingsdialogDAO.sistEndretAv(oppfoelgingsdialogId, innloggetAktoerId);
        if (tiltak.id == null) {
            createEvent("nyttTiltak").report();
            return tiltakDAO.create(tiltak
                    .oppfoelgingsdialogId(oppfoelgingsdialogId)
                    .opprettetAvAktoerId(innloggetAktoerId)
                    .sistEndretAvAktoerId(innloggetAktoerId)).id;
        } else {
            return tiltakDAO.update(tiltak
                    .oppfoelgingsdialogId(oppfoelgingsdialogId)
                    .sistEndretAvAktoerId(innloggetAktoerId)).id;
        }
    }

    @Transactional
    public void slettTiltak(Long tiltakId, String fnr) {
        String innloggetAktoerId = aktoerService.hentAktoerIdForFnr(fnr);
        Tiltak tiltak = tiltakDAO.finnTiltakById(tiltakId);

        if (!tiltak.opprettetAvAktoerId.equals(innloggetAktoerId)) {
            throw new ForbiddenException("Ikke tilgang");
        }

        if (godkjenningerDAO.godkjenningerByOppfoelgingsdialogId(tiltak.oppfoelgingsdialogId).stream().anyMatch(pGodkjenning -> pGodkjenning.godkjent)) {
            throw new ConflictException();
        }

        oppfoelingsdialogDAO.sistEndretAv(tiltak.oppfoelgingsdialogId, innloggetAktoerId);
        List<Kommentar> kommetarer = kommentarDAO.finnKommentarerByTiltakId(tiltakId);
        kommetarer.forEach(kommentar -> kommentarDAO.delete(kommentar.id));
        tiltakDAO.deleteById(tiltakId);
    }
}
