package no.nav.syfo.config.ws.wsconfig;

import no.altinn.services.serviceengine.correspondence._2009._10.ICorrespondenceAgencyExternalBasic;
//import no.nav.modig.jaxws.handlers.MDCOutHandler;
import no.nav.sbl.dialogarena.common.cxf.CXFClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static java.lang.System.getProperty;
import static no.nav.metrics.MetricsFactory.createTimerProxyForWebService;
import static no.nav.syfo.util.PropertyUtil.ALTINN_BEHANDLEALTINNMELDING_V1_ENDPOINTURL;

@Configuration
public class AltinnConfig {

    @Bean
    public ICorrespondenceAgencyExternalBasic iCorrespondenceAgencyExternal() {
        return createTimerProxyForWebService(
                "Altinn.ICorrespondenceAgencyExternal",
                new CXFClient<>(ICorrespondenceAgencyExternalBasic.class)
                        .address(getProperty(ALTINN_BEHANDLEALTINNMELDING_V1_ENDPOINTURL))
                        .configureStsForSystemUser()
//                        .withHandler(new MDCOutHandler())
                        .build(),
                ICorrespondenceAgencyExternalBasic.class);
    }
}
