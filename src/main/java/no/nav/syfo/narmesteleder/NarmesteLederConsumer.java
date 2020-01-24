package no.nav.syfo.narmesteleder;

import no.nav.syfo.aareg.AaregConsumer;
import no.nav.syfo.aareg.Arbeidsforhold;
import no.nav.syfo.aktorregister.AktorregisterConsumer;
import no.nav.syfo.azuread.AzureAdTokenConsumer;
import no.nav.syfo.metric.Metrikk;
import no.nav.syfo.model.*;
import no.nav.syfo.pdl.PdlConsumer;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static no.nav.syfo.aareg.OpplysningspliktigArbeidsgiver.Type.Organisasjon;
import static no.nav.syfo.util.MapUtil.mapListe;
import static no.nav.syfo.util.RestUtils.bearerHeader;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.OK;

@Component
public class NarmesteLederConsumer {
    private static final Logger LOG = getLogger(NarmesteLederConsumer.class);

    private final AaregConsumer aaregConsumer;
    private final AktorregisterConsumer aktorregisterConsumer;
    private final AzureAdTokenConsumer azureAdTokenConsumer;
    private final Metrikk metrikk;
    private final PdlConsumer pdlConsumer;
    private final RestTemplate restTemplate;
    private final String url;
    private final String syfonarmestelederId;

    public static final String HENT_ANSATTE_SYFONARMESTELEDER = "hent_ansatte_syfonarmesteleder";
    public static final String HENT_ANSATTE_SYFONARMESTELEDER_FEILET = "hent_ansatte_syfonarmesteleder_feilet";
    public static final String HENT_ANSATTE_SYFONARMESTELEDER_VELLYKKET = "hent_ansatte_syfonarmesteleder_vellykket";
    public static final String HENT_LEDER_SYFONARMESTELEDER = "hent_leder_syfonarmesteleder";
    public static final String HENT_LEDER_SYFONARMESTELEDER_FEILET = "hent_leder_syfonarmesteleder_feilet";
    public static final String HENT_LEDER_SYFONARMESTELEDER_VELLYKKET = "hent_leder_syfonarmesteleder_vellykket";

    public static final String ERROR_MESSAGE_BASE = "Kall mot syfonarmesteleder feiler med HTTP-";

    @Autowired
    public NarmesteLederConsumer(
            AaregConsumer aaregConsumer,
            AktorregisterConsumer aktorregisterConsumer,
            AzureAdTokenConsumer azureAdTokenConsumer,
            Metrikk metrikk,
            PdlConsumer pdlConsumer,
            RestTemplate restTemplateMedProxy,
            @Value("${syfonarmesteleder.url}") String url,
            @Value("${syfonarmesteleder.id}") String syfonarmestelederId
    ) {
        this.aaregConsumer = aaregConsumer;
        this.aktorregisterConsumer = aktorregisterConsumer;
        this.azureAdTokenConsumer = azureAdTokenConsumer;
        this.metrikk = metrikk;
        this.pdlConsumer = pdlConsumer;
        this.restTemplate = restTemplateMedProxy;
        this.url = url;
        this.syfonarmestelederId = syfonarmestelederId;
    }

    @Cacheable(value = "ansatte", key = "#aktorId", condition = "#aktorId != null")
    public List<Ansatt> ansatte(String aktorId) {
        metrikk.tellHendelse(HENT_ANSATTE_SYFONARMESTELEDER);
        String token = azureAdTokenConsumer.getAccessToken(syfonarmestelederId);

        ResponseEntity<List<NarmesteLederRelasjon>> response = restTemplate.exchange(
                getAnsatteUrl(aktorId),
                GET,
                entity(token),
                new ParameterizedTypeReference<List<NarmesteLederRelasjon>>() {
                }
        );

        if (response.getStatusCode() != OK) {
            metrikk.tellHendelse(HENT_ANSATTE_SYFONARMESTELEDER_FEILET);
            final String message = ERROR_MESSAGE_BASE + response.getStatusCode();
            LOG.error(message);
            throw new RuntimeException(message);
        }

        metrikk.tellHendelse(HENT_ANSATTE_SYFONARMESTELEDER_VELLYKKET);
        return mapListe(response.getBody(), narmestelederRelasjon2Ansatt);
    }

    @Cacheable(value = "leder", key = "#aktorId + #virksomhetsnummer", condition = "#aktorId != null && #virksomhetsnummer != null")
    public Optional<Naermesteleder> narmesteLeder(String aktorId, String virksomhetsnummer) {
        metrikk.tellHendelse(HENT_LEDER_SYFONARMESTELEDER);
        String token = azureAdTokenConsumer.getAccessToken(syfonarmestelederId);

        ResponseEntity<NarmestelederResponse> response = restTemplate.exchange(
                getLederUrl(aktorId, virksomhetsnummer),
                GET,
                entity(token),
                NarmestelederResponse.class
        );

        if (response.getStatusCode() != OK) {
            metrikk.tellHendelse(HENT_LEDER_SYFONARMESTELEDER_FEILET);
            final String message = ERROR_MESSAGE_BASE + response.getStatusCode();
            LOG.error(message);
            throw new RuntimeException(message);
        }

        if (response.getBody().narmesteLederRelasjon == null) {
            return Optional.empty();
        }

        NarmesteLederRelasjon relasjon = response.getBody().narmesteLederRelasjon;

        String lederAktorId = relasjon.narmesteLederAktorId;
        String lederFnr = aktorregisterConsumer.hentFnrForAktor(lederAktorId);
        String lederNavn = pdlConsumer.person(lederFnr).getName();

        metrikk.tellHendelse(HENT_LEDER_SYFONARMESTELEDER_VELLYKKET);
        return Optional.of(narmestelederRelasjon2Leder(relasjon, lederNavn));
    }

    private HttpEntity entity(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, bearerHeader(token));
        return new HttpEntity<>(headers);
    }

    private String getAnsatteUrl(String aktoerId) {
        return url + "/syfonarmesteleder/narmesteLeder/" + aktoerId;
    }

    private String getLederUrl(String aktoerId, String virksomhetsnummer) {
        return UriComponentsBuilder.fromHttpUrl(url + "/syfonarmesteleder/sykmeldt/" + aktoerId).queryParam("orgnummer", virksomhetsnummer).toUriString();
    }

    private static Function<NarmesteLederRelasjon, Ansatt> narmestelederRelasjon2Ansatt = narmesteLederRelasjon ->
            new Ansatt()
                    .aktoerId(narmesteLederRelasjon.aktorId)
                    .virksomhetsnummer(narmesteLederRelasjon.orgnummer);

    private Naermesteleder narmestelederRelasjon2Leder(NarmesteLederRelasjon narmesteLederRelasjon, String lederNavn) {
        return new Naermesteleder()
                .epost(narmesteLederRelasjon.narmesteLederEpost)
                .mobil(narmesteLederRelasjon.narmesteLederTelefonnummer)
                .naermesteLederStatus(
                        new NaermesteLederStatus()
                                .erAktiv(true)
                                .aktivFom(narmesteLederRelasjon.aktivFom)
                                .aktivTom(null))
                .orgnummer(narmesteLederRelasjon.orgnummer)
                .navn(lederNavn)
                .naermesteLederAktoerId(narmesteLederRelasjon.narmesteLederAktorId);
    }

    public boolean erAktorLederForAktor(String naermesteLederAktorId, String ansattAktorId) {
        List<String> ansatteAktorId = ansatte(naermesteLederAktorId).stream()
                .map(Ansatt::aktoerId)
                .collect(toList());

        return ansatteAktorId.contains(ansattAktorId);
    }

    private LocalDate tilLocalDate(String date) {
        return LocalDate.parse(date);
    }
}