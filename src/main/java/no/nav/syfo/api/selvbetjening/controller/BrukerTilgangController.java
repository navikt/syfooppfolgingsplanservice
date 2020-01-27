package no.nav.syfo.api.selvbetjening.controller;

import lombok.extern.slf4j.Slf4j;
import no.nav.security.oidc.context.OIDCRequestContextHolder;
import no.nav.security.spring.oidc.validation.api.ProtectedWithClaims;
import no.nav.syfo.api.selvbetjening.domain.RSTilgang;
import no.nav.syfo.brukertilgang.BrukertilgangConsumer;
import no.nav.syfo.metric.Metrikk;
import no.nav.syfo.service.BrukertilgangService;
import no.nav.syfo.service.PersonService;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.ws.rs.ForbiddenException;

import static no.nav.syfo.oidc.OIDCIssuer.EKSTERN;
import static no.nav.syfo.oidc.OIDCUtil.getSubjectEksternMedThrows;
import static no.nav.syfo.util.HeaderUtil.NAV_PERSONIDENT;

@Slf4j
@RestController
@ProtectedWithClaims(issuer = EKSTERN)
@RequestMapping(value = "/api/tilgang")
public class BrukerTilgangController {

    final static String IKKE_TILGANG_GRUNN_DISKRESJONSMERKET = "kode6-7";

    private final OIDCRequestContextHolder contextHolder;
    private final BrukertilgangConsumer brukertilgangConsumer;
    private final BrukertilgangService brukertilgangService;
    private final PersonService personService;
    private final Metrikk metrikk;

    @Inject
    public BrukerTilgangController(
            OIDCRequestContextHolder contextHolder,
            PersonService personService,
            BrukertilgangConsumer brukertilgangConsumer,
            BrukertilgangService brukertilgangService,
            Metrikk metrikk
    ) {
        this.contextHolder = contextHolder;
        this.brukertilgangConsumer = brukertilgangConsumer;
        this.brukertilgangService = brukertilgangService;
        this.personService = personService;
        this.metrikk = metrikk;
    }

    @GetMapping
    public RSTilgang harTilgang(@RequestParam(value = "fnr", required = false) String oppslaattFnr) {
        String innloggetIdent = getSubjectEksternMedThrows(contextHolder);
        String oppslaattIdent = StringUtils.isEmpty(oppslaattFnr) ? innloggetIdent : oppslaattFnr;

        if (!brukertilgangService.tilgangTilOppslattIdent(innloggetIdent, oppslaattIdent)) {
            log.error("Ikke tilgang: Bruker spør om noen andre enn seg selv eller egne ansatte");
            throw new ForbiddenException();
        }

        metrikk.tellHendelse("sjekk_brukertilgang");

        if (personService.erDiskresjonsmerket(oppslaattIdent)) {
            return new RSTilgang()
                    .harTilgang(false)
                    .ikkeTilgangGrunn(IKKE_TILGANG_GRUNN_DISKRESJONSMERKET);
        }
        return new RSTilgang().harTilgang(true);
    }

    @GetMapping(path = "/ansatt")
    public Boolean accessToAnsatt(@RequestHeader MultiValueMap<String, String> headers) {
        headers.forEach((key, value) -> log.info("JTRACE key {}, value {}", key, value));
        String oppslaattIdent = headers.getFirst(NAV_PERSONIDENT.toLowerCase());

        if (StringUtils.isEmpty(oppslaattIdent)) {
            throw new IllegalArgumentException("Fant ikke Ident i Header ved sjekk av tilgang til Ident");
        } else {
            metrikk.tellHendelse("accessToIdent");

            return brukertilgangConsumer.hasAccessToAnsatt(oppslaattIdent);
        }
    }
}
