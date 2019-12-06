package no.nav.syfo.azuread;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AzureAdTokenConfig {
    @Bean(name="restTemplateMedProxy")
    RestTemplate restTemplateMedProxy() {
        return new RestTemplateBuilder()
                .additionalCustomizers(new NaisProxyCustomizer())
                .build();
    }
}
