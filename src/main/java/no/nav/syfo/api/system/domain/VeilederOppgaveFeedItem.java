package no.nav.syfo.api.system.domain;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(fluent = true)
public class VeilederOppgaveFeedItem {
    public String uuid;
    public String type;
    public String tildeltIdent;
    public String tildeltEnhet;
    public String lenke;
    public String fnr;
    public String virksomhetsnummer;
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    public LocalDateTime created;
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    public LocalDateTime sistEndret;
    public String sistEndretAv;
    public String status;


    public enum FeedHendelseType {
        SE_OPPFOLGINGSPLAN,
    }
}

