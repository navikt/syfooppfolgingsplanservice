package no.nav.syfo.aareg.utils;

import no.nav.syfo.aareg.*;
import no.nav.syfo.aareg.OpplysningspliktigArbeidsgiver.Type;

import java.time.LocalDate;
import java.util.Collections;

public class AaregConsumerTestUtils {
    public static final String ORGNUMMER = "123456789";
    public static final String WRONG_ORGNUMMER = "987654321";
    public static final String AT_AKTORID = "1234567890987";
    public static final String AT_FNR = "12345678901";
    public static final LocalDate VALID_DATE = LocalDate.now().plusMonths(1);
    public static final LocalDate PASSED_DATE = LocalDate.of(1970, 1, 2);
    public static final String YRKESKODE = "1234567";
    public static final String YRKESNAVN = "yrkesnavn";
    public static final Double STILLINGSPROSENT = 50.0;

    public static Arbeidsforhold simpleArbeidsforhold() {
        return new Arbeidsforhold()
                .arbeidsgiver(new OpplysningspliktigArbeidsgiver()
                        .organisasjonsnummer(ORGNUMMER)
                        .type(OpplysningspliktigArbeidsgiver.Type.Organisasjon))
                .arbeidstaker(new Person()
                        .aktoerId(AT_AKTORID)
                        .type(Person.Type.Person)
                        .offentligIdent(AT_FNR));
    }

    public static Arbeidsforhold validArbeidsforhold() {
        return mockArbeidsforhold(ORGNUMMER, Type.Organisasjon, VALID_DATE);
    }

    public static Arbeidsforhold arbeidsforholdTypePerson() {
        return mockArbeidsforhold(ORGNUMMER, Type.Person, VALID_DATE);
    }

    public static Arbeidsforhold arbeidsforholdWithPassedDate() {
        return mockArbeidsforhold(ORGNUMMER, Type.Organisasjon, PASSED_DATE);
    }

    public static Arbeidsforhold arbeidsforholdWithWrongOrgnummer() {
        return mockArbeidsforhold(WRONG_ORGNUMMER, Type.Organisasjon, VALID_DATE);
    }

    private static Arbeidsforhold mockArbeidsforhold(String orgnummer, Type type, LocalDate tom) {
        return new Arbeidsforhold()
                .arbeidsgiver(new OpplysningspliktigArbeidsgiver()
                        .organisasjonsnummer(orgnummer)
                        .type(type))
                .arbeidstaker(new Person()
                        .aktoerId(AT_AKTORID)
                        .type(Person.Type.Person)
                        .offentligIdent(AT_FNR))
                .ansettelsesperiode(new Ansettelsesperiode()
                        .periode(new Periode()
                                .fom(LocalDate.now().minusYears(1).toString())
                                .tom(tom.toString())))
                .arbeidsavtaler(Collections.singletonList(new Arbeidsavtale()
                        .yrke(YRKESKODE)
                        .stillingsprosent(STILLINGSPROSENT)));
    }
}
