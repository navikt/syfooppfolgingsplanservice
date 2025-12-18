package no.nav.syfo.dokumentporten

import java.time.LocalDateTime
import junit.framework.TestCase.assertEquals
import org.junit.Test

class DocumentTest {

    @Test
    fun `title returns correct string with name`() {
        val name = "Lisa Lisen"
        val expected = "Oppfølgingsplan for Lisa Lisen"
        assertEquals(expected, Document.title(name))
    }

    @Test
    fun `title returns correct string with null name`() {
        val expected = "Oppfølgingsplan for null"
        assertEquals(expected, Document.title(null))
    }

    @Test
    fun `summary returns correct string`() {
        val arbeidstakerNavn = "Lisa Lisen"
        val arbeidsgiverNavn = "Leder Ledersen"
        val date = LocalDateTime.of(2025, 10, 11, 12, 0)
        val expected = "Leder Ledersen har opprettet en oppfølgingsplan for Lisa Lisen på \"Dine sykmeldte\" hos Nav opprettet den 11.10.2025"
        assertEquals(expected, Document.summary(arbeidstakerNavn, arbeidsgiverNavn, date))
    }
}
