package no.nav.syfo.api.selvbetjening.controller

import no.nav.syfo.aktorregister.AktorregisterConsumer
import no.nav.syfo.api.AbstractRessursTilgangTest
import no.nav.syfo.brukertilgang.BrukertilgangConsumer
import no.nav.syfo.model.Organisasjonsinformasjon
import no.nav.syfo.model.Sykmelding
import no.nav.syfo.model.Sykmeldingsperiode
import no.nav.syfo.sykmeldinger.ArbeidstakerSykmeldingerConsumer
import no.nav.syfo.testhelper.OidcTestHelper.loggInnBruker
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_AKTORID
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_FNR
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.ResponseEntity
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import java.time.LocalDate
import java.util.*
import javax.inject.Inject

class ArbeidstakerSykmeldingerControllerTest : AbstractRessursTilgangTest() {
    @MockBean
    lateinit var aktorregisterConsumer: AktorregisterConsumer

    @MockBean
    lateinit var brukertilgangConsumer: BrukertilgangConsumer

    @MockBean
    lateinit var arbeidstakerSykmeldingerConsumer: ArbeidstakerSykmeldingerConsumer

    @Inject
    private lateinit var sykmeldingerController: ArbeidstakerSykmeldingerController
    val sykmelding = Sykmelding(
            "1",
            ARBEIDSTAKER_FNR,
            listOf(Sykmeldingsperiode().fom(LocalDate.now()).tom(LocalDate.now().plusDays(30))),
            Organisasjonsinformasjon().orgNavn("orgnavn").orgnummer("orgnummer")
    )
    val sendteSykmeldinger = listOf(sykmelding)

    private val httpHeaders = getHttpHeaders()

    @Before
    fun setup() {
        Mockito.`when`(aktorregisterConsumer.hentAktorIdForFnr(ARBEIDSTAKER_FNR)).thenReturn(ARBEIDSTAKER_AKTORID)
    }

    @Test
    fun get_sendte_sykmeldinger_ok() {
        loggInnBruker(contextHolder, ARBEIDSTAKER_FNR)
        Mockito.`when`(arbeidstakerSykmeldingerConsumer.getSendteSykmeldinger(ARBEIDSTAKER_AKTORID, "token")).thenReturn(Optional.of(sendteSykmeldinger))

        val res: ResponseEntity<*> = sykmeldingerController.getSendteSykmeldinger(httpHeaders)
        val body = res.body as List<*>

        Assert.assertEquals(200, res.statusCodeValue.toLong())
        Assert.assertEquals(sendteSykmeldinger, body)
    }

    @Test
    fun get_sendte_sykmeldinger_noContent() {
        loggInnBruker(contextHolder, ARBEIDSTAKER_FNR)
        Mockito.`when`(brukertilgangConsumer.hasAccessToAnsatt(ARBEIDSTAKER_FNR)).thenReturn(true)
        Mockito.`when`(arbeidstakerSykmeldingerConsumer.getSendteSykmeldinger(ARBEIDSTAKER_AKTORID, "token")).thenReturn(Optional.empty())

        val res: ResponseEntity<*> = sykmeldingerController.getSendteSykmeldinger(httpHeaders)

        Assert.assertEquals(200, res.statusCodeValue.toLong())
    }

    private fun getHttpHeaders(): MultiValueMap<String, String> {
        val headers: MultiValueMap<String, String> = LinkedMultiValueMap()
        headers.add("authorization", "token")
        return headers
    }
}