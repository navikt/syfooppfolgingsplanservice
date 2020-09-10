package no.nav.syfo.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(fluent = true)
public class FeiletSending {
    public Long id;
    public Long oppfolgingsplanId;
    public int number_of_tries;
    public int max_retries;
    public LocalDateTime sistEndretDato;
    public LocalDateTime opprettetDato;
}
