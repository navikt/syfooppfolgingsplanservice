package no.nav.syfo.oppgave.oppfoelgingsdialog;

import lombok.extern.slf4j.Slf4j;
import no.nav.syfo.domain.Oppfoelgingsdialog;
import no.nav.syfo.domain.OppfoelgingsdialogAltinn;
import no.nav.syfo.metric.Metrikk;
import no.nav.syfo.oppgave.Jobb;
import no.nav.syfo.oppgave.Oppgavetype;
import no.nav.syfo.service.*;
import no.nav.syfo.util.Toggle;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

import static no.nav.syfo.oppgave.Oppgavetype.OPPFOELGINGSDIALOG_ARKIVER;

@Slf4j
@Service
public class JobbLoggSendOppfoelgingsdialogTilAltinn implements Jobb {

    private final JuridiskLoggService juridiskLoggService;
    private final OppfoelgingsdialogService oppfoelgingsdialogService;
    private final PdfService pdfService;
    private final Metrikk metrikk;
    private final Toggle toggle;

    @Inject
    public JobbLoggSendOppfoelgingsdialogTilAltinn(
            JuridiskLoggService juridiskLoggService,
            OppfoelgingsdialogService oppfoelgingsdialogService,
            PdfService pdfService,
            Metrikk metrikk,
            Toggle toggle
    ) {
        this.juridiskLoggService = juridiskLoggService;
        this.oppfoelgingsdialogService = oppfoelgingsdialogService;
        this.pdfService = pdfService;
        this.metrikk = metrikk;
        this.toggle = toggle;
    }

    @Override
    public Oppgavetype oppgavetype() {
        return OPPFOELGINGSDIALOG_ARKIVER;
    }

    @Override
    public void utfoerOppgave(String oppfoelgingsdialogId) {
        if (toggle.toggleBatch()) {
            log.info("TRACEBATCH: run {}", this.getClass().getName());

            Oppfoelgingsdialog oppfoelgingsdialog = oppfoelgingsdialogService.hentGodkjentOppfoelgingsdialog(Long.valueOf(oppfoelgingsdialogId));

            byte[] oppfoelgingsdialogPdf = pdfService.hentPdfTilAltinn(oppfoelgingsdialog);

            OppfoelgingsdialogAltinn oppfoelgingsdialogAltinn = new OppfoelgingsdialogAltinn(oppfoelgingsdialog, oppfoelgingsdialogPdf);

            juridiskLoggService.loggSendOppfoelgingsdialogTilAltinn(oppfoelgingsdialogAltinn);

            metrikk.tellHendelse("logget_plan_sendt_til_altinn");
        }
    }
}
