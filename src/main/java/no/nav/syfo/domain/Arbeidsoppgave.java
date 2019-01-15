package no.nav.syfo.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(fluent = true)
public class Arbeidsoppgave {
    public Long id;
    public long oppfoelgingsdialogId;
    public String navn;

    public boolean erVurdertAvSykmeldt;
    public Gjennomfoering gjennomfoering = new Gjennomfoering();
    public String sistEndretAvAktoerId;
    public LocalDateTime sistEndretDato;
    public String opprettetAvAktoerId;
    public LocalDateTime opprettetDato;

}
