package no.nav.syfo.service;

import no.nav.syfo.aktorregister.AktorregisterConsumer;
import no.nav.syfo.domain.Oppfoelgingsdialog;
import no.nav.syfo.metric.Metrikk;
import no.nav.syfo.repository.dao.OppfoelingsdialogDAO;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.ws.rs.ForbiddenException;

import static no.nav.syfo.util.OppfoelgingsdialogUtil.erArbeidstakeren;

@Service
public class SamtykkeService {

    private AktorregisterConsumer aktorregisterConsumer;
    private Metrikk metrikk;
    private OppfoelingsdialogDAO oppfoelingsdialogDAO;
    private TilgangskontrollService tilgangskontrollService;

    @Inject
    public SamtykkeService(
            AktorregisterConsumer aktorregisterConsumer,
            Metrikk metrikk,
            OppfoelingsdialogDAO oppfoelingsdialogDAO,
            TilgangskontrollService tilgangskontrollService
    ) {
        this.aktorregisterConsumer = aktorregisterConsumer;
        this.metrikk = metrikk;
        this.oppfoelingsdialogDAO = oppfoelingsdialogDAO;
        this.tilgangskontrollService = tilgangskontrollService;

    }

    public void giSamtykke(Long oppfoelgingsdialogId, String fnr, boolean giSamtykke) {
        String aktoerId = aktorregisterConsumer.hentAktorIdForFnr(fnr);
        Oppfoelgingsdialog oppfoelgingsdialog = oppfoelingsdialogDAO.finnOppfolgingsplanMedId(oppfoelgingsdialogId);

        if (!tilgangskontrollService.aktorTilhorerOppfolgingsplan(aktoerId, oppfoelgingsdialog)) {
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
