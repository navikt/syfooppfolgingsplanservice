package no.nav.syfo.config.ws.wsconfig;

import no.nav.sbl.dialogarena.common.cxf.CXFClient;
import no.nav.sbl.dialogarena.types.Pingable;
import no.nav.sbl.dialogarena.types.Pingable.Ping.PingMetadata;
import no.nav.syfo.mocks.BehandleSakMock;
import no.nav.tjeneste.virksomhet.behandlesak.v1.BehandleSakV1;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

import static java.lang.System.getProperty;
import static no.nav.sbl.dialogarena.common.cxf.InstanceSwitcher.createMetricsProxyWithInstanceSwitcher;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.feilet;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.lyktes;

@Configuration
public class BehandleSakConfig {

    private static final String MOCK_KEY = "behandleSak.withmock";
    private static final String ENDEPUNKT_URL = getProperty("VIRKSOMHET_BEHANDLESAK_V1_ENDPOINTURL");
    private static final String ENDEPUNKT_NAVN = "BEHANDLE_SAKV1";
    private static final boolean KRITISK = false;

    @Bean
    public BehandleSakV1 behandleSakV1() {
        BehandleSakV1 prod = factory()
                .configureStsForSystemUser()
                .build();
        BehandleSakV1 mock = new BehandleSakMock();
        return createMetricsProxyWithInstanceSwitcher(
                ENDEPUNKT_NAVN,
                prod,
                mock,
                MOCK_KEY,
                BehandleSakV1.class
        );
    }

    @Bean
    public Pingable behandleSakPing() {
        PingMetadata pingMetadata = new PingMetadata(
                UUID.randomUUID().toString(),
                ENDEPUNKT_URL,
                ENDEPUNKT_NAVN,
                KRITISK
        );
        BehandleSakV1 pinger = factory()
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

    private CXFClient<BehandleSakV1> factory() {
        return new CXFClient<>(BehandleSakV1.class)
                .address(ENDEPUNKT_URL);
    }

}
