package no.nav.syfo.service

import no.nav.syfo.aareg.AaregConsumer
import no.nav.syfo.aareg.AaregUtils.stillingsprosentWithMaxScale
import no.nav.syfo.aareg.OpplysningspliktigArbeidsgiver
import no.nav.syfo.fellesKodeverk.FellesKodeverkConsumer
import no.nav.syfo.model.Stilling
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class ArbeidsforholdService(private val aaregConsumer: AaregConsumer, private val fellesKodeverkConsumer: FellesKodeverkConsumer) {

    fun arbeidstakersStillingerForOrgnummer(fnr: String, fom: LocalDate, orgnummer: String): List<Stilling> {
        return aaregConsumer.arbeidsforholdArbeidstaker(fnr)
            .filter { arbeidsforhold -> arbeidsforhold.arbeidsgiver.type.equals(OpplysningspliktigArbeidsgiver.Type.Organisasjon) }
            .filter { arbeidsforhold -> arbeidsforhold.arbeidsgiver.organisasjonsnummer.equals(orgnummer) }
            .filter { arbeidsforhold ->
                arbeidsforhold.ansettelsesperiode.periode.tom == null || !arbeidsforhold.ansettelsesperiode.periode.tom.tilLocalDate().isBefore(fom)
            }
            .flatMap { arbeidsforhold ->
                arbeidsforhold.arbeidsavtaler()
            }
            .map { arbeidsavtale ->
                Stilling()
                    .yrke(fellesKodeverkConsumer.stillingsnavnFromKode(arbeidsavtale.yrke))
                    .prosent(stillingsprosentWithMaxScale(arbeidsavtale.stillingsprosent))
            }
    }

}

private fun String.tilLocalDate(): LocalDate {
    return LocalDate.parse(this)
}