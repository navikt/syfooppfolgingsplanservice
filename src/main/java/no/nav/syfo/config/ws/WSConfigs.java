package no.nav.syfo.config.ws;

import no.nav.syfo.config.ws.wsconfig.SyfoOppfoelgingConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        SyfoOppfoelgingConfig.class,
})
public class WSConfigs {
}
