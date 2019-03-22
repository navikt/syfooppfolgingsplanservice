package no.nav.syfo.oppgave.oppfoelgingsdialog;

import lombok.extern.slf4j.Slf4j;
import no.nav.syfo.domain.Oppfoelgingsdialog;
import no.nav.syfo.domain.OppfoelgingsdialogAltinn;
import no.nav.syfo.oppgave.Jobb;
import no.nav.syfo.oppgave.Oppgavetype;
import no.nav.syfo.service.*;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

import static java.lang.System.getProperty;
import static no.nav.syfo.oppgave.Oppgavetype.OPPFOELGINGSDIALOG_ARKIVER;
import static no.nav.syfo.util.PropertyUtil.LOCAL_MOCK;
import static no.nav.syfo.util.ToggleUtil.toggleBatch;

@Slf4j
@Service
public class JobbLoggSendOppfoelgingsdialogTilAltinn implements Jobb {

    private final JuridiskLoggService juridiskLoggService;
    private final OppfoelgingsdialogService oppfoelgingsdialogService;
    private final PdfService pdfService;

    @Inject
    public JobbLoggSendOppfoelgingsdialogTilAltinn(
            JuridiskLoggService juridiskLoggService,
            OppfoelgingsdialogService oppfoelgingsdialogService,
            PdfService pdfService
    ) {
        this.juridiskLoggService = juridiskLoggService;
        this.oppfoelgingsdialogService = oppfoelgingsdialogService;
        this.pdfService = pdfService;
    }

    @Override
    public Oppgavetype oppgavetype() {
        return OPPFOELGINGSDIALOG_ARKIVER;
    }

    @Override
    public void utfoerOppgave(String oppfoelgingsdialogId) {
        if (!"true".equals(getProperty(LOCAL_MOCK)) && toggleBatch()) {
            log.info("TRACEBATCH: run {}", this.getClass().getName());

            Oppfoelgingsdialog oppfoelgingsdialog = oppfoelgingsdialogService.hentGodkjentOppfoelgingsdialog(Long.valueOf(oppfoelgingsdialogId));

            byte[] oppfoelgingsdialogPdf = pdfService.hentPdfTilAltinn(oppfoelgingsdialog);

            OppfoelgingsdialogAltinn oppfoelgingsdialogAltinn = new OppfoelgingsdialogAltinn(oppfoelgingsdialog, oppfoelgingsdialogPdf);

            juridiskLoggService.loggSendOppfoelgingsdialogTilAltinn(oppfoelgingsdialogAltinn);
        }
    }
}
