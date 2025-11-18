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
    val orgnumber: String,
    val dialogTitle: String,
    val dialogSummary: String,
) {
    companion object {
        val dateFormatter = DateTimeFormatter
            .ofPattern("dd.MM.yyyy")
            .withLocale(Locale.forLanguageTag("nb-NO"))
            .withZone(ZoneId.of("Europe/Oslo"))
        fun title(name: String?): String =
            "Oppfølgingsplan for $name"

        fun summary(date: LocalDateTime): String =
            "Oppfølgingsplan opprettet den ${dateFormatter.format(date)}"
    }
}

enum class DocumentType {
    OPPFOLGINGSPLAN,
}
