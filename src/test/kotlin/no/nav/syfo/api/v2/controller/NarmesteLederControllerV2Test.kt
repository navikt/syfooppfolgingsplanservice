package no.nav.syfo.api.v2.controller

import no.nav.syfo.api.AbstractRessursTilgangTest
import no.nav.syfo.api.v2.domain.NarmesteLeder
import no.nav.syfo.brukertilgang.BrukertilgangConsumer
import no.nav.syfo.model.NaermesteLederStatus
import no.nav.syfo.model.Naermesteleder
import no.nav.syfo.narmesteleder.NarmesteLederConsumer
import no.nav.syfo.oidc.OIDCIssuer.EKSTERN
import no.nav.syfo.oidc.OIDCUtil.getIssuerToken
import no.nav.syfo.testhelper.OidcTestHelper
import no.nav.syfo.testhelper.UserConstants
import org.junit.Assert
import org.junit.Test
import org.mockito.Mockito
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.ResponseEntity
import java.time.LocalDate
import java.util.*
import javax.inject.Inject

class NarmesteLederControllerV2Test : AbstractRessursTilgangTest() {

    @MockBean
    lateinit var brukertilgangConsumer: BrukertilgangConsumer

    @MockBean
    lateinit var narmesteLederConsumer: NarmesteLederConsumer

    @Inject
    private lateinit var narmesteLederController: NarmesteLederControllerV2

    private val naermesteleder = Naermesteleder()
        .naermesteLederFnr(UserConstants.LEDER_FNR)
        .orgnummer(UserConstants.VIRKSOMHETSNUMMER)
        .naermesteLederStatus(NaermesteLederStatus().erAktiv(true).aktivFom(LocalDate.now()).aktivTom(LocalDate.now()))
        .navn("Test Testesen")

    @Test
    fun narmesteLeder_ansatt_ok() {
        OidcTestHelper.loggInnBruker(contextHolder, UserConstants.LEDER_FNR)
        Mockito.`when`(brukertilgangConsumer.hasAccessToAnsatt(
            UserConstants.ARBEIDSTAKER_FNR,
            getIssuerToken(contextHolder, EKSTERN)
        )).thenReturn(true)
        Mockito.`when`(narmesteLederConsumer.narmesteLeder(UserConstants.ARBEIDSTAKER_FNR, UserConstants.VIRKSOMHETSNUMMER))
            .thenReturn(Optional.of(naermesteleder))
        val res: ResponseEntity<*> = narmesteLederController.getNarmesteLeder(UserConstants.ARBEIDSTAKER_FNR, UserConstants.VIRKSOMHETSNUMMER)
        val body = res.body as NarmesteLeder
        Assert.assertEquals(200, res.statusCodeValue.toLong())
        Assert.assertEquals(naermesteleder.naermesteLederFnr, body.fnr)
        Assert.assertEquals(naermesteleder.orgnummer, body.virksomhetsnummer)
        Assert.assertEquals(naermesteleder.naermesteLederStatus.erAktiv, body.erAktiv)
        Assert.assertEquals(naermesteleder.naermesteLederStatus.aktivFom, body.aktivFom)
        Assert.assertEquals(naermesteleder.naermesteLederStatus.aktivTom, body.aktivTom)
    }

    @Test
    fun narmesteLeder_self_ok() {
        OidcTestHelper.loggInnBruker(contextHolder, UserConstants.ARBEIDSTAKER_FNR)
        Mockito.`when`(narmesteLederConsumer.narmesteLeder(UserConstants.ARBEIDSTAKER_FNR, UserConstants.VIRKSOMHETSNUMMER))
            .thenReturn(Optional.of(naermesteleder))
        val res: ResponseEntity<*> = narmesteLederController.getNarmesteLeder(UserConstants.ARBEIDSTAKER_FNR, UserConstants.VIRKSOMHETSNUMMER)
        val body = res.body as NarmesteLeder
        Assert.assertEquals(200, res.statusCodeValue.toLong())
        Assert.assertEquals(naermesteleder.naermesteLederFnr, body.fnr)
        Assert.assertEquals(naermesteleder.orgnummer, body.virksomhetsnummer)
        Assert.assertEquals(naermesteleder.naermesteLederStatus.erAktiv, body.erAktiv)
        Assert.assertEquals(naermesteleder.naermesteLederStatus.aktivFom, body.aktivFom)
        Assert.assertEquals(naermesteleder.naermesteLederStatus.aktivTom, body.aktivTom)
    }

    @Test
    fun narmesteLeder_noContent() {
        OidcTestHelper.loggInnBruker(contextHolder, UserConstants.LEDER_FNR)
        Mockito.`when`(brukertilgangConsumer.hasAccessToAnsatt(
            UserConstants.ARBEIDSTAKER_FNR,
            getIssuerToken(contextHolder, EKSTERN)
        )).thenReturn(true)
        Mockito.`when`(narmesteLederConsumer.narmesteLeder(UserConstants.ARBEIDSTAKER_FNR, UserConstants.VIRKSOMHETSNUMMER)).thenReturn(Optional.empty())
        val res: ResponseEntity<*> = narmesteLederController.getNarmesteLeder(UserConstants.ARBEIDSTAKER_FNR, UserConstants.VIRKSOMHETSNUMMER)
        Assert.assertEquals(204, res.statusCodeValue.toLong())
    }

    @Test
    fun narmesteLeder_forbidden() {
        OidcTestHelper.loggInnBruker(contextHolder, UserConstants.LEDER_FNR)
        Mockito.`when`(brukertilgangConsumer.hasAccessToAnsatt(
            UserConstants.ARBEIDSTAKER_FNR,
            getIssuerToken(contextHolder, EKSTERN)
        )).thenReturn(false)
        val res: ResponseEntity<*> = narmesteLederController.getNarmesteLeder(UserConstants.ARBEIDSTAKER_FNR, UserConstants.VIRKSOMHETSNUMMER)
        Assert.assertEquals(403, res.statusCodeValue.toLong())
    }
}
