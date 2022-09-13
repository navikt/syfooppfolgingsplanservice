package no.nav.syfo.api.selvbetjening.controller

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.syfo.api.selvbetjening.domain.BrukerkontekstConstant.ARBEIDSGIVER
import no.nav.syfo.api.selvbetjening.domain.RSBrukerOppfolgingsplan
import no.nav.syfo.api.selvbetjening.domain.RSOpprettOppfoelgingsdialog
import no.nav.syfo.api.selvbetjening.mapper.RSBrukerOppfolgingsplanMapper.oppfolgingsplan2rs
import no.nav.syfo.metric.Metrikk
import no.nav.syfo.narmesteleder.NarmesteLederConsumer
import no.nav.syfo.service.OppfolgingsplanService
import no.nav.syfo.tokenx.TokenXUtil
import no.nav.syfo.tokenx.TokenXUtil.TokenXIssuer.TOKENX
import no.nav.syfo.tokenx.TokenXUtil.fnrFromIdportenTokenX
import no.nav.syfo.util.MapUtil.mapListe
import no.nav.syfo.util.OppfoelgingsdialogUtil.populerOppfolgingsplanerMedAvbruttPlanListe
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.web.bind.annotation.*
import javax.inject.Inject
import javax.ws.rs.ForbiddenException

@RestController
@ProtectedWithClaims(issuer = TOKENX, claimMap = ["acr=Level4"])
@RequestMapping(value = ["/api/v2/arbeidsgiver/oppfolgingsplaner"])
class ArbeidsgiverOppfolgingsplanControllerV2 @Inject constructor(
    private val contextHolder: TokenValidationContextHolder,
    private val narmesteLederConsumer: NarmesteLederConsumer,
    private val oppfolgingsplanService: OppfolgingsplanService,
    private val metrikk: Metrikk,
    @Value("\${tokenx.idp}")
    val tokenxIdp: String,
    @Value("\${oppfolgingsplan.frontend.client.id}")
    val oppfolgingsplanClientId: String,
) {
    @GetMapping(produces = [APPLICATION_JSON_VALUE])
    fun hentArbeidsgiversOppfolgingsplaner(): List<RSBrukerOppfolgingsplan> {
        val innloggetIdent = TokenXUtil.validateTokenXClaims(contextHolder, tokenxIdp, oppfolgingsplanClientId)
            .fnrFromIdportenTokenX()
            .value
        val liste = mapListe(
            oppfolgingsplanService.hentAktorsOppfolgingsplaner(ARBEIDSGIVER, innloggetIdent),
            oppfolgingsplan2rs
        )
        metrikk.tellHendelse("hent_oppfolgingsplan_ag")
        return populerOppfolgingsplanerMedAvbruttPlanListe(liste)
    }

    @PostMapping(consumes = [APPLICATION_JSON_VALUE], produces = [APPLICATION_JSON_VALUE])
    fun opprettOppfolgingsplanSomArbeidsgiver(@RequestBody rsOpprettOppfolgingsplan: RSOpprettOppfoelgingsdialog): Long {
        val innloggetFnr = TokenXUtil.validateTokenXClaims(contextHolder, tokenxIdp, oppfolgingsplanClientId)
            .fnrFromIdportenTokenX()
            .value
        val sykmeldtFnr = rsOpprettOppfolgingsplan.sykmeldtFnr
        return if (narmesteLederConsumer.erNaermesteLederForAnsatt(innloggetFnr, sykmeldtFnr)) {
            val id = oppfolgingsplanService.opprettOppfolgingsplan(rsOpprettOppfolgingsplan, innloggetFnr)
            metrikk.tellHendelse("opprett_oppfolgingsplan_ag")
            id
        } else {
            throw ForbiddenException()
        }
    }
}