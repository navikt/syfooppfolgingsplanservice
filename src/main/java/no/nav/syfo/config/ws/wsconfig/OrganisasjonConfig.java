package no.nav.syfo.config.ws.wsconfig;

import no.nav.sbl.dialogarena.common.cxf.CXFClient;
import no.nav.sbl.dialogarena.types.Pingable;
import no.nav.sbl.dialogarena.types.Pingable.Ping.PingMetadata;
import no.nav.syfo.mocks.OrganisasjonMock;
import no.nav.tjeneste.virksomhet.organisasjon.v4.OrganisasjonV4;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

import static java.lang.System.getProperty;
import static no.nav.sbl.dialogarena.common.cxf.InstanceSwitcher.createMetricsProxyWithInstanceSwitcher;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.feilet;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.lyktes;

@Configuration
public class OrganisasjonConfig {

    private static final String MOCK_KEY = "ereg.withmock";
    private static final String ENDEPUNKT_URL = getProperty("VIRKSOMHET_ORGANISASJON_V4_ENDPOINTURL");
    private static final String ENDEPUNKT_NAVN = "ORGANISASJON_V4";
    private static final boolean KRITISK = true;

    @Bean
    public OrganisasjonV4 organisasjonV4() {
        // TODO: JWT eller systemUser(om ikke batch trenger denne, velg JWT
        OrganisasjonV4 prod = factory()
                .configureStsForOnBehalfOfWithJWT()
                .build();
        OrganisasjonV4 mock = new OrganisasjonMock();
        return createMetricsProxyWithInstanceSwitcher(
                ENDEPUNKT_NAVN,
                prod,
                mock,
                MOCK_KEY,
                OrganisasjonV4.class
        );
    }


    @Bean
    public Pingable organisasjonPing() {
        PingMetadata pingMetadata = new PingMetadata(
                UUID.randomUUID().toString(),
                ENDEPUNKT_URL,
                ENDEPUNKT_NAVN,
                KRITISK
        );
        final OrganisasjonV4 pinger = factory()
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

    private CXFClient<OrganisasjonV4> factory() {
        return new CXFClient<>(OrganisasjonV4.class).address(ENDEPUNKT_URL);
    }

}
