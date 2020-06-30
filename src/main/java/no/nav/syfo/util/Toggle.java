package no.nav.syfo.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Toggle {

    private boolean toggleBatch;
    private boolean toggleBatchSak;
    private String envName;

    public Toggle(
            @Value("${toggle.enable.batch:false}") boolean toggleBatch,
            @Value("${toggle.enable.batch.sak:true}") boolean toggleBatchSak,
            @Value("${nais.cluster.name:prod-fss}") String envName
    ) {
        this.toggleBatch = toggleBatch;
        this.toggleBatchSak = toggleBatchSak;
        this.envName = envName;
    }

    public boolean erPreprod() {
        return envName.contains("dev-fss");
    }
}
