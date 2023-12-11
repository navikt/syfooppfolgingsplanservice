package no.nav.syfo.service;

import no.nav.syfo.domain.Oppfolgingsplan;
import no.nav.syfo.metric.Metrikk;
import no.nav.syfo.pdl.PdlConsumer;
import no.nav.syfo.repository.dao.OppfolgingsplanDAO;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import jakarta.ws.rs.ForbiddenException;

import static no.nav.syfo.util.OppfoelgingsdialogUtil.erArbeidstakeren;

@Service
public class SamtykkeService {

    private PdlConsumer pdlConsumer;
    private Metrikk metrikk;
    private OppfolgingsplanDAO oppfolgingsplanDAO;
    private TilgangskontrollService tilgangskontrollService;

    @Inject
    public SamtykkeService(
            PdlConsumer pdlConsumer,
            Metrikk metrikk,
            OppfolgingsplanDAO oppfolgingsplanDAO,
            TilgangskontrollService tilgangskontrollService
    ) {
        this.pdlConsumer = pdlConsumer;
        this.metrikk = metrikk;
        this.oppfolgingsplanDAO = oppfolgingsplanDAO;
        this.tilgangskontrollService = tilgangskontrollService;

    }

    public void giSamtykke(Long oppfoelgingsdialogId, String fnr, boolean giSamtykke) {
        String aktoerId = pdlConsumer.aktorid(fnr);
        Oppfolgingsplan oppfolgingsplan = oppfolgingsplanDAO.finnOppfolgingsplanMedId(oppfoelgingsdialogId);

        if (!tilgangskontrollService.brukerTilhorerOppfolgingsplan(fnr, oppfolgingsplan)) {
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
