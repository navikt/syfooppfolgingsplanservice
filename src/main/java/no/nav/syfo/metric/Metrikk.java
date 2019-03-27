package no.nav.syfo.metric;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import no.nav.syfo.domain.AsynkOppgave;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static java.time.LocalDate.now;
import static java.time.temporal.ChronoUnit.SECONDS;
import static no.nav.syfo.util.DatoUtil.dagerMellom;

@Controller
public class Metrikk {

    private final MeterRegistry registry;

    @Inject
    public Metrikk(MeterRegistry registry) {
        this.registry = registry;
    }

    public void tellHendelse(String navn) {
        registry.counter(
                addPrefix(navn),
                Tags.of("type", "info")
        ).increment();
    }

    public void tellHendelseMedAntall(String navn, long antall) {
        registry.counter(
                addPrefix(navn),
                Tags.of("type", "info")
        ).increment(antall);
    }

    public void tellAsynkOppgave(AsynkOppgave asynkOppgave, boolean success) {
        registry.counter(
                addPrefix("asynkOppgave"),
                Tags.of(
                        "type", "info",
                        "oppgavetype", asynkOppgave.oppgavetype,
                        "success", String.valueOf(success)
                )
        ).increment();

        registry.counter(
                addPrefix("antallForsok"),
                Tags.of("type", "info")
        ).increment(asynkOppgave.antallForsoek);

        registry.counter(
                addPrefix("antallSekunderPaKo"),
                Tags.of("type", "info")
        ).increment(SECONDS.between(asynkOppgave.opprettetTidspunkt(), LocalDateTime.now()));
    }

    public void tellAntallDagerSiden(LocalDateTime tidspunkt, String navn) {
        int dagerIMellom = dagerMellom(tidspunkt.toLocalDate(), now());

        registry.counter(
                addPrefix(navn),
                Tags.of("type", "info")
        ).increment(dagerIMellom);
    }

    public void tellAntallDagerMellom(String navn, LocalDate tidspunktFom, LocalDate tidspunktTom) {
        int dagerIMellom = dagerMellom(tidspunktFom, tidspunktTom);

        registry.counter(
                addPrefix(navn),
                Tags.of("type", "info")
        ).increment(dagerIMellom);
    }

    public void tellOPSamtykke(boolean harGittSamtykke) {
        String navn = "samtykke";
        registry.counter(
                navn,
                Tags.of(
                        "type", "info",
                        "harGittSamtykke", harGittSamtykke ? "ja" : "nei"
                )
        ).increment();
    }

    public void tellHttpKall(int kode) {
        registry.counter(
                addPrefix("httpstatus"),
                Tags.of(
                        "type", "info",
                        "kode", String.valueOf(kode)
                )
        ).increment();
    }

    private String addPrefix(String navn) {
        String METRIKK_PREFIX = "syfooppfolgingsplanservice_";
        return METRIKK_PREFIX + navn;
    }
}
