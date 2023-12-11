package no.nav.syfo.api.v3.controller

import no.nav.syfo.api.AbstractRessursTilgangTest
import no.nav.syfo.api.v3.domain.Arbeidsforhold
import no.nav.syfo.brukertilgang.BrukertilgangConsumer
import no.nav.syfo.model.Stilling
import no.nav.syfo.service.ArbeidsforholdService
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_FNR
import no.nav.syfo.testhelper.UserConstants.LEDER_FNR
import no.nav.syfo.testhelper.UserConstants.VIRKSOMHETSNUMMER
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.`when`
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.ResponseEntity
import java.math.BigDecimal
import java.time.LocalDate
import javax.inject.Inject

class ArbeidsforholdControllerV3Test : AbstractRessursTilgangTest() {

    @MockBean
    lateinit var brukertilgangConsumer: BrukertilgangConsumer

    @MockBean
    lateinit var arbeidsforholdService: ArbeidsforholdService

    @Inject
    private lateinit var arbeidsforholdController: ArbeidsforholdControllerV3

    val stilling = Stilling().yrke("Bilmekaniker").prosent(BigDecimal.TEN).fom(LocalDate.now().minusYears(1)).tom(LocalDate.now())

    @Test
    fun narmesteLeder_ansatt_ok() {
        tokenValidationTestUtil.logInAsUser(LEDER_FNR)
        `when`(brukertilgangConsumer.hasAccessToAnsatt(ARBEIDSTAKER_FNR)).thenReturn(true)
        `when`(arbeidsforholdService.arbeidstakersStillingerForOrgnummer(ARBEIDSTAKER_FNR, listOf(VIRKSOMHETSNUMMER)))
            .thenReturn(listOf(stilling))
        val res: ResponseEntity<List<Arbeidsforhold>> = arbeidsforholdController.getArbeidsforhold(ARBEIDSTAKER_FNR, VIRKSOMHETSNUMMER)
        val body = res.body as List<Arbeidsforhold>
        val arbeidsforhold = body[0]
        assertEquals(200, res.statusCode.value().toLong())
        assertEquals(stilling.yrke, arbeidsforhold.yrke)
        assertEquals(stilling.prosent, arbeidsforhold.prosent)
        assertEquals(stilling.fom, arbeidsforhold.fom)
        assertEquals(stilling.tom, arbeidsforhold.tom)
    }

    @Test
    fun narmesteLeder_self_ok() {
        tokenValidationTestUtil.logInAsUser(ARBEIDSTAKER_FNR)
        `when`(arbeidsforholdService.arbeidstakersStillingerForOrgnummer(ARBEIDSTAKER_FNR, listOf(VIRKSOMHETSNUMMER)))
            .thenReturn(listOf(stilling))
        val res: ResponseEntity<List<Arbeidsforhold>> = arbeidsforholdController.getArbeidsforhold(ARBEIDSTAKER_FNR, VIRKSOMHETSNUMMER)
        val body = res.body as List<Arbeidsforhold>
        val arbeidsforhold = body[0]
        assertEquals(200, res.statusCode.value().toLong())
        assertEquals(stilling.yrke, arbeidsforhold.yrke)
        assertEquals(stilling.prosent, arbeidsforhold.prosent)
        assertEquals(stilling.fom, arbeidsforhold.fom)
        assertEquals(stilling.tom, arbeidsforhold.tom)
    }

    @Test
    fun narmesteLeder_forbidden() {
        tokenValidationTestUtil.logInAsUser(LEDER_FNR)
        `when`(brukertilgangConsumer.hasAccessToAnsatt(ARBEIDSTAKER_FNR)).thenReturn(false)

        val res: ResponseEntity<*> = arbeidsforholdController.getArbeidsforhold(ARBEIDSTAKER_FNR, VIRKSOMHETSNUMMER)
        assertEquals(403, res.statusCode.value().toLong())
    }
}
