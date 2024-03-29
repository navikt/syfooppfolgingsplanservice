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
import java.util.List;

import static no.nav.syfo.util.OppfoelgingsdialogUtil.eksisterendeTiltakHoererTilDialog;
import static no.nav.syfo.util.OppfoelgingsdialogUtil.kanEndreElement;

@Service
public class TiltakService {

    private PdlConsumer pdlConsumer;
    private GodkjenningerDAO godkjenningerDAO;
    private KommentarDAO kommentarDAO;
    private Metrikk metrikk;
    private OppfolgingsplanDAO oppfolgingsplanDAO;
    private TilgangskontrollService tilgangskontrollService;
    private TiltakDAO tiltakDAO;

    @Inject
    public TiltakService(
            PdlConsumer pdlConsumer,
            GodkjenningerDAO godkjenningerDAO,
            KommentarDAO kommentarDAO,
            Metrikk metrikk,
            OppfolgingsplanDAO oppfolgingsplanDAO,
            TilgangskontrollService tilgangskontrollService,
            TiltakDAO tiltakDAO
    ) {
        this.pdlConsumer = pdlConsumer;
        this.godkjenningerDAO = godkjenningerDAO;
        this.kommentarDAO = kommentarDAO;
        this.metrikk = metrikk;
        this.oppfolgingsplanDAO = oppfolgingsplanDAO;
        this.tilgangskontrollService = tilgangskontrollService;
        this.tiltakDAO = tiltakDAO;
    }

    @Transactional
    public Long lagreTiltak(Long oppfoelgingsdialogId, Tiltak tiltak, String fnr) {
        Oppfolgingsplan oppfolgingsplan = oppfolgingsplanDAO.finnOppfolgingsplanMedId(oppfoelgingsdialogId);
        String innloggetAktoerId = pdlConsumer.aktorid(fnr);

        if (!eksisterendeTiltakHoererTilDialog(tiltak.id, tiltakDAO.finnTiltakByOppfoelgingsdialogId(oppfoelgingsdialogId)) || !tilgangskontrollService.brukerTilhorerOppfolgingsplan(fnr, oppfolgingsplan)) {
            throw new ForbiddenException("Ikke tilgang");
        }

        if (godkjenningerDAO.godkjenningerByOppfoelgingsdialogId(oppfoelgingsdialogId).stream().anyMatch(pGodkjenning -> pGodkjenning.godkjent)) {
            throw new ConflictException();
        }

        oppfolgingsplanDAO.sistEndretAv(oppfoelgingsdialogId, innloggetAktoerId);
        if (tiltak.id == null) {
            metrikk.tellHendelse("lagre_tiltak_nytt");
            return tiltakDAO.create(tiltak
                    .oppfoelgingsdialogId(oppfoelgingsdialogId)
                    .opprettetAvAktoerId(innloggetAktoerId)
                    .sistEndretAvAktoerId(innloggetAktoerId)).id;
        } else {
            metrikk.tellHendelse("lagre_tiltak_eksisterende");
            return tiltakDAO.update(tiltak
                    .oppfoelgingsdialogId(oppfoelgingsdialogId)
                    .sistEndretAvAktoerId(innloggetAktoerId)).id;
        }
    }

    @Transactional
    public void slettTiltak(Long tiltakId, String fnr) {
        String innloggetAktoerId = pdlConsumer.aktorid(fnr);
        Tiltak tiltak = tiltakDAO.finnTiltakById(tiltakId);
        Oppfolgingsplan oppfolgingsplan = oppfolgingsplanDAO.finnOppfolgingsplanMedId(tiltak.oppfoelgingsdialogId);

        if (!tilgangskontrollService.brukerTilhorerOppfolgingsplan(fnr, oppfolgingsplan) || !kanEndreElement(innloggetAktoerId, oppfolgingsplan.arbeidstaker.aktoerId, tiltak.opprettetAvAktoerId)) {
            throw new ForbiddenException("Ikke tilgang");
        }

        if (godkjenningerDAO.godkjenningerByOppfoelgingsdialogId(tiltak.oppfoelgingsdialogId).stream().anyMatch(pGodkjenning -> pGodkjenning.godkjent)) {
            throw new ConflictException();
        }

        oppfolgingsplanDAO.sistEndretAv(tiltak.oppfoelgingsdialogId, innloggetAktoerId);
        List<Kommentar> kommetarer = kommentarDAO.finnKommentarerByTiltakId(tiltakId);
        kommetarer.forEach(kommentar -> kommentarDAO.delete(kommentar.id));
        tiltakDAO.deleteById(tiltakId);
    }
}
