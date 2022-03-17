package no.nav.syfo.api.selvbetjening.controller

import no.nav.security.token.support.core.context.TokenValidationContext
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.security.token.support.core.jwt.JwtToken
import no.nav.syfo.aktorregister.AktorregisterConsumer
import no.nav.syfo.api.AbstractRessursTilgangTest
import no.nav.syfo.brukertilgang.BrukertilgangConsumer
import no.nav.syfo.model.Organisasjonsinformasjon
import no.nav.syfo.model.Sykmelding
import no.nav.syfo.model.Sykmeldingsperiode
import no.nav.syfo.oidc.OIDCIssuer
import no.nav.syfo.sykmeldinger.ArbeidstakerSykmeldingerConsumer
import no.nav.syfo.testhelper.OidcTestHelper.loggInnBruker
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_AKTORID
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_FNR
import no.nav.syfo.util.encodedJWTToken
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.ResponseEntity
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

    @MockBean
    lateinit var tokenValidationContextHolder: TokenValidationContextHolder

    @Inject
    private lateinit var sykmeldingerController: ArbeidstakerSykmeldingerController

    private val sykmelding = Sykmelding(
        "1",
        ARBEIDSTAKER_FNR,
        listOf(Sykmeldingsperiode().fom(LocalDate.now()).tom(LocalDate.now().plusDays(30))),
        Organisasjonsinformasjon().orgNavn("orgnavn").orgnummer("orgnummer")
    )
    private val sendteSykmeldinger = listOf(sykmelding)
    private val encodedToken = encodedJWTToken(ARBEIDSTAKER_FNR)
    private val bearerToken = "Bearer $encodedToken"

    @Before
    fun setup() {
        val issuerShortNameValidatedTokenMap = HashMap<String, JwtToken>()
        issuerShortNameValidatedTokenMap[OIDCIssuer.EKSTERN] = JwtToken(encodedToken)

        val tokenValidationContext = TokenValidationContext(
            issuerShortNameValidatedTokenMap
        )

        Mockito.`when`(tokenValidationContextHolder.tokenValidationContext).thenReturn(tokenValidationContext)
        Mockito.`when`(aktorregisterConsumer.hentAktorIdForFnr(ARBEIDSTAKER_FNR)).thenReturn(ARBEIDSTAKER_AKTORID)
    }

    @Test
    fun get_sendte_sykmeldinger_ok() {
        loggInnBruker(contextHolder, ARBEIDSTAKER_FNR)
        Mockito.`when`(arbeidstakerSykmeldingerConsumer.getSendteSykmeldinger(ARBEIDSTAKER_AKTORID, bearerToken, false))
            .thenReturn(Optional.of(sendteSykmeldinger))

        val res: ResponseEntity<*> = sykmeldingerController.getSendteSykmeldinger("false")
        val body = res.body as List<*>

        Assert.assertEquals(200, res.statusCodeValue.toLong())
        Assert.assertEquals(sendteSykmeldinger, body)
    }

    @Test
    fun get_sendte_sykmeldinger_noContent() {
        loggInnBruker(contextHolder, ARBEIDSTAKER_FNR)
        Mockito.`when`(brukertilgangConsumer.hasAccessToAnsatt(ARBEIDSTAKER_FNR)).thenReturn(true)
        Mockito.`when`(arbeidstakerSykmeldingerConsumer.getSendteSykmeldinger(ARBEIDSTAKER_AKTORID, bearerToken, false))
            .thenReturn(Optional.empty())

        val res: ResponseEntity<*> = sykmeldingerController.getSendteSykmeldinger("false")

        Assert.assertEquals(200, res.statusCodeValue.toLong())
    }
}