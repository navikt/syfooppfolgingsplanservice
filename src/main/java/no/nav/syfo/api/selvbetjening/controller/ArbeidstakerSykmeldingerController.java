package no.nav.syfo.api.selvbetjening.controller;

import no.nav.security.token.support.core.api.ProtectedWithClaims;
import no.nav.security.token.support.core.context.TokenValidationContextHolder;
import no.nav.syfo.aktorregister.AktorregisterConsumer;
import no.nav.syfo.metric.Metrikk;
import no.nav.syfo.model.Sykmelding;
import no.nav.syfo.tokenx.tokendings.TokenDingsConsumer;
import no.nav.syfo.sykmeldinger.ArbeidstakerSykmeldingerConsumer;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

import static no.nav.syfo.oidc.OIDCIssuer.EKSTERN;
import static no.nav.syfo.oidc.OIDCUtil.getIssuerToken;
import static no.nav.syfo.oidc.OIDCUtil.getSubjectEksternMedThrows;
import static org.slf4j.LoggerFactory.getLogger;

@RestController
@ProtectedWithClaims(issuer = EKSTERN)
@RequestMapping(value = "/api/arbeidstaker/sykmeldinger")
public class ArbeidstakerSykmeldingerController {

    private final TokenValidationContextHolder oidcContextHolder;
    private final Metrikk metrikk;
    private final AktorregisterConsumer aktorregisterConsumer;
    private final ArbeidstakerSykmeldingerConsumer arbeidstakerSykmeldingerConsumer;
    private final TokenDingsConsumer tokenDingsConsumer;
    @Value("${syfosmregister.id}")
    private String targetApp;

    @Inject
    public ArbeidstakerSykmeldingerController(
            TokenValidationContextHolder oidcContextHolder,
            Metrikk metrikk,
            AktorregisterConsumer aktorregisterConsumer,
            ArbeidstakerSykmeldingerConsumer arbeidstakerSykmeldingerConsumer,
            TokenDingsConsumer tokenDingsConsumer) {
        this.oidcContextHolder = oidcContextHolder;
        this.metrikk = metrikk;
        this.aktorregisterConsumer = aktorregisterConsumer;
        this.arbeidstakerSykmeldingerConsumer = arbeidstakerSykmeldingerConsumer;
        this.tokenDingsConsumer = tokenDingsConsumer;
    }

    @ResponseBody
    @GetMapping
    public ResponseEntity<List<Sykmelding>> getSendteSykmeldinger(@RequestParam(required = false) String today) {
        metrikk.tellHendelse("get_sykmeldinger");

        String issuerToken = getIssuerToken(oidcContextHolder, EKSTERN);

        String exchangedToken = tokenDingsConsumer.exchangeToken(issuerToken, targetApp);
        String bearerToken = "Bearer " + exchangedToken;

        String innloggetIdent = getSubjectEksternMedThrows(oidcContextHolder);
        String oppslattIdentAktorId = aktorregisterConsumer.hentAktorIdForFnr(innloggetIdent);

        final boolean isTodayPresent = Boolean.parseBoolean(today);

        Optional<List<Sykmelding>> sendteSykmeldinger = arbeidstakerSykmeldingerConsumer.getSendteSykmeldinger(oppslattIdentAktorId, bearerToken, isTodayPresent);

        return sendteSykmeldinger.map(sykmeldinger -> ResponseEntity
                .status(HttpStatus.OK)
                .body(sykmeldinger)).orElseGet(() -> ResponseEntity
                .status(HttpStatus.OK)
                .body(List.of()));
    }
}
