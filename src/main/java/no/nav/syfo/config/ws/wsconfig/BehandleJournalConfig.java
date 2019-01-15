package no.nav.syfo.config.ws.wsconfig;

import no.nav.sbl.dialogarena.common.cxf.CXFClient;
import no.nav.sbl.dialogarena.types.Pingable;
import no.nav.sbl.dialogarena.types.Pingable.Ping.PingMetadata;
import no.nav.syfo.mocks.BehandleJournalMock;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.BehandleJournalV2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

import static java.lang.System.getProperty;
import static no.nav.sbl.dialogarena.common.cxf.InstanceSwitcher.createMetricsProxyWithInstanceSwitcher;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.feilet;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.lyktes;

@Configuration
public class BehandleJournalConfig {

    private static final String MOCK_KEY = "behandlejournalv2.withmock";
    private static final String ENDEPUNKT_URL = getProperty("BEHANDLEJOURNAL_V2_ENDPOINTURL");
    private static final String ENDEPUNKT_NAVN = "BEHANDLEJOURNAL_V2";
    private static final boolean KRITISK = false;

    @Bean
    public BehandleJournalV2 behandleJournalV2() {
        BehandleJournalV2 prod = factory()
                .configureStsForSystemUser()
                .enableMtom()
                .build();
        BehandleJournalV2 mock = new BehandleJournalMock();
        return createMetricsProxyWithInstanceSwitcher(
                ENDEPUNKT_NAVN,
                prod,
                mock,
                MOCK_KEY,
                BehandleJournalV2.class
        );
    }

    @Bean
    public Pingable behandleJournalPing() {
        PingMetadata pingMetadata = new PingMetadata(
                UUID.randomUUID().toString(),
                ENDEPUNKT_URL,
                ENDEPUNKT_NAVN,
                KRITISK
        );
        BehandleJournalV2 pinger = factory()
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

    private CXFClient<BehandleJournalV2> factory() {
        return new CXFClient<>(BehandleJournalV2.class)
                .address(ENDEPUNKT_URL);
    }
}
