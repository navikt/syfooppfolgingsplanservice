package no.nav.syfo.config.ws.wsconfig;

import no.nav.sbl.dialogarena.common.cxf.CXFClient;
import no.nav.sbl.dialogarena.types.Pingable;
import no.nav.sbl.dialogarena.types.Pingable.Ping.PingMetadata;
import no.nav.syfo.mocks.BrukerprofilMock;
import no.nav.tjeneste.virksomhet.brukerprofil.v3.BrukerprofilV3;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

import static java.lang.System.getProperty;
import static no.nav.sbl.dialogarena.common.cxf.InstanceSwitcher.createMetricsProxyWithInstanceSwitcher;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.feilet;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.lyktes;

@Configuration
public class TpsConfig {

    private static final String MOCK_KEY = "brukerprofil.withmock";
    private static final String ENDEPUNKT_URL = getProperty("VIRKSOMHET_BRUKERPROFIL_V3_ENDPOINTURL");
    private static final String ENDEPUNKT_NAVN = "BRUKERPROFIL_V3";
    private static final boolean KRITISK = true;

    @Bean
    public BrukerprofilV3 brukerprofilV3() {
        BrukerprofilV3 prod = factory()
                .configureStsForSystemUser()
                .build();
        BrukerprofilV3 mock = new BrukerprofilMock();
        return createMetricsProxyWithInstanceSwitcher(
                ENDEPUNKT_NAVN,
                prod,
                mock,
                MOCK_KEY,
                BrukerprofilV3.class
        );
    }

    @Bean
    public Pingable brukerprofilV3Ping() {
        PingMetadata pingMetadata = new PingMetadata(
                UUID.randomUUID().toString(),
                ENDEPUNKT_URL,
                ENDEPUNKT_NAVN,
                KRITISK
        );
        return () -> {
            try {
                factory()
                        .configureStsForSystemUser()
                        .build();
                return lyktes(pingMetadata);
            } catch (Exception e) {
                return feilet(pingMetadata, e);
            }
        };
    }

    private CXFClient<BrukerprofilV3> factory() {
        return new CXFClient<>(BrukerprofilV3.class)
                .address(ENDEPUNKT_URL);
    }
}
