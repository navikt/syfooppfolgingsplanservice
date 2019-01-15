package no.nav.syfo.oppgave.oppfoelgingsdialog;

import no.nav.syfo.domain.Oppfoelgingsdialog;
import no.nav.syfo.domain.OppfoelgingsdialogAltinn;
import no.nav.syfo.oppgave.Jobb;
import no.nav.syfo.oppgave.Oppgavetype;
import no.nav.syfo.scheduler.ProsesserInnkomnePlaner;
import no.nav.syfo.service.JuridiskLoggService;
import no.nav.syfo.service.OppfoelgingsdialogService;
import no.nav.syfo.service.PdfService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

import static java.lang.System.getProperty;
import static no.nav.syfo.oppgave.Oppgavetype.OPPFOELGINGSDIALOG_ARKIVER;
import static no.nav.syfo.util.PropertyUtil.LOCAL_MOCK;
import static no.nav.syfo.util.ToggleUtil.toggleBatch;

@Service
public class JobbLoggSendOppfoelgingsdialogTilAltinn implements Jobb {

    public static final Logger LOG = LoggerFactory.getLogger(JobbLoggSendOppfoelgingsdialogTilAltinn.class);

    private OppfoelgingsdialogService oppfoelgingsdialogService;
    private JuridiskLoggService juridiskLoggService;
    private PdfService pdfService;

    @Override
    public Oppgavetype oppgavetype() {
        return OPPFOELGINGSDIALOG_ARKIVER;
    }

    @Override
    public void utfoerOppgave(String oppfoelgingsdialogId) {
        if (!"true".equals(getProperty(LOCAL_MOCK)) && toggleBatch()) {
            LOG.info("TRACEBATCH: run {}", this.getClass().getName());

            Oppfoelgingsdialog oppfoelgingsdialog = oppfoelgingsdialogService.hentGodkjentOppfoelgingsdialog(Long.valueOf(oppfoelgingsdialogId));

            byte[] oppfoelgingsdialogPdf = pdfService.hentPdfTilAltinn(oppfoelgingsdialog);

            OppfoelgingsdialogAltinn oppfoelgingsdialogAltinn = new OppfoelgingsdialogAltinn(oppfoelgingsdialog, oppfoelgingsdialogPdf);

            juridiskLoggService.loggSendOppfoelgingsdialogTilAltinn(oppfoelgingsdialogAltinn);
        }
    }

    @Inject
    public void setOppfoelgingsdialogService(OppfoelgingsdialogService oppfoelgingsdialogService) {
        this.oppfoelgingsdialogService = oppfoelgingsdialogService;
    }

    @Inject
    public void setJuridiskLoggService(JuridiskLoggService juridiskLoggService) {
        this.juridiskLoggService = juridiskLoggService;
    }

    @Inject
    public void setPdfService(PdfService pdfService) {
        this.pdfService = pdfService;
    }
}
