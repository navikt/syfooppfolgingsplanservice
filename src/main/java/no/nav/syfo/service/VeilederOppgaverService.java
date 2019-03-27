package no.nav.syfo.service;

import no.nav.syfo.model.VeilederOppgave;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import java.util.List;

import static javax.ws.rs.client.ClientBuilder.newClient;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static no.nav.syfo.util.RestUtils.basicCredentials;

@Service
public class VeilederOppgaverService {
    private final Client client = newClient();

    @Value("${syfoveilederoppgaver.system.v1.url}")
    private String syfoveilederoppgaveUrl;

    @Value("${syfoveilederoppgaver.systemapi.username}")
    private String syfoveilederoppgaveUsername;

    @Value("${syfoveilederoppgaver.systemapi.password}")
    private String syfoveilederoppgavePassword;

    public List<VeilederOppgave> get(String fnr) {
        return client.target(syfoveilederoppgaveUrl)
                .queryParam("fnr", fnr)
                .request(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, basicCredentials(syfoveilederoppgaveUsername, syfoveilederoppgavePassword))
                .get(new GenericType<List<VeilederOppgave>>() {
                });
    }
}
