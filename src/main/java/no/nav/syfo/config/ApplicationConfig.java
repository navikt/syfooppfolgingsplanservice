package no.nav.syfo.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.*;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableTransactionManagement
@EnableScheduling
@EnableCaching
public class ApplicationConfig {

    @Bean
    @Primary
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean(name = "scheduler")
    public RestTemplate restTemplateScheduler() {
        return new RestTemplate();
    }

    @Bean(name="restTemplateMedProxy")
    public RestTemplate restTemplateMedProxy() {
        return new RestTemplateBuilder()
                .additionalCustomizers(new NaisProxyCustomizer())
                .build();
    }
}
