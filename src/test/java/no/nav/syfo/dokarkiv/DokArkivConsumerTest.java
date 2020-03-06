package no.nav.syfo.dokarkiv;

import no.nav.syfo.metric.Metrikk;
import no.nav.syfo.pdl.PdlConsumer;
import no.nav.syfo.sts.StsConsumer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import javax.inject.Inject;

import static org.mockito.Mockito.when;

public class DokArkivConsumerTest {
    @MockBean
    private Metrikk metrikk;
    @Inject
    private RestTemplate restTemplate;
    @MockBean
    private StsConsumer stsConsumer;

    @Value("${dokarkiv.url}")
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
    public void journalforOppfolgingsplan() {
    }
}
