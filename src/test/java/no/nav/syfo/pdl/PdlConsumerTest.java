package no.nav.syfo.pdl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.syfo.LocalApplication;
import no.nav.syfo.metric.Metrikk;
import no.nav.syfo.pdl.exceptions.*;
import no.nav.syfo.sts.StsConsumer;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.inject.Inject;

import static java.util.Collections.singletonList;
import static no.nav.syfo.pdl.PdlConsumer.*;
import static no.nav.syfo.util.RestUtils.bearerHeader;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = LocalApplication.class)
@DirtiesContext
public class PdlConsumerTest {
    @MockBean
    private Metrikk metrikk;
    @Inject
    private RestTemplate restTemplate;
    @MockBean
    private StsConsumer stsConsumer;

    @Value("${pdl.url}")
    private String url;

    @Inject
    private PdlConsumer pdlConsumer;

    private MockRestServiceServer mockRestServiceServer;

    @Before
    public void setUp() {
        this.mockRestServiceServer = MockRestServiceServer.bindTo(restTemplate).build();
        when(stsConsumer.token()).thenReturn("token");
    }

    @After
    public void tearDown() {
        mockRestServiceServer.verify();
    }

    @Test
    public void person_returns_correct_name() {
        PdlHentPerson expectedPdlHentPerson = new PdlHentPerson()
                .hentPerson(new PdlPerson()
                        .navn(singletonList(new PdlPersonNavn()
                                .fornavn("Fornavn")
                                .mellomnavn("Mellomnavn")
                                .etternavn("Etternavn"))
                        )
                );

        PdlPersonResponse pdlPersonResponse = new PdlPersonResponse()
                .errors(null)
                .data(expectedPdlHentPerson);

        mockResponseFromPDL(pdlPersonResponse);

        PdlHentPerson result = pdlConsumer.person("321");

        assertThat(result.getName()).isEqualTo(expectedPdlHentPerson.getName());
    }

    @Test(expected = EmptyPDLContent.class)
    public void exception_when_empty_responseBody_from_pdl() {
        mockResponseFromPDL(null);

        pdlConsumer.person("123");
    }

    @Test(expected = PDLResponseBodyContainsError.class)
    public void exception_when_errors_in_responseBody() {
        PdlError error = new PdlError()
                .message("Fant ikke person")
                .extensions(new PdlErrorExtension()
                        .code("not_found")
                        .classification("ExecutionAborted"));

        PdlPersonResponse pdlPersonResponse = new PdlPersonResponse()
                .errors(singletonList(error))
                .data(new PdlHentPerson().hentPerson(null));

        mockResponseFromPDL(pdlPersonResponse);

        pdlConsumer.person("123");
    }

    @Test(expected = NameFromPDLIsNull.class)
    public void exception_when_getName_returns_null() {
        PdlPersonResponse pdlPersonResponse = new PdlPersonResponse()
                .errors(null)
                .data(new PdlHentPerson().hentPerson(null));

        mockResponseFromPDL(pdlPersonResponse);

        pdlConsumer.person("123");
    }

    private void mockResponseFromPDL(PdlPersonResponse pdlPersonResponse) {
        String responseBody = pdlResponseAsJsonString(pdlPersonResponse);

        String token = stsConsumer.token();

        final String uriString = UriComponentsBuilder.fromHttpUrl(url).toUriString();
        mockRestServiceServer.expect(once(), requestTo(uriString))
                .andExpect(method(POST))
                .andExpect(header(TEMA_HEADER, ALLE_TEMA_HEADERVERDI))
                .andExpect(header(AUTHORIZATION, bearerHeader(token)))
                .andExpect(header(NAV_CONSUMER_TOKEN_HEADER, bearerHeader(token)))
                .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));
    }

    private String pdlResponseAsJsonString(PdlPersonResponse response) {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            return objectMapper.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
