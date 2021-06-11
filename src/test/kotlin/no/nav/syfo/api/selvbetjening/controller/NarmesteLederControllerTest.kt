package no.nav.syfo.api.selvbetjening.controller

import no.nav.syfo.api.AbstractRessursTilgangTest
import no.nav.syfo.brukertilgang.BrukertilgangConsumer
import no.nav.syfo.model.Naermesteleder
import no.nav.syfo.narmesteleder.NarmesteLederConsumer
import no.nav.syfo.testhelper.OidcTestHelper.loggInnBruker
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_FNR
import no.nav.syfo.testhelper.UserConstants.LEDER_FNR
import no.nav.syfo.testhelper.UserConstants.VIRKSOMHETSNUMMER
import no.nav.syfo.util.NAV_PERSONIDENT_HEADER
import org.junit.Assert
import org.junit.Test
import org.mockito.Mockito
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.ResponseEntity
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import java.util.*
import javax.inject.Inject

class NarmesteLederControllerTest : AbstractRessursTilgangTest() {

    @MockBean
    lateinit var brukertilgangConsumer: BrukertilgangConsumer

    @MockBean
    lateinit var narmesteLederConsumer: NarmesteLederConsumer

    @Inject
    private lateinit var narmesteLederController: NarmesteLederController

    private val naermesteleder = Naermesteleder()
            .naermesteLederFnr(LEDER_FNR)
    private val httpHeaders = getHttpHeaders()

    @Test
    fun narmesteLeder_ansatt_ok() {
        loggInnBruker(contextHolder, LEDER_FNR)
        Mockito.`when`(brukertilgangConsumer.hasAccessToAnsatt(ARBEIDSTAKER_FNR)).thenReturn(true)
        Mockito.`when`(narmesteLederConsumer.narmesteLeder(ARBEIDSTAKER_FNR, VIRKSOMHETSNUMMER))
            .thenReturn(Optional.of(naermesteleder))
        val res: ResponseEntity<*> = narmesteLederController.getNarmesteLeder(httpHeaders, VIRKSOMHETSNUMMER)
        val body = res.body as Naermesteleder
        Assert.assertEquals(200, res.statusCodeValue.toLong())
        Assert.assertEquals(naermesteleder, body)
    }

    @Test
    fun narmesteLeder_self_ok() {
        loggInnBruker(contextHolder, ARBEIDSTAKER_FNR)
        Mockito.`when`(narmesteLederConsumer.narmesteLeder(ARBEIDSTAKER_FNR, VIRKSOMHETSNUMMER))
            .thenReturn(Optional.of(naermesteleder))
        val res: ResponseEntity<*> = narmesteLederController.getNarmesteLeder(httpHeaders, VIRKSOMHETSNUMMER)
        val body = res.body as Naermesteleder
        Assert.assertEquals(200, res.statusCodeValue.toLong())
        Assert.assertEquals(naermesteleder, body)
    }

    @Test
    fun narmesteLeder_noContent() {
        loggInnBruker(contextHolder, LEDER_FNR)
        Mockito.`when`(brukertilgangConsumer.hasAccessToAnsatt(ARBEIDSTAKER_FNR)).thenReturn(true)
        Mockito.`when`(narmesteLederConsumer.narmesteLeder(ARBEIDSTAKER_FNR, VIRKSOMHETSNUMMER)).thenReturn(Optional.empty())
        val res: ResponseEntity<*> = narmesteLederController.getNarmesteLeder(httpHeaders, VIRKSOMHETSNUMMER)
        Assert.assertEquals(204, res.statusCodeValue.toLong())
    }

    @Test
    fun narmesteLeder_forbidden() {
        loggInnBruker(contextHolder, LEDER_FNR)
        Mockito.`when`(brukertilgangConsumer.hasAccessToAnsatt(ARBEIDSTAKER_FNR)).thenReturn(false)
        val res: ResponseEntity<*> = narmesteLederController.getNarmesteLeder(httpHeaders, VIRKSOMHETSNUMMER)
        Assert.assertEquals(403, res.statusCodeValue.toLong())
    }

    private fun getHttpHeaders(): MultiValueMap<String, String> {
        val headers: MultiValueMap<String, String> = LinkedMultiValueMap()
        headers.add(NAV_PERSONIDENT_HEADER, ARBEIDSTAKER_FNR)
        return headers
    }
}