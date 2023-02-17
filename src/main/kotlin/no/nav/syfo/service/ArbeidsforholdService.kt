package no.nav.syfo.service

import no.nav.syfo.aareg.AaregConsumer
import no.nav.syfo.aareg.Arbeidsforhold
import no.nav.syfo.aareg.OpplysningspliktigArbeidsgiver
import no.nav.syfo.aareg.OpplysningspliktigArbeidsgiver.Type.Organisasjon
import no.nav.syfo.fellesKodeverk.FellesKodeverkConsumer
import no.nav.syfo.fellesKodeverk.KodeverkKoderBetydningerResponse
import no.nav.syfo.model.Stilling
import no.nav.syfo.pdl.PdlConsumer
import no.nav.syfo.util.lowerCapitalize
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate

@Service
class ArbeidsforholdService(private val aaregConsumer: AaregConsumer, private val fellesKodeverkConsumer: FellesKodeverkConsumer, private val pdlConsumer: PdlConsumer) {

    private val log = LoggerFactory.getLogger(ArbeidsforholdService::class.java)

    fun arbeidstakersStillingerForOrgnummer(aktorId: String, fom: LocalDate, orgnummer: String): List<Stilling> {
        val fnr: String = pdlConsumer.fnr(aktorId)
        return aaregConsumer.arbeidsforholdArbeidstaker(fnr)
            .toStillingForOrgnummer(orgnummer, fom)
    }

    fun arbeidstakersFnrStillingerForOrgnummer(fnr: String, fom: LocalDate, orgnummer: String): List<Stilling> {
        return aaregConsumer.arbeidsforholdArbeidstaker(fnr)
            .toStillingForOrgnummer(orgnummer, fom)
    }

    fun arbeidsforhold(fnr: String): Map<String, List<no.nav.syfo.model.Arbeidsforhold>> {
        return aaregConsumer.arbeidsforholdArbeidstaker(fnr)
            .filter { arbeidsforhold -> arbeidsforhold.hasType(Organisasjon) }
            .map { arbeidsforhold ->
                no.nav.syfo.model.Arbeidsforhold().apply {
                    orgnummer = arbeidsforhold.arbeidsgiver.organisasjonsnummer
                    fom = LocalDate.parse(arbeidsforhold.ansettelsesperiode.periode.fom)
                    tom = LocalDate.parse(arbeidsforhold.ansettelsesperiode.periode.tom)
                    stillinger = arbeidsforhold.arbeidsavtaler.map {
                        Stilling().apply {
                            yrke = it.yrke.toStillingsnavn()
                            prosent = it.stillingsprosent.withMaxScale()
                        }
                    }
                }
            }.groupBy { arbeidsforhold -> arbeidsforhold.orgnummer }
    }

    private fun List<Arbeidsforhold>.toStillingForOrgnummer(orgnummer: String, fom: LocalDate): List<Stilling> {
        return filter { arbeidsforhold -> arbeidsforhold.hasType(Organisasjon) }
            .filter { arbeidsforhold -> arbeidsforhold.hasOrgnummer(orgnummer) }
            .filter { arbeidsforhold -> arbeidsforhold.validOn(fom) }
            .flatMap { arbeidsforhold -> arbeidsforhold.arbeidsavtaler }
            .map { arbeidsavtale ->
                Stilling().apply {
                    yrke = arbeidsavtale.yrke.toStillingsnavn()
                    prosent = arbeidsavtale.stillingsprosent.withMaxScale()
                }
            }
    }

    private fun String.tilLocalDate(): LocalDate {
        return LocalDate.parse(this)
    }

    private fun Arbeidsforhold.hasType(type: OpplysningspliktigArbeidsgiver.Type): Boolean {
        return arbeidsgiver.type.equals(type)
    }

    private fun Arbeidsforhold.hasOrgnummer(orgnummer: String): Boolean {
        return arbeidsgiver.organisasjonsnummer.equals(orgnummer)
    }

    private fun Arbeidsforhold.validOn(fom: LocalDate): Boolean {
        return ansettelsesperiode.periode.tom == null || !ansettelsesperiode.periode.tom.tilLocalDate().isBefore(fom)
    }

    private fun String.toStillingsnavn(): String {
        val kodeverkBetydninger: KodeverkKoderBetydningerResponse = fellesKodeverkConsumer.kodeverkKoderBetydninger()
        val stillingsnavnFraFellesKodeverk = kodeverkBetydninger.betydninger[this]?.get(0)?.beskrivelser?.get("nb")?.tekst
        if (stillingsnavnFraFellesKodeverk == null) {
            log.error("Couldn't find navn for stillingskode: $this")
        }
        val stillingsnavn = stillingsnavnFraFellesKodeverk ?: "Ugyldig yrkeskode $this"
        return stillingsnavn.lowerCapitalize()
    }

}

fun Double.withMaxScale(): BigDecimal {
    val percentAsBigDecimal = BigDecimal(this)
    return if (percentAsBigDecimal.scale() > 1) {
        percentAsBigDecimal.setScale(1, RoundingMode.HALF_UP)
    } else percentAsBigDecimal
}

