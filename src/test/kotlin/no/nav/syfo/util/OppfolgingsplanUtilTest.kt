package no.nav.syfo.util

import no.nav.syfo.domain.*
import org.assertj.core.api.Assertions
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import java.time.LocalDateTime

@RunWith(MockitoJUnitRunner::class)
class OppfolgingsplanUtilTest {
    @Test
    fun sisteGodkjenningAvAnnenPartSortererRiktig() {
        val godkjenning = OppfoelgingsdialogUtil.sisteGodkjenningAvAnnenPart(Oppfolgingsplan()
            .arbeidstaker(Person().aktoerId("123"))
            .godkjenninger(listOf(
                Godkjenning()
                    .godkjentAvAktoerId("123")
                    .godkjenningsTidspunkt(LocalDateTime.now().minusHours(2))
                    .beskrivelse("1"),
                Godkjenning()
                    .godkjentAvAktoerId("123")
                    .godkjenningsTidspunkt(LocalDateTime.now().minusHours(3))
                    .beskrivelse("2"),
                Godkjenning()
                    .godkjentAvAktoerId("123")
                    .godkjenningsTidspunkt(LocalDateTime.now().minusHours(1))
                    .beskrivelse("3")
            )), "123")
        Assertions.assertThat(godkjenning.beskrivelse).isEqualTo("3")
    }

    @Test
    fun arbeidsgiverHarGjortEndringerIMellomtiden() {
        val b = OppfoelgingsdialogUtil.annenPartHarGjortEndringerImellomtiden(Oppfolgingsplan()
            .sistEndretArbeidsgiver(LocalDateTime.now().minusSeconds(10))
            .arbeidstaker(Person().sistAksessert(LocalDateTime.now().minusSeconds(20)).aktoerId("aktoerId"))
            , "aktoerId")
        Assertions.assertThat(b).isTrue()
    }

    @Test
    fun sykmeldtHarGjortEndringerIMellomtiden() {
        val b = OppfoelgingsdialogUtil.annenPartHarGjortEndringerImellomtiden(Oppfolgingsplan()
            .sistEndretSykmeldt(LocalDateTime.now().minusSeconds(10))
            .arbeidsgiver(Person().sistAksessert(LocalDateTime.now().minusSeconds(20)))
            .arbeidstaker(Person().aktoerId("aktoerId"))
            , "arbeidsgiveraktoerId")
        Assertions.assertThat(b).isTrue()
    }

    @Test
    fun taalerAtSistAksessertAnnenPartErNull() {
        val b = OppfoelgingsdialogUtil.annenPartHarGjortEndringerImellomtiden(Oppfolgingsplan()
            .sistEndretSykmeldt(null)
            .arbeidsgiver(Person().sistAksessert(LocalDateTime.now().minusSeconds(20)))
            .arbeidstaker(Person().aktoerId("aktoerId"))
            , "arbeidsgiveraktoerId")
        Assertions.assertThat(b).isFalse()
    }

    @Test
    fun tiltakHoererIkkeTilDialogGirFalseDersomIngenID() {
        val b = OppfoelgingsdialogUtil.eksisterendeTiltakHoererTilDialog(null, listOf(
            Tiltak().id(1L),
            Tiltak().id(2L),
            Tiltak().id(3L),
            Tiltak().id(4L)
        ))
        Assertions.assertThat(b).isTrue()
    }

    @Test
    fun tiltakHoererIkkeTilDialogGirTrueDersomIngenMatch() {
        val b = OppfoelgingsdialogUtil.eksisterendeTiltakHoererTilDialog(5L, listOf(
            Tiltak().id(1L),
            Tiltak().id(2L),
            Tiltak().id(3L),
            Tiltak().id(4L)
        ))
        Assertions.assertThat(b).isFalse()
    }

    @Test
    fun tiltakHoererIkkeTilDialogGirFalseDersomMatch() {
        val b = OppfoelgingsdialogUtil.eksisterendeTiltakHoererTilDialog(3L, listOf(
            Tiltak().id(1L),
            Tiltak().id(2L),
            Tiltak().id(3L),
            Tiltak().id(4L)
        ))
        Assertions.assertThat(b).isTrue()
    }
}
