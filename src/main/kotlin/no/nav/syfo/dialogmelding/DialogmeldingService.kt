package no.nav.syfo.dialogmelding

import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.syfo.metric.Metrikk
import no.nav.syfo.oidc.OIDCUtil.getSluttbrukerToken
import no.nav.syfo.tokenx.tokendings.TokenDingsConsumer
import no.nav.syfo.util.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI


@Service
class DialogmeldingService(
    @Value("\${isdialogmelding.url}") dialogmeldingUrl: String,
    @Value("\${isdialogmelding.client.id}")
    private val dialogmeldingClientId: String,
    private val metrikk: Metrikk,
    private val contextHolder: TokenValidationContextHolder,
    private val restTemplate: RestTemplate,
    private val tokenDingsConsumer: TokenDingsConsumer,
) {

    private val SEND_OPPFOLGINGSPLAN_PATH = "/api/person/v1/oppfolgingsplan"
    private val delMedFastlegeUriTemplate: UriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(dialogmeldingUrl)
        .path(SEND_OPPFOLGINGSPLAN_PATH)
    private val log = LoggerFactory.getLogger(DialogmeldingService::class.java)

    fun sendOppfolgingsplanTilFastlege(sendesTilFnr: String, pdf: ByteArray) {
        val rsOppfoelgingsplan = RSOppfoelgingsplan(sendesTilFnr, pdf)
        val tilgangTilBrukerUriMedFnr = delMedFastlegeUriTemplate.build().toUri()
        val token = getSluttbrukerToken(contextHolder)
        val exchangedToken = tokenDingsConsumer.exchangeToken(token, dialogmeldingClientId)
        try {
            kallUriMedTemplate(
                tilgangTilBrukerUriMedFnr,
                rsOppfoelgingsplan,
                exchangedToken,
                false
            )
        } catch (e: OppslagFeiletException) {
            log.warn("Fanget OppslagFeiletException: {}", e.message)
            throw e
        }
    }

    private fun kallUriMedTemplate(uri: URI, rsOppfoelgingsplan: RSOppfoelgingsplan, token: String, lps: Boolean) {
        tellPlanForsoktDeltMedFastlegeKallLPS()
        try {
            restTemplate.postForLocation(uri, entity(rsOppfoelgingsplan, token))
            tellPlanDeltMedFastlegeKall(lps, true)
        } catch (e: HttpClientErrorException) {
            val responsekode = e.rawStatusCode
            tellPlanDeltMedFastlegeKall(lps, false)
            if (responsekode == 404) {
                throw OppslagFeiletException("Feil ved oppslag av fastlege eller partnerinformasjon")
            } else {
                log.error("Feil ved sending av oppfølgingsdialog til fastlege Fikk responskode $responsekode", e)
            }
            throw e
        } catch (e: HttpServerErrorException) {
            val responsekode = e.rawStatusCode
            log.error("Feil ved sending av oppfølgingsdialog til fastlege: Fikk responskode $responsekode", e)
            tellPlanDeltMedFastlegeKall(lps, false)
            throw InnsendingFeiletException("Kunne ikke dele med fastlege")
        } catch (e: Exception) {
            log.error("Feil ved sending av oppfølgingsdialog til fastlege", e)
            tellPlanDeltMedFastlegeKall(lps, false)
            throw e
        }
    }

    private fun entity(rsOppfoelgingsplan: RSOppfoelgingsplan, token: String): HttpEntity<RSOppfoelgingsplan> {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.add(AUTHORIZATION, bearerHeader(token))
        headers.add(NAV_CALL_ID_HEADER, createCallId())
        return HttpEntity(rsOppfoelgingsplan, headers)
    }

    private fun tellPlanForsoktDeltMedFastlegeKallLPS() {
        metrikk.tellHendelse("tell_antall_lps_forsokt_delt_fastlege")
    }

    private fun tellPlanDeltMedFastlegeKall(lps: Boolean, delt: Boolean) {
        if (lps) metrikk.tellHendelseMedTag("lps_plan_delt_med_fastlege", "delt", delt)
        metrikk.tellHendelseMedTag("plan_delt_med_fastlege", "delt", delt)
    }
}
