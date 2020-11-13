package no.nav.syfo

import no.nav.security.token.support.test.spring.TokenGeneratorConfiguration
import no.nav.syfo.util.PropertyUtil
import no.nav.syfo.util.ToggleUtil
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.core.env.Environment
import java.util.*

@Configuration
@Import(TokenGeneratorConfiguration::class)
class LocalApplicationConfig(environment: Environment) {
    init {
        System.setProperty("SECURITYTOKENSERVICE_URL", Objects.requireNonNull(environment.getProperty("securitytokenservice.url")))
        System.setProperty("SRV_USERNAME", Objects.requireNonNull(environment.getProperty("srvsyfooppfolgingsplanservice.username")))
        System.setProperty("SRV_PASSWORD", Objects.requireNonNull(environment.getProperty("srvsyfooppfolgingsplanservice.password")))
        System.setProperty(PropertyUtil.FASIT_ENVIRONMENT_NAME, ToggleUtil.ENVIRONMENT_MODE.q1.name)
        System.setProperty(PropertyUtil.LOCAL_MOCK, "true")
        System.setProperty("VARSELPRODUKSJON_VARSLINGER_QUEUENAME", Objects.requireNonNull(environment.getProperty("varselproduksjon.varslinger.queuename")))
        System.setProperty("VARSELPRODUKSJON_BEST_SRVMLD_M_KONTAKT_QUEUENAME", Objects.requireNonNull(environment.getProperty("varselproduksjon.best.srvmled.m.kontakt.queuename")))
    }
}
