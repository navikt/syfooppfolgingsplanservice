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
import no.nav.syfo.model.Sykmelding;
import no.nav.syfo.service.BrukertilgangService;
import no.nav.syfo.sykmeldinger.SykmeldingerConsumer;

@RestController
@ProtectedWithClaims(issuer = EKSTERN)
@RequestMapping(value = "/api/sykmeldinger")
public class SykmeldingerController {
    private static final Logger LOG = getLogger(SykmeldingerController.class);

    private final TokenValidationContextHolder oidcContextHolder;
    private final Metrikk metrikk;
    private final AktorregisterConsumer aktorregisterConsumer;
    private final BrukertilgangService brukertilgangService;
    private final SykmeldingerConsumer sykmeldingerConsumer;

    @Inject
    public SykmeldingerController(
            TokenValidationContextHolder oidcContextHolder,
            Metrikk metrikk,
            AktorregisterConsumer aktorregisterConsumer,
            BrukertilgangService brukertilgangService,
            SykmeldingerConsumer sykmeldingerConsumer
    ) {
        this.oidcContextHolder = oidcContextHolder;
        this.metrikk = metrikk;
        this.aktorregisterConsumer = aktorregisterConsumer;
        this.brukertilgangService = brukertilgangService;
        this.sykmeldingerConsumer = sykmeldingerConsumer;
    }


    @ResponseBody
    @GetMapping
    public ResponseEntity<List<Sykmelding>> getSendteSykmeldinger(@RequestHeader MultiValueMap<String, String> headers) {
        metrikk.tellHendelse("get_sykmeldinger");

        String oppslaattIdent = headers.getFirst(NAV_PERSONIDENT_HEADER.toLowerCase());

        if (StringUtils.isEmpty(oppslaattIdent)) {
            LOG.error("Fant ikke oppslaatt ident ved henting av sykmeldinger for ident");
            throw new IllegalArgumentException("Fant ikke Ident i Header ved henting av sykmeldinger for ident");
        } else {
            String innloggetIdent = getSubjectEksternMedThrows(oidcContextHolder);
            if (!brukertilgangService.tilgangTilOppslattIdent(innloggetIdent, oppslaattIdent)) {
                LOG.error("Ikke tilgang til sykmeldinger: Bruker spør om noen andre enn seg selv eller egne ansatte");
                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .build();
            }
            String oppslattIdentAktorId = aktorregisterConsumer.hentAktorIdForFnr(oppslaattIdent);

            Optional<List<Sykmelding>> sendteSykmeldinger = sykmeldingerConsumer.getSendteSykmeldinger(oppslattIdentAktorId);

            return sendteSykmeldinger.map(sykmeldinger -> ResponseEntity
                    .status(HttpStatus.OK)
                    .body(sykmeldinger)).orElseGet(() -> ResponseEntity
                    .status(HttpStatus.OK)
                    .body(List.of()));
        }
    }
}
