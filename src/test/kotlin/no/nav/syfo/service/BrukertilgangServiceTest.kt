package no.nav.syfo.service

import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.syfo.LocalApplication
import no.nav.syfo.brukertilgang.BrukertilgangConsumer
import no.nav.syfo.oidc.OIDCIssuer.EKSTERN
import no.nav.syfo.oidc.OIDCUtil.getIssuerToken
import no.nav.syfo.testhelper.OidcTestHelper.loggInnBruker
import org.assertj.core.api.Assertions
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit4.SpringRunner
import javax.inject.Inject

@RunWith(SpringRunner::class)
@SpringBootTest(classes = [LocalApplication::class])
@DirtiesContext
class BrukertilgangServiceTest {
    @MockBean
    private lateinit var brukertilgangConsumer: BrukertilgangConsumer

    @Inject
    lateinit var contextHolder: TokenValidationContextHolder

    @Inject
    private lateinit var brukertilgangService: BrukertilgangService

    @Test
    fun sporOmNoenAndreEnnSegSelvGirFalseNaarManSporOmSegSelv() {
        loggInnBruker(contextHolder, INNLOGGET_FNR)
        val tilgang = brukertilgangService.tilgangTilOppslattIdent(INNLOGGET_FNR, INNLOGGET_FNR)
        Assertions.assertThat(tilgang).isTrue()
    }

    @Test
    fun sporOmNoenAndreEnnSegSelvGirFalseNaarManSporOmEnAnsatt() {
        loggInnBruker(contextHolder, INNLOGGET_FNR)
        Mockito.`when`(brukertilgangConsumer.hasAccessToAnsatt(SPOR_OM_FNR, getIssuerToken(contextHolder, EKSTERN))).thenReturn(true)
        val tilgang = brukertilgangService.tilgangTilOppslattIdent(INNLOGGET_FNR, SPOR_OM_FNR)
        Assertions.assertThat(tilgang).isTrue()
    }

    @Test
    fun sporOmNoenAndreEnnSegSelvGirTrueNaarManSporOmEnSomIkkeErSegSelvOgIkkeAnsatt() {
        loggInnBruker(contextHolder, INNLOGGET_FNR)
        Mockito.`when`(brukertilgangConsumer.hasAccessToAnsatt(SPOR_OM_FNR, getIssuerToken(contextHolder, EKSTERN))).thenReturn(false)
        val tilgang = brukertilgangService.tilgangTilOppslattIdent(INNLOGGET_FNR, SPOR_OM_FNR)
        Assertions.assertThat(tilgang).isFalse()
    }

    companion object {
        private const val INNLOGGET_FNR = "12345678901"
        private const val SPOR_OM_FNR = "12345678902"
    }
}
