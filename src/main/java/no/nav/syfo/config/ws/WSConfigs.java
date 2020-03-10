package no.nav.syfo.config.ws;

import no.nav.syfo.config.ws.wsconfig.*;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        AktoerConfig.class,
        DKIFConfig.class,
        OrganisasjonConfig.class,
        SyfoOppfoelgingConfig.class,
})
public class WSConfigs {
}
