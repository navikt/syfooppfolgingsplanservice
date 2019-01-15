package no.nav.syfo.domain.rs;

public class RSOppfoelgingsplan {
    private String sykmeldtFnr;
    private byte[] oppfolgingsplanPdf;

    public RSOppfoelgingsplan(String sykmeldtFnr, byte[] oppfolgingsplanPdf) {
        this.sykmeldtFnr = sykmeldtFnr;
        this.oppfolgingsplanPdf = oppfolgingsplanPdf;
    }

    public String getSykmeldtFnr() {
        return sykmeldtFnr;
    }

    public byte[] getOppfolgingsplanPdf() {
        return oppfolgingsplanPdf;
    }
}
