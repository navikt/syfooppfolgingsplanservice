package no.nav.syfo.service;

import no.nav.common.auth.SsoToken;
import no.nav.common.auth.SubjectHandler;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static java.lang.System.getProperty;
import static javax.ws.rs.client.ClientBuilder.newClient;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;

@Component
public class TilgangsKontroll {

    private static final String TILGANGSKONTROLL_API_NOKKEL = "TILGANGSKONTROLLAPI_URL";
    private static final String TILGANG_TIL_BRUKER_PATH = "/tilgangtilbruker";
    private static final String TILGANG_TIL_ENHETPATH = "/tilgangtilenhet";
    private static final String TILGANG_TIL_TJENESTEN = "/tilgangtiltjenesten";

    private final Client client = newClient();

    @Cacheable(value = "tilgang", keyGenerator = "userkeygenerator")
    public void sjekkTilgangTilPerson(String fnr) {
        Response response = client.target(hentUrl(TILGANG_TIL_BRUKER_PATH))
                .queryParam("fnr", fnr)
                .request(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, hentToken())
                .get();

        hentStatus(response, "Brukeren har ikke tilgang til denne personen");
    }

    @Cacheable(value = "tilgang", keyGenerator = "userkeygenerator")
    public void sjekkTilgangTilEnhet(String enhet) {
        Response response = client.target(hentUrl(TILGANG_TIL_ENHETPATH))
                .queryParam("enhet", enhet)
                .request(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, hentToken())
                .get();
        hentStatus(response, "Brukeren har ikke tilgang til følgende enhet: " + enhet);
    }

    @Cacheable(value = "tilgang", keyGenerator = "userkeygenerator")
    public void sjekkTilgangTilTjenesten() {
        Response response = client.target(hentUrl(TILGANG_TIL_TJENESTEN))
                .request(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, hentToken())
                .get();
        hentStatus(response, "Brukeren har ikke tilgang til denne tjenesten");
    }

    private void hentStatus(Response response, String feilMelding) {
        if (200 != response.getStatus()) {
            if (403 == response.getStatus()) {
                throw new ForbiddenException(feilMelding);
            } else {
                throw new WebApplicationException(response);
            }
        }
    }

    private String hentToken() {
        return "Bearer " + SubjectHandler.getSsoToken(SsoToken.Type.OIDC)
                .orElseThrow((() -> new NotAuthorizedException("Finner ikke token")));
    }

    private String hentUrl(String sluttUrl) {
        return getProperty(TILGANGSKONTROLL_API_NOKKEL) + sluttUrl;
    }
}
