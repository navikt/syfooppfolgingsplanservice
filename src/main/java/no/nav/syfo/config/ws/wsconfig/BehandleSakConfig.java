package no.nav.syfo.config.ws.wsconfig;

import no.nav.syfo.service.ws.*;
import no.nav.tjeneste.virksomhet.behandlesak.v1.BehandleSakV1;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.*;

import static java.util.Collections.singletonList;

@Configuration
public class BehandleSakConfig {

    public static final String MOCK_KEY = "behandleSak.withmock";
    @Value("${virksomhet.behandlesak.v1.endpointurl}")
    private String serviceUrl;

    @Bean
    @ConditionalOnProperty(value = MOCK_KEY, havingValue = "false", matchIfMissing = true)
    @Primary
    public BehandleSakV1 behandleSakV1() {
        BehandleSakV1 port = factory();
        STSClientConfig.configureRequestSamlToken(port);
        return port;
    }

    @SuppressWarnings("unchecked")
    private BehandleSakV1 factory() {
        return new WsClient<BehandleSakV1>()
                .createPort(serviceUrl, BehandleSakV1.class, singletonList(new LogErrorHandler()));
    }

}
