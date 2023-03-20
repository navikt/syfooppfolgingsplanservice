package no.nav.syfo.service

import no.nav.syfo.aareg.AaregConsumer
import no.nav.syfo.aareg.AaregUtils.stillingsprosentWithMaxScale
import no.nav.syfo.aareg.Arbeidsforhold
import no.nav.syfo.aareg.OpplysningspliktigArbeidsgiver
import no.nav.syfo.fellesKodeverk.FellesKodeverkConsumer
import no.nav.syfo.fellesKodeverk.KodeverkKoderBetydningerResponse
import no.nav.syfo.model.Stilling
import no.nav.syfo.pdl.PdlConsumer
import no.nav.syfo.util.lowerCapitalize
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class ArbeidsforholdService(private val aaregConsumer: AaregConsumer, private val fellesKodeverkConsumer: FellesKodeverkConsumer, private val pdlConsumer: PdlConsumer) {

    private val log = LoggerFactory.getLogger(ArbeidsforholdService::class.java)

    fun arbeidstakersStillingerForOrgnummer(aktorId: String, fom: LocalDate, orgnummer: String): List<Stilling> {
        val fnr: String = pdlConsumer.fnr(aktorId)
        val arbeidsforholdList: List<Arbeidsforhold> = aaregConsumer.arbeidsforholdArbeidstaker(fnr)
        return arbeidsforholdList2StillingForOrgnummer(arbeidsforholdList, orgnummer, fom)
    }

    fun arbeidstakersFnrStillingerForOrgnummer(fnr: String, fom: LocalDate, orgnummer: String): List<Stilling> {
        return arbeidsforholdList2StillingForOrgnummer(aaregConsumer.arbeidsforholdArbeidstaker(fnr), orgnummer, fom)
    }

    private fun arbeidsforholdList2StillingForOrgnummer(arbeidsforholdListe: List<Arbeidsforhold>, orgnummer: String, fom: LocalDate): List<Stilling> {
        return arbeidsforholdListe
            .filter { arbeidsforhold -> arbeidsforhold.arbeidsgiver.type.equals(OpplysningspliktigArbeidsgiver.Type.Organisasjon) }
            .filter { arbeidsforhold -> arbeidsforhold.arbeidsgiver.organisasjonsnummer.equals(orgnummer) }
            .filter { arbeidsforhold ->
                arbeidsforhold.ansettelsesperiode.periode.tom == null || !arbeidsforhold.ansettelsesperiode.periode.tom.tilLocalDate().isBefore(fom)
            }
            .flatMap { arbeidsforhold ->
                arbeidsforhold.arbeidsavtaler
            }
            .map { arbeidsavtale ->
                Stilling().apply {
                    yrke = stillingsnavnFromKode(arbeidsavtale.yrke)
                    prosent = stillingsprosentWithMaxScale(arbeidsavtale.stillingsprosent)
                }
            }
    }

    private fun stillingsnavnFromKode(stillingskode: String): String {
        val kodeverkBetydninger: KodeverkKoderBetydningerResponse = fellesKodeverkConsumer.kodeverkKoderBetydninger()
        val stillingsnavnFraFellesKodeverk = kodeverkBetydninger.betydninger[stillingskode]?.get(0)?.beskrivelser?.get("nb")?.tekst
        if (stillingsnavnFraFellesKodeverk == null) {
            log.error("Couldn't find navn for stillingskode: $stillingskode")
        }
        val stillingsnavn = stillingsnavnFraFellesKodeverk ?: "Ugyldig yrkeskode $stillingskode"
        return stillingsnavn.lowerCapitalize()
    }

}

private fun String.tilLocalDate(): LocalDate {
    return LocalDate.parse(this)
}
