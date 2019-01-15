package no.nav.syfo.repository.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(fluent = true)
public class PVeilederBehandling {
    Long oppgaveId;
    String oppgaveUUID;
    Long godkjentplanId;
    String tildeltIdent;
    String tildeltEnhet;
    LocalDateTime opprettetDato;
    LocalDateTime sistEndret;
    String sistEndretAv;
    String status;
}
