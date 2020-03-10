package no.nav.syfo.service;

import no.nav.syfo.aktorregister.AktorregisterConsumer;
import no.nav.syfo.domain.Oppfolgingsplan;
import no.nav.syfo.metric.Metrikk;
import no.nav.syfo.repository.dao.OppfolgingsplanDAO;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.ws.rs.ForbiddenException;

import static no.nav.syfo.util.OppfoelgingsdialogUtil.erArbeidstakeren;

@Service
public class SamtykkeService {

    private AktorregisterConsumer aktorregisterConsumer;
    private Metrikk metrikk;
    private OppfolgingsplanDAO oppfolgingsplanDAO;
    private TilgangskontrollService tilgangskontrollService;

    @Inject
    public SamtykkeService(
            AktorregisterConsumer aktorregisterConsumer,
            Metrikk metrikk,
            OppfolgingsplanDAO oppfolgingsplanDAO,
            TilgangskontrollService tilgangskontrollService
    ) {
        this.aktorregisterConsumer = aktorregisterConsumer;
        this.metrikk = metrikk;
        this.oppfolgingsplanDAO = oppfolgingsplanDAO;
        this.tilgangskontrollService = tilgangskontrollService;

    }

    public void giSamtykke(Long oppfoelgingsdialogId, String fnr, boolean giSamtykke) {
        String aktoerId = aktorregisterConsumer.hentAktorIdForFnr(fnr);
        Oppfolgingsplan oppfolgingsplan = oppfolgingsplanDAO.finnOppfolgingsplanMedId(oppfoelgingsdialogId);

        if (!tilgangskontrollService.aktorTilhorerOppfolgingsplan(aktoerId, oppfolgingsplan)) {
            throw new ForbiddenException("Ikke tilgang");
        }

        metrikk.tellOPSamtykke(giSamtykke);
        if (erArbeidstakeren(oppfolgingsplan, aktoerId)) {
            oppfolgingsplanDAO.lagreSamtykkeSykmeldt(oppfoelgingsdialogId, giSamtykke);
        } else {
            oppfolgingsplanDAO.lagreSamtykkeArbeidsgiver(oppfoelgingsdialogId, giSamtykke);
        }
    }
}
