package no.nav.syfo.config;

import no.nav.syfo.api.system.authorization.AuthorizationFilterFeed;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.*;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.jta.JtaTransactionManager;
import org.springframework.web.client.RestTemplate;

import javax.inject.Inject;
import javax.sql.DataSource;

import static java.util.Arrays.asList;

@Configuration
@EnableTransactionManagement
@EnableScheduling
@EnableAspectJAutoProxy
public class ApplicationConfig {

    // Sørger for at flyway migrering skjer etter at JTA transaction manager er ferdig satt opp av Spring.
    // Forhindrer WARNING: transaction manager not running? loggspam fra Atomikos.

    @Inject
    private DataSource dataSource;

    @Bean
    FlywayMigrationStrategy flywayMigrationStrategy(final JtaTransactionManager jtaTransactionManager) {
        return flyway -> {
            flyway.setValidateOnMigrate(false);
            flyway.setDataSource(dataSource);
            flyway.migrate();
        };
    }

    @Bean
    @Primary
    public RestTemplate restTemplate(ClientHttpRequestInterceptor... interceptors) {
        RestTemplate template = new RestTemplate();
        template.setInterceptors(asList(interceptors));
        return template;
    }

    @Bean(name = "kubernetes")
    public RestTemplate restTemplateKubernetes() {
        return new RestTemplate();
    }

    @Bean
    FilterRegistrationBean<AuthorizationFilterFeed> feedFilter() {
        FilterRegistrationBean<AuthorizationFilterFeed> registrationBean = new FilterRegistrationBean<>();

        registrationBean.setFilter(new AuthorizationFilterFeed());
        registrationBean.addUrlPatterns("/api/system/feed/*");

        return registrationBean;
    }
}
