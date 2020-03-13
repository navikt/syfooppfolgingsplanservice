package no.nav.syfo.mock;

import no.nav.syfo.domain.*;

import java.time.LocalDateTime;

import static java.time.LocalDate.now;
import static java.util.Arrays.asList;
import static no.nav.syfo.testhelper.UserConstants.*;

public class MockOppfolgingsplan {

    private static Person arbeidstakeren() {
        return new Person()
                .aktoerId(ARBEIDSTAKER_AKTORID);
    }

    private static Person arbeidsgiveren() {
        return new Person()
                .aktoerId(LEDER_AKTORID);
    }

    public static Oppfolgingsplan oppfoelgingsdialogOpprettet() {
        return new Oppfolgingsplan()
                .id(1L)
                .status("UNDER_ARBEID")
                .opprettet(LocalDateTime.now().minusDays(7))
                .sistEndretDato(LocalDateTime.now())
                .virksomhet(new Virksomhet()
                        .virksomhetsnummer(VIRKSOMHETSNUMMER)
                )
                .sistEndretAvAktoerId(LEDER_AKTORID)
                .arbeidstaker(arbeidstakeren())
                .arbeidsgiver(arbeidsgiveren());
    }

    public static Oppfolgingsplan oppfoelgingsdialogGodkjentTvang() {
        return oppfoelgingsdialogOpprettet()
                .godkjentPlan(java.util.Optional.ofNullable(new GodkjentPlan()
                        .opprettetTidspunkt(LocalDateTime.now().minusDays(1))
                        .tvungenGodkjenning(true)
                        .gyldighetstidspunkt(new Gyldighetstidspunkt()
                                .fom(now().plusDays(3))
                                .tom(now().plusDays(33))
                                .evalueres(now().plusDays(40))
                        )))
                .tiltakListe(asList(
                        new Tiltak()
                ))
                .arbeidsoppgaveListe(asList(
                        new Arbeidsoppgave()
                ));
    }
}
