package no.nav.syfo.api.v2.controller

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.syfo.api.selvbetjening.domain.BrukerkontekstConstant
import no.nav.syfo.api.selvbetjening.domain.RSOpprettOppfoelgingsdialog
import no.nav.syfo.api.v3.domain.oppfolgingsplan.BrukerOppfolgingsplan
import no.nav.syfo.api.v3.mapper.populerPlanerMedAvbruttPlanListe
import no.nav.syfo.api.v3.mapper.toBrukerOppfolgingsplan
import no.nav.syfo.domain.Oppfolgingsplan
import no.nav.syfo.metric.Metrikk
import no.nav.syfo.service.OppfolgingsplanService
import no.nav.syfo.tokenx.TokenXUtil
import no.nav.syfo.tokenx.TokenXUtil.TokenXIssuer.TOKENX
import no.nav.syfo.tokenx.TokenXUtil.fnrFromIdportenTokenX
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import javax.inject.Inject

@RestController
@ProtectedWithClaims(issuer = TOKENX, claimMap = ["acr=Level4"])
@RequestMapping(value = ["/api/v3/arbeidstaker/oppfolgingsplaner"])
class ArbeidstakerOppfolgingsplanControllerV3 @Inject constructor(
    private val contextHolder: TokenValidationContextHolder,
    private val oppfolgingsplanService: OppfolgingsplanService,
    private val metrikk: Metrikk,
    @Value("\${tokenx.idp}")
    private val tokenxIdp: String,
    @Value("\${oppfolgingsplan.frontend.client.id}")
    private val oppfolgingsplanClientId: String,
    @Value("\${ditt.sykefravaer.frontend.client.id}")
    private val dittSykefravaerClientId: String,
) {
    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun hentArbeidstakersOppfolgingsplaner(): List<BrukerOppfolgingsplan> {
        val innloggetIdent = TokenXUtil.validateTokenXClaims(contextHolder, tokenxIdp, oppfolgingsplanClientId, dittSykefravaerClientId)
            .fnrFromIdportenTokenX()
            .value
        val arbeidstakersOppfolgingsplaner: List<Oppfolgingsplan> = oppfolgingsplanService.hentAktorsOppfolgingsplaner(BrukerkontekstConstant.ARBEIDSTAKER, innloggetIdent)
        val liste = arbeidstakersOppfolgingsplaner.map { it.toBrukerOppfolgingsplan() }
        liste.forEach { plan -> plan.populerPlanerMedAvbruttPlanListe(liste) }
        metrikk.tellHendelse("hent_oppfolgingsplan_at")
        return liste
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
