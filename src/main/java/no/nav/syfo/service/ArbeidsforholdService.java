package no.nav.syfo.service;

import no.nav.syfo.model.Stilling;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.binding.ArbeidsforholdV3;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.binding.FinnArbeidsforholdPrArbeidstakerSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.binding.FinnArbeidsforholdPrArbeidstakerUgyldigInput;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.NorskIdent;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Organisasjon;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Regelverker;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.meldinger.FinnArbeidsforholdPrArbeidstakerRequest;
import org.slf4j.Logger;
import org.springframework.cache.annotation.Cacheable;

import javax.inject.Inject;
import javax.xml.datatype.XMLGregorianCalendar;
import java.time.LocalDate;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.slf4j.LoggerFactory.getLogger;

public class ArbeidsforholdService {
    private static final Logger LOG = getLogger(ArbeidsforholdService.class);
    private static final Regelverker A_ORDNINGEN = new Regelverker();

    static {
        A_ORDNINGEN.setValue("A_ORDNINGEN");
    }

    @Inject
    private ArbeidsforholdV3 arbeidsforholdV3;

    @Inject
    private AktoerService aktoerService;

    private List<Stilling> hentArbeidsforholdMedFnr(String fnr, LocalDate fom, String orgnr) {
        if (isBlank(fnr) || !fnr.matches("\\d{11}$")) {
            LOG.error("Prøvde å hente arbeidsforhold");
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
            LOG.error("Feil ved henting av arbeidsforhold", e);
            throw new RuntimeException();
        }
    }

    @Cacheable(value = "arbeidsforhold", keyGenerator = "userkeygenerator")
    public List<Stilling> hentArbeidsforholdMedAktoerId(String aktoerId, LocalDate oppfoelgingsdialogStartDato, String orgnr) {
        return hentArbeidsforholdMedFnr(aktoerService.hentFnrForAktoer(aktoerId), oppfoelgingsdialogStartDato, orgnr);
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
