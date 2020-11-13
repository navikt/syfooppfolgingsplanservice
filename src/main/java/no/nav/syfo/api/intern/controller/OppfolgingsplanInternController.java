package no.nav.syfo.api.intern.controller;

import no.nav.security.token.support.core.api.ProtectedWithClaims;
import no.nav.syfo.aktorregister.AktorregisterConsumer;
import no.nav.syfo.api.intern.domain.*;
import no.nav.syfo.domain.*;
import no.nav.syfo.ereg.EregConsumer;
import no.nav.syfo.repository.dao.OppfolgingsplanDAO;
import no.nav.syfo.service.*;
import no.nav.syfo.veiledertilgang.VeilederTilgangConsumer;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static no.nav.syfo.api.intern.mappers.OppfoelgingsdialogRestMapper.oppfoelgingsdialog2rs;
import static no.nav.syfo.oidc.OIDCIssuer.AZURE;
import static no.nav.syfo.util.MapUtil.mapListe;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@ProtectedWithClaims(issuer = AZURE)
@RequestMapping(value = "/api/internad/v1/oppfolgingsplan/{fnr}")
public class OppfolgingsplanInternController {

    private AktorregisterConsumer aktorregisterConsumer;
    private BrukerprofilService brukerprofilService;
    private EregConsumer eregConsumer;
    private OppfolgingsplanDAO oppfolgingsplanDAO;
    private VeilederTilgangConsumer veilederTilgangConsumer;

    @Inject
    public OppfolgingsplanInternController(
            final AktorregisterConsumer aktorregisterConsumer,
            final BrukerprofilService brukerprofilService,
            final EregConsumer eregConsumer,
            final OppfolgingsplanDAO oppfolgingsplanDAO,
            final VeilederTilgangConsumer veilederTilgangConsumer
    ) {
        this.aktorregisterConsumer = aktorregisterConsumer;
        this.brukerprofilService = brukerprofilService;
        this.eregConsumer = eregConsumer;
        this.oppfolgingsplanDAO = oppfolgingsplanDAO;
        this.veilederTilgangConsumer = veilederTilgangConsumer;
    }

    private String finnDeltAvNavn(Oppfolgingsplan oppfolgingsplan) {
        if (oppfolgingsplan.sistEndretAvAktoerId.equals(oppfolgingsplan.arbeidstaker.aktoerId)) {
            return brukerprofilService.hentNavnByAktoerId(oppfolgingsplan.sistEndretAvAktoerId);
        }
        return eregConsumer.virksomhetsnavn(oppfolgingsplan.virksomhet.virksomhetsnummer);
    }

    @GetMapping(produces = APPLICATION_JSON_VALUE)
    @RequestMapping(value = "/historikk")
    public List<RSHistorikk> getHistorikk(@PathVariable("fnr") String fnr) {
        Fodselsnummer personFnr = new Fodselsnummer(fnr);

        veilederTilgangConsumer.throwExceptionIfVeilederWithoutAccess(personFnr);

        List<Oppfolgingsplan> oppfoelgingsplaner = oppfolgingsplanDAO.oppfolgingsplanerKnyttetTilSykmeldt(aktorregisterConsumer.hentAktorIdForFnr(personFnr.getValue()))
                .stream()
                .map(oppfoelgingsdialog -> oppfolgingsplanDAO.populate(oppfoelgingsdialog))
                .filter(oppfoelgingsdialog -> oppfoelgingsdialog.godkjentPlan.isPresent())
                .filter(oppfoelgingsdialog -> oppfoelgingsdialog.godkjentPlan.get().deltMedNAV)
                .collect(toList());

        List<RSHistorikk> opprettetHistorikk = mapListe(
                oppfoelgingsplaner,
                oppfoelgingsdialog -> new RSHistorikk()
                        .tekst("Oppfølgingsplanen ble delt med NAV av " + finnDeltAvNavn(oppfoelgingsdialog) + ".")
                        .tidspunkt(oppfoelgingsdialog.godkjentPlan.get().deltMedNAVTidspunkt)
        );
        return opprettetHistorikk;
    }

    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public List<RSOppfoelgingsdialog> getOppfolgingsplaner(@PathVariable("fnr") String fnr) {
        Fodselsnummer personFnr = new Fodselsnummer(fnr);

        veilederTilgangConsumer.throwExceptionIfVeilederWithoutAccess(personFnr);

        return mapListe(oppfolgingsplanDAO.oppfolgingsplanerKnyttetTilSykmeldt(aktorregisterConsumer.hentAktorIdForFnr(personFnr.getValue()))
                        .stream()
                        .map(oppfoelgingsdialog -> oppfolgingsplanDAO.populate(oppfoelgingsdialog))
                        .filter(oppfoelgingsdialog -> oppfoelgingsdialog.godkjentPlan.isPresent())
                        .filter(oppfoelgingsdialog -> oppfoelgingsdialog.godkjentPlan.get().deltMedNAV)
                        .collect(toList()),
                oppfoelgingsdialog2rs);
    }
}
