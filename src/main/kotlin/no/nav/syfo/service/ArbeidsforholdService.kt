package no.nav.syfo.service

import no.nav.syfo.aareg.AaregConsumer
import no.nav.syfo.aareg.AaregUtils.stillingsprosentWithMaxScale
import no.nav.syfo.aareg.Arbeidsforhold
import no.nav.syfo.aareg.OpplysningspliktigArbeidsgiver
import no.nav.syfo.fellesKodeverk.FellesKodeverkConsumer
import no.nav.syfo.model.Stilling
import no.nav.syfo.pdl.PdlConsumer
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class ArbeidsforholdService(private val aaregConsumer: AaregConsumer, private val fellesKodeverkConsumer: FellesKodeverkConsumer, private val pdlConsumer: PdlConsumer) {

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
                    yrke = fellesKodeverkConsumer.stillingsnavnFromKode(arbeidsavtale.yrke)
                    prosent = stillingsprosentWithMaxScale(arbeidsavtale.stillingsprosent)
                }
            }
    }
}

private fun String.tilLocalDate(): LocalDate {
    return LocalDate.parse(this)
}