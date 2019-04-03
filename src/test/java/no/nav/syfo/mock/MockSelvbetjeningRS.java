package no.nav.syfo.mock;

import no.nav.syfo.api.selvbetjening.domain.*;

import java.time.LocalDateTime;

import static java.time.LocalDate.now;
import static java.util.Arrays.asList;
import static no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_FNR;
import static no.nav.syfo.testhelper.UserConstants.LEDER_FNR;

public class MockSelvbetjeningRS {

    private static RSPerson rsArbeidstakeren() {
        return new RSPerson()
                .fnr(ARBEIDSTAKER_FNR);
    }

    private static RSNaermesteLeder rsArbeidsgiveren() {
        return new RSNaermesteLeder()
                .fnr(LEDER_FNR);
    }

    public static RSTiltak rsTiltakLagreNytt() {
        return new RSTiltak()
                .tiltaknavn("Tiltaknavn")
                .beskrivelse("Dette er en beskrivelse av et tiltak")
                .fom(now().plusDays(2))
                .tom(now().plusDays(4))
                .status("godkjent")
                .gjennomfoering("Dette er en gjennomføring av et tiltak")
                .knyttetTilArbeidsoppgaveId(1L)
                .gjennomfoering("Dette tiltaket kan følges opp ved å gjennomføres");
    }

    public static RSTiltak rsTiltakLagreEksisterende() {
        return rsTiltakLagreNytt()
                .tiltakId(1L);
    }

    public static RSTiltak rsTiltak() {
        return rsTiltakLagreEksisterende()
                .kommentarer(asList(rsKommentarArbeidstaker(), rsKommentarArbeidsgiver()))
                .opprettetAv(rsArbeidstakeren())
                .sistEndretAv(rsArbeidstakeren());
    }

    private static RSKommentar rsKommentarArbeidstaker() {
        return new RSKommentar()
                .id(1L)
                .tekst("Dette tiltaket er ikke nyttig for meg")
                .opprettetTidspunkt(LocalDateTime.now().minusHours(4))
                .opprettetAv(rsArbeidstakeren())
                .sistEndretAv(rsArbeidstakeren());
    }

    private static RSKommentar rsKommentarArbeidsgiver() {
        return new RSKommentar()
                .id(2L)
                .tekst("Dette tiltaket ser veldig nyttig ut")
                .opprettetTidspunkt(LocalDateTime.now().minusHours(3))
                .opprettetAv(rsArbeidsgiveren())
                .sistEndretAv(rsArbeidsgiveren());
    }
}
