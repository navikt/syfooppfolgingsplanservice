package no.nav.syfo.api.v2.controller


import no.nav.syfo.api.AbstractRessursTilgangTest
import no.nav.syfo.api.selvbetjening.controller.ArbeidstakerSykmeldingerControllerV2
import no.nav.syfo.api.v2.domain.sykmelding.SykmeldingV2
import no.nav.syfo.api.v2.domain.sykmelding.toSykmeldingV2
import no.nav.syfo.brukertilgang.BrukertilgangConsumer
import no.nav.syfo.model.Organisasjonsinformasjon
import no.nav.syfo.model.Sykmelding
import no.nav.syfo.model.Sykmeldingsperiode
import no.nav.syfo.pdl.PdlConsumer
import no.nav.syfo.sykmeldinger.ArbeidstakerSykmeldingerConsumer
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_AKTORID
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_FNR
import no.nav.syfo.testhelper.loggInnBrukerTokenX
import no.nav.syfo.tokenx.tokendings.TokenDingsConsumer
import no.nav.syfo.util.encodedJWTTokenX
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.ResponseEntity
import java.time.LocalDate
import java.util.*
import javax.inject.Inject

class ArbeidstakerSykmeldingerControllerV2Test : AbstractRessursTilgangTest() {
    @MockBean
    lateinit var pdlConsumer: PdlConsumer

    @MockBean
    lateinit var brukertilgangConsumer: BrukertilgangConsumer

    @MockBean
    lateinit var arbeidstakerSykmeldingerConsumer: ArbeidstakerSykmeldingerConsumer

    @MockBean
    lateinit var tokenDingsConsumer: TokenDingsConsumer

    @Inject
    private lateinit var sykmeldingerController: ArbeidstakerSykmeldingerControllerV2

    @Value("\${oppfolgingsplan.frontend.client.id}")
    private lateinit var oppfolgingsplanClientId: String

    private val sykmelding = Sykmelding(
        "1",
        ARBEIDSTAKER_FNR,
        listOf(Sykmeldingsperiode().fom(LocalDate.now()).tom(LocalDate.now().plusDays(30))),
        Organisasjonsinformasjon().orgNavn("orgnavn").orgnummer("orgnummer")
    )
    private val sendteSykmeldinger = listOf(sykmelding)
    private val encodedTokenX = encodedJWTTokenX(ARBEIDSTAKER_FNR)
    private val bearerToken = "Bearer $encodedTokenX"

    @Before
    fun setup() {
        loggInnBrukerTokenX(contextHolder, ARBEIDSTAKER_FNR, oppfolgingsplanClientId)

        `when`(pdlConsumer.aktorid(ARBEIDSTAKER_FNR)).thenReturn(ARBEIDSTAKER_AKTORID)
        `when`(tokenDingsConsumer.exchangeToken(anyString(), anyString())).thenReturn(encodedTokenX)
    }

    @Test
    fun get_sendte_sykmeldinger_ok() {
        `when`(arbeidstakerSykmeldingerConsumer.getSendteSykmeldinger(ARBEIDSTAKER_AKTORID, bearerToken, false))
            .thenReturn(Optional.of(sendteSykmeldinger))

        val res: ResponseEntity<*> = sykmeldingerController.getSendteSykmeldinger("false")
        val body = res.body as List<*>

        val sendteSykmeldingerV2 = sendteSykmeldinger.map { it.toSykmeldingV2() }

        assertEquals(200, res.statusCodeValue.toLong())
        assertEquals(sendteSykmeldingerV2, body)
    }

    @Test
    fun get_sendte_sykmeldinger_noContent() {
        `when`(brukertilgangConsumer.hasAccessToAnsatt(ARBEIDSTAKER_FNR)).thenReturn(true)
        `when`(arbeidstakerSykmeldingerConsumer.getSendteSykmeldinger(ARBEIDSTAKER_AKTORID, bearerToken, false))
            .thenReturn(Optional.empty())

        val res: ResponseEntity<*> = sykmeldingerController.getSendteSykmeldinger("false")
        val body = res.body as List<*>

        assertEquals(200, res.statusCodeValue.toLong())
        assertEquals(emptyList<SykmeldingV2>(), body)
    }
}
