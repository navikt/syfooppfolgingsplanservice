package no.nav.syfo.domain;

import static no.nav.syfo.util.DigestUtil.sha512AsBase64String;

public class OppfoelgingsdialogAltinn {

    public Oppfoelgingsdialog oppfoelgingsdialog;
    private byte[] oppfoelgingsdialogPdf;
    private String hashOppfoelgingsdialogPDF;

    public OppfoelgingsdialogAltinn(Oppfoelgingsdialog oppfoelgingsdialog, byte[] oppfoelgingsdialogPdf) {
        this.oppfoelgingsdialog = oppfoelgingsdialog;
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