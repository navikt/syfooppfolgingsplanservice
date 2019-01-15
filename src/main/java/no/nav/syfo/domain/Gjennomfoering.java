package no.nav.syfo.domain;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class Gjennomfoering {
    public String gjennomfoeringStatus;
    public Boolean paaAnnetSted;
    public Boolean medMerTid;
    public Boolean medHjelp;
    public String kanBeskrivelse;
    public String kanIkkeBeskrivelse;

    public enum KanGjennomfoeres {
        KAN,
        KAN_IKKE,
        TILRETTELEGGING
    }
}
