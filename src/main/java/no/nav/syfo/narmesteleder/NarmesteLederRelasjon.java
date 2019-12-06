package no.nav.syfo.narmesteleder;

import java.time.LocalDate;
import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class NarmesteLederRelasjon {
    public String aktorId;
    public String orgnummer;
    public String narmesteLederAktorId;
    public String narmesteLederTelefonnummer;
    public String narmesteLederEpost;
    public LocalDate aktivFom;
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
