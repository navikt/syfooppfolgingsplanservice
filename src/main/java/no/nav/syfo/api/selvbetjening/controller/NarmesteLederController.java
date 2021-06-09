package no.nav.syfo.api.selvbetjening.controller;

import no.nav.security.token.support.core.api.ProtectedWithClaims;
import no.nav.security.token.support.core.context.TokenValidationContextHolder;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.util.Optional;

import static no.nav.syfo.oidc.OIDCIssuer.EKSTERN;
import static no.nav.syfo.oidc.OIDCUtil.getSubjectEksternMedThrows;
import static no.nav.syfo.util.RequestUtilKt.NAV_PERSONIDENT_HEADER;
import static org.slf4j.LoggerFactory.getLogger;

import no.nav.syfo.metric.Metrikk;
import no.nav.syfo.model.Naermesteleder;
import no.nav.syfo.narmesteleder.NarmesteLederConsumer;
import no.nav.syfo.service.BrukertilgangService;

@RestController
@ProtectedWithClaims(issuer = EKSTERN)
@RequestMapping(value = "/api/narmesteleder")
public class NarmesteLederController {

    private static final Logger LOG = getLogger(NarmesteLederController.class);

    private final TokenValidationContextHolder oidcContextHolder;
    private final Metrikk metrikk;
    private final BrukertilgangService brukertilgangService;
    private final NarmesteLederConsumer narmesteLederConsumer;

    @Inject
    public NarmesteLederController(
            TokenValidationContextHolder oidcContextHolder,
            Metrikk metrikk,
            BrukertilgangService brukertilgangService,
            NarmesteLederConsumer narmesteLederConsumer
    ) {
        this.oidcContextHolder = oidcContextHolder;
        this.metrikk = metrikk;
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

        String oppslaattIdent = headers.getFirst(NAV_PERSONIDENT_HEADER.toLowerCase());

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

            Optional<Naermesteleder> narmesteLeder = narmesteLederConsumer.narmesteLeder(oppslaattIdent, virksomhetsnummer);
            if (narmesteLeder.isPresent()) {
                return ResponseEntity
                        .status(HttpStatus.OK)
                        .body(narmesteLeder.get());
            } else {
                metrikk.tellHendelse("get_narmesteleder_no_content");
                return ResponseEntity
                        .status(HttpStatus.NO_CONTENT)
                        .build();
            }
        }
    }
}
