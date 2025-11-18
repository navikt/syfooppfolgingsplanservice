package no.nav.syfo.oppgave.oppfoelgingsdialog;

import no.nav.syfo.arkivporten.ArkivportenConsumer;
import no.nav.syfo.arkivporten.Document;
import no.nav.syfo.arkivporten.DocumentType;
import no.nav.syfo.domain.Oppfolgingsplan;
import no.nav.syfo.metric.Metrikk;
import no.nav.syfo.oppgave.Jobb;
import no.nav.syfo.oppgave.Oppgavetype;
import no.nav.syfo.pdl.PdlConsumer;
import no.nav.syfo.service.OppfolgingsplanService;
import no.nav.syfo.service.PdfService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

import java.util.UUID;

import static no.nav.syfo.oppgave.Oppgavetype.OPPFOELGINGSDIALOG_ARKIVPORTEN_SEND;

@Service
public class JobbSendOppfoelgingsdialogTilArkivporten implements Jobb {

    private final PdlConsumer pdlConsumer;
    private final Metrikk metrikk;
    private final OppfolgingsplanService oppfolgingsplanService;
    private final PdfService pdfService;
    private final ArkivportenConsumer arkivportenConsumer;

    @Override
    public Oppgavetype oppgavetype() {
        return OPPFOELGINGSDIALOG_ARKIVPORTEN_SEND;
    }

    @Inject
    public JobbSendOppfoelgingsdialogTilArkivporten(
            PdlConsumer pdlConsumer,
            Metrikk metrikk,
            OppfolgingsplanService oppfolgingsplanService,
            PdfService pdfService, ArkivportenConsumer arkivportenConsumer
    ) {
        this.pdlConsumer = pdlConsumer;
        this.metrikk = metrikk;
        this.oppfolgingsplanService = oppfolgingsplanService;
        this.pdfService = pdfService;
        this.arkivportenConsumer = arkivportenConsumer;
    }


    @Override
    public void utfoerOppgave(String oppfoelgingsdialogId) {
        Oppfolgingsplan oppfolgingsplan = oppfolgingsplanService.hentGodkjentOppfolgingsplan(Long.valueOf(oppfoelgingsdialogId));
        oppfolgingsplan.arbeidstaker.fnr = pdlConsumer.fnr(oppfolgingsplan.arbeidstaker.aktoerId);
        String arbeidstakerNavn = pdlConsumer.personName(oppfolgingsplan.arbeidstaker.fnr);

        byte[] oppfoelgingsdialogPdf = pdfService.hentPdfTilArkivporten(oppfolgingsplan);
        Document document = new Document(
                UUID.fromString(oppfolgingsplan.uuid),
                DocumentType.OPPFOLGINGSPLAN,
                oppfoelgingsdialogPdf,
                "application/pdf",
                oppfolgingsplan.virksomhet.virksomhetsnummer,
                Document.Companion.title(arbeidstakerNavn),
                Document.Companion.summary(oppfolgingsplan.sistEndretDato)
        );
        arkivportenConsumer.sendDocument(document);
        metrikk.tellHendelse("plan_sendt_til_arkivporten");
    }
}
