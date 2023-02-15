package no.nav.syfo.repository.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(fluent = true)
public class POppfoelgingsdialog {
    public Long id;
    public String uuid;
    public String opprettetAv;
    public String aktoerId;
    public LocalDateTime created;
    public Boolean samtykkeArbeidsgiver;
    public Boolean samtykkeSykmeldt;
    public String virksomhetsnummer;
    public String sistEndretAv;
    public LocalDateTime sisteInnloggingArbeidsgiver;
    public LocalDateTime sisteInnloggingSykmeldt;
    public LocalDateTime sistAksessertArbeidsgiver;
    public LocalDateTime sistAksessertSykmeldt;
    public LocalDateTime sistEndretArbeidsgiver;
    public LocalDateTime sistEndretSykmeldt;
    public LocalDateTime sistEndret;

    public String smFnr;
    public String opprettetAvFnr;
    public String sistEndretAvFnr;
}
