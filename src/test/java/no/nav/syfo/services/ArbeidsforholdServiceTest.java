package no.nav.syfo.services;

import no.nav.syfo.model.Stilling;
import no.nav.syfo.service.AktoerService;
import no.nav.syfo.service.ArbeidsforholdService;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.binding.ArbeidsforholdV3;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.*;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.meldinger.FinnArbeidsforholdPrArbeidstakerResponse;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.xml.datatype.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.GregorianCalendar;
import java.util.List;

import static java.time.LocalDate.now;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ArbeidsforholdServiceTest {

    @Mock
    private ArbeidsforholdV3 arbeidsforholdV3;
    @Mock
    private AktoerService aktoerService;

    @InjectMocks
    private ArbeidsforholdService arbeidsforholdService;

    @Before
    public void setup() {
        when(aktoerService.hentFnrForAktoer(anyString())).thenReturn("12345678901");
    }

    @Test
    public void skalFiltereBortUtdaterteAnsettelser() throws Exception {
        when(arbeidsforholdV3.finnArbeidsforholdPrArbeidstaker(any())).thenReturn(finnArbeidsforholdPrArbeidstakerResponse());
        List<Stilling> stillinger = arbeidsforholdService.hentArbeidsforholdMedAktoerId("1234567890123", now().minusDays(10), "orgnummer");
        AssertionsForClassTypes.assertThat(stillinger.size()).isEqualTo(2);
    }

    private FinnArbeidsforholdPrArbeidstakerResponse finnArbeidsforholdPrArbeidstakerResponse() {
        FinnArbeidsforholdPrArbeidstakerResponse finnArbeidsforholdPrArbeidstakerResponse = new FinnArbeidsforholdPrArbeidstakerResponse();
        finnArbeidsforholdPrArbeidstakerResponse.getArbeidsforhold().add(aktivtArbeidsforhold());
        finnArbeidsforholdPrArbeidstakerResponse.getArbeidsforhold().add(inaktivtArbeidsforhold());
        finnArbeidsforholdPrArbeidstakerResponse.getArbeidsforhold().add(tomDatoNullArbeidsforhold());
        return finnArbeidsforholdPrArbeidstakerResponse;
    }

    private Arbeidsforhold aktivtArbeidsforhold() {
        Arbeidsforhold arbeidsforhold = new Arbeidsforhold();
        Organisasjon organisasjon = new Organisasjon();
        organisasjon.setOrgnummer("orgnummer");
        arbeidsforhold.setArbeidsgiver(organisasjon);
        Arbeidsavtale arbeidsavtale = new Arbeidsavtale();
        Yrker yrke = new Yrker();
        yrke.setValue("aktivtArbeidsforhold");
        arbeidsavtale.setYrke(yrke);
        arbeidsavtale.setStillingsprosent(new BigDecimal(20));
        arbeidsforhold.getArbeidsavtale().add(arbeidsavtale);

        AnsettelsesPeriode ansettelsesPeriode = new AnsettelsesPeriode();
        Gyldighetsperiode gyldighetsperiode = new Gyldighetsperiode();

        gyldighetsperiode.setTom(toXMLGregorianCalendar(now().plusDays(10)));
        ansettelsesPeriode.setPeriode(gyldighetsperiode);
        arbeidsforhold.setAnsettelsesPeriode(ansettelsesPeriode);

        return arbeidsforhold;
    }

    private Arbeidsforhold inaktivtArbeidsforhold() {
        Arbeidsforhold arbeidsforhold = new Arbeidsforhold();
        Organisasjon organisasjon = new Organisasjon();
        organisasjon.setOrgnummer("orgnummer");
        arbeidsforhold.setArbeidsgiver(organisasjon);
        Arbeidsavtale arbeidsavtale = new Arbeidsavtale();
        Yrker yrke = new Yrker();
        yrke.setValue("inaktivtArbeidsforhold");
        arbeidsavtale.setYrke(yrke);
        arbeidsavtale.setStillingsprosent(new BigDecimal(20));

        AnsettelsesPeriode ansettelsesPeriode = new AnsettelsesPeriode();
        Gyldighetsperiode gyldighetsperiode = new Gyldighetsperiode();

        gyldighetsperiode.setTom(toXMLGregorianCalendar(now().minusDays(20)));
        ansettelsesPeriode.setPeriode(gyldighetsperiode);
        arbeidsforhold.setAnsettelsesPeriode(ansettelsesPeriode);
        arbeidsforhold.getArbeidsavtale().add(arbeidsavtale);

        return arbeidsforhold;
    }

    private Arbeidsforhold tomDatoNullArbeidsforhold() {
        Arbeidsforhold arbeidsforhold = new Arbeidsforhold();
        Organisasjon organisasjon = new Organisasjon();
        organisasjon.setOrgnummer("orgnummer");
        arbeidsforhold.setArbeidsgiver(organisasjon);
        Arbeidsavtale arbeidsavtale = new Arbeidsavtale();
        Yrker yrke = new Yrker();
        yrke.setValue("tomDatoNullArbeidsforhold");
        arbeidsavtale.setYrke(yrke);
        arbeidsavtale.setStillingsprosent(new BigDecimal(20));

        AnsettelsesPeriode ansettelsesPeriode = new AnsettelsesPeriode();
        Gyldighetsperiode gyldighetsperiode = new Gyldighetsperiode();

        gyldighetsperiode.setTom(null);
        ansettelsesPeriode.setPeriode(gyldighetsperiode);
        arbeidsforhold.setAnsettelsesPeriode(ansettelsesPeriode);
        arbeidsforhold.getArbeidsavtale().add(arbeidsavtale);

        return arbeidsforhold;
    }

    private XMLGregorianCalendar toXMLGregorianCalendar(LocalDate dato) {
        GregorianCalendar gregorianCalendar = GregorianCalendar.from(dato.atStartOfDay(ZoneId.systemDefault()));
        try {
            return DatatypeFactory.newInstance().newXMLGregorianCalendar(gregorianCalendar);
        } catch (DatatypeConfigurationException e) {
            throw new RuntimeException();
        }
    }
}
