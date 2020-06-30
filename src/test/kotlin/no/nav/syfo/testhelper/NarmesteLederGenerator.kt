package no.nav.syfo.testhelper

import no.nav.syfo.model.Naermesteleder
import no.nav.syfo.testhelper.UserConstants.LEDER_AKTORID

class NarmesteLederGenerator {
    private val naermesteleder = Naermesteleder()
        .naermesteLederAktoerId(LEDER_AKTORID)

    fun generateNarmesteLeder(): Naermesteleder {
        return naermesteleder
    }
}
