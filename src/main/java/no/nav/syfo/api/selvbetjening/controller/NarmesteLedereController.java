package no.nav.syfo.api.selvbetjening.controller;

import no.nav.security.token.support.core.api.ProtectedWithClaims;
import no.nav.security.token.support.core.context.TokenValidationContextHolder;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

import static no.nav.syfo.oidc.OIDCIssuer.EKSTERN;
import static no.nav.syfo.oidc.OIDCUtil.getSubjectEksternMedThrows;
import static no.nav.syfo.util.RequestUtilKt.NAV_PERSONIDENT_HEADER;
import static org.slf4j.LoggerFactory.getLogger;

import no.nav.syfo.aktorregister.AktorregisterConsumer;
import no.nav.syfo.metric.Metrikk;
import no.nav.syfo.model.Naermesteleder;
import no.nav.syfo.narmesteleder.NarmesteLedereConsumer;
import no.nav.syfo.service.BrukertilgangService;

@RestController
@ProtectedWithClaims(issuer = EKSTERN)
@RequestMapping(value = "/api/narmesteledere")
public class NarmesteLedereController {

    private static final Logger LOG = getLogger(NarmesteLedereController.class);

    private final TokenValidationContextHolder oidcContextHolder;
    private final Metrikk metrikk;
    private final AktorregisterConsumer aktorregisterConsumer;
    private final BrukertilgangService brukertilgangService;
    private final NarmesteLedereConsumer narmesteLedereConsumer;

    @Inject
    public NarmesteLedereController(
            TokenValidationContextHolder oidcContextHolder,
            Metrikk metrikk,
            AktorregisterConsumer aktorregisterConsumer,
            BrukertilgangService brukertilgangService,
            NarmesteLedereConsumer narmesteLedereConsumer
    ) {
        this.oidcContextHolder = oidcContextHolder;
        this.metrikk = metrikk;
        this.aktorregisterConsumer = aktorregisterConsumer;
        this.brukertilgangService = brukertilgangService;
        this.narmesteLedereConsumer = narmesteLedereConsumer;
    }

    @ResponseBody
    @GetMapping
    public ResponseEntity<List<Naermesteleder>> getNarmesteLedere(
            @RequestHeader MultiValueMap<String, String> headers
    ) {
        metrikk.tellHendelse("get_narmesteledere");

        String oppslaattIdent = headers.getFirst(NAV_PERSONIDENT_HEADER.toLowerCase());

        if (StringUtils.isEmpty(oppslaattIdent)) {
            LOG.error("Fant ikke oppslaatt Ident ved henting av narmeste ledere for Ident");
            throw new IllegalArgumentException("Fant ikke Ident i Header ved henting av naermeste ledere for ident");
        } else {
            String innloggetIdent = getSubjectEksternMedThrows(oidcContextHolder);
            if (!brukertilgangService.tilgangTilOppslattIdent(innloggetIdent, oppslaattIdent)) {
                LOG.error("Ikke tilgang til naermeste ledere: Bruker spør om noen andre enn seg selv eller egne ansatte");
                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .build();
            }

            Optional<List<Naermesteleder>> narmesteLeder = narmesteLedereConsumer.narmesteLedere(oppslaattIdent);
            if (narmesteLeder.isPresent()) {
                return ResponseEntity
                        .status(HttpStatus.OK)
                        .body(narmesteLeder.get());
            } else {
                metrikk.tellHendelse("get_narmesteledere_no_content");
                return ResponseEntity
                        .status(HttpStatus.NO_CONTENT)
                        .build();
            }
        }
    }
}
