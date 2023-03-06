package no.nav.syfo.narmesteleder;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class NarmesteLederRelasjon {
    public String aktorId;
    public String fnr;
    public String orgnummer;
    public String narmesteLederId;
    public String narmesteLederFnr;
    public String narmesteLederTelefonnummer;
    public String narmesteLederEpost;
    public LocalDate aktivFom;
    public LocalDate aktivTom;
    public boolean arbeidsgiverForskutterer;
    public boolean skrivetilgang;
    public List<Tilgang> tilganger;

    public enum Tilgang {
        SYKMELDING,
        SYKEPENGESOKNAD,
        MOTE,
        OPPFOLGINGSPLAN,
    }
}
