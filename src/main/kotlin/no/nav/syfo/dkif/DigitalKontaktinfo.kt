package no.nav.syfo.dkif

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

data class DigitalKontaktinfo (
    val kanVarsles: Boolean,
    val reservert: Boolean,
    val mobiltelefonnummer: String?,
    val epostadresse: String?
)

object KontaktinfoMapper {

    private val objectMapper: ObjectMapper = ObjectMapper().apply {
        registerKotlinModule()
        registerModule(JavaTimeModule())
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
    }

    fun mapPerson(json: String): DigitalKontaktinfo {
        val jsonNode = objectMapper.readTree(json)

        jsonNode.let {
            return DigitalKontaktinfo(
                it["kanVarsles"]?.asBoolean() ?: false,
                it["reservert"]?.asBoolean() ?: false,
                it["mobiltelefonnummer"]?.asText(),
                it["epostadresse"]?.asText()
            )
        }
    }
}
