package no.nav.syfo.domain;

import static no.nav.syfo.util.DigestUtil.sha512AsBase64String;

public class OppfolgingsplanAltinn {

    public Oppfoelgingsdialog oppfoelgingsdialog;
    private byte[] oppfoelgingsdialogPdf;
    private String hashOppfoelgingsdialogPDF;

    public OppfolgingsplanAltinn(Oppfoelgingsdialog oppfolgingsplan, byte[] oppfoelgingsdialogPdf) {
        this.oppfoelgingsdialog = oppfolgingsplan;
        this.oppfoelgingsdialogPdf = oppfoelgingsdialogPdf;
    }

    public String getHashOppfoelgingsdialogPDF() {
        if (hashOppfoelgingsdialogPDF == null) {
            hashOppfoelgingsdialogPDF = sha512AsBase64String(oppfoelgingsdialogPdf);
        }
        return hashOppfoelgingsdialogPDF;
    }

    public byte[] getOppfoelgingsdialogPDF() {
        return oppfoelgingsdialogPdf;
    }
}
