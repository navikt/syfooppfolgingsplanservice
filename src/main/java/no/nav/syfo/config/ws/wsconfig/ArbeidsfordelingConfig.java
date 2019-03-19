package no.nav.syfo.config.ws.wsconfig;

import no.nav.syfo.service.ws.*;
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.ArbeidsfordelingV1;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.*;

import static java.util.Collections.singletonList;

@Configuration
public class ArbeidsfordelingConfig {

    public static final String MOCK_KEY = "arbeidsfordeling.withmock";
    @Value("${virksomhet.arbeidsfordeling.v1.endpointurl}")
    private String serviceUrl;

    @Bean
    @ConditionalOnProperty(value = MOCK_KEY, havingValue = "false", matchIfMissing = true)
    @Primary
    public ArbeidsfordelingV1 arbeidsfordelingV1() {
        ArbeidsfordelingV1 port = factory();
        STSClientConfig.configureRequestSamlToken(port);
        return port;
    }

    @SuppressWarnings("unchecked")
    private ArbeidsfordelingV1 factory() {
        return new WsClient<ArbeidsfordelingV1>()
                .createPort(serviceUrl, ArbeidsfordelingV1.class, singletonList(new LogErrorHandler()));
    }
}
