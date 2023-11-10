package no.nav.syfo.testhelper

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import no.nav.syfo.util.NAV_PERSONIDENT_HEADER
import no.nav.syfo.veiledertilgang.Tilgang
import no.nav.syfo.veiledertilgang.VeilederTilgangConsumer.Companion.TILGANGSKONTROLL_PERSON_PATH
import no.nav.syfo.veiledertilgang.VeilederTilgangConsumer.Companion.TILGANGSKONTROLL_SYFO_PATH
import org.springframework.http.*
import org.springframework.test.web.client.ExpectedCount
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.ResponseCreator
import org.springframework.test.web.client.match.MockRestRequestMatchers
import org.springframework.test.web.client.response.MockRestResponseCreators
import org.springframework.web.util.UriComponentsBuilder

fun mockSvarFraIstilgangskontrollTilgangTilBruker(
    azureTokenEndpoint: String,
    tilgangskontrollUrl: String,
    mockRestServiceWithProxyServer: MockRestServiceServer,
    mockRestServiceServer: MockRestServiceServer,
    status: HttpStatus,
    fnr: String,
) {
    mockAndExpectAzureADV2(mockRestServiceWithProxyServer, azureTokenEndpoint, generateAzureAdV2TokenResponse())

    val oboToken = generateAzureAdV2TokenResponse().access_token

    val uriString = UriComponentsBuilder.fromHttpUrl(tilgangskontrollUrl)
        .path(TILGANGSKONTROLL_PERSON_PATH)
        .toUriString()
    mockRestServiceServer.expect(ExpectedCount.manyTimes(), MockRestRequestMatchers.requestTo(uriString))
        .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
        .andExpect(MockRestRequestMatchers.header(HttpHeaders.AUTHORIZATION, "Bearer $oboToken"))
        .andExpect(MockRestRequestMatchers.header(NAV_PERSONIDENT_HEADER, fnr))
        .andRespond(response(status))
}

fun mockSvarFraIstilgangskontrollTilgangTilSYFO(
    azureTokenEndpoint: String,
    tilgangskontrollUrl: String,
    mockRestServiceWithProxyServer: MockRestServiceServer,
    mockRestServiceServer: MockRestServiceServer,
    status: HttpStatus,
) {
    mockAndExpectAzureADV2(mockRestServiceWithProxyServer, azureTokenEndpoint, generateAzureAdV2TokenResponse())

    val oboToken = generateAzureAdV2TokenResponse().access_token

    val uriString = UriComponentsBuilder.fromHttpUrl(tilgangskontrollUrl)
        .path(TILGANGSKONTROLL_SYFO_PATH)
        .toUriString()
    mockRestServiceServer.expect(ExpectedCount.manyTimes(), MockRestRequestMatchers.requestTo(uriString))
        .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
        .andExpect(MockRestRequestMatchers.header(HttpHeaders.AUTHORIZATION, "Bearer $oboToken"))
        .andRespond(response(status))
}

private fun response(status: HttpStatus): ResponseCreator {
    return if (status == HttpStatus.OK) {
        MockRestResponseCreators.withSuccess(tilgangAsJsonString(), MediaType.APPLICATION_JSON)
    } else {
        MockRestResponseCreators.withStatus(
            status,
        )
    }
}

private fun tilgangAsJsonString(): String {
    val objectMapper = ObjectMapper()
    val module = JavaTimeModule()
    objectMapper.registerModule(module)
    val tilgang = Tilgang(erGodkjent = true)
    return try {
        objectMapper.writeValueAsString(tilgang)
    } catch (e: JsonProcessingException) {
        throw RuntimeException(e)
    }
}
