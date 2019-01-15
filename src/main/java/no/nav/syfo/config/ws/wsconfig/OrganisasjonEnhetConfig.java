package no.nav.syfo.config.ws.wsconfig;

import no.nav.sbl.dialogarena.common.cxf.CXFClient;
import no.nav.sbl.dialogarena.types.Pingable;
import no.nav.sbl.dialogarena.types.Pingable.Ping.PingMetadata;
import no.nav.syfo.mocks.OrganisasjonEnhetMock;
import no.nav.tjeneste.virksomhet.organisasjonenhet.v2.OrganisasjonEnhetV2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

import static java.lang.System.getProperty;
import static no.nav.sbl.dialogarena.common.cxf.InstanceSwitcher.createMetricsProxyWithInstanceSwitcher;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.feilet;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.lyktes;

@Configuration
public class OrganisasjonEnhetConfig {

    private static final String ENDEPUNKT_URL = getProperty("VIRKSOMHET_ORGANISASJONENHET_V2_ENDPOINTURL");
    private static final String ENDEPUNKT_NAVN = "OrganisasjonEnhetV2";
    private static final boolean KRITISK = false;

    @Bean
    public OrganisasjonEnhetV2 organisasjonEnhetV2() {
        OrganisasjonEnhetV2 prod = factory()
                .configureStsForSystemUser()
                .build();
        OrganisasjonEnhetV2 mock = new OrganisasjonEnhetMock();
        return createMetricsProxyWithInstanceSwitcher(
                ENDEPUNKT_NAVN,
                prod,
                mock,
                "tillatmock",
                OrganisasjonEnhetV2.class
        );
    }

    @Bean
    public Pingable pingOrganisasjonEnhet() {
        PingMetadata pingMetadata = new PingMetadata(
                UUID.randomUUID().toString(),
                ENDEPUNKT_URL,
                ENDEPUNKT_NAVN,
                KRITISK
        );
        OrganisasjonEnhetV2 pinger = factory()
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

    private CXFClient<OrganisasjonEnhetV2> factory() {
        return new CXFClient<>(OrganisasjonEnhetV2.class).address(ENDEPUNKT_URL);
    }

}
