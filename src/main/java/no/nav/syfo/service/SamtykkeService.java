package no.nav.syfo.service;

import no.nav.syfo.domain.Oppfoelgingsdialog;
import no.nav.syfo.repository.dao.OppfoelingsdialogDAO;

import javax.inject.Inject;
import javax.ws.rs.ForbiddenException;

import static no.nav.metrics.MetricsFactory.createEvent;
import static no.nav.syfo.util.OppfoelgingsdialogUtil.erArbeidstakeren;

public class SamtykkeService {

    @Inject
    private OppfoelingsdialogDAO oppfoelingsdialogDAO;
    @Inject
    private TilgangskontrollService tilgangskontrollService;
    @Inject
    private AktoerService aktoerService;

    public void giSamtykke(Long oppfoelgingsdialogId, String fnr, boolean giSamtykke) {
        String aktoerId = aktoerService.hentAktoerIdForFnr(fnr);
        Oppfoelgingsdialog oppfoelgingsdialog = oppfoelingsdialogDAO.finnOppfoelgingsdialogMedId(oppfoelgingsdialogId);

        if (!tilgangskontrollService.aktoerTilhoererDialogen(aktoerId, oppfoelgingsdialog)) {
            throw new ForbiddenException("Ikke tilgang");
        }

        createEvent("samtykke").addFieldToReport("giSamtykke", giSamtykke).report();
        if (erArbeidstakeren(oppfoelgingsdialog, aktoerId)) {
            oppfoelingsdialogDAO.lagreSamtykkeSykmeldt(oppfoelgingsdialogId, giSamtykke);
        } else {
            oppfoelingsdialogDAO.lagreSamtykkeArbeidsgiver(oppfoelgingsdialogId, giSamtykke);
        }
    }
}
