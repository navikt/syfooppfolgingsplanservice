package no.nav.syfo.azuread;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import no.nav.syfo.LocalApplication;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import javax.inject.Inject;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = LocalApplication.class)
@DirtiesContext
public class AzureAdTokenConsumerTest {
    @Inject
    private RestTemplate restTemplateMedProxy;

    private AzureAdTokenConsumer azureAdTokenConsumer;

    private MockRestServiceServer mockRestServiceServer;

    private final String TOKEN_URL = "https://url.nav.no";

    @Before
    public void setup() {
        this.mockRestServiceServer = MockRestServiceServer.bindTo(restTemplateMedProxy).build();
        azureAdTokenConsumer = new AzureAdTokenConsumer(restTemplateMedProxy, TOKEN_URL, "clientId", "clientSecret");
    }

    @After
    public void tearDown() {
        mockRestServiceServer.verify();
    }

    @Test
    public void henterTokenFraAzureHvisTokenMangler() {
        Instant expiresOn = Instant.now().plusSeconds(300L);
        String tokenGyldig = "token";
        String responseBody = azureAdResponseAsJsonString(expiresOn, "ressursID", tokenGyldig);
        mockRestServiceServer.expect(once(), requestTo(TOKEN_URL)).andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

        final String token = azureAdTokenConsumer.getAccessToken("test");

        assertThat(token).isEqualTo(tokenGyldig);
    }

    @Test
    public void henterTokenFraAzureHvisTokenErUtlopt() {
        Instant expiresOnUtlopt = Instant.now().minusSeconds(30L);
        String tokenUtlopt = "token_utlopt";
        String responseBodyUtlopt = azureAdResponseAsJsonString(expiresOnUtlopt, "ressursID", tokenUtlopt);

        Instant expiresOnGyldig = Instant.now().plusSeconds(300L);
        String tokenGyldig = "token_gyldig";
        String responseBodyGyldig = azureAdResponseAsJsonString(expiresOnGyldig, "ressursID", tokenGyldig);

        mockRestServiceServer.expect(once(), requestTo(TOKEN_URL)).andRespond(withSuccess(responseBodyUtlopt, MediaType.APPLICATION_JSON));
        mockRestServiceServer.expect(once(), requestTo(TOKEN_URL)).andRespond(withSuccess(responseBodyGyldig, MediaType.APPLICATION_JSON));

        azureAdTokenConsumer.getAccessToken("test");
        final String token = azureAdTokenConsumer.getAccessToken("test");

        assertThat(token).isEqualTo(tokenGyldig);
    }

    @Test
    public void brukerEksisterendeTokenHvisGyldig() {
        Instant expiresOnGyldig = Instant.now().plusSeconds(300L);
        String tokenGyldig = "token_gyldig";
        String responseBodyGyldig = azureAdResponseAsJsonString(expiresOnGyldig, "ressursID", tokenGyldig);

        mockRestServiceServer.expect(once(), requestTo(TOKEN_URL)).andRespond(withSuccess(responseBodyGyldig, MediaType.APPLICATION_JSON));

        azureAdTokenConsumer.getAccessToken("test");
        final String token = azureAdTokenConsumer.getAccessToken("test");

        assertThat(token).isEqualTo(tokenGyldig);
    }

    @Test
    public void henterTokenFraAzureHvisTokenForResourceMangler() {
        Instant expiresOn1 = Instant.now().minusSeconds(300L);
        String tokenForResource1 = "token_1";
        String resource1 = "resource_1";
        String responseBody1 = azureAdResponseAsJsonString(expiresOn1, resource1, tokenForResource1);

        Instant expiresOn2 = Instant.now().plusSeconds(300L);
        String tokenForResource2 = "token_2";
        String resource2 = "resource_2";
        String responseBody2 = azureAdResponseAsJsonString(expiresOn2, resource2, tokenForResource2);

        mockRestServiceServer.expect(once(), requestTo(TOKEN_URL)).andRespond(withSuccess(responseBody1, MediaType.APPLICATION_JSON));
        mockRestServiceServer.expect(once(), requestTo(TOKEN_URL)).andRespond(withSuccess(responseBody2, MediaType.APPLICATION_JSON));

        final String token1 = azureAdTokenConsumer.getAccessToken(resource1);
        final String token2 = azureAdTokenConsumer.getAccessToken(resource2);
        final String token3 = azureAdTokenConsumer.getAccessToken(resource2);

        assertThat(token1).isEqualTo(token1);
        assertThat(token2).isEqualTo(token2);
        assertThat(token3).isEqualTo(token2);
    }

    private String azureAdResponseAsJsonString(Instant expiresOn, String resource, String token) {
        ObjectMapper objectMapper = new ObjectMapper();
        JavaTimeModule module = new JavaTimeModule();
        objectMapper.registerModule(module);

        AzureAdResponse azureAdResponse = new AzureAdResponse()
                .access_token(token)
                .token_type("Bearer")
                .expires_in("3600")
                .ext_expires_in("3600")
                .expires_on(expiresOn)
                .not_before(Long.toString(expiresOn.getEpochSecond()))
                .resource(resource);

        try {
            return objectMapper.writeValueAsString(azureAdResponse);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
