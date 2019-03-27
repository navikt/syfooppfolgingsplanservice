package no.nav.syfo.config.ws.wsconfig;

import no.nav.syfo.service.ws.*;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.BehandleJournalV2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.*;

import static java.util.Collections.singletonList;

@Configuration
public class BehandleJournalConfig {

    public static final String MOCK_KEY = "behandlejournalv2.withmock";
    @Value("${behandlejournal.v2.endpointurl}")
    private String serviceUrl;

    @Bean
    @ConditionalOnProperty(value = MOCK_KEY, havingValue = "false", matchIfMissing = true)
    @Primary
    public BehandleJournalV2 behandleJournalV2() {
        BehandleJournalV2 port = factory();
        STSClientConfig.configureRequestSamlToken(port);
        return port;
    }

    @SuppressWarnings("unchecked")
    private BehandleJournalV2 factory() {
        return new WsClient<BehandleJournalV2>()
                .createPort(serviceUrl, BehandleJournalV2.class, singletonList(new LogErrorHandler()));
    }
}
