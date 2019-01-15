package no.nav.syfo.config;

import no.nav.apiapp.ApiApplication;
import no.nav.apiapp.config.ApiAppConfigurator;
import no.nav.brukerdialog.security.oidc.SystemUserTokenProvider;
import no.nav.metrics.aspects.CountAspect;
import no.nav.metrics.aspects.TimerAspect;
import no.nav.syfo.api.system.AuthorizationFilter;
import no.nav.syfo.config.cache.CacheConfig;
import no.nav.syfo.config.mq.MQConfig;
import no.nav.syfo.config.ws.WSConfigs;
import no.nav.syfo.util.MigrationUtil;
import org.springframework.context.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.servlet.DispatcherType;
import javax.servlet.ServletContext;
import javax.sql.DataSource;
import javax.ws.rs.client.Client;

import static java.util.EnumSet.allOf;
import static javax.ws.rs.client.ClientBuilder.newClient;

@Configuration
@EnableAspectJAutoProxy
@ComponentScan("no.nav.syfo")
@Import({
        CacheConfig.class,
        DatabaseConfig.class,
        MQConfig.class,
        ServiceConfig.class,
        WSConfigs.class,
})
public class ApplicationConfig implements ApiApplication.NaisApiApplication {
    public static final String APPLICATION_NAME = "syfooppfolgingsplanservice";
    public static final String VEILARBLOGIN_REDIRECT_URL_URL = "VEILARBLOGIN_REDIRECT_URL_URL";

    @Bean
    public TimerAspect timerAspect() {
        return new TimerAspect();
    }

    @Bean
    public CountAspect countAspect() {
        return new CountAspect();
    }


    @Bean
    public SystemUserTokenProvider systemUserTokenProvider() {
        return new SystemUserTokenProvider();
    }

    @Bean
    public Client client() {
        return newClient();
    }

    @Inject
    private DataSource dataSource;

    @Transactional
    @Override
    public void startup(ServletContext servletContext) {
        servletContext.addFilter(AuthorizationFilter.class.getSimpleName(), new AuthorizationFilter())
                .addMappingForUrlPatterns(allOf(DispatcherType.class), false, "/api/system/*");

        MigrationUtil.createTables(dataSource);
    }

    @Override
    public void configure(ApiAppConfigurator apiAppConfigurator) {
        apiAppConfigurator
                .addPublicPath("/api/system/.*")
                .issoLogin()
                .azureADB2CLogin()
                .sts();
    }
}
