package no.nav.syfo.mocks;

import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.binding.*;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.*;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.meldinger.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

import static no.nav.syfo.config.ws.wsconfig.AAregConfig.MOCK_KEY;

@Service
@ConditionalOnProperty(value = MOCK_KEY, havingValue = "true")
public class ArbeidsforholdMock implements ArbeidsforholdV3 {
    public FinnArbeidsforholdPrArbeidsgiverResponse finnArbeidsforholdPrArbeidsgiver(FinnArbeidsforholdPrArbeidsgiverRequest parameters)
            throws FinnArbeidsforholdPrArbeidsgiverForMangeForekomster, FinnArbeidsforholdPrArbeidsgiverSikkerhetsbegrensning, FinnArbeidsforholdPrArbeidsgiverUgyldigInput {
        throw new RuntimeException("Ikke implementert i mock. Se ArbeidsforholdMock");
    }

    public FinnArbeidsforholdPrArbeidstakerResponse finnArbeidsforholdPrArbeidstaker(FinnArbeidsforholdPrArbeidstakerRequest parameters)
            throws FinnArbeidsforholdPrArbeidstakerSikkerhetsbegrensning, FinnArbeidsforholdPrArbeidstakerUgyldigInput {
        Arbeidsforhold arbeidsforhold = new Arbeidsforhold();
        Organisasjon arbeidsgiver = new Organisasjon();
        arbeidsgiver.setOrgnummer("991651365");
        arbeidsforhold.setArbeidsgiver(arbeidsgiver);
        AnsettelsesPeriode ansettelsesPeriode = new AnsettelsesPeriode();
        Gyldighetsperiode periode = new Gyldighetsperiode();
        periode.setFom(null);
        periode.setTom(null);
        ansettelsesPeriode.setPeriode(periode);
        arbeidsforhold.setAnsettelsesPeriode(ansettelsesPeriode);
        Arbeidsavtale arbeidsavtale = new Arbeidsavtale();
        Yrker yrker = new Yrker();
        yrker.setValue("Snekker");
        arbeidsavtale.setYrke(yrker);
        arbeidsavtale.setStillingsprosent(new BigDecimal(100.00));
        arbeidsforhold.getArbeidsavtale().add(arbeidsavtale);
        FinnArbeidsforholdPrArbeidstakerResponse response = new FinnArbeidsforholdPrArbeidstakerResponse();
        response.getArbeidsforhold().add(arbeidsforhold);
        return response;
    }

    public HentArbeidsforholdHistorikkResponse hentArbeidsforholdHistorikk(HentArbeidsforholdHistorikkRequest parameters)
            throws HentArbeidsforholdHistorikkArbeidsforholdIkkeFunnet, HentArbeidsforholdHistorikkSikkerhetsbegrensning {
        throw new RuntimeException("Ikke implementert i mock. Se ArbeidsforholdMock");
    }

    public FinnArbeidstakerePrArbeidsgiverResponse finnArbeidstakerePrArbeidsgiver(FinnArbeidstakerePrArbeidsgiverRequest parameters)
            throws FinnArbeidstakerePrArbeidsgiverSikkerhetsbegrensning, FinnArbeidstakerePrArbeidsgiverUgyldigInput {
        throw new RuntimeException("Ikke implementert i mock. Se ArbeidsforholdMock");
    }

    public void ping() {
    }
}
