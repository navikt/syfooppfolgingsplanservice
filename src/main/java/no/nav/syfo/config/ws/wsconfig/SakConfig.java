package no.nav.syfo.config.ws.wsconfig;

import no.nav.sbl.dialogarena.common.cxf.CXFClient;
import no.nav.sbl.dialogarena.types.Pingable;
import no.nav.sbl.dialogarena.types.Pingable.Ping.PingMetadata;
import no.nav.syfo.mocks.SakV1Mock;
import no.nav.tjeneste.virksomhet.sak.v1.SakV1;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

import static java.lang.System.getProperty;
import static no.nav.sbl.dialogarena.common.cxf.InstanceSwitcher.createMetricsProxyWithInstanceSwitcher;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.feilet;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.lyktes;

@Configuration
public class SakConfig {

    private static final String MOCK_KEY = "sak.withmock";
    private static final String ENDEPUNKT_URL = getProperty("VIRKSOMHET_SAK_V1_ENDPOINTURL");
    private static final String ENDEPUNKT_NAVN = "SAK_V1";
    private static final boolean KRITISK = false;

    @Bean
    public SakV1 sakV1test() {
        SakV1 prod = factory()
                .configureStsForSystemUser()
                .build();
        SakV1 mock = new SakV1Mock();
        return createMetricsProxyWithInstanceSwitcher(
                ENDEPUNKT_NAVN,
                prod,
                mock,
                MOCK_KEY,
                SakV1.class
        );
    }

    @Bean
    public Pingable sakPing() {
        PingMetadata pingMetadata = new PingMetadata(
                UUID.randomUUID().toString(),
                ENDEPUNKT_URL,
                ENDEPUNKT_NAVN,
                KRITISK
        );
        SakV1 pinger = factory()
                .configureStsForSystemUser()
                .build();
        return () -> {
            try {
                pinger.ping();
                return lyktes(pingMetadata);
            } catch (Exception e) {
                return feilet(pingMetadata, e);
            }
        };
    }

    private CXFClient<SakV1> factory() {
        return new CXFClient<>(SakV1.class)
                .address(ENDEPUNKT_URL);
    }
}
