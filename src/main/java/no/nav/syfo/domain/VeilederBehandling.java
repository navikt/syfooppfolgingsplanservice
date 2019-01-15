package no.nav.syfo.domain;

import lombok.Data;
import lombok.experimental.Accessors;
import no.nav.syfo.repository.domain.VeilederBehandlingStatus;

import java.time.LocalDateTime;

@Data
@Accessors(fluent = true)
public class VeilederBehandling {
    public Long oppgaveId;
    public String oppgaveUUID;
    public Long godkjentplanId;
    public String tildeltIdent;
    public String tildeltEnhet;
    public LocalDateTime opprettetDato;
    public LocalDateTime sistEndret;
    public String sistEndretAv;
    public VeilederBehandlingStatus status;
}
