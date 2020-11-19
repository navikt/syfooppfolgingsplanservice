package no.nav.syfo.lps

import no.nav.syfo.lps.database.OppfolgingsplanLPSRetryDAO
import org.springframework.stereotype.Repository
import javax.inject.Inject

@Repository
class OppfolgingsplanLPSRetryService @Inject constructor(
    private val oppfolgingsplanLPSRetryDAO: OppfolgingsplanLPSRetryDAO
) {
    fun getOrCreate(
        archiveReference: String,
        xml: String
    ): Long {
        val planLPSRetryList = oppfolgingsplanLPSRetryDAO.get(archiveReference)
        return if (planLPSRetryList.isNotEmpty()) {
            planLPSRetryList.first().id
        } else {
            oppfolgingsplanLPSRetryDAO.create(
                archiveReference = archiveReference,
                xml = xml
            )
        }
    }

    fun delete(
        archiveReference: String
    ) {
        oppfolgingsplanLPSRetryDAO.delete(archiveReference)
    }
}
