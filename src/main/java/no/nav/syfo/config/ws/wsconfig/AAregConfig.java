package no.nav.syfo.config.ws.wsconfig;

import no.nav.sbl.dialogarena.common.cxf.CXFClient;
import no.nav.sbl.dialogarena.types.Pingable;
import no.nav.sbl.dialogarena.types.Pingable.Ping.PingMetadata;
import no.nav.syfo.mocks.ArbeidsforholdMock;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.binding.ArbeidsforholdV3;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

import static java.lang.System.getProperty;
import static no.nav.sbl.dialogarena.common.cxf.InstanceSwitcher.createMetricsProxyWithInstanceSwitcher;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.feilet;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.lyktes;

@Configuration
public class AAregConfig {

    private static final String MOCK_KEY = getProperty("arbeidsforhold.aareg.withmock");
    private static final String ENDEPUNKT_URL = getProperty("VIRKSOMHET_ARBEIDSFORHOLD_V3_ENDPOINTURL");
    private static final String ENDEPUNKT_NAVN = "ARBEIDSFORHOLD_V3";
    private static final boolean KRITISK = true;

    @Bean
    public ArbeidsforholdV3 arbeidsforholdV3() {
        // TODO: JWT eller systemUser(om ikke batch trenger denne, velg JWT
        ArbeidsforholdV3 prod = factory()
                .configureStsForSystemUser()
                .build();
        ArbeidsforholdV3 mock = new ArbeidsforholdMock();
        return createMetricsProxyWithInstanceSwitcher(
                ENDEPUNKT_NAVN,
                prod,
                mock,
                MOCK_KEY,
                ArbeidsforholdV3.class
        );
    }

    @Bean
    public Pingable arbeidsforholdPing() {
        PingMetadata metadata = new PingMetadata(
                UUID.randomUUID().toString(),
                ENDEPUNKT_URL,
                ENDEPUNKT_NAVN,
                KRITISK
        );
        final ArbeidsforholdV3 arbeidsforholdPing = factory()
                .configureStsForSystemUser()
                .build();
        return () -> {
            try {
                arbeidsforholdPing.ping();
                return lyktes(metadata);
            } catch (Exception e) {
                // TODO: Dette kan fjernes når Arbeidsforhold implementerer sin Ping uten avhengigheter bakover
                if (e.getMessage().contains("Organisasjon")) {
                    return lyktes(new PingMetadata( UUID.randomUUID().toString(), ENDEPUNKT_URL, ENDEPUNKT_NAVN + e.getMessage(), KRITISK));
                }
                return feilet(metadata, e);
            }
        };
    }

    private CXFClient<ArbeidsforholdV3> factory() {
        return new CXFClient<>(ArbeidsforholdV3.class)
                .address(ENDEPUNKT_URL);
    }
}
