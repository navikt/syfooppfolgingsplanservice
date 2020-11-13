package no.nav.syfo.api.selvbetjening.controller

import no.nav.syfo.api.AbstractRessursTilgangTest
import no.nav.syfo.service.ArbeidsoppgaveService
import no.nav.syfo.testhelper.OidcTestHelper.loggInnBruker
import no.nav.syfo.testhelper.OidcTestHelper.loggUtAlle
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_FNR
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.springframework.boot.test.mock.mockito.MockBean
import javax.inject.Inject

class ArbeidsoppgaveControllerTest : AbstractRessursTilgangTest() {
    @MockBean
    lateinit var arbeidsoppgaveService: ArbeidsoppgaveService

    @Inject
    private lateinit var arbeidsoppgaveController: ArbeidsoppgaveController

    @Before
    fun setup() {
        loggInnBruker(contextHolder, ARBEIDSTAKER_FNR)
    }

    @Test
    fun sletter_arbeidsoppgave_som_bruker() {
        arbeidsoppgaveController.slettArbeidsoppgave(1L)
        Mockito.verify(arbeidsoppgaveService).slettArbeidsoppgave(1L, ARBEIDSTAKER_FNR)
    }

    @Test(expected = RuntimeException::class)
    fun finner_ikke_innlogget_bruker() {
        loggUtAlle(contextHolder)
        arbeidsoppgaveController.slettArbeidsoppgave(1L)
    }
}
