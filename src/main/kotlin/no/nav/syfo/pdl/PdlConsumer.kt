package no.nav.syfo.pdl

import no.nav.syfo.config.CacheConfig.*
import no.nav.syfo.metric.Metrikk
import no.nav.syfo.sts.StsConsumer
import no.nav.syfo.util.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.Cacheable
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.*
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.client.RestTemplate

@Service
class PdlConsumer(
    private val metric: Metrikk,
    @Value("\${pdl.url}") private val pdlUrl: String,
    private val stsConsumer: StsConsumer,
    @param:Qualifier("scheduler") private val restTemplate: RestTemplate
) : InitializingBean {
    fun person(ident: String): PdlHentPerson? {
        metric.tellHendelse("call_pdl")

        val query = this::class.java.getResource("/pdl/hentPerson.graphql").readText().replace("[\n\r]", "")
        val entity = createRequestEntity(PdlRequest(query, Variables(ident)))
        try {
            val pdlPerson = restTemplate.exchange(
                pdlUrl,
                HttpMethod.POST,
                entity,
                object : ParameterizedTypeReference<PdlPersonResponse>() {}
            )

            val pdlPersonReponse = pdlPerson.body!!
            LOG.info("PDL DATA: ${pdlPersonReponse?.data?.hentPerson?.navn}")
            return if (pdlPersonReponse.errors != null && pdlPersonReponse.errors.isNotEmpty()) {
                metric.tellHendelse("call_pdl_fail")
                pdlPersonReponse.errors.forEach {
                    LOG.error("Error while requesting person from PersonDataLosningen: ${it.errorMessage()}")
                }
                null
            } else {
                metric.tellHendelse("call_pdl_success")
                pdlPersonReponse.data
            }
        } catch (exception: RestClientResponseException) {
            metric.tellHendelse("call_pdl_fail")
            LOG.error("Error from PDL with request-url: $pdlUrl", exception)
            throw exception
        }
    }

    fun personName(ident: String): String? {
        return person(ident)?.fullName()
    }

    fun isKode6Or7(ident: String): Boolean {
        return person(ident)?.isKode6Or7() ?: throw PdlRequestFailedException()
    }

    @Cacheable(cacheNames = [CACHENAME_AKTOER_ID], key = "#fnr")
    fun aktorid(fnr: String): String {
        return hentIdentFraPDL(fnr, IdentType.AKTORID)
    }

    @Cacheable(cacheNames = [CACHENAME_AKTOER_FNR], key = "#aktorId")
    fun fnr(aktorId: String): String {
        return hentIdentFraPDL(aktorId, IdentType.FOLKEREGISTERIDENT)
    }

    @Cacheable(cacheNames = [CACHENAME_GJELDENDE_FNR], key = "#fnr")
    fun gjeldendeFnr(fnr: String): String {
        return hentIdentFraPDL(fnr, IdentType.FOLKEREGISTERIDENT)
    }

    fun hentIdentFraPDL(ident: String, identType: IdentType): String {
        metric.tellHendelse("call_pdl")
        val gruppe = identType.name

        val query = this::class.java.getResource("/pdl/hentIdenter.graphql").readText().replace("[\n\r]", "")
        val entity = createRequestEntity(
            PdlRequest(query, Variables(ident = ident, grupper = gruppe))
        )
        try {
            val pdlIdenter = restTemplate.exchange(
                pdlUrl,
                HttpMethod.POST,
                entity,
                object : ParameterizedTypeReference<PdlIdenterResponse>() {}
            )

            val pdlIdenterReponse = pdlIdenter.body!!
            if (pdlIdenterReponse.errors != null && pdlIdenterReponse.errors.isNotEmpty()) {
                metric.tellHendelse("call_pdl_fail")
                pdlIdenterReponse.errors.forEach {
                    LOG.error("Error while requesting $gruppe from PersonDataLosningen: ${it.errorMessage()}")
                }
                throw RuntimeException("Error while requesting $gruppe from PDL")
            } else {
                metric.tellHendelse("call_pdl_success")
                try {
                    return pdlIdenterReponse.data?.hentIdenter?.identer?.first()?.ident!!
                } catch (e: NoSuchElementException) {
                    LOG.info("Error while requesting $gruppe from PDL. Empty list in hentIdenter response")
                    throw RuntimeException("Error while requesting $gruppe from PDL")
                }
            }
        } catch (exception: RestClientResponseException) {
            metric.tellHendelse("call_pdl_fail")
            LOG.error("Error from PDL with request-url: $pdlUrl", exception)
            throw exception
        }
    }

    private fun createRequestEntity(request: PdlRequest): HttpEntity<PdlRequest> {
        val stsToken: String = stsConsumer.token()
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.set(PDL_BEHANDLINGSNUMMER_HEADER, BEHANDLINGSNUMMER_OPPFOLGINGSPLAN)
        headers.set(AUTHORIZATION, bearerHeader(stsToken))
        headers.set(NAV_CONSUMER_TOKEN_HEADER, bearerHeader(stsToken))
        return HttpEntity(request, headers)
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(PdlConsumer::class.java)
        lateinit var pdlConsumer: PdlConsumer
    }

    override fun afterPropertiesSet() {
        pdlConsumer = this
    }
}
