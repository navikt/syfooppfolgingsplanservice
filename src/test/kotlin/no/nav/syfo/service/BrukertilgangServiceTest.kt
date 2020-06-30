package no.nav.syfo.service

import no.nav.syfo.brukertilgang.BrukertilgangConsumer
import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.*
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class BrukertilgangServiceTest {
    @Mock
    private lateinit var brukertilgangConsumer: BrukertilgangConsumer

    @InjectMocks
    private lateinit var brukertilgangService: BrukertilgangService

    @Before
    fun setup() {
    }

    @Test
    fun sporOmNoenAndreEnnSegSelvGirFalseNaarManSporOmSegSelv() {
        val tilgang = brukertilgangService.tilgangTilOppslattIdent(INNLOGGET_FNR, INNLOGGET_FNR)
        Assertions.assertThat(tilgang).isTrue()
    }

    @Test
    fun sporOmNoenAndreEnnSegSelvGirFalseNaarManSporOmEnAnsatt() {
        Mockito.`when`(brukertilgangConsumer.hasAccessToAnsatt(SPOR_OM_FNR)).thenReturn(true)
        val tilgang = brukertilgangService.tilgangTilOppslattIdent(INNLOGGET_FNR, SPOR_OM_FNR)
        Assertions.assertThat(tilgang).isTrue()
    }

    @Test
    fun sporOmNoenAndreEnnSegSelvGirTrueNaarManSporOmEnSomIkkeErSegSelvOgIkkeAnsatt() {
        Mockito.`when`(brukertilgangConsumer.hasAccessToAnsatt(SPOR_OM_FNR)).thenReturn(false)
        val tilgang = brukertilgangService.tilgangTilOppslattIdent(INNLOGGET_FNR, SPOR_OM_FNR)
        Assertions.assertThat(tilgang).isFalse()
    }

    companion object {
        private const val INNLOGGET_FNR = "12345678901"
        private const val INNLOGGET_AKTOERID = "1234567890123"
        private const val SPOR_OM_FNR = "12345678902"
        private const val SPOR_OM_AKTOERID = "1234567890122"
    }
}
