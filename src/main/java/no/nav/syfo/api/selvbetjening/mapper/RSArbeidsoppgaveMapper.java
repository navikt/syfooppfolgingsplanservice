package no.nav.syfo.api.selvbetjening.mapper;

import no.nav.syfo.api.selvbetjening.domain.RSArbeidsoppgave;
import no.nav.syfo.domain.Arbeidsoppgave;

import java.util.function.Function;

import static no.nav.syfo.api.selvbetjening.mapper.RSGjennomfoeringMapper.rs2gjennomfoering;
import static no.nav.syfo.util.MapUtil.mapNullable;

public class RSArbeidsoppgaveMapper {

    public static Function<RSArbeidsoppgave, Arbeidsoppgave> rs2arbeidsoppgave = rsArbeidsoppgave ->
            new Arbeidsoppgave()
                    .id(rsArbeidsoppgave.arbeidsoppgaveId)
                    .navn(rsArbeidsoppgave.arbeidsoppgavenavn)
                    .gjennomfoering(mapNullable(rsArbeidsoppgave.gjennomfoering, rs2gjennomfoering));
}
