package no.nav.syfo.config.ws.wsconfig;

import no.nav.syfo.service.ws.*;
import no.nav.tjeneste.virksomhet.aktoer.v2.Aktoer_v2PortType;
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
    public Aktoer_v2PortType aktoerV2() {
        Aktoer_v2PortType port = factory();
        STSClientConfig.configureRequestSamlToken(port);
        return port;
    }

    @SuppressWarnings("unchecked")
    private Aktoer_v2PortType factory() {
        return new WsClient<Aktoer_v2PortType>()
                .createPort(serviceUrl, Aktoer_v2PortType.class, singletonList(new LogErrorHandler()));
    }
}
