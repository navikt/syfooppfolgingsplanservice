package no.nav.syfo.oppgave.oppfoelgingsdialog;

import lombok.extern.slf4j.Slf4j;
import no.nav.syfo.domain.Oppfoelgingsdialog;
import no.nav.syfo.domain.OppfoelgingsdialogAltinn;
import no.nav.syfo.metric.Metrikk;
import no.nav.syfo.oppgave.Jobb;
import no.nav.syfo.oppgave.Oppgavetype;
import no.nav.syfo.service.*;
import no.nav.syfo.ws.AltinnConsumer;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

import static java.time.LocalDateTime.now;
import static no.nav.syfo.oppgave.Oppgavetype.OPPFOELGINGSDIALOG_SEND;

@Slf4j
@Service
public class JobbSendOppfoelgingsdialogTilAltinn implements Jobb {

    private final AktoerService aktoerService;
    private final AltinnConsumer altinnConsumer;
    private final Metrikk metrikk;
    private final OppfoelgingsdialogService oppfoelgingsdialogService;
    private final PdfService pdfService;

    @Override
    public Oppgavetype oppgavetype() {
        return OPPFOELGINGSDIALOG_SEND;
    }

    @Inject
    public JobbSendOppfoelgingsdialogTilAltinn(
            AktoerService aktoerService,
            AltinnConsumer altinnConsumer,
            Metrikk metrikk,
            OppfoelgingsdialogService oppfoelgingsdialogService,
            PdfService pdfService
    ) {
        this.aktoerService = aktoerService;
        this.altinnConsumer = altinnConsumer;
        this.metrikk = metrikk;
        this.oppfoelgingsdialogService = oppfoelgingsdialogService;
        this.pdfService = pdfService;
    }


    @Override
    public void utfoerOppgave(String oppfoelgingsdialogId) {
        log.info("TRACEBATCH: run {}", this.getClass().getName());

        Oppfoelgingsdialog oppfoelgingsdialog = oppfoelgingsdialogService.hentGodkjentOppfoelgingsdialog(Long.valueOf(oppfoelgingsdialogId));
        oppfoelgingsdialog.arbeidstaker.fnr = aktoerService.hentFnrForAktoer(oppfoelgingsdialog.arbeidstaker.aktoerId);

        byte[] oppfoelgingsdialogPdf = pdfService.hentPdfTilAltinn(oppfoelgingsdialog);

        OppfoelgingsdialogAltinn oppfoelgingsdialogAltinn = new OppfoelgingsdialogAltinn(oppfoelgingsdialog, oppfoelgingsdialogPdf);

        altinnConsumer.sendOppfoelgingsplanTilArbeidsgiver(oppfoelgingsdialogAltinn);


        LocalDateTime dato = Optional.ofNullable(Objects.requireNonNull(oppfoelgingsdialog.godkjentPlan
                .orElse(null)).opprettetTidspunkt)
                .orElse(now());

        metrikk.tellAntallDagerSiden(dato, "antallDagerOpprettetOppfoelgingsdialog");
        metrikk.tellHendelse("plan_sendt_til_altinn");
    }
}
