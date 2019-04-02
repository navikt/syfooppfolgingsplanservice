package no.nav.syfo.api.selvbetjening.mapper;

import no.nav.syfo.api.selvbetjening.domain.RSTiltak;
import no.nav.syfo.domain.Tiltak;

import java.util.function.Function;

public class RSTiltakMapper {

    public static Function<RSTiltak, Tiltak> rs2tiltak = rsTiltak -> new Tiltak()
            .id(rsTiltak.tiltakId)
            .beskrivelse(rsTiltak.beskrivelse)
            .beskrivelseIkkeAktuelt(rsTiltak.beskrivelseIkkeAktuelt)
            .fom(rsTiltak.fom)
            .tom(rsTiltak.tom)
            .navn(rsTiltak.tiltaknavn)
            .gjennomfoering(rsTiltak.gjennomfoering)
            .status(rsTiltak.status);
}
