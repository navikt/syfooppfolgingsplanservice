package no.nav.syfo.config.ws.wsconfig;

import no.nav.sbl.dialogarena.common.cxf.CXFClient;
import no.nav.sbl.dialogarena.types.Pingable;
import no.nav.sbl.dialogarena.types.Pingable.Ping.PingMetadata;
import no.nav.syfo.mocks.OppfolgingMock;
import no.nav.tjeneste.virksomhet.sykefravaersoppfoelging.v1.SykefravaersoppfoelgingV1;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

import static java.lang.System.getProperty;
import static no.nav.sbl.dialogarena.common.cxf.InstanceSwitcher.createMetricsProxyWithInstanceSwitcher;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.feilet;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.lyktes;

@Configuration
public class SyfoOppfoelgingConfig {

    private static final String MOCK_KEY = "sykefravaersoppfoelging.syfoservice.withmock";
    private static final String ENDEPUNKT_URL = getProperty("SYKEFRAVAERSOPPFOELGING_V1_ENDPOINTURL");
    private static final String ENDEPUNKT_NAVN = "SYKEFRAVAERSOPPFOELGING_V1";
    private static final boolean KRITISK = true;

    @Bean
    public SykefravaersoppfoelgingV1 sykefravaersoppfoelgingV1() {
        // TODO: JWT eller systemUser(om ikke batch trenger denne, velg JWT
        SykefravaersoppfoelgingV1 prod = factory()
                .configureStsForOnBehalfOfWithJWT()
                .build();
        SykefravaersoppfoelgingV1 mock = new OppfolgingMock();
        return createMetricsProxyWithInstanceSwitcher(ENDEPUNKT_NAVN,
                prod,
                mock,
                MOCK_KEY,
                SykefravaersoppfoelgingV1.class
        );
    }

    @Bean
    public Pingable sykefravaersoppfoelging() {
        PingMetadata pingMetadata = new PingMetadata(
                UUID.randomUUID().toString(),
                ENDEPUNKT_URL,
                ENDEPUNKT_NAVN,
                KRITISK
        );
        final SykefravaersoppfoelgingV1 sykefravaersoppfoelgingV1Ping = factory()
                .configureStsForSystemUser()
                .build();
        return () -> {
            try {
                sykefravaersoppfoelgingV1Ping.ping();
                return lyktes(pingMetadata);
            } catch (Exception e) {
                return feilet(pingMetadata, e);
            }
        };
    }

    private CXFClient<SykefravaersoppfoelgingV1> factory() {
        return new CXFClient<>(SykefravaersoppfoelgingV1.class)
                .address(ENDEPUNKT_URL);
    }
}
