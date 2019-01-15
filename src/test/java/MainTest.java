import no.nav.apiapp.TestContext;
import no.nav.syfo.config.DatabaseTestContext;
import no.nav.syfo.util.ToggleUtil;
import no.nav.testconfig.ApiAppTest;

import static java.lang.System.getProperty;
import static java.lang.System.setProperty;
import static no.nav.sbl.dialogarena.test.SystemProperties.setFrom;
import static no.nav.syfo.util.PropertyUtil.FASIT_ENVIRONMENT_NAME;
import static no.nav.testconfig.ApiAppTest.setupTestContext;
import static org.glassfish.jersey.server.ServerProperties.APPLICATION_NAME;

public class MainTest {
    private static final String PORT = "8583";

    public static void main(String[] args) throws Exception {
        setFrom("jetty-environment.properties");

        setupTestContext(ApiAppTest.Config
                .builder()
                .applicationName(APPLICATION_NAME)
                .build()
        );

        DatabaseTestContext.setupContext(getProperty("database"));
        TestContext.setup();

        setProperty(FASIT_ENVIRONMENT_NAME, ToggleUtil.ENVIRONMENT_MODE.q1.name());

        Main.main(PORT);
    }
}
