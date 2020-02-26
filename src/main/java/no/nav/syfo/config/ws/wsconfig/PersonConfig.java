package no.nav.syfo.config.ws.wsconfig;

import no.nav.syfo.service.ws.*;
import no.nav.tjeneste.virksomhet.person.v3.binding.PersonV3;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.*;

import static java.util.Collections.singletonList;

@Configuration
public class PersonConfig {

    public static final String MOCK_KEY = "person.withmock";

    @SuppressWarnings("unchecked")
    @Bean
    @ConditionalOnProperty(value = MOCK_KEY, havingValue = "false", matchIfMissing = true)
    @Primary
    public PersonV3 personV3(@Value("${virksomhet.person.v3.endpointurl}") String serviceUrl) {
        PersonV3 port = new WsClient<PersonV3>().createPort(serviceUrl, PersonV3.class, singletonList(new LogErrorHandler()));
        STSClientConfig.configureRequestSamlToken(port);
        return port;
    }
}
