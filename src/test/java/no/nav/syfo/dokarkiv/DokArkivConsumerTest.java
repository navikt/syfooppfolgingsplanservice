package no.nav.syfo.dokarkiv;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import no.nav.syfo.LocalApplication;
import no.nav.syfo.domain.GodkjentPlan;
import no.nav.syfo.domain.Oppfoelgingsdialog;
import no.nav.syfo.domain.Person;
import no.nav.syfo.domain.Virksomhet;
import no.nav.syfo.metric.Metrikk;
import no.nav.syfo.sts.StsConsumer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.springframework.test.web.client.ExpectedCount.once;

import javax.inject.Inject;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = LocalApplication.class)
@DirtiesContext
public class DokArkivConsumerTest {

    @Inject
    private RestTemplate restTemplate;

    @MockBean
    private StsConsumer stsConsumer;

    @Value("${dokarkiv.url}")
    private String url;


    private DokArkivConsumer dokArkivConsumer;

    @Inject
    private Metrikk metrikk;

    private MockRestServiceServer mockRestServiceServer;

    private final String DOKARKIV_URL = "https://url.nav.no";

    private static final String AKTOR_ID = "K1234";
    private static final Virksomhet KAKEBUA = new Virksomhet()
            .navn("Kakebua")
            .virksomhetsnummer("123456789");
    private static final Person KAKEMONSTERET = new Person()
            .navn("Kake M. Onster")
            .fnr("01010100099")
            .aktoerId(AKTOR_ID);

    @Before
    public void setUp() {
        this.mockRestServiceServer = MockRestServiceServer.bindTo(restTemplate).build();

        when(stsConsumer.token()).thenReturn("token");
        dokArkivConsumer = new DokArkivConsumer(restTemplate, DOKARKIV_URL, stsConsumer, metrikk);
    }

    @After
    public void tearDown() {
        mockRestServiceServer.verify();
    }

    @Test
    public void journalforOppfolgingsplan() {
        String responseBody = journalPostResponseAsJsonString();
        mockRestServiceServer.expect(once(), requestTo(DOKARKIV_URL + "/rest/journalpostapi/v1/journalpost"))
                .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));
        Oppfoelgingsdialog oppfolgingsplan = new Oppfoelgingsdialog()
                .virksomhet(KAKEBUA)
                .arbeidstaker(KAKEMONSTERET)
                .sistEndretAvAktoerId(AKTOR_ID);
        byte[] dokument = "dokument".getBytes();
        GodkjentPlan godkjentplan = new GodkjentPlan()
                .dokument(dokument);

        Integer journpostID = dokArkivConsumer.journalforOppfolgingsplan(oppfolgingsplan, godkjentplan);

        assertThat(journpostID).isEqualTo(123);
    }

    private String journalPostResponseAsJsonString() {
        ObjectMapper objectMapper = new ObjectMapper();
        JavaTimeModule module = new JavaTimeModule();
        objectMapper.registerModule(module);

        List<DokumentInfo> dokumentListe = new ArrayList<>();
        DokumentInfo dokumentInfo = new DokumentInfo();
        dokumentListe.add(dokumentInfo);
        JournalpostResponse journalpostResponse = new JournalpostResponse()
                .journalpostId(123)
                .journalpostferdigstilt(true)
                .journalstatus("ENDELIG")
                .melding(null)
                .dokumenter(dokumentListe);

        try {
            return objectMapper.writeValueAsString(journalpostResponse);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
