package no.nav.syfo.config.ws.wsconfig;

import no.nav.syfo.service.ws.*;
import no.nav.tjeneste.virksomhet.sak.v1.SakV1;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.*;

import static java.util.Collections.singletonList;

@Configuration
public class SakConfig {

    public static final String MOCK_KEY = "sak.withmock";
    @Value("${virksomhet.sak.v1.endpointurl}")
    private String serviceUrl;

    @Bean
    @ConditionalOnProperty(value = MOCK_KEY, havingValue = "false", matchIfMissing = true)
    @Primary
    public SakV1 sakV1() {
        SakV1 port = factory();
        STSClientConfig.configureRequestSamlToken(port);
        return port;
    }

    @SuppressWarnings("unchecked")
    private SakV1 factory() {
        return new WsClient<SakV1>()
                .createPort(serviceUrl, SakV1.class, singletonList(new LogErrorHandler()));
    }
}
