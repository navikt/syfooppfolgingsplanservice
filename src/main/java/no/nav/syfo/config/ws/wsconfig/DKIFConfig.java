package no.nav.syfo.config.ws.wsconfig;

import no.nav.sbl.dialogarena.common.cxf.CXFClient;
import no.nav.sbl.dialogarena.types.Pingable;
import no.nav.sbl.dialogarena.types.Pingable.Ping.PingMetadata;
import no.nav.syfo.mocks.DKIFMock;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.DigitalKontaktinformasjonV1;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

import static java.lang.System.getProperty;
import static no.nav.sbl.dialogarena.common.cxf.InstanceSwitcher.createMetricsProxyWithInstanceSwitcher;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.feilet;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.lyktes;

@Configuration
public class DKIFConfig {

    private static final String MOCK_KEY = "dkif.withmock";
    private static final String ENDEPUNKT_URL = getProperty("VIRKSOMHET_DIGITALKONTAKINFORMASJON_V1_ENDPOINTURL");
    private static final String ENDEPUNKT_NAVN = "DIGITALKONTAKTINFORMASJON_V1";
    private static final boolean KRITISK = true;

    @Bean
    public DigitalKontaktinformasjonV1 digitalKontaktinformasjonV1() {
        DigitalKontaktinformasjonV1 prod = factory()
                .configureStsForOnBehalfOfWithJWT()
                .build();
        DigitalKontaktinformasjonV1 mock = new DKIFMock();
        return createMetricsProxyWithInstanceSwitcher(
                ENDEPUNKT_NAVN,
                prod,
                mock,
                MOCK_KEY,
                DigitalKontaktinformasjonV1.class
        );
    }

    @Bean
    public Pingable dkifV1Ping() {
        PingMetadata pingMetadata = new PingMetadata(
                UUID.randomUUID().toString(),
                ENDEPUNKT_URL,
                ENDEPUNKT_NAVN,
                KRITISK
        );
        DigitalKontaktinformasjonV1 pinger = factory()
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

    private CXFClient<DigitalKontaktinformasjonV1> factory() {
        return new CXFClient<>(DigitalKontaktinformasjonV1.class)
                .address(ENDEPUNKT_URL);
    }

}
