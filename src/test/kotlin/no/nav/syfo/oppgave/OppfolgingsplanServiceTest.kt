package no.nav.syfo.oppgave

import no.nav.syfo.domain.*
import no.nav.syfo.pdl.PdlConsumer
import no.nav.syfo.repository.dao.GodkjentplanDAO
import no.nav.syfo.repository.dao.OppfolgingsplanDAO
import no.nav.syfo.service.*
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_FNR
import no.nav.syfo.testhelper.UserConstants.LEDER_FNR
import no.nav.syfo.util.OppfoelgingsdialogUtil
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.*
import org.mockito.junit.MockitoJUnitRunner
import java.time.LocalDateTime
import java.util.*
import jakarta.ws.rs.ForbiddenException

@RunWith(MockitoJUnitRunner::class)
class OppfolgingsplanServiceTest {
    @Mock
    private lateinit var oppfolgingsplanDAO: OppfolgingsplanDAO

    @Mock
    private lateinit var tilgangskontrollService: TilgangskontrollService

    @Mock
    private lateinit var pdlConsumer: PdlConsumer

    @Mock
    private lateinit var godkjentplanDAO: GodkjentplanDAO

    @Mock
    private lateinit var brukerprofilService: BrukerprofilService

    @InjectMocks
    private lateinit var oppfolgingsplanService: OppfolgingsplanService

    @Test
    fun arbeidsgiversOppfolgingsplanerOk() {
        val dialog1 = Oppfolgingsplan().id(1L).arbeidstaker(Person().aktoerId("sykmeldt")).virksomhet(Virksomhet().virksomhetsnummer("1"))
        Mockito.`when`(pdlConsumer.aktorid(LEDER_FNR)).thenReturn("123")
        Mockito.`when`(pdlConsumer.aktorid(ARBEIDSTAKER_FNR)).thenReturn("sykmeldt")
        Mockito.`when`(tilgangskontrollService.erNaermesteLederForSykmeldt(LEDER_FNR, ARBEIDSTAKER_FNR, "1")).thenReturn(true)
        Mockito.`when`(oppfolgingsplanDAO.oppfolgingsplanerKnyttetTilSykmeldtogVirksomhet(ArgumentMatchers.same("sykmeldt"), ArgumentMatchers.same("1"))).thenReturn(Arrays.asList(
            dialog1,
        ))
        Mockito.`when`(oppfolgingsplanDAO.populate(dialog1)).thenReturn(dialog1)
        val dialoger = oppfolgingsplanService.arbeidsgiversOppfolgingsplanerPaFnr( LEDER_FNR, ARBEIDSTAKER_FNR, "1")
        assertThat(dialoger.size).isEqualTo(1)
        assertThat(dialoger[0].id).isEqualTo(1L)
    }

    @Test(expected=ForbiddenException::class)
    fun kanIkkeSporrePaEnAnsattManIkkeErNarmesteLederFor() {
        Mockito.`when`(pdlConsumer.aktorid(LEDER_FNR)).thenReturn("123")
        Mockito.`when`(pdlConsumer.aktorid(ARBEIDSTAKER_FNR)).thenReturn("sykmeldt")
        Mockito.`when`(tilgangskontrollService.erNaermesteLederForSykmeldt(LEDER_FNR, ARBEIDSTAKER_FNR, "1")).thenReturn(false)

        oppfolgingsplanService.arbeidsgiversOppfolgingsplanerPaFnr( LEDER_FNR, ARBEIDSTAKER_FNR, "1")
    }

    @Test
    fun fjernerDenEneGodkjenningen() {
        val godkjenninger = OppfoelgingsdialogUtil.fjernEldsteGodkjenning(listOf(
            Godkjenning().godkjenningsTidspunkt(LocalDateTime.now())
        ))
        assertThat(godkjenninger.size).isEqualTo(0)
    }

    @Test
    fun fjernerEldsteGodkjenning() {
        val godkjenninger = OppfoelgingsdialogUtil.fjernEldsteGodkjenning(listOf(
            Godkjenning().godkjentAvAktoerId("1").godkjenningsTidspunkt(LocalDateTime.now()),
            Godkjenning().godkjentAvAktoerId("2").godkjenningsTidspunkt(LocalDateTime.now().minusDays(1))
        ))
        assertThat(godkjenninger.size).isEqualTo(1)
        assertThat(godkjenninger[0].godkjentAvAktoerId).isEqualTo("1")
    }
}
