package no.nav.syfo.testhelper;

import no.nav.syfo.mocks.AktoerMock;

public class UserConstants {

    public static final String ARBEIDSTAKER_FNR = "12345678912";
    public static final String ARBEIDSTAKER_AKTORID = AktoerMock.mockAktorId(ARBEIDSTAKER_FNR);
    public static final String LEDER_FNR = "12987654321";
    public static final String LEDER_AKTORID = AktoerMock.mockAktorId(LEDER_FNR);
    public static final String VIRKSOMHETSNUMMER = "123456789";
    public static final String NAV_ENHET = "0330";
    public static final String VEILEDER_ID = "Z999999";

    public static final String PERSON_NAME_FIRST = "First";
    public static final String PERSON_NAME_MIDDLE = "Middle";
    public static final String PERSON_NAME_LAST = "Last";

    public static final String PERSON_FULL_NAME = PERSON_NAME_FIRST + " " +  PERSON_NAME_MIDDLE + " " + PERSON_NAME_LAST;
}
