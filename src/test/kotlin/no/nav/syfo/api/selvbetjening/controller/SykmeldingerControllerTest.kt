package no.nav.syfo.api.selvbetjening.controller

import com.google.common.net.HttpHeaders.AUTHORIZATION
import no.nav.syfo.aktorregister.AktorregisterConsumer
import no.nav.syfo.api.AbstractRessursTilgangTest
import no.nav.syfo.brukertilgang.BrukertilgangConsumer
import no.nav.syfo.model.OrganisasjonsInformasjon
import no.nav.syfo.model.Sykmelding
import no.nav.syfo.model.Sykmeldingsperiode
import no.nav.syfo.sykmeldinger.SykmeldingerConsumer
import no.nav.syfo.testhelper.OidcTestHelper.loggInnBruker
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_AKTORID
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_FNR
import org.joda.time.LocalDate
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.ResponseEntity
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import java.util.*
import javax.inject.Inject

class SykmeldingerControllerTest : AbstractRessursTilgangTest() {
    @MockBean
    lateinit var aktorregisterConsumer: AktorregisterConsumer

    @MockBean
    lateinit var brukertilgangConsumer: BrukertilgangConsumer

    @MockBean
    lateinit var sykmeldingerConsumer: SykmeldingerConsumer

    @Inject
    private lateinit var sykmeldingerController: SykmeldingerController
    val sykmelding = Sykmelding(
        "1",
        listOf(Sykmeldingsperiode().fom(LocalDate()).tom(LocalDate())),
        OrganisasjonsInformasjon().orgNavn("orgnavn").orgnummer("orgnummer")
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
        Mockito.`when`(sykmeldingerConsumer.getSendteSykmeldinger(ARBEIDSTAKER_AKTORID, "token")).thenReturn(Optional.of(sendteSykmeldinger))

        val res: ResponseEntity<*> = sykmeldingerController.getSendteSykmeldinger(httpHeaders)
        val body = res.body as List<*>

        Assert.assertEquals(200, res.statusCodeValue.toLong())
        Assert.assertEquals(sendteSykmeldinger, body)
    }

    @Test
    fun get_sendte_sykmeldinger_noContent() {
        loggInnBruker(contextHolder, ARBEIDSTAKER_FNR)
        Mockito.`when`(brukertilgangConsumer.hasAccessToAnsatt(ARBEIDSTAKER_FNR)).thenReturn(true)
        Mockito.`when`(sykmeldingerConsumer.getSendteSykmeldinger(ARBEIDSTAKER_AKTORID, "token")).thenReturn(Optional.empty())

        val res: ResponseEntity<*> = sykmeldingerController.getSendteSykmeldinger(httpHeaders)

        Assert.assertEquals(200, res.statusCodeValue.toLong())
    }

    private fun getHttpHeaders(): MultiValueMap<String, String> {
        val headers: MultiValueMap<String, String> = LinkedMultiValueMap()
        headers.add("authorization", "token")
        return headers
    }
}