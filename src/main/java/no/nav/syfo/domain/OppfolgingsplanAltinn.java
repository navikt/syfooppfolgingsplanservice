package no.nav.syfo.domain;

import static no.nav.syfo.util.DigestUtil.sha512AsBase64String;

public class OppfolgingsplanAltinn {

    public Oppfolgingsplan oppfolgingsplan;
    private byte[] oppfoelgingsdialogPdf;
    private String hashOppfoelgingsdialogPDF;

    public OppfolgingsplanAltinn(Oppfolgingsplan oppfolgingsplan, byte[] oppfoelgingsdialogPdf) {
        this.oppfolgingsplan = oppfolgingsplan;
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
