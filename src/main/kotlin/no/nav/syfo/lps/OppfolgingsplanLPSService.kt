package no.nav.syfo.lps

import no.nav.helse.op2016.Skjemainnhold
import no.nav.syfo.domain.Fodselsnummer
import no.nav.syfo.domain.Virksomhetsnummer
import no.nav.syfo.lps.database.OppfolgingsplanLPSDAO
import org.springframework.stereotype.Repository
import javax.inject.Inject

@Repository
class OppfolgingsplanLPSService @Inject constructor(
    private val oppfolgingsplanLPSDAO: OppfolgingsplanLPSDAO
) {
    fun receivePlan(
        batch: String,
        skjemainnhold: Skjemainnhold,
        virksomhetsnummer: Virksomhetsnummer
    ) {
        savePlan(
            batch,
            skjemainnhold,
            virksomhetsnummer
        )
    }

    private fun savePlan(
        batch: String,
        skjemainnhold: Skjemainnhold,
        virksomhetsnummer: Virksomhetsnummer
    ) {
        oppfolgingsplanLPSDAO.create(
            arbeidstakerFnr = Fodselsnummer(skjemainnhold.sykmeldtArbeidstaker.fnr),
            virksomhetsnummer = virksomhetsnummer.value,
            xml = batch,
            delt_med_nav = skjemainnhold.mottaksInformasjon.isOppfolgingsplanSendesTiNav,
            del_med_fastlege = skjemainnhold.mottaksInformasjon.isOppfolgingsplanSendesTilFastlege,
            delt_med_fastlege = false
        )
    }
}
