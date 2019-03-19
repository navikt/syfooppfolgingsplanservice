package no.nav.syfo.service;

import no.nav.syfo.domain.Oppfoelgingsdialog;
import no.nav.syfo.metric.Metrikk;
import no.nav.syfo.repository.dao.OppfoelingsdialogDAO;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.ws.rs.ForbiddenException;

import static no.nav.syfo.util.OppfoelgingsdialogUtil.erArbeidstakeren;

@Service
public class SamtykkeService {

    private AktoerService aktoerService;
    private Metrikk metrikk;
    private OppfoelingsdialogDAO oppfoelingsdialogDAO;
    private TilgangskontrollService tilgangskontrollService;

    @Inject
    public SamtykkeService(
            AktoerService aktoerService,
            Metrikk metrikk,
            OppfoelingsdialogDAO oppfoelingsdialogDAO,
            TilgangskontrollService tilgangskontrollService
    ) {
        this.aktoerService = aktoerService;
        this.metrikk = metrikk;
        this.oppfoelingsdialogDAO = oppfoelingsdialogDAO;
        this.tilgangskontrollService = tilgangskontrollService;

    }

    public void giSamtykke(Long oppfoelgingsdialogId, String fnr, boolean giSamtykke) {
        String aktoerId = aktoerService.hentAktoerIdForFnr(fnr);
        Oppfoelgingsdialog oppfoelgingsdialog = oppfoelingsdialogDAO.finnOppfoelgingsdialogMedId(oppfoelgingsdialogId);

        if (!tilgangskontrollService.aktoerTilhoererDialogen(aktoerId, oppfoelgingsdialog)) {
            throw new ForbiddenException("Ikke tilgang");
        }

        metrikk.tellOPSamtykke(giSamtykke);
        if (erArbeidstakeren(oppfoelgingsdialog, aktoerId)) {
            oppfoelingsdialogDAO.lagreSamtykkeSykmeldt(oppfoelgingsdialogId, giSamtykke);
        } else {
            oppfoelingsdialogDAO.lagreSamtykkeArbeidsgiver(oppfoelgingsdialogId, giSamtykke);
        }
    }
}
