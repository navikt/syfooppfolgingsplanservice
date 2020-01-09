package no.nav.syfo.pdl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Java6Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class PdlHentPersonTest {
    @Test
    public void full_name_without_middlename() {
        PdlHentPerson pdlHentPerson = mockPdlHentPerson(null);

        String name = pdlHentPerson.getName();

        assertThat(name.split(" ").length).isEqualTo(2);
        assertThat(name).isEqualTo("Fornavn Etternavn");
    }

    @Test
    public void full_name_with_middleName() {
        PdlHentPerson pdlHentPerson = mockPdlHentPerson("Mellomnavn");

        String name = pdlHentPerson.getName();

        assertThat(name.split(" ").length).isEqualTo(3);
        assertThat(name).isEqualTo("Fornavn Mellomnavn Etternavn");
    }

    @Test
    public void null_when_hentPerson_is_null() {
        PdlHentPerson pdlHentPerson = new PdlHentPerson().hentPerson(null);

        String name = pdlHentPerson.getName();

        assertThat(name).isNull();
    }

    @Test
    public void null_when_PdlPersonNavnList_is_null() {
        PdlHentPerson pdlHentPerson = new PdlHentPerson().hentPerson(new PdlPerson().navn(null));

        String name = pdlHentPerson.getName();

        assertThat(name).isNull();
    }

    @Test
    public void null_when_PdlPersonNavnList_is_empty() {
        PdlHentPerson pdlHentPerson = new PdlHentPerson().hentPerson(new PdlPerson().navn(emptyList()));

        String name = pdlHentPerson.getName();

        assertThat(name).isNull();
    }

    private PdlHentPerson mockPdlHentPerson(String middleName) {
        return new PdlHentPerson()
                .hentPerson(new PdlPerson()
                        .navn(singletonList(new PdlPersonNavn()
                                .fornavn("Fornavn")
                                .mellomnavn(middleName)
                                .etternavn("Etternavn"))
                        ));
    }
}
