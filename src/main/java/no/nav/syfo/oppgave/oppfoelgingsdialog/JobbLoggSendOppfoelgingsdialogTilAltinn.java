package no.nav.syfo.oppgave.oppfoelgingsdialog;

import lombok.extern.slf4j.Slf4j;
import no.nav.syfo.domain.Oppfoelgingsdialog;
import no.nav.syfo.domain.OppfolgingsplanAltinn;
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
    private final OppfolgingsplanService oppfolgingsplanService;
    private final PdfService pdfService;
    private final Metrikk metrikk;
    private final Toggle toggle;

    @Inject
    public JobbLoggSendOppfoelgingsdialogTilAltinn(
            JuridiskLoggService juridiskLoggService,
            OppfolgingsplanService oppfolgingsplanService,
            PdfService pdfService,
            Metrikk metrikk,
            Toggle toggle
    ) {
        this.juridiskLoggService = juridiskLoggService;
        this.oppfolgingsplanService = oppfolgingsplanService;
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
        Oppfoelgingsdialog oppfoelgingsdialog = oppfolgingsplanService.hentGodkjentOppfolgingsplan(Long.valueOf(oppfoelgingsdialogId));

        byte[] oppfoelgingsdialogPdf = pdfService.hentPdfTilAltinn(oppfoelgingsdialog);

        OppfolgingsplanAltinn oppfolgingsplanAltinn = new OppfolgingsplanAltinn(oppfoelgingsdialog, oppfoelgingsdialogPdf);

        juridiskLoggService.loggSendOppfoelgingsdialogTilAltinn(oppfolgingsplanAltinn);

        metrikk.tellHendelse("logget_plan_sendt_til_altinn");
    }
}
