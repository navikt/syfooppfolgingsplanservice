package no.nav.syfo.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Toggle {

    private boolean toggleBatch;

    public Toggle(
            @Value("${toggle.enable.batch:false}") boolean toggleBatch
    ) {
        this.toggleBatch = toggleBatch;
    }

    public boolean toggleBatch() {
        return toggleBatch;
    }
}
