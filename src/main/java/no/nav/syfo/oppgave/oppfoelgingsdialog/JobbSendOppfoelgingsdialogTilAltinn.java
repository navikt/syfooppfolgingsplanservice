package no.nav.syfo.oppgave.oppfoelgingsdialog;

import lombok.extern.slf4j.Slf4j;
import no.nav.syfo.aktorregister.AktorregisterConsumer;
import no.nav.syfo.domain.Oppfoelgingsdialog;
import no.nav.syfo.domain.OppfolgingsplanAltinn;
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

    private final AktorregisterConsumer aktorregisterConsumer;
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
            AktorregisterConsumer aktorregisterConsumer,
            AltinnConsumer altinnConsumer,
            Metrikk metrikk,
            OppfolgingsplanService oppfolgingsplanService,
            PdfService pdfService
    ) {
        this.aktorregisterConsumer = aktorregisterConsumer;
        this.altinnConsumer = altinnConsumer;
        this.metrikk = metrikk;
        this.oppfolgingsplanService = oppfolgingsplanService;
        this.pdfService = pdfService;
    }


    @Override
    public void utfoerOppgave(String oppfoelgingsdialogId) {
        log.info("TRACEBATCH: run {}", this.getClass().getName());

        Oppfoelgingsdialog oppfoelgingsdialog = oppfolgingsplanService.hentGodkjentOppfolgingsplan(Long.valueOf(oppfoelgingsdialogId));
        oppfoelgingsdialog.arbeidstaker.fnr = aktorregisterConsumer.hentFnrForAktor(oppfoelgingsdialog.arbeidstaker.aktoerId);

        byte[] oppfoelgingsdialogPdf = pdfService.hentPdfTilAltinn(oppfoelgingsdialog);

        OppfolgingsplanAltinn oppfolgingsplanAltinn = new OppfolgingsplanAltinn(oppfoelgingsdialog, oppfoelgingsdialogPdf);

        altinnConsumer.sendOppfolgingsplanTilArbeidsgiver(oppfolgingsplanAltinn);


        LocalDateTime dato = Optional.ofNullable(Objects.requireNonNull(oppfoelgingsdialog.godkjentPlan
                .orElse(null)).opprettetTidspunkt)
                .orElse(now());

        metrikk.tellAntallDagerSiden(dato, "antallDagerOpprettetOppfoelgingsdialog");
        metrikk.tellHendelse("plan_sendt_til_altinn");
    }
}
