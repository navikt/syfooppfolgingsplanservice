package no.nav.syfo.api

import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.syfo.LocalApplication
import no.nav.syfo.oidc.OIDCIssuer
import no.nav.syfo.testhelper.OidcTestHelper.loggUtAlle
import no.nav.syfo.veiledertilgang.VeilederTilgangConsumer
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.*
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.client.ExpectedCount
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers
import org.springframework.test.web.client.response.MockRestResponseCreators
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import javax.inject.Inject

/**
 * Hensikten her er å samle koden som mock svar fra syfo-tilgangskontroll.
 * Subklasser arver tilgangskontrollResponse, som de kan sette opp til å returnere 200 OK, 403 Forbidden eller
 * 500-feil.
 */
@RunWith(SpringRunner::class)
@SpringBootTest(classes = [LocalApplication::class])
@DirtiesContext
abstract class AbstractRessursTilgangTest {

    @Value("\${azure.openid.config.token.endpoint}")
    lateinit var azureTokenEndpoint: String

    @Value("\${tilgangskontrollapi.url}")
    lateinit var tilgangskontrollUrl: String

    @Value("\${dev}")
    private lateinit var dev: String

    @Inject
    lateinit var contextHolder: TokenValidationContextHolder

    @Inject
    private lateinit var restTemplate: RestTemplate
    lateinit var mockRestServiceServer: MockRestServiceServer

    @Inject
    @Qualifier("restTemplateMedProxy")
    private lateinit var restTemplateWithProxy: RestTemplate
    lateinit var mockRestServiceWithProxyServer: MockRestServiceServer

    @Before
    fun setUp() {
        mockRestServiceServer = MockRestServiceServer.bindTo(restTemplate).build()
        mockRestServiceWithProxyServer = MockRestServiceServer.bindTo(restTemplateWithProxy).build()
    }

    @After
    open fun tearDown() {
        mockRestServiceServer.verify()
        mockRestServiceWithProxyServer.verify()
        loggUtAlle(contextHolder)
    }

    fun mockSvarFraTilgangTilBrukerViaAzure(fnr: String, status: HttpStatus) {
        val uriString = UriComponentsBuilder.fromHttpUrl(tilgangskontrollUrl)
            .path(VeilederTilgangConsumer.TILGANG_TIL_BRUKER_VIA_AZURE_PATH)
            .queryParam(VeilederTilgangConsumer.FNR, fnr)
            .toUriString()
        val idToken = contextHolder.tokenValidationContext.getJwtToken(OIDCIssuer.AZURE).tokenAsString
        mockRestServiceServer.expect(ExpectedCount.manyTimes(), MockRestRequestMatchers.requestTo(uriString))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
            .andExpect(MockRestRequestMatchers.header(HttpHeaders.AUTHORIZATION, "Bearer $idToken"))
            .andRespond(MockRestResponseCreators.withStatus(status))
    }

    fun mockSvarFraTilgangTilTjenestenViaAzure(status: HttpStatus?) {
        val uriString = UriComponentsBuilder.fromHttpUrl(tilgangskontrollUrl)
            .path(VeilederTilgangConsumer.TILGANG_TIL_TJENESTEN_VIA_AZURE_PATH)
            .toUriString()
        val idToken = contextHolder.tokenValidationContext.getJwtToken(OIDCIssuer.AZURE).tokenAsString
        mockRestServiceServer.expect(ExpectedCount.manyTimes(), MockRestRequestMatchers.requestTo(uriString))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
            .andExpect(MockRestRequestMatchers.header(HttpHeaders.AUTHORIZATION, "Bearer $idToken"))
            .andRespond(MockRestResponseCreators.withStatus(status))
    }
}
