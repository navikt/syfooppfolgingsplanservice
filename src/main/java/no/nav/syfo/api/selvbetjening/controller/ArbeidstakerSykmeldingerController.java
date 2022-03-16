package no.nav.syfo.api.selvbetjening.controller;

import no.nav.security.token.support.core.api.ProtectedWithClaims;
import no.nav.security.token.support.core.context.TokenValidationContextHolder;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

import static no.nav.syfo.oidc.OIDCIssuer.EKSTERN;
import static no.nav.syfo.oidc.OIDCUtil.getSubjectEksternMedThrows;
import static no.nav.syfo.oidc.OIDCUtil.getIssuerToken;
import static org.slf4j.LoggerFactory.getLogger;

import no.nav.syfo.aktorregister.AktorregisterConsumer;
import no.nav.syfo.metric.Metrikk;
import no.nav.syfo.model.Sykmelding;
import no.nav.syfo.service.BrukertilgangService;
import no.nav.syfo.sykmeldinger.ArbeidstakerSykmeldingerConsumer;

@RestController
@ProtectedWithClaims(issuer = EKSTERN)
@RequestMapping(value = "/api/arbeidstaker/sykmeldinger")
public class ArbeidstakerSykmeldingerController {
    private static final Logger LOG = getLogger(ArbeidstakerSykmeldingerController.class);

    private final TokenValidationContextHolder contextHolder;
    private final Metrikk metrikk;
    private final AktorregisterConsumer aktorregisterConsumer;
    private final BrukertilgangService brukertilgangService;
    private final ArbeidstakerSykmeldingerConsumer arbeidstakerSykmeldingerConsumer;

    @Inject
    public ArbeidstakerSykmeldingerController(
            TokenValidationContextHolder contextHolder,
            Metrikk metrikk,
            AktorregisterConsumer aktorregisterConsumer,
            BrukertilgangService brukertilgangService,
            ArbeidstakerSykmeldingerConsumer arbeidstakerSykmeldingerConsumer
    ) {
        this.contextHolder = contextHolder;
        this.metrikk = metrikk;
        this.aktorregisterConsumer = aktorregisterConsumer;
        this.brukertilgangService = brukertilgangService;
        this.arbeidstakerSykmeldingerConsumer = arbeidstakerSykmeldingerConsumer;
    }

    @ResponseBody
    @GetMapping
    public ResponseEntity<List<Sykmelding>> getSendteSykmeldinger(@RequestParam(required = false) String today) {
        metrikk.tellHendelse("get_sykmeldinger");
        String idToken = getIssuerToken(contextHolder, EKSTERN);
        String innloggetIdent = getSubjectEksternMedThrows(contextHolder);
        String oppslattIdentAktorId = aktorregisterConsumer.hentAktorIdForFnr(innloggetIdent);

        final boolean isTodayPresent = Boolean.parseBoolean(today);

        Optional<List<Sykmelding>> sendteSykmeldinger = arbeidstakerSykmeldingerConsumer.getSendteSykmeldinger(oppslattIdentAktorId, idToken, isTodayPresent);

        return sendteSykmeldinger.map(sykmeldinger -> ResponseEntity
                .status(HttpStatus.OK)
                .body(sykmeldinger)).orElseGet(() -> ResponseEntity
                .status(HttpStatus.OK)
                .body(List.of()));
    }
}
