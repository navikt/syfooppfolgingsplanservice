package no.nav.syfo.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Toggle {

    private boolean toggleBatch;
    private String envName;

    public Toggle(
            @Value("${toggle.enable.batch:false}") boolean toggleBatch,
            @Value("${fasit.environment.name:p}") String envName
    ) {
        this.toggleBatch = toggleBatch;
        this.envName = envName;
    }

    public boolean toggleBatch() {
        return toggleBatch;
    }

    public boolean erPreprod() {
        return envName.contains("q");
    }
}
