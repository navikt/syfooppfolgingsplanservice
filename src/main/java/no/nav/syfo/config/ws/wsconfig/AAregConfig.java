package no.nav.syfo.config.ws.wsconfig;

import no.nav.syfo.service.ws.*;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.binding.ArbeidsforholdV3;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.*;

import static java.util.Collections.singletonList;

@Configuration
public class AAregConfig {

    public static final String MOCK_KEY = "arbeidsforhold.aareg.withmock";
    @Value("${virksomhet.arbeidsforhold.v3.endpointurl}")
    private String serviceUrl;

    @Bean
    @ConditionalOnProperty(value = MOCK_KEY, havingValue = "false", matchIfMissing = true)
    @Primary
    public ArbeidsforholdV3 arbeidsforholdV3() {
        ArbeidsforholdV3 port = factory();
        STSClientConfig.configureRequestSamlToken(port);
        return port;
    }

    @SuppressWarnings("unchecked")
    private ArbeidsforholdV3 factory() {
        return new WsClient<ArbeidsforholdV3>()
                .createPort(serviceUrl, ArbeidsforholdV3.class, singletonList(new LogErrorHandler()));
    }
}
