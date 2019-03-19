package no.nav.syfo.service;

import no.nav.syfo.domain.*;
import no.nav.syfo.metric.Metrikk;
import no.nav.syfo.repository.dao.*;
import no.nav.syfo.util.ConflictException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.ws.rs.ForbiddenException;
import java.util.List;

import static no.nav.syfo.util.OppfoelgingsdialogUtil.eksisterendeTiltakHoererTilDialog;

@Service
public class TiltakService {

    private AktoerService aktoerService;
    private GodkjenningerDAO godkjenningerDAO;
    private KommentarDAO kommentarDAO;
    private Metrikk metrikk;
    private OppfoelingsdialogDAO oppfoelingsdialogDAO;
    private TilgangskontrollService tilgangskontrollService;
    private TiltakDAO tiltakDAO;

    @Inject
    public TiltakService(
            AktoerService aktoerService,
            GodkjenningerDAO godkjenningerDAO,
            KommentarDAO kommentarDAO,
            Metrikk metrikk,
            OppfoelingsdialogDAO oppfoelingsdialogDAO,
            TilgangskontrollService tilgangskontrollService,
            TiltakDAO tiltakDAO
    ) {
        this.aktoerService = aktoerService;
        this.godkjenningerDAO = godkjenningerDAO;
        this.kommentarDAO = kommentarDAO;
        this.metrikk = metrikk;
        this.oppfoelingsdialogDAO = oppfoelingsdialogDAO;
        this.tilgangskontrollService = tilgangskontrollService;
        this.tiltakDAO = tiltakDAO;
    }

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
            metrikk.tellHendelse("nyttTiltak");
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
