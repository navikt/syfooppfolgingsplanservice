package no.nav.syfo.config.ws.wsconfig;

import no.nav.sbl.dialogarena.common.cxf.CXFClient;
import no.nav.sbl.dialogarena.types.Pingable;
import no.nav.sbl.dialogarena.types.Pingable.Ping.PingMetadata;
import no.nav.syfo.mocks.ArbeidsfordelingMock;
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.ArbeidsfordelingV1;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

import static java.lang.System.getProperty;
import static no.nav.sbl.dialogarena.common.cxf.InstanceSwitcher.createMetricsProxyWithInstanceSwitcher;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.feilet;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.lyktes;

@Configuration
public class ArbeidsfordelingConfig {

    private static final String MOCK_KEY = "arbeidsfordeling.withmock";
    private static final String ENDEPUNKT_URL = getProperty("VIRKSOMHET_ARBEIDSFORDELING_V1_ENDPOINTURL");
    private static final String ENDEPUNKT_NAVN = "ARBEIDSFORDELING_V1";
    private static final boolean KRITISK = true;

    @Bean
    public ArbeidsfordelingV1 arbeidsfordelingV1() {
        ArbeidsfordelingV1 prod = factory()
                .configureStsForSystemUser()
                .build();
        ArbeidsfordelingV1 mock = new ArbeidsfordelingMock();
        return createMetricsProxyWithInstanceSwitcher(
                ENDEPUNKT_NAVN,
                prod,
                mock,
                MOCK_KEY,
                ArbeidsfordelingV1.class
        );
    }

    @Bean
    public Pingable pingArbfordeling() {
        PingMetadata pingMetadata = new PingMetadata(
                UUID.randomUUID().toString(),
                ENDEPUNKT_URL,
                ENDEPUNKT_NAVN,
                KRITISK
        );
        ArbeidsfordelingV1 pinger = factory()
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

    private CXFClient<ArbeidsfordelingV1> factory() {
        return new CXFClient<>(ArbeidsfordelingV1.class)
                .address(ENDEPUNKT_URL);
    }
}
