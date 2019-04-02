package no.nav.syfo.api.selvbetjening.mapper;

import no.nav.syfo.api.selvbetjening.domain.RSGjennomfoering;
import no.nav.syfo.domain.Gjennomfoering;

import java.util.function.Function;

import static no.nav.syfo.domain.Gjennomfoering.KanGjennomfoeres.*;

public class RSGjennomfoeringMapper {

    public static Function<RSGjennomfoering, Gjennomfoering> rs2gjennomfoering = rsGjennomfoering -> {
        if (KAN.name().equals(rsGjennomfoering.kanGjennomfoeres)) {
            return new Gjennomfoering().gjennomfoeringStatus(KAN.name());
        } else if (KAN_IKKE.name().equals(rsGjennomfoering.kanGjennomfoeres)) {
            return new Gjennomfoering()
                    .gjennomfoeringStatus(KAN_IKKE.name())
                    .kanIkkeBeskrivelse(rsGjennomfoering.kanIkkeBeskrivelse);
        } else if (TILRETTELEGGING.name().equals(rsGjennomfoering.kanGjennomfoeres)) {
            return new Gjennomfoering()
                    .gjennomfoeringStatus(TILRETTELEGGING.name())
                    .kanBeskrivelse(rsGjennomfoering.kanBeskrivelse)
                    .paaAnnetSted(rsGjennomfoering.paaAnnetSted)
                    .medMerTid(rsGjennomfoering.medMerTid)
                    .medHjelp(rsGjennomfoering.medHjelp);
        }
        return new Gjennomfoering();
    };
}
