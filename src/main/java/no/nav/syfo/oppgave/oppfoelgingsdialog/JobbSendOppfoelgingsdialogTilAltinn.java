package no.nav.syfo.oppgave.oppfoelgingsdialog;

import lombok.extern.slf4j.Slf4j;
import no.nav.metrics.Event;
import no.nav.syfo.domain.Oppfoelgingsdialog;
import no.nav.syfo.domain.OppfoelgingsdialogAltinn;
import no.nav.syfo.oppgave.Jobb;
import no.nav.syfo.oppgave.Oppgavetype;
import no.nav.syfo.service.*;
import no.nav.syfo.ws.AltinnConsumer;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Optional;

import static java.lang.System.getProperty;
import static java.time.LocalDateTime.now;
import static java.time.temporal.ChronoUnit.DAYS;
import static no.nav.metrics.MetricsFactory.createEvent;
import static no.nav.syfo.oppgave.Oppgavetype.OPPFOELGINGSDIALOG_SEND;
import static no.nav.syfo.util.PropertyUtil.LOCAL_MOCK;
import static no.nav.syfo.util.ToggleUtil.toggleBatch;

@Slf4j
@Service
public class JobbSendOppfoelgingsdialogTilAltinn implements Jobb {

    private final AktoerService aktoerService;
    private final AltinnConsumer altinnConsumer;
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
            OppfoelgingsdialogService oppfoelgingsdialogService,
            PdfService pdfService
    ) {
        this.aktoerService = aktoerService;
        this.altinnConsumer = altinnConsumer;
        this.oppfoelgingsdialogService = oppfoelgingsdialogService;
        this.pdfService = pdfService;
    }


    @Override
    public void utfoerOppgave(String oppfoelgingsdialogId) {
        if (!"true".equals(getProperty(LOCAL_MOCK)) && toggleBatch()) {
            log.info("TRACEBATCH: run {}", this.getClass().getName());

            Oppfoelgingsdialog oppfoelgingsdialog = oppfoelgingsdialogService.hentGodkjentOppfoelgingsdialog(Long.valueOf(oppfoelgingsdialogId));
            oppfoelgingsdialog.arbeidstaker.fnr = aktoerService.hentFnrForAktoer(oppfoelgingsdialog.arbeidstaker.aktoerId);

            byte[] oppfoelgingsdialogPdf = pdfService.hentPdfTilAltinn(oppfoelgingsdialog);

            OppfoelgingsdialogAltinn oppfoelgingsdialogAltinn = new OppfoelgingsdialogAltinn(oppfoelgingsdialog, oppfoelgingsdialogPdf);

            altinnConsumer.sendOppfoelgingsplanTilArbeidsgiver(oppfoelgingsdialogAltinn);

            Event event = createEvent("antallDagerOpprettetOppfoelgingsdialog");
            long dager = DAYS.between(Optional.ofNullable(oppfoelgingsdialog.godkjentPlan
                    .orElse(null).opprettetTidspunkt)
                    .orElse(now()), now());
            event.addFieldToReport("dager", dager);
            event.report();
        }
    }
}
