package no.nav.syfo.narmesteleder;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;
import static no.nav.syfo.config.CacheConfig.CACHENAME_ANSATTE;
import static no.nav.syfo.config.CacheConfig.CACHENAME_LEDER;
import static no.nav.syfo.util.CredentialUtilKt.bearerHeader;
import static no.nav.syfo.util.MapUtil.mapListe;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.OK;

import no.nav.syfo.azuread.AzureAdTokenConsumer;
import no.nav.syfo.metric.Metrikk;
import no.nav.syfo.model.Ansatt;
import no.nav.syfo.model.Naermesteleder;
import no.nav.syfo.pdl.PdlConsumer;
import no.nav.syfo.pdl.exceptions.NameFromPDLIsNull;

@Component
public class NarmesteLederConsumer {
    public static final String HENT_ANSATTE_SYFONARMESTELEDER = "hent_ansatte_syfonarmesteleder";
    public static final String HENT_ANSATTE_SYFONARMESTELEDER_FEILET = "hent_ansatte_syfonarmesteleder_feilet";
    public static final String HENT_ANSATTE_SYFONARMESTELEDER_VELLYKKET = "hent_ansatte_syfonarmesteleder_vellykket";
    public static final String HENT_LEDER_SYFONARMESTELEDER = "hent_leder_syfonarmesteleder";
    public static final String HENT_LEDER_SYFONARMESTELEDER_FEILET = "hent_leder_syfonarmesteleder_feilet";
    public static final String HENT_LEDER_SYFONARMESTELEDER_VELLYKKET = "hent_leder_syfonarmesteleder_vellykket";
    public static final String ERROR_MESSAGE_BASE = "Kall mot syfonarmesteleder feiler med HTTP-";
    private static final Logger LOG = getLogger(NarmesteLederConsumer.class);
    private static final Function<NarmesteLederRelasjon, Ansatt> narmestelederRelasjon2Ansatt = narmesteLederRelasjon ->
            new Ansatt()
                    .fnr(narmesteLederRelasjon.fnr)
                    .virksomhetsnummer(narmesteLederRelasjon.orgnummer);
    private final AzureAdTokenConsumer azureAdTokenConsumer;
    private final NarmesteLederRelasjonConverter narmesteLederRelasjonConverter;
    private final Metrikk metrikk;
    private final PdlConsumer pdlConsumer;
    private final RestTemplate restTemplate;
    private final String narmestelederUrl;
    private final String narmestelederScope;

    @Autowired
    public NarmesteLederConsumer(
            AzureAdTokenConsumer azureAdTokenConsumer,
            NarmesteLederRelasjonConverter narmesteLederRelasjonConverter,
            Metrikk metrikk,
            PdlConsumer pdlConsumer,
            RestTemplate restTemplateMedProxy,
            @Value("${narmesteleder.url}") String narmestelederUrl,
            @Value("${narmesteleder.scope}") String narmestelederScope
    ) {
        this.azureAdTokenConsumer = azureAdTokenConsumer;
        this.narmesteLederRelasjonConverter = narmesteLederRelasjonConverter;
        this.metrikk = metrikk;
        this.pdlConsumer = pdlConsumer;
        this.restTemplate = restTemplateMedProxy;
        this.narmestelederUrl = narmestelederUrl;
        this.narmestelederScope = narmestelederScope;
    }

    @Cacheable(value = CACHENAME_ANSATTE, key = "#fnr", condition = "#fnr != null")
    public List<Ansatt> ansatte(String fnr) {
        metrikk.tellHendelse(HENT_ANSATTE_SYFONARMESTELEDER);
        String token = azureAdTokenConsumer.getAccessToken(narmestelederScope);

        ResponseEntity<List<NarmesteLederRelasjon>> response = restTemplate.exchange(
                getAnsatteUrl(),
                GET,
                entityForNarmesteLeder(token, fnr),
                new ParameterizedTypeReference<>() {
                }
        );

        throwExceptionIfError(response.getStatusCode(), HENT_ANSATTE_SYFONARMESTELEDER_FEILET);

        metrikk.tellHendelse(HENT_ANSATTE_SYFONARMESTELEDER_VELLYKKET);
        return mapListe(response.getBody(), narmestelederRelasjon2Ansatt);
    }

    @Cacheable(value = CACHENAME_LEDER, key = "#fnr + #virksomhetsnummer", condition = "#fnr != null && #virksomhetsnummer != null")
    public Optional<Naermesteleder> narmesteLeder(String fnr, String virksomhetsnummer) {
        metrikk.tellHendelse(HENT_LEDER_SYFONARMESTELEDER);
        String token = azureAdTokenConsumer.getAccessToken(narmestelederScope);

        ResponseEntity<NarmestelederResponse> response = restTemplate.exchange(
                getLederUrl(virksomhetsnummer),
                GET,
                entityForSykmeldt(token, fnr),
                NarmestelederResponse.class
        );
        throwExceptionIfError(response.getStatusCode(), HENT_LEDER_SYFONARMESTELEDER_FEILET);

        if (response.getBody().narmesteLederRelasjon == null) {
            return Optional.empty();
        }

        NarmesteLederRelasjon relasjon = response.getBody().narmesteLederRelasjon;

        String lederFnr = relasjon.narmesteLederFnr;
        String lederNavn = Optional.ofNullable(pdlConsumer.personName(lederFnr)).orElseThrow(() -> new NameFromPDLIsNull("Name of leader was null"));

        metrikk.tellHendelse(HENT_LEDER_SYFONARMESTELEDER_VELLYKKET);
        return Optional.of(narmesteLederRelasjonConverter.convert(relasjon, lederNavn));
    }

    private void throwExceptionIfError(HttpStatus statusCode, String metricEventKey) {
        if (statusCode != OK) {
            metrikk.tellHendelse(metricEventKey);
            final String message = ERROR_MESSAGE_BASE + statusCode;
            LOG.error(message);
            throw new RuntimeException(message);
        }
    }

    private HttpEntity entityForSykmeldt(String token, String sykmeldtFnr) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, bearerHeader(token));
        headers.add("Sykmeldt-Fnr", sykmeldtFnr);
        return new HttpEntity<>(headers);
    }

    private HttpEntity entityForNarmesteLeder(String token, String lederFnr) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, bearerHeader(token));
        headers.add("Narmeste-Leder-Fnr", lederFnr);
        return new HttpEntity<>(headers);
    }

    private String getAnsatteUrl() {
        return narmestelederUrl + "/leder/narmesteleder/aktive";
    }

    private String getLederUrl(String virksomhetsnummer) {
        return UriComponentsBuilder.fromHttpUrl(narmestelederUrl + "/sykmeldt/narmesteleder").queryParam("orgnummer", virksomhetsnummer).toUriString();
    }

    public boolean erNaermesteLederForAnsatt(String naermesteLederFnr, String ansattFnr) {
        List<String> ansatteFnr = ansatte(naermesteLederFnr).stream()
                .map(Ansatt::fnr)
                .collect(toList());

        return ansatteFnr.contains(ansattFnr);
    }
}
