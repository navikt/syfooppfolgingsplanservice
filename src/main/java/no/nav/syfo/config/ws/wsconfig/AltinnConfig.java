package no.nav.syfo.config.ws.wsconfig;

import no.altinn.services.serviceengine.correspondence._2009._10.ICorrespondenceAgencyExternalBasic;
import no.nav.syfo.service.ws.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;

import static java.util.Collections.singletonList;

@Configuration
public class AltinnConfig {

    @Value("${ekstern.altinn.behandlealtinnmelding.v1.endpointurl}")
    private String serviceUrl;

    @Value("${srv.username}")
    private String usernname;

    @Value("${srv.password}")
    private String passwordl;
    @Bean
    @Primary
    public ICorrespondenceAgencyExternalBasic iCorrespondenceAgencyExternal() {
        ICorrespondenceAgencyExternalBasic port = factory();
        STSClientConfig.configureRequestSamlToken(port, usernname, passwordl);
        return port;
    }

    @SuppressWarnings("unchecked")
    private ICorrespondenceAgencyExternalBasic factory() {
        return new WsClient<ICorrespondenceAgencyExternalBasic>()
                .createPort(serviceUrl, ICorrespondenceAgencyExternalBasic.class, singletonList(new LogErrorHandler()));
    }
}
