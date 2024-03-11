package no.nav.syfo.api.v3.controller

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.syfo.api.v3.domain.Historikk
import no.nav.syfo.api.v3.domain.oppfoelgingsdialog.RSOppfoelgingsdialog
import no.nav.syfo.api.v3.mapper.oppfoelgingsdialog2rs
import no.nav.syfo.domain.Fodselsnummer
import no.nav.syfo.domain.Oppfolgingsplan
import no.nav.syfo.ereg.EregConsumer
import no.nav.syfo.oidc.OIDCIssuer.INTERN_AZUREAD_V2
import no.nav.syfo.pdl.PdlConsumer
import no.nav.syfo.repository.dao.OppfolgingsplanDAO
import no.nav.syfo.service.BrukerprofilService
import no.nav.syfo.util.NAV_PERSONIDENT_HEADER
import no.nav.syfo.veiledertilgang.VeilederTilgangConsumer
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.stream.Collectors
import javax.inject.Inject

@RestController
@ProtectedWithClaims(issuer = INTERN_AZUREAD_V2)
@RequestMapping(value = ["/api/internad/v3/oppfolgingsplan"])
class OppfolgingsplanInternControllerV3 @Inject constructor(
    private val pdlConsumer: PdlConsumer,
    private val brukerprofilService: BrukerprofilService,
    private val eregConsumer: EregConsumer,
    private val oppfolgingsplanDAO: OppfolgingsplanDAO,
    private val veilederTilgangConsumer: VeilederTilgangConsumer
) {
    private fun finnDeltAvNavn(oppfolgingsplan: Oppfolgingsplan): String {
        if (oppfolgingsplan.sistEndretAvAktoerId == oppfolgingsplan.arbeidstaker.aktoerId) {
            return brukerprofilService.hentNavnByAktoerId(oppfolgingsplan.sistEndretAvAktoerId)
        }
        return eregConsumer.virksomhetsnavn(oppfolgingsplan.virksomhet.virksomhetsnummer)
    }

    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    @RequestMapping(value = ["/historikk"])
    fun getHistorikk(@RequestHeader(name = NAV_PERSONIDENT_HEADER) personident: String): List<Historikk> {
        val personFnr = Fodselsnummer(personident)

        veilederTilgangConsumer.throwExceptionIfVeilederWithoutAccessWithOBO(personFnr)

        val oppfoelgingsplaner = oppfolgingsplanDAO.oppfolgingsplanerKnyttetTilSykmeldt(
            pdlConsumer.aktorid(personFnr.value)
        )
            .stream()
            .map { oppfoelgingsdialog: Oppfolgingsplan? -> oppfolgingsplanDAO.populate(oppfoelgingsdialog) }
            .filter { oppfoelgingsdialog: Oppfolgingsplan -> oppfoelgingsdialog.godkjentPlan.isPresent }
            .filter { oppfoelgingsdialog: Oppfolgingsplan -> oppfoelgingsdialog.godkjentPlan.get().deltMedNAV }
            .collect(Collectors.toList())

        return oppfoelgingsplaner.map {
            Historikk(
                tekst = "Oppfølgingsplanen ble delt med NAV av " + finnDeltAvNavn(it) + ".",
                tidspunkt = it.godkjentPlan.get().deltMedNAVTidspunkt
            )
        }
    }

    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getOppfolgingsplaner(@RequestHeader(name = NAV_PERSONIDENT_HEADER) personident: String): List<RSOppfoelgingsdialog> {
        val personFnr = Fodselsnummer(personident)

        veilederTilgangConsumer.throwExceptionIfVeilederWithoutAccessWithOBO(personFnr)

        val planer = oppfolgingsplanDAO.oppfolgingsplanerKnyttetTilSykmeldt(pdlConsumer.aktorid(personFnr.value))
            .stream()
            .map { oppfoelgingsdialog: Oppfolgingsplan? -> oppfolgingsplanDAO.populate(oppfoelgingsdialog) }
            .filter { oppfoelgingsdialog: Oppfolgingsplan -> oppfoelgingsdialog.godkjentPlan.isPresent }
            .filter { oppfoelgingsdialog: Oppfolgingsplan -> oppfoelgingsdialog.godkjentPlan.get().deltMedNAV }
            .collect(Collectors.toList())

        return planer.map { oppfoelgingsdialog2rs(it) }
    }
}
