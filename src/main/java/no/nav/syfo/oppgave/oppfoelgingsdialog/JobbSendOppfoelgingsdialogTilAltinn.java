package no.nav.syfo.oppgave.oppfoelgingsdialog;

import no.nav.syfo.domain.Oppfolgingsplan;
import no.nav.syfo.domain.OppfolgingsplanAltinn;
import no.nav.syfo.metric.Metrikk;
import no.nav.syfo.oppgave.Jobb;
import no.nav.syfo.oppgave.Oppgavetype;
import no.nav.syfo.pdl.PdlConsumer;
import no.nav.syfo.service.OppfolgingsplanService;
import no.nav.syfo.service.PdfService;
import no.nav.syfo.ws.AltinnConsumer;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

import static java.time.LocalDateTime.now;
import static no.nav.syfo.oppgave.Oppgavetype.OPPFOELGINGSDIALOG_SEND;

@Service
public class JobbSendOppfoelgingsdialogTilAltinn implements Jobb {

    private final PdlConsumer pdlConsumer;
    private final AltinnConsumer altinnConsumer;
    private final Metrikk metrikk;
    private final OppfolgingsplanService oppfolgingsplanService;
    private final PdfService pdfService;

    @Override
    public Oppgavetype oppgavetype() {
        return OPPFOELGINGSDIALOG_SEND;
    }

    @Inject
    public JobbSendOppfoelgingsdialogTilAltinn(
            PdlConsumer pdlConsumer,
            AltinnConsumer altinnConsumer,
            Metrikk metrikk,
            OppfolgingsplanService oppfolgingsplanService,
            PdfService pdfService
    ) {
        this.pdlConsumer = pdlConsumer;
        this.altinnConsumer = altinnConsumer;
        this.metrikk = metrikk;
        this.oppfolgingsplanService = oppfolgingsplanService;
        this.pdfService = pdfService;
    }


    @Override
    public void utfoerOppgave(String oppfoelgingsdialogId) {
        Oppfolgingsplan oppfolgingsplan = oppfolgingsplanService.hentGodkjentOppfolgingsplan(Long.valueOf(oppfoelgingsdialogId));
        oppfolgingsplan.arbeidstaker.fnr = pdlConsumer.fnr(oppfolgingsplan.arbeidstaker.aktoerId);

        byte[] oppfoelgingsdialogPdf = pdfService.hentPdfTilAltinn(oppfolgingsplan);

        OppfolgingsplanAltinn oppfolgingsplanAltinn = new OppfolgingsplanAltinn(oppfolgingsplan, oppfoelgingsdialogPdf);

        altinnConsumer.sendOppfolgingsplanTilArbeidsgiver(oppfolgingsplanAltinn);


        LocalDateTime dato = Optional.ofNullable(Objects.requireNonNull(oppfolgingsplan.godkjentPlan
                .orElse(null)).opprettetTidspunkt)
                .orElse(now());

        metrikk.tellAntallDagerSiden(dato, "antallDagerOpprettetOppfoelgingsdialog");
        metrikk.tellHendelse("plan_sendt_til_altinn");
    }
}
