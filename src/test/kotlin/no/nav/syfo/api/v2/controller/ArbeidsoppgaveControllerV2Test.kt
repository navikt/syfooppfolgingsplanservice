package no.nav.syfo.api.v2.controller

import no.nav.syfo.api.AbstractRessursTilgangTest
import no.nav.syfo.service.ArbeidsoppgaveService
import no.nav.syfo.testhelper.OidcTestHelper.loggUtAlle
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_FNR
import no.nav.syfo.testhelper.loggInnBrukerTokenX
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.mock.mockito.MockBean
import javax.inject.Inject

class ArbeidsoppgaveControllerV2Test : AbstractRessursTilgangTest() {
    @MockBean
    lateinit var arbeidsoppgaveService: ArbeidsoppgaveService

    @Inject
    private lateinit var arbeidsoppgaveController: ArbeidsoppgaveControllerV2


    @Value("\${tokenx.idp}")
    private lateinit var tokenxIdp: String

    @Value("\${oppfolgingsplan.frontend.client.id}")
    private lateinit var oppfolgingsplanClientId: String

    @Before
    fun setup() {
        loggInnBrukerTokenX(contextHolder, ARBEIDSTAKER_FNR, oppfolgingsplanClientId, tokenxIdp)
    }

    @Test
    fun sletter_arbeidsoppgave_som_bruker() {
        arbeidsoppgaveController.slettArbeidsoppgave(1L)
        verify(arbeidsoppgaveService).slettArbeidsoppgave(1L, ARBEIDSTAKER_FNR)
    }

    @Test(expected = RuntimeException::class)
    fun finner_ikke_innlogget_bruker() {
        loggUtAlle(contextHolder)
        arbeidsoppgaveController.slettArbeidsoppgave(1L)
    }
}
