package no.nav.syfo.localconfig;

import no.nav.security.spring.oidc.test.TokenGeneratorConfiguration;
import no.nav.syfo.util.ToggleUtil;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static no.nav.syfo.util.PropertyUtil.FASIT_ENVIRONMENT_NAME;

@Configuration
@Import(TokenGeneratorConfiguration.class)
public class LocalApplicationConfig {

    public LocalApplicationConfig(Environment environment) {
        System.setProperty("SECURITYTOKENSERVICE_URL", requireNonNull(environment.getProperty("securitytokenservice.url")));
        System.setProperty("SRVSYFOOPPFOLGINGSPLANSERVICE_USERNAME", requireNonNull(environment.getProperty("srvsyfooppfolgingsplanservice.username")));
        System.setProperty("SRVSYFOOPPFOLGINGSPLANSERVICE_PASSWORD", requireNonNull(environment.getProperty("srvsyfooppfolgingsplanservice.password")));

        System.setProperty(FASIT_ENVIRONMENT_NAME, ToggleUtil.ENVIRONMENT_MODE.q1.name());

        System.setProperty("VARSELPRODUKSJON_VARSLINGER_QUEUENAME", requireNonNull(environment.getProperty("varselproduksjon.varslinger.queuename")));
        System.setProperty("VARSELPRODUKSJON_BEST_SRVMLD_M_KONTAKT_QUEUENAME", requireNonNull(environment.getProperty("varselproduksjon.best.srvmled.m.kontakt.queuename")));
    }

    @Bean
    public RestTemplate restTemplate(ClientHttpRequestInterceptor... interceptors) {
        RestTemplate template = new RestTemplate();
        template.setInterceptors(asList(interceptors));
        return template;
    }
}
