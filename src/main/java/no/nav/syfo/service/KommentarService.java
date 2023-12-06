package no.nav.syfo.service;

import no.nav.syfo.domain.*;
import no.nav.syfo.pdl.PdlConsumer;
import no.nav.syfo.repository.dao.*;
import no.nav.syfo.util.ConflictException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import jakarta.ws.rs.ForbiddenException;

import static no.nav.syfo.util.OppfoelgingsdialogUtil.kanEndreElement;

@Service
public class KommentarService {

    private PdlConsumer pdlConsumer;
    private GodkjenningerDAO godkjenningerDAO;
    private KommentarDAO kommentarDAO;
    private OppfolgingsplanDAO oppfolgingsplanDAO;
    private TiltakDAO tiltakDAO;
    private TilgangskontrollService tilgangskontrollService;

    @Inject
    public KommentarService(
            PdlConsumer pdlConsumer,
            GodkjenningerDAO godkjenningerDAO,
            KommentarDAO kommentarDAO,
            OppfolgingsplanDAO oppfolgingsplanDAO,
            TiltakDAO tiltakDAO,
            TilgangskontrollService tilgangskontrollService
    ) {
        this.pdlConsumer = pdlConsumer;
        this.godkjenningerDAO = godkjenningerDAO;
        this.kommentarDAO = kommentarDAO;
        this.oppfolgingsplanDAO = oppfolgingsplanDAO;
        this.tiltakDAO = tiltakDAO;
        this.tilgangskontrollService = tilgangskontrollService;
    }

    @Transactional
    public Long lagreKommentar(Long tiltakId, Kommentar kommentar, String fnr) {
        Tiltak tiltak = tiltakDAO.finnTiltakById(tiltakId);
        String innloggetAktoerId = pdlConsumer.aktorid(fnr);

        Oppfolgingsplan oppfolgingsplan = oppfolgingsplanDAO.finnOppfolgingsplanMedId(tiltak.oppfoelgingsdialogId);
        if (kommentarenErIkkeOpprettetAvNoenAndre(kommentar, innloggetAktoerId, oppfolgingsplan)) {
            throw new ForbiddenException("Ikke tilgang");
        }

        if (godkjenningerDAO.godkjenningerByOppfoelgingsdialogId(tiltak.oppfoelgingsdialogId).stream().anyMatch(pGodkjenning -> pGodkjenning.godkjent)) {
            throw new ConflictException();
        }

        oppfolgingsplanDAO.sistEndretAv(tiltak.oppfoelgingsdialogId, innloggetAktoerId);
        if (kommentar.id == null) {
            return kommentarDAO.create(kommentar
                    .tiltakId(tiltakId)
                    .opprettetAvAktoerId(innloggetAktoerId)
                    .sistEndretAvAktoerId(innloggetAktoerId)
            ).id;
        } else {
            return kommentarDAO.update(kommentar).id;
        }
    }

    private boolean kommentarenErIkkeOpprettetAvNoenAndre(Kommentar kommentar, String innloggetAktoerId, Oppfolgingsplan oppfolgingsplan) {
        return kommentar.id != null && oppfolgingsplan.tiltakListe.stream().noneMatch(pTiltak -> pTiltak.opprettetAvAktoerId.equals(innloggetAktoerId));
    }

    @Transactional
    public void slettKommentar(Long kommentarId, String fnr) {
        String innloggetAktoerId = pdlConsumer.aktorid(fnr);
        Kommentar kommentar = kommentarDAO.finnKommentar(kommentarId);
        Oppfolgingsplan oppfolgingsplan = oppfolgingsplanDAO.oppfolgingsplanByTiltakId(kommentar.tiltakId);

        if (!tilgangskontrollService.brukerTilhorerOppfolgingsplan(fnr, oppfolgingsplan) || !kanEndreElement(innloggetAktoerId, oppfolgingsplan.arbeidstaker.aktoerId, kommentar.opprettetAvAktoerId)) {
            throw new ForbiddenException("Ikke tilgang");
        }

        if (godkjenningerDAO.godkjenningerByOppfoelgingsdialogId(oppfolgingsplan.id).stream().anyMatch(godkjenning -> godkjenning.godkjent)) {
            throw new ConflictException();
        }

        oppfolgingsplanDAO.sistEndretAv(oppfolgingsplan.id, innloggetAktoerId);
        kommentarDAO.delete(kommentarId);
    }
}
