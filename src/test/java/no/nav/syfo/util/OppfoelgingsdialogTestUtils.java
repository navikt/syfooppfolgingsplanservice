package no.nav.syfo.util;

import no.nav.syfo.domain.*;

import java.time.LocalDateTime;

import static java.time.LocalDate.now;
import static java.util.Arrays.asList;

public class OppfoelgingsdialogTestUtils {

    private static final String SYKMELDT_AKTOERID = "1010101010101";
    private static final String ARBEIDSGIVER_FNR = "10101010100";
    private static final String ARBEIDSGIVER_AKTOERID = "1010101010100";
    public static final String VIRKSOMHETSNUMMER = "123456789";

    private static Person arbeidstakeren() {
        return new Person()
                .aktoerId(SYKMELDT_AKTOERID);
    }

    private static Person arbeidsgiveren() {
        return new Person()
                .aktoerId(ARBEIDSGIVER_AKTOERID);
    }

    private static Oppfoelgingsdialog oppfoelgingsdialogOpprettet() {
        return new Oppfoelgingsdialog()
                .id(1L)
                .status("UNDER_ARBEID")
                .opprettet(LocalDateTime.now().minusDays(7))
                .sistEndretDato(LocalDateTime.now())
                .virksomhet(new Virksomhet()
                        .virksomhetsnummer(VIRKSOMHETSNUMMER)
                )
                .sistEndretAvAktoerId(ARBEIDSGIVER_AKTOERID)
                .arbeidstaker(arbeidstakeren())
                .arbeidsgiver(arbeidsgiveren());
    }

    public static Oppfoelgingsdialog oppfoelgingsdialogGodkjentTvang() {
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
