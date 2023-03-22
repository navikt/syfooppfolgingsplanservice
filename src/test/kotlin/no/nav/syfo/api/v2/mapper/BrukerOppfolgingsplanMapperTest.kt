package no.nav.syfo.api.v2.mapper


import no.nav.syfo.api.v2.domain.Virksomhet
import no.nav.syfo.api.v2.domain.oppfolgingsplan.*
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_FNR
import no.nav.syfo.testhelper.UserConstants.VIRKSOMHETSNUMMER
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class BrukerOppfolgingsplanMapperTest {

    private val VIRKSOMHETSNUMMER2 = "123456780"

    private val oppfolgingsplan = BrukerOppfolgingsplan(
        1L, LocalDateTime.now(), LocalDate.now(), Status.AKTIV, Virksomhet(VIRKSOMHETSNUMMER), arbeidsgiver = Arbeidsgiver(
            NarmesteLeder(VIRKSOMHETSNUMMER)
        ), arbeidstaker = Person(fnr = ARBEIDSTAKER_FNR), sistEndretAv = Person(fnr = ARBEIDSTAKER_FNR)
    )

    private val oppfolgingsplan2 = BrukerOppfolgingsplan(
        2L, LocalDateTime.now(), LocalDate.now(), Status.AKTIV, Virksomhet(VIRKSOMHETSNUMMER2), arbeidsgiver = Arbeidsgiver(
            NarmesteLeder(VIRKSOMHETSNUMMER2)
        ), arbeidstaker = Person(fnr = ARBEIDSTAKER_FNR), sistEndretAv = Person(fnr = ARBEIDSTAKER_FNR)
    )

    @Test
    fun toVirksomhetsnummerDontReturnDuplicates() {
        val list = listOf(oppfolgingsplan, oppfolgingsplan2, oppfolgingsplan).toVirksomhetsnummer()
        assertThat(list.size).isEqualTo(2)
        assertThat(list).contains(VIRKSOMHETSNUMMER, VIRKSOMHETSNUMMER2)
    }
}
