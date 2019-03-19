package no.nav.syfo.service;

import no.nav.syfo.model.VeilederOppgave;
import org.springframework.stereotype.Service;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import java.util.List;

import static java.lang.System.getProperty;
import static javax.ws.rs.client.ClientBuilder.newClient;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static no.nav.syfo.util.RestUtils.basicCredentials;

@Service
public class VeilederOppgaverService {
    private final Client client = newClient();

    private final String SYFOVEILEDEROPPGAVER_SYSTEM_V1 = "SYFOVEILEDEROPPGAVER_SYSTEM_V1_URL";
    private final String SYFOVEILEDEROPPGAVER_SYSTEMAPI = "SYFOVEILEDEROPPGAVER_SYSTEMAPI";

    public List<VeilederOppgave> get(String fnr) {
        return client.target(getProperty(SYFOVEILEDEROPPGAVER_SYSTEM_V1))
                .queryParam("fnr", fnr)
                .request(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, basicCredentials(SYFOVEILEDEROPPGAVER_SYSTEMAPI))
                .get(new GenericType<List<VeilederOppgave>>() {
                });
    }
}
