package no.nav.syfo.metric

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tags
import no.nav.syfo.domain.AsynkOppgave
import no.nav.syfo.util.DatoUtil
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@Component
class Metrikk @Inject constructor(
        private val registry: MeterRegistry
) {
    fun countOutgoingReponses(navn: String, statusCode: Int) {
        registry.counter(
                addPrefix(navn),
                Tags.of(
                        "type", "info",
                        "status", statusCode.toString()
                )
        ).increment()
    }

    fun tellHendelse(navn: String) {
        registry.counter(
                addPrefix(navn),
                Tags.of("type", "info")
        ).increment()
    }

    fun tellHendelseMedTag(navn: String, tagKey: String, tagValue: Any) {
        registry.counter(
                addPrefix(navn),
                Tags.of(
                        "type", "info",
                        tagKey, tagValue.toString()
                )
        ).increment()
    }

    fun tellHendelseMedAntall(navn: String, antall: Long) {
        registry.counter(
                addPrefix(navn),
                Tags.of("type", "info")
        ).increment(antall.toDouble())
    }

    fun tellAsynkOppgave(asynkOppgave: AsynkOppgave, success: Boolean) {
        registry.counter(
                addPrefix("asynkOppgave"),
                Tags.of(
                        "type", "info",
                        "oppgavetype", asynkOppgave.oppgavetype,
                        "success", success.toString())
        ).increment()
        registry.counter(
                addPrefix("antallForsok"),
                Tags.of("type", "info")
        ).increment(asynkOppgave.antallForsoek.toDouble())
        registry.counter(
                addPrefix("antallSekunderPaKo"),
                Tags.of("type", "info")
        ).increment(ChronoUnit.SECONDS.between(asynkOppgave.opprettetTidspunkt, LocalDateTime.now()).toDouble())
    }

    fun tellAntallDagerSiden(tidspunkt: LocalDateTime, navn: String) {
        val dagerIMellom = DatoUtil.dagerMellom(tidspunkt.toLocalDate(), LocalDate.now())
        registry.counter(
                addPrefix(navn),
                Tags.of("type", "info")
        ).increment(dagerIMellom.toDouble())
    }

    fun tellAntallDagerMellom(navn: String, tidspunktFom: LocalDate, tidspunktTom: LocalDate) {
        val dagerIMellom = DatoUtil.dagerMellom(tidspunktFom, tidspunktTom)
        registry.counter(
                addPrefix(navn),
                Tags.of("type", "info")
        ).increment(dagerIMellom.toDouble())
    }

    fun tellOPSamtykke(harGittSamtykke: Boolean) {
        val navn = "samtykke"
        registry.counter(
                navn,
                Tags.of(
                        "type", "info",
                        "harGittSamtykke", if (harGittSamtykke) "ja" else "nei"
                )
        ).increment()
    }

    fun tellHttpKall(kode: Int) {
        registry.counter(
                addPrefix("httpstatus"),
                Tags.of(
                        "type", "info",
                        "kode", kode.toString())
        ).increment()
    }

    private fun addPrefix(navn: String): String {
        val metricPrefix = "syfooppfolgingsplanservice_"
        return metricPrefix + navn
    }
}
