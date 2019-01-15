package no.nav.syfo.repository.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(fluent = true)
public class PArbeidsoppgave {
    public Long id;
    public long oppfoelgingsdialogId;
    public String navn;
    public boolean erVurdertAvSykmeldt;
    public String opprettetAvAktoerId;
    public String sistEndretAvAktoerId;
    public LocalDateTime sistEndretDato;
    public LocalDateTime opprettetDato;
    public String gjennomfoeringStatus;
    public Boolean paaAnnetSted;
    public Boolean medMerTid;
    public Boolean medHjelp;
    public String kanBeskrivelse;
    public String kanIkkeBeskrivelse;
}
