package no.nav.syfo.mappers.persistency;

import no.nav.syfo.domain.AsynkOppgave;
import no.nav.syfo.repository.domain.PAsynkoppgave;

import java.util.function.Function;

public class PAsynkoppgaveMapper {

    public static Function<PAsynkoppgave, AsynkOppgave> p2asynkoppgave = pAsynkoppgave ->
            new AsynkOppgave()
                    .id(pAsynkoppgave.id)
                    .opprettetTidspunkt(pAsynkoppgave.opprettetTidspunkt)
                    .oppgavetype(pAsynkoppgave.oppgavetype)
                    .avhengigAv(pAsynkoppgave.avhengigAv)
                    .antallForsoek(pAsynkoppgave.antallForsoek)
                    .ressursId(pAsynkoppgave.ressursId);
}
