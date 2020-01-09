package no.nav.syfo.sts;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.syfo.LocalApplication;
import no.nav.syfo.metric.Metrikk;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.inject.Inject;

import static no.nav.syfo.util.RestUtils.basicCredentials;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = LocalApplication.class)
@DirtiesContext
public class StsConsumerTest {
    @MockBean
    private Metrikk metrikk;
    @Value("${srv.password}")
    private String password;
    @Inject
    private RestTemplate restTemplate;

    @Value("${security.token.service.rest.url}")
    private String url;
    @Value("${srv.username}")
    private String username;

    @Inject
    private StsConsumer stsConsumer;

    private MockRestServiceServer mockRestServiceServer;

    @Before
    public void setUp() {
        this.mockRestServiceServer = MockRestServiceServer.bindTo(restTemplate).build();
    }

    @After
    public void tearDown() {
        mockRestServiceServer.verify();
    }

    @Test
    public void token_returns_correct_token_and_cached_token() {
        StsToken expectedToken = new StsToken()
                .access_token("token")
                .token_type("type")
                .expires_in(3600);

        mockResponseFromSTS(expectedToken);

        String result = stsConsumer.token();
        String result2 = stsConsumer.token();

        assertThat(result).isEqualTo(expectedToken.access_token);
        assertThat(result2).isEqualTo(expectedToken.access_token);

    }

    private void mockResponseFromSTS(StsToken stsToken) {
        String credentials = basicCredentials(username, password);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, credentials);

        String responseBody = stsTokenAsJsonString(stsToken);

        final String uriString = UriComponentsBuilder.fromHttpUrl(url + "/rest/v1/sts/token?grant_type=client_credentials&scope=openid").toUriString();
        mockRestServiceServer.expect(once(), requestTo(uriString))
                .andExpect(method(GET))
                .andExpect(header(AUTHORIZATION, credentials))
                .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));
    }

    private String stsTokenAsJsonString(StsToken stsToken) {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            return objectMapper.writeValueAsString(stsToken);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
