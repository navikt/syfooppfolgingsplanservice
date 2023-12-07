package no.nav.syfo.config;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.routing.DefaultProxyRoutePlanner;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

public class NaisProxyCustomizer implements RestTemplateCustomizer {

    @Override
    public void customize(RestTemplate restTemplate) {
        final HttpHost proxy = new HttpHost("webproxy-nais.nav.no", 8088);
        final HttpClient client = HttpClientBuilder.create()
                .setRoutePlanner(new DefaultProxyRoutePlanner(proxy) {
                    @Override
                    public HttpHost determineProxy(HttpHost target, HttpContext context)
                            throws HttpException {
                        if (target.getHostName().contains("microsoft")) {
                            return super.determineProxy(target, context);
                        }
                        return null;
                    }
                })
                .build();
        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory(client));
    }
}
