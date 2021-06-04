package no.nav.syfo.oppgave

import no.nav.syfo.aktorregister.AktorregisterConsumer
import no.nav.syfo.api.selvbetjening.domain.BrukerkontekstConstant
import no.nav.syfo.domain.*
import no.nav.syfo.model.Ansatt
import no.nav.syfo.narmesteleder.NarmesteLederConsumer
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

@RunWith(MockitoJUnitRunner::class)
class OppfolgingsplanServiceTest {
    @Mock
    private lateinit var oppfolgingsplanDAO: OppfolgingsplanDAO

    @Mock
    private lateinit var narmesteLederConsumer: NarmesteLederConsumer

    @Mock
    private lateinit var tilgangskontrollService: TilgangskontrollService

    @Mock
    private lateinit var aktorregisterConsumer: AktorregisterConsumer

    @Mock
    private lateinit var godkjentplanDAO: GodkjentplanDAO

    @Mock
    private lateinit var brukerprofilService: BrukerprofilService

    @InjectMocks
    private lateinit var oppfolgingsplanService: OppfolgingsplanService

    @Test
    fun oppfolgingsplanerFraAndreBedrifterBlirFiltrertBort() {
        val dialog1 = Oppfolgingsplan().id(1L).arbeidstaker(Person().aktoerId("sykmeldt")).virksomhet(Virksomhet().virksomhetsnummer("1"))
        val dialog2 = Oppfolgingsplan().id(2L).arbeidstaker(Person().aktoerId("sykmeldt")).virksomhet(Virksomhet().virksomhetsnummer("2"))
        Mockito.`when`(aktorregisterConsumer.hentAktorIdForFnr("123")).thenReturn(LEDER_FNR)
        Mockito.`when`(aktorregisterConsumer.hentAktorIdForFnr(ARBEIDSTAKER_FNR)).thenReturn("sykmeldt")
        Mockito.`when`(narmesteLederConsumer.ansatte(ArgumentMatchers.anyString())).thenReturn(Arrays.asList(Ansatt().fnr(ARBEIDSTAKER_FNR).virksomhetsnummer("1")))
        Mockito.`when`(oppfolgingsplanDAO.oppfolgingsplanerKnyttetTilSykmeldt(ArgumentMatchers.anyString())).thenReturn(Arrays.asList(
            dialog1,
            dialog2
        ))
        Mockito.`when`(oppfolgingsplanDAO.populate(dialog1)).thenReturn(dialog1)
        val dialoger = oppfolgingsplanService.hentAktorsOppfolgingsplaner(BrukerkontekstConstant.ARBEIDSGIVER, "123")
        assertThat(dialoger.size).isEqualTo(1)
        assertThat(dialoger[0].id).isEqualTo(1L)
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
