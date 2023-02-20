package no.nav.syfo.mq

import no.nav.syfo.mq.mock.JmsTemplateMock
import org.springframework.context.annotation.*
import org.springframework.jms.annotation.EnableJms
import org.springframework.jms.core.JmsTemplate

@Configuration
@EnableJms
@Profile("local")
class JmsTestConfig {
    @Bean(name = ["servicevarselqueue"])
    fun servicevarselqueue(): JmsTemplate {
        return JmsTemplateMock("servicevarselqueue")
    }
}
