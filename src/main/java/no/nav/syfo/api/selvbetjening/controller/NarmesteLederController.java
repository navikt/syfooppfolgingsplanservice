package no.nav.syfo.api.selvbetjening.controller;

import no.nav.security.oidc.context.OIDCRequestContextHolder;
import no.nav.security.spring.oidc.validation.api.ProtectedWithClaims;
import no.nav.syfo.aktorregister.AktorregisterConsumer;
import no.nav.syfo.metric.Metrikk;
import no.nav.syfo.model.Naermesteleder;
import no.nav.syfo.narmesteleder.NarmesteLederConsumer;
import no.nav.syfo.service.BrukertilgangService;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.util.Optional;

import static no.nav.syfo.oidc.OIDCIssuer.EKSTERN;
import static no.nav.syfo.oidc.OIDCUtil.getSubjectEksternMedThrows;
import static no.nav.syfo.util.HeaderUtil.NAV_PERSONIDENT;
import static org.slf4j.LoggerFactory.getLogger;

@RestController
@ProtectedWithClaims(issuer = EKSTERN)
@RequestMapping(value = "/api/narmesteleder")
public class NarmesteLederController {

    private static final Logger LOG = getLogger(NarmesteLederController.class);

    private final OIDCRequestContextHolder oidcContextHolder;
    private final Metrikk metrikk;
    private final AktorregisterConsumer aktorregisterConsumer;
    private final BrukertilgangService brukertilgangService;
    private final NarmesteLederConsumer narmesteLederConsumer;

    @Inject
    public NarmesteLederController(
            OIDCRequestContextHolder oidcContextHolder,
            Metrikk metrikk,
            AktorregisterConsumer aktorregisterConsumer,
            BrukertilgangService brukertilgangService,
            NarmesteLederConsumer narmesteLederConsumer
    ) {
        this.oidcContextHolder = oidcContextHolder;
        this.metrikk = metrikk;
        this.aktorregisterConsumer = aktorregisterConsumer;
        this.brukertilgangService = brukertilgangService;
        this.narmesteLederConsumer = narmesteLederConsumer;
    }

    @ResponseBody
    @GetMapping(path = "/{virksomhetsnummer}")
    public ResponseEntity<Naermesteleder> getNarmesteLeder(
            @RequestHeader MultiValueMap<String, String> headers,
            @PathVariable("virksomhetsnummer") String virksomhetsnummer
    ) {
        metrikk.tellHendelse("get_narmesteleder");

        String oppslaattIdent = headers.getFirst(NAV_PERSONIDENT.toLowerCase());

        if (StringUtils.isEmpty(oppslaattIdent)) {
            LOG.error("Fant ikke oppslaatt Ident ved henting av narmeste leder for Ident");
            throw new IllegalArgumentException("Fant ikke Ident i Header ved henting av naermeste leder for ident");
        } else {
            String innloggetIdent = getSubjectEksternMedThrows(oidcContextHolder);
            if (!brukertilgangService.tilgangTilOppslattIdent(innloggetIdent, oppslaattIdent)) {
                LOG.error("Ikke tilgang til naermeste leder: Bruker spør om noen andre enn seg selv eller egne ansatte");
                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .build();
            }
            String oppslattIdentAktorId = aktorregisterConsumer.hentAktorIdForFnr(oppslaattIdent);

            Optional<Naermesteleder> narmesteLeder = narmesteLederConsumer.narmesteLeder(oppslattIdentAktorId, virksomhetsnummer);
            if (narmesteLeder.isPresent()) {
                return ResponseEntity
                        .status(HttpStatus.OK)
                        .body(narmesteLeder.get());
            } else {
                metrikk.tellHendelse("get_narmesteleder_no_content");
                LOG.error("Fant ikke naermeste leder for oppslaatt Ident");
                return ResponseEntity
                        .status(HttpStatus.NO_CONTENT)
                        .build();
            }
        }
    }
}
