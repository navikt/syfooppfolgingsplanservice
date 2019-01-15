package no.nav.syfo.oppgave.oppfoelgingsdialog;

import no.nav.metrics.Event;
import no.nav.syfo.domain.Oppfoelgingsdialog;
import no.nav.syfo.domain.OppfoelgingsdialogAltinn;
import no.nav.syfo.oppgave.Jobb;
import no.nav.syfo.oppgave.Oppgavetype;
import no.nav.syfo.service.AktoerService;
import no.nav.syfo.service.OppfoelgingsdialogService;
import no.nav.syfo.service.PdfService;
import no.nav.syfo.ws.AltinnConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

@Service
public class JobbSendOppfoelgingsdialogTilAltinn implements Jobb {

    public static final Logger LOG = LoggerFactory.getLogger(JobbSendOppfoelgingsdialogTilAltinn.class);

    private OppfoelgingsdialogService oppfoelgingsdialogService;
    private AltinnConsumer altinnConsumer;
    private AktoerService aktoerService;
    private PdfService pdfService;

    @Override
    public Oppgavetype oppgavetype() {
        return OPPFOELGINGSDIALOG_SEND;
    }

    @Override
    public void utfoerOppgave(String oppfoelgingsdialogId) {
        if (!"true".equals(getProperty(LOCAL_MOCK)) && toggleBatch()) {
            LOG.info("TRACEBATCH: run {}", this.getClass().getName());

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

    @Inject
    public void setOppfoelgingsdialogService(OppfoelgingsdialogService oppfoelgingsdialogService) {
        this.oppfoelgingsdialogService = oppfoelgingsdialogService;
    }

    @Inject
    public void setAktoerIdConsumer(AktoerService aktoerService) {
        this.aktoerService = aktoerService;
    }

    @Inject
    public void setAltinnConsumer(AltinnConsumer altinnConsumer) {
        this.altinnConsumer = altinnConsumer;
    }

    @Inject
    public void setPdfService(PdfService pdfService) {
        this.pdfService = pdfService;
    }
}
