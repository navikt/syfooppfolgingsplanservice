package no.nav.syfo.config.ws.wsconfig;

import no.nav.syfo.service.ws.*;
import no.nav.tjeneste.virksomhet.aktoer.v2.AktoerV2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.*;

import static java.util.Collections.singletonList;

@Configuration
public class AktoerConfig {

    public static final String MOCK_KEY = "aktoer.withmock";
    @Value("${aktoer.v2.endpointurl}")
    private String serviceUrl;

    @Bean
    @ConditionalOnProperty(value = MOCK_KEY, havingValue = "false", matchIfMissing = true)
    @Primary
    public AktoerV2 aktoerV2() {
        AktoerV2 port = factory();
        STSClientConfig.configureRequestSamlToken(port);
        return port;
    }

    @SuppressWarnings("unchecked")
    private AktoerV2 factory() {
        return new WsClient<AktoerV2>()
                .createPort(serviceUrl, AktoerV2.class, singletonList(new LogErrorHandler()));
    }
}
