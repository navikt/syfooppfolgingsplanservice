package no.nav.syfo.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.syfo.aktorregister.AktorregisterConsumer;
import no.nav.syfo.model.Stilling;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.binding.*;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.*;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.meldinger.FinnArbeidsforholdPrArbeidstakerRequest;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.xml.datatype.XMLGregorianCalendar;
import java.time.LocalDate;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Slf4j
@Service
public class ArbeidsforholdService {

    private static final Regelverker A_ORDNINGEN = new Regelverker();

    static {
        A_ORDNINGEN.setValue("A_ORDNINGEN");
    }

    private ArbeidsforholdV3 arbeidsforholdV3;

    private AktorregisterConsumer aktorregisterConsumer;

    @Inject
    public ArbeidsforholdService(
            ArbeidsforholdV3 arbeidsforholdV3,
            AktorregisterConsumer aktorregisterConsumer
    ) {
        this.arbeidsforholdV3 = arbeidsforholdV3;
        this.aktorregisterConsumer = aktorregisterConsumer;
    }

    private List<Stilling> hentArbeidsforholdMedFnr(String fnr, LocalDate fom, String orgnr) {
        if (isBlank(fnr) || !fnr.matches("\\d{11}$")) {
            log.error("Prøvde å hente arbeidsforhold");
            throw new RuntimeException();
        }
        try {
            FinnArbeidsforholdPrArbeidstakerRequest request = lagArbeidsforholdRequest(fnr);
            return arbeidsforholdV3.finnArbeidsforholdPrArbeidstaker(request).getArbeidsforhold().stream()
                    .filter(arbeidsforhold -> arbeidsforhold.getArbeidsgiver() instanceof Organisasjon)
                    .filter(arbeidsforhold -> ((Organisasjon) arbeidsforhold.getArbeidsgiver()).getOrgnummer().equals(orgnr))
                    .filter(arbeidsforhold -> arbeidsforhold.getAnsettelsesPeriode().getPeriode().getTom() == null || !tilLocalDate(arbeidsforhold.getAnsettelsesPeriode().getPeriode().getTom()).isBefore(fom))
                    .flatMap(arbeidsforhold -> arbeidsforhold.getArbeidsavtale().stream())
                    .map(arbeidsavtale -> new Stilling()
                            .yrke(arbeidsavtale.getYrke().getValue())
                            .prosent(arbeidsavtale.getStillingsprosent())
                    )
                    .collect(toList());
        } catch (FinnArbeidsforholdPrArbeidstakerUgyldigInput | FinnArbeidsforholdPrArbeidstakerSikkerhetsbegrensning e) {
            log.error("Feil ved henting av arbeidsforhold", e);
            throw new RuntimeException();
        }
    }

    public List<Stilling> hentArbeidsforholdMedAktoerId(String aktoerId, LocalDate oppfoelgingsdialogStartDato, String orgnr) {
        return hentArbeidsforholdMedFnr(aktorregisterConsumer.hentFnrForAktor(aktoerId), oppfoelgingsdialogStartDato, orgnr);
    }

    private LocalDate tilLocalDate(XMLGregorianCalendar xmlGregorianCalendar) {
        return xmlGregorianCalendar.toGregorianCalendar().toZonedDateTime().toLocalDate();
    }

    private FinnArbeidsforholdPrArbeidstakerRequest lagArbeidsforholdRequest(String fnr) {
        FinnArbeidsforholdPrArbeidstakerRequest request = new FinnArbeidsforholdPrArbeidstakerRequest();
        request.setRapportertSomRegelverk(A_ORDNINGEN);
        request.setIdent(ident(fnr));
        return request;
    }

    private NorskIdent ident(String fodselsnummer) {
        NorskIdent ident = new NorskIdent();
        ident.setIdent(fodselsnummer);
        return ident;
    }
}
