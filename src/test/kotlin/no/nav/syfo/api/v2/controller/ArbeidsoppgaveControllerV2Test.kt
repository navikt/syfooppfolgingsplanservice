package no.nav.syfo.api.v2.controller

import no.nav.syfo.api.AbstractRessursTilgangTest
import no.nav.syfo.service.ArbeidsoppgaveService
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_FNR
import org.junit.Test
import org.mockito.Mockito.verify
import org.springframework.boot.test.mock.mockito.MockBean
import javax.inject.Inject

class ArbeidsoppgaveControllerV2Test : AbstractRessursTilgangTest() {
    @MockBean
    lateinit var arbeidsoppgaveService: ArbeidsoppgaveService

    @Inject
    private lateinit var arbeidsoppgaveController: ArbeidsoppgaveControllerV2

    @Test
    fun sletter_arbeidsoppgave_som_bruker() {
        tokenValidationTestUtil.logInAsUser(ARBEIDSTAKER_FNR)
        arbeidsoppgaveController.slettArbeidsoppgave(1L)
        verify(arbeidsoppgaveService).slettArbeidsoppgave(1L, ARBEIDSTAKER_FNR)
    }

    @Test(expected = RuntimeException::class)
    fun finner_ikke_innlogget_bruker() {
        arbeidsoppgaveController.slettArbeidsoppgave(1L)
    }
}
