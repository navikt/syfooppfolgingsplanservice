package no.nav.syfo.arkivporten

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

data class Document(
    val documentId: UUID,
    val type: DocumentType,
    val content: ByteArray,
    val contentType: String,
    val orgNumber: String,
    val title: String,
    val summary: String,
    val fnr: String,
    val fullName: String,
) {
    companion object {
        val dateFormatter = DateTimeFormatter
            .ofPattern("dd.MM.yyyy")
            .withLocale(Locale.forLanguageTag("nb-NO"))
            .withZone(ZoneId.of("Europe/Oslo"))
        fun title(name: String?): String =
            "Oppfølgingsplan for $name"

        fun summary(arbeidstakerNavn: String, arbeidsgiverNavn: String, date: LocalDateTime): String =
            "${arbeidsgiverNavn} har opprettet en oppfølgingsplan for ${arbeidstakerNavn} på \"Dine sykmeldte\" hos Nav den opprettet den ${dateFormatter.format(date)}"
    }
}
// Leder Ledersen har opprettet en oppfølgingsplan for Lisa Lisen på "Dine sykmeldte" hos Nav den 11.10.25.
enum class DocumentType {
    OPPFOLGINGSPLAN,
}
