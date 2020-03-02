package no.nav.syfo.api.selvbetjening.controller;

import no.nav.security.oidc.api.ProtectedWithClaims;
import no.nav.security.oidc.context.OIDCRequestContextHolder;
import no.nav.syfo.api.selvbetjening.domain.RSKommentar;
import no.nav.syfo.domain.Kommentar;
import no.nav.syfo.metric.Metrikk;
import no.nav.syfo.service.KommentarService;
import no.nav.syfo.service.TiltakService;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.util.function.Function;

import static no.nav.syfo.oidc.OIDCIssuer.EKSTERN;
import static no.nav.syfo.oidc.OIDCUtil.getSubjectEksternMedThrows;
import static no.nav.syfo.util.MapUtil.map;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@ProtectedWithClaims(issuer = EKSTERN)
@RequestMapping(value = "/api/tiltak/actions/{id}")
public class TiltakController {

    private final OIDCRequestContextHolder contextHolder;
    private final KommentarService kommentarService;
    private final TiltakService tiltakService;
    private final Metrikk metrikk;

    @Inject
    public TiltakController(
            OIDCRequestContextHolder contextHolder,
            KommentarService kommentarService,
            TiltakService tiltakService,
            Metrikk metrikk
    ) {
        this.contextHolder = contextHolder;
        this.kommentarService = kommentarService;
        this.tiltakService = tiltakService;
        this.metrikk = metrikk;
    }

    @PostMapping(path = "/slett")
    public void slettTiltak(@PathVariable("id") Long id) {
        String innloggetIdent = getSubjectEksternMedThrows(contextHolder);

        tiltakService.slettTiltak(id, innloggetIdent);

        metrikk.tellHendelse("slett_tiltak");
    }

    @PostMapping(path = "/lagreKommentar", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public long lagreKommentar(
            @PathVariable("id") Long id,
            @RequestBody RSKommentar rsKommentar) {
        String innloggetIdent = getSubjectEksternMedThrows(contextHolder);

        Kommentar kommentar = map(rsKommentar, rsKommentar2kommentar);

        long kommentarId = kommentarService.lagreKommentar(id, kommentar, innloggetIdent);

        metrikk.tellHendelse("lagre_kommentar");

        return kommentarId;
    }

    public static Function<RSKommentar, Kommentar> rsKommentar2kommentar = rsKommentar -> new Kommentar()
            .id(rsKommentar.id)
            .tekst(rsKommentar.tekst);
}
