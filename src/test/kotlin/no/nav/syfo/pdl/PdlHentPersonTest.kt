package no.nav.syfo.pdl

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class PdlHentPersonTest {
    @Test
    fun full_name_without_middlename() {
        val pdlHentPerson = mockPdlHentPerson(null)
        val name: String? = pdlHentPerson?.fullName()
        assertThat(name!!.split(" ").toTypedArray().size).isEqualTo(2)
        assertThat(name).isEqualTo("Fornavn Etternavn")
    }

    @Test
    fun full_name_with_middleName() {
        val pdlHentPerson = mockPdlHentPerson("Mellomnavn")
        val name: String? = pdlHentPerson?.fullName()
        assertThat(name!!.split(" ").toTypedArray().size).isEqualTo(3)
        assertThat(name).isEqualTo("Fornavn Mellomnavn Etternavn")
    }

    @Test
    fun null_when_hentPerson_is_null() {
        val pdlHentPerson: PdlHentPerson = PdlHentPerson(null)
        val name: String? = pdlHentPerson.fullName()
        assertThat(name).isNull()
    }

    @Test
    fun null_when_PdlPersonNavnList_is_empty() {
        val pdlHentPerson: PdlHentPerson = PdlHentPerson(PdlPerson(emptyList(), null))
        val name: String? = pdlHentPerson.fullName()
        assertThat(name).isNull()
    }

    private fun mockPdlHentPerson(middleName: String?): PdlHentPerson? {
        return PdlHentPerson(PdlPerson(
                listOf(PdlPersonNavn(
                        fornavn = "Fornavn",
                        mellomnavn = middleName,
                        etternavn = "Etternavn"
                )),
                null
        ))
    }
}
