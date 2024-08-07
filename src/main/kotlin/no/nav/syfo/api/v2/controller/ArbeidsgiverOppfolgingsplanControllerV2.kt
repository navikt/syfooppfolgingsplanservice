package no.nav.syfo.api.v2.controller

import jakarta.ws.rs.ForbiddenException
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.syfo.api.v2.domain.oppfolgingsplan.BrukerOppfolgingsplan
import no.nav.syfo.api.v2.domain.oppfolgingsplan.OpprettOppfolgingsplanRequest
import no.nav.syfo.api.v2.mapper.populerArbeidstakersStillinger
import no.nav.syfo.api.v2.mapper.populerPlanerMedAvbruttPlanListe
import no.nav.syfo.api.v2.mapper.toBrukerOppfolgingsplan
import no.nav.syfo.metric.Metrikk
import no.nav.syfo.narmesteleder.NarmesteLederConsumer
import no.nav.syfo.pdl.PdlConsumer
import no.nav.syfo.service.ArbeidsforholdService
import no.nav.syfo.service.OppfolgingsplanService
import no.nav.syfo.tokenx.TokenXUtil
import no.nav.syfo.tokenx.TokenXUtil.TokenXIssuer.TOKENX
import no.nav.syfo.tokenx.TokenXUtil.fnrFromIdportenTokenX
import no.nav.syfo.util.NAV_PERSONIDENT_HEADER
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.web.bind.annotation.*
import javax.inject.Inject

@RestController
@ProtectedWithClaims(issuer = TOKENX, claimMap = ["acr=Level4", "acr=idporten-loa-high"], combineWithOr = true)
@RequestMapping(value = ["/api/v2/arbeidsgiver/oppfolgingsplaner"])
class ArbeidsgiverOppfolgingsplanControllerV2 @Inject constructor(
    private val contextHolder: TokenValidationContextHolder,
    private val narmesteLederConsumer: NarmesteLederConsumer,
    private val oppfolgingsplanService: OppfolgingsplanService,
    private val arbeidsforholdService: ArbeidsforholdService,
    private val pdlConsumer: PdlConsumer,
    private val metrikk: Metrikk,
    @Value("\${oppfolgingsplan.frontend.client.id}")
    private val oppfolgingsplanClientId: String,
) {

    @GetMapping(produces = [APPLICATION_JSON_VALUE])
    fun hentArbeidsgiversOppfolgingsplanerPaPersonident(@RequestHeader(name = NAV_PERSONIDENT_HEADER) personident: String, @RequestParam("virksomhetsnummer") virksomhetsnummer: String): List<BrukerOppfolgingsplan> {
        val innloggetIdent = TokenXUtil.validateTokenXClaims(contextHolder, oppfolgingsplanClientId)
            .fnrFromIdportenTokenX()
            .value
        val arbeidsgiversOppfolgingsplaner = oppfolgingsplanService.arbeidsgiversOppfolgingsplanerPaFnr(innloggetIdent, personident, virksomhetsnummer)
        val liste = arbeidsgiversOppfolgingsplaner.map { it.toBrukerOppfolgingsplan(pdlConsumer) }
        liste.forEach { plan -> plan.populerPlanerMedAvbruttPlanListe(liste) }
        val arbeidsforhold = arbeidsforholdService.arbeidstakersStillingerForOrgnummer(personident, listOf(virksomhetsnummer))
        liste.forEach { plan -> plan.populerArbeidstakersStillinger(arbeidsforhold) }
        metrikk.tellHendelse("hent_oppfolgingsplan_ag")
        return liste
    }

    @PostMapping(consumes = [APPLICATION_JSON_VALUE], produces = [APPLICATION_JSON_VALUE])
    fun opprettOppfolgingsplanSomArbeidsgiver(@RequestBody opprettOppfolgingsplan: OpprettOppfolgingsplanRequest): Long {
        val innloggetFnr = TokenXUtil.validateTokenXClaims(contextHolder, oppfolgingsplanClientId)
            .fnrFromIdportenTokenX()
            .value
        val sykmeldtFnr = opprettOppfolgingsplan.sykmeldtFnr
        return if (narmesteLederConsumer.erNaermesteLederForAnsatt(innloggetFnr, sykmeldtFnr)) {
            val id = oppfolgingsplanService.opprettOppfolgingsplan(innloggetFnr, opprettOppfolgingsplan.virksomhetsnummer, sykmeldtFnr)
            metrikk.tellHendelse("opprett_oppfolgingsplan_ag")
            id
        } else {
            throw ForbiddenException()
        }
    }
}
