package no.nav.syfo.api.v2.controller

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.syfo.api.selvbetjening.domain.BrukerkontekstConstant
import no.nav.syfo.api.selvbetjening.domain.RSBrukerOppfolgingsplan
import no.nav.syfo.api.selvbetjening.domain.RSOpprettOppfoelgingsdialog
import no.nav.syfo.api.selvbetjening.mapper.RSBrukerOppfolgingsplanMapper
import no.nav.syfo.metric.Metrikk
import no.nav.syfo.service.OppfolgingsplanService
import no.nav.syfo.tokenx.TokenXUtil
import no.nav.syfo.tokenx.TokenXUtil.fnrFromIdportenTokenX
import no.nav.syfo.util.MapUtil
import no.nav.syfo.util.OppfoelgingsdialogUtil
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import javax.inject.Inject

@RestController
@ProtectedWithClaims(issuer = TokenXUtil.TokenXIssuer.TOKENX, claimMap = ["acr=Level4"])
@RequestMapping(value = ["/api/v2/arbeidstaker/oppfolgingsplaner"])
class ArbeidstakerOppfolgingsplanControllerV2 @Inject constructor(
    private val contextHolder: TokenValidationContextHolder,
    private val oppfolgingsplanService: OppfolgingsplanService,
    private val metrikk: Metrikk,
    @Value("\${tokenx.idp}")
    private val tokenxIdp: String,
    @Value("\${oppfolgingsplan.frontend.client.id}")
    private val oppfolgingsplanClientId: String,
) {
    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun hentArbeidstakersOppfolgingsplaner(): List<RSBrukerOppfolgingsplan> {
        val innloggetIdent = TokenXUtil.validateTokenXClaims(contextHolder, tokenxIdp, oppfolgingsplanClientId, "dev-gcp:plattformsikkerhet:debug-dings")
            .fnrFromIdportenTokenX()
            .value
        val liste = MapUtil.mapListe(
            oppfolgingsplanService.hentAktorsOppfolgingsplaner(BrukerkontekstConstant.ARBEIDSTAKER, innloggetIdent), RSBrukerOppfolgingsplanMapper.oppfolgingsplan2rs
        )
        metrikk.tellHendelse("hent_oppfolgingsplan_at")
        return OppfoelgingsdialogUtil.populerOppfolgingsplanerMedAvbruttPlanListe(liste)
    }

    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun opprettOppfolgingsplanSomArbeidstaker(@RequestBody rsOpprettOppfoelgingsdialog: RSOpprettOppfoelgingsdialog): Long {
        val innloggetFnr = TokenXUtil.validateTokenXClaims(contextHolder, tokenxIdp, oppfolgingsplanClientId)
            .fnrFromIdportenTokenX()
            .value
        rsOpprettOppfoelgingsdialog.sykmeldtFnr = innloggetFnr
        val id = oppfolgingsplanService.opprettOppfolgingsplan(rsOpprettOppfoelgingsdialog, innloggetFnr)
        metrikk.tellHendelse("opprett_oppfolgingsplan_at")
        return id
    }
}
