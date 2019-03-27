package no.nav.syfo.config.ws.wsconfig;

import no.nav.syfo.service.ws.*;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.DigitalKontaktinformasjonV1;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.*;

import static java.util.Collections.singletonList;

@Configuration
public class DKIFConfig {

    public static final String MOCK_KEY = "dkif.withmock";
    @Value("${virksomhet.digitalkontakinformasjon.v1.endpointurl}")
    private String serviceUrl;

    @Bean
    @ConditionalOnProperty(value = MOCK_KEY, havingValue = "false", matchIfMissing = true)
    @Primary
    public DigitalKontaktinformasjonV1 digitalKontaktinformasjonV1() {
        DigitalKontaktinformasjonV1 port = factory();
        STSClientConfig.configureRequestSamlToken(port);
        return port;
    }

    @SuppressWarnings("unchecked")
    private DigitalKontaktinformasjonV1 factory() {
        return new WsClient<DigitalKontaktinformasjonV1>()
                .createPort(serviceUrl, DigitalKontaktinformasjonV1.class, singletonList(new LogErrorHandler()));
    }

}
