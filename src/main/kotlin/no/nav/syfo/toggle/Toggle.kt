package no.nav.syfo.toggle

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class Toggle(
    @Value("\${toggle.enable.batch:false}") private val toggleBatch: Boolean,
    @Value("\${toggle.enable.batch.sak:true}") private val toggleBatchSak: Boolean,
    @Value("\${nais.cluster.name:prod-fss}") private val envName: String
) {
    fun erPreprod(): Boolean {
        return envName.contains("dev-fss")
    }
}
