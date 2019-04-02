package no.nav.syfo.api.selvbetjening.domain;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class RSGjennomfoering {
    public String kanGjennomfoeres;
    public Boolean paaAnnetSted;
    public Boolean medMerTid;
    public Boolean medHjelp;
    public String kanBeskrivelse;
    public String kanIkkeBeskrivelse;
}
