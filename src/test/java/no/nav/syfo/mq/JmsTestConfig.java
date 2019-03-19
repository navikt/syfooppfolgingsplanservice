package no.nav.syfo.mq;

import no.nav.syfo.mq.mock.JmsTemplateMock;
import org.springframework.context.annotation.*;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.core.JmsTemplate;

@Configuration
@EnableJms
@Profile({"local"})
public class JmsTestConfig {

    @Bean(name = "servicevarselqueue")
    public JmsTemplate servicevarselqueue() {
        return new JmsTemplateMock("servicevarselqueue");
    }

    @Bean(name = "tredjepartsvarselqueue")
    public JmsTemplate tredjepartsvarselqueue() {
        return new JmsTemplateMock("tredjepartsvarselqueue");
    }
}
