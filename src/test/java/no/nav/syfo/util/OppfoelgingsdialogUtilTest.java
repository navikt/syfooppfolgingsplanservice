package no.nav.syfo.util;

import no.nav.syfo.domain.Godkjenning;
import no.nav.syfo.domain.Oppfoelgingsdialog;
import no.nav.syfo.domain.Person;
import no.nav.syfo.domain.Tiltak;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static java.time.LocalDateTime.now;
import static java.util.Arrays.asList;
import static no.nav.syfo.util.OppfoelgingsdialogUtil.*;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class OppfoelgingsdialogUtilTest {

    @Test
    public void sisteGodkjenningAvAnnenPartSortererRiktig() {
        Godkjenning godkjenning = sisteGodkjenningAvAnnenPart(new Oppfoelgingsdialog()
                .arbeidstaker(new Person().aktoerId("123"))
                .godkjenninger(asList(
                        new Godkjenning()
                                .godkjentAvAktoerId("123")
                                .godkjenningsTidspunkt(now().minusHours(2))
                                .beskrivelse("1"),
                        new Godkjenning()
                                .godkjentAvAktoerId("123")
                                .godkjenningsTidspunkt(now().minusHours(3))
                                .beskrivelse("2"),
                        new Godkjenning()
                                .godkjentAvAktoerId("123")
                                .godkjenningsTidspunkt(now().minusHours(1))
                                .beskrivelse("3")
                )), "123");

        assertThat(godkjenning.beskrivelse).isEqualTo("3");
    }


    @Test
    public void arbeidsgiverHarGjortEndringerIMellomtiden() {
        boolean b = annenPartHarGjortEndringerImellomtiden(new Oppfoelgingsdialog()
                        .sistEndretArbeidsgiver(now().minusSeconds(10))
                        .arbeidstaker(new Person().sistAksessert(now().minusSeconds(20)).aktoerId("aktoerId"))
                , "aktoerId");
        assertThat(b).isTrue();
    }

    @Test
    public void sykmeldtHarGjortEndringerIMellomtiden() {
        boolean b = annenPartHarGjortEndringerImellomtiden(new Oppfoelgingsdialog()
                        .sistEndretSykmeldt(now().minusSeconds(10))
                        .arbeidsgiver(new Person().sistAksessert(now().minusSeconds(20)))
                .arbeidstaker(new Person().aktoerId("aktoerId"))
                , "arbeidsgiveraktoerId");
        assertThat(b).isTrue();
    }

    @Test
    public void taalerAtSistAksessertAnnenPartErNull() {
        boolean b = annenPartHarGjortEndringerImellomtiden(new Oppfoelgingsdialog()
                .sistEndretSykmeldt(null)
                .arbeidsgiver(new Person().sistAksessert(now().minusSeconds(20)))
                .arbeidstaker(new Person().aktoerId("aktoerId"))
                , "arbeidsgiveraktoerId");
        assertThat(b).isFalse();
    }

    @Test
    public void tiltakHoererIkkeTilDialogGirFalseDersomIngenID() {
        boolean b = eksisterendeTiltakHoererTilDialog(null, asList(
                new Tiltak().id(1L),
                new Tiltak().id(2L),
                new Tiltak().id(3L),
                new Tiltak().id(4L)
        ));

        assertThat(b).isTrue();
    }

    @Test
    public void tiltakHoererIkkeTilDialogGirTrueDersomIngenMatch() {
        boolean b = eksisterendeTiltakHoererTilDialog(5L, asList(
                new Tiltak().id(1L),
                new Tiltak().id(2L),
                new Tiltak().id(3L),
                new Tiltak().id(4L)
        ));

        assertThat(b).isFalse();
    }

    @Test
    public void tiltakHoererIkkeTilDialogGirFalseDersomMatch() {
        boolean b = eksisterendeTiltakHoererTilDialog(3L, asList(
                new Tiltak().id(1L),
                new Tiltak().id(2L),
                new Tiltak().id(3L),
                new Tiltak().id(4L)
        ));

        assertThat(b).isTrue();
    }
}
