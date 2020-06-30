package no.nav.syfo.toggle

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class Toggle(
    @Value("\${nais.cluster.name:prod-fss}") private val envName: String
) {
    fun erPreprod(): Boolean {
        return envName.contains("dev-fss")
    }
}
