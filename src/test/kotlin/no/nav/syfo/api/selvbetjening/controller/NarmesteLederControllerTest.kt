package no.nav.syfo.api.selvbetjening.controller

import no.nav.syfo.aktorregister.AktorregisterConsumer
import no.nav.syfo.api.AbstractRessursTilgangTest
import no.nav.syfo.api.selvbetjening.controller.NarmesteLederController
import no.nav.syfo.brukertilgang.BrukertilgangConsumer
import no.nav.syfo.model.Naermesteleder
import no.nav.syfo.narmesteleder.NarmesteLederConsumer
import no.nav.syfo.testhelper.NarmesteLederGenerator
import no.nav.syfo.testhelper.OidcTestHelper.loggInnBruker
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_AKTORID
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_FNR
import no.nav.syfo.testhelper.UserConstants.LEDER_AKTORID
import no.nav.syfo.testhelper.UserConstants.LEDER_FNR
import no.nav.syfo.testhelper.UserConstants.VIRKSOMHETSNUMMER
import no.nav.syfo.util.HeaderUtil
import org.junit.*
import org.mockito.Mockito
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.ResponseEntity
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import java.util.*
import javax.inject.Inject

class NarmesteLederControllerTest : AbstractRessursTilgangTest() {
    @MockBean
    lateinit var aktorregisterConsumer: AktorregisterConsumer

    @MockBean
    lateinit var brukertilgangConsumer: BrukertilgangConsumer

    @MockBean
    lateinit var narmesteLederConsumer: NarmesteLederConsumer

    @Inject
    private lateinit var narmesteLederController: NarmesteLederController
    private val narmesteLederGenerator = NarmesteLederGenerator()
    private val naermesteleder = narmesteLederGenerator.generateNarmesteLeder()
        .naermesteLederAktoerId(LEDER_AKTORID)
    private val httpHeaders = getHttpHeaders()

    @Before
    fun setup() {
        Mockito.`when`(aktorregisterConsumer.hentAktorIdForFnr(ARBEIDSTAKER_FNR)).thenReturn(ARBEIDSTAKER_AKTORID)
    }

    @Test
    fun narmesteLeder_ansatt_ok() {
        loggInnBruker(oidcRequestContextHolder, LEDER_FNR)
        Mockito.`when`(brukertilgangConsumer.hasAccessToAnsatt(ARBEIDSTAKER_FNR)).thenReturn(true)
        Mockito.`when`(narmesteLederConsumer.narmesteLeder(ARBEIDSTAKER_AKTORID, VIRKSOMHETSNUMMER))
            .thenReturn(Optional.of(naermesteleder))
        val res: ResponseEntity<*> = narmesteLederController.getNarmesteLeder(httpHeaders, VIRKSOMHETSNUMMER)
        val body = res.body as Naermesteleder
        Assert.assertEquals(200, res.statusCodeValue.toLong())
        Assert.assertEquals(naermesteleder, body)
    }

    @Test
    fun narmesteLeder_self_ok() {
        loggInnBruker(oidcRequestContextHolder, ARBEIDSTAKER_FNR)
        Mockito.`when`(narmesteLederConsumer.narmesteLeder(ARBEIDSTAKER_AKTORID, VIRKSOMHETSNUMMER))
            .thenReturn(Optional.of(naermesteleder))
        val res: ResponseEntity<*> = narmesteLederController.getNarmesteLeder(httpHeaders, VIRKSOMHETSNUMMER)
        val body = res.body as Naermesteleder
        Assert.assertEquals(200, res.statusCodeValue.toLong())
        Assert.assertEquals(naermesteleder, body)
    }

    @Test
    fun narmesteLeder_noContent() {
        loggInnBruker(oidcRequestContextHolder, LEDER_FNR)
        Mockito.`when`(brukertilgangConsumer.hasAccessToAnsatt(ARBEIDSTAKER_FNR)).thenReturn(true)
        Mockito.`when`(narmesteLederConsumer.narmesteLeder(ARBEIDSTAKER_AKTORID, VIRKSOMHETSNUMMER)).thenReturn(Optional.empty())
        val res: ResponseEntity<*> = narmesteLederController.getNarmesteLeder(httpHeaders, VIRKSOMHETSNUMMER)
        Assert.assertEquals(204, res.statusCodeValue.toLong())
    }

    @Test
    fun narmesteLeder_forbidden() {
        loggInnBruker(oidcRequestContextHolder, LEDER_FNR)
        Mockito.`when`(brukertilgangConsumer.hasAccessToAnsatt(ARBEIDSTAKER_FNR)).thenReturn(false)
        val res: ResponseEntity<*> = narmesteLederController.getNarmesteLeder(httpHeaders, VIRKSOMHETSNUMMER)
        Assert.assertEquals(403, res.statusCodeValue.toLong())
    }

    private fun getHttpHeaders(): MultiValueMap<String, String> {
        val headers: MultiValueMap<String, String> = LinkedMultiValueMap()
        headers.add(HeaderUtil.NAV_PERSONIDENT, ARBEIDSTAKER_FNR)
        return headers
    }
}