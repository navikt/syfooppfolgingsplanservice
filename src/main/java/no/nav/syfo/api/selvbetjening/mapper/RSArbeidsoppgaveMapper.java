package no.nav.syfo.api.selvbetjening.mapper;

import no.nav.syfo.api.selvbetjening.domain.RSArbeidsoppgave;
import no.nav.syfo.domain.Arbeidsoppgave;
import no.nav.syfo.domain.Gjennomfoering;

import java.util.function.Function;

import static java.util.Optional.ofNullable;
import static no.nav.syfo.api.selvbetjening.mapper.RSGjennomfoeringMapper.rs2gjennomfoering;

public class RSArbeidsoppgaveMapper {

    public static Function<RSArbeidsoppgave, Arbeidsoppgave> rs2arbeidsoppgave = rsArbeidsoppgave ->
            new Arbeidsoppgave()
                    .id(rsArbeidsoppgave.arbeidsoppgaveId)
                    .navn(rsArbeidsoppgave.arbeidsoppgavenavn)
                    .gjennomfoering(ofNullable(rsArbeidsoppgave.gjennomfoering).map(rs2gjennomfoering).orElse(new Gjennomfoering()));
}
