package no.nav.syfo.service.ws;

import org.apache.cxf.Bus;
import org.apache.cxf.binding.soap.Soap12;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.ws.policy.*;
import org.apache.cxf.ws.policy.attachment.reference.ReferenceResolver;
import org.apache.cxf.ws.policy.attachment.reference.RemoteReferenceResolver;
import org.apache.cxf.ws.security.trust.STSClient;
import org.apache.neethi.Policy;

import java.util.HashMap;

public class STSClientConfig {
    // Only use no transportbinding on localhost, should use the requestSamlPolicy.xml with transport binding https
    // when in production.
    private static final String STS_REQUEST_SAML_POLICY = "classpath:policy/requestSamlPolicy.xml";
    private static final String STS_CLIENT_AUTHENTICATION_POLICY = "classpath:policy/untPolicy.xml";

    public static <T> T configureRequestSamlToken(T port) {
        Client client = ClientProxy.getClient(port);
        // do not have onbehalfof token so cache token in endpoint
        configureStsRequestSamlToken(client, true);
        return port;
    }

    public static <T> T configureRequestSamlTokenOnBehalfOfOidc(T port) {
        Client client = ClientProxy.getClient(port);
        // Add interceptor to exctract token from request context and add to STS
        // request as the OnbehalfOf element. Could use a callbackhandler instead if the oidc token
        // can be retrieved from the thread, i.e. Spring SecurityContext etc, leaving this to the implementer of
        // the application.
        client.getOutInterceptors().add(new OnBehalfOfOutInterceptor());

        // want to cache the token with the OnBehalfOfToken, not per proxy
        configureStsRequestSamlToken(client, false);
        return port;
    }

    protected static void configureStsRequestSamlToken(Client client, boolean cacheTokenInEndpoint) {
        // TODO: remove custom client when STS is updated to support the cxf client
        STSClient stsClient = createCustomSTSClient(client.getBus());
        configureStsWithPolicyForClient(stsClient, client, STS_REQUEST_SAML_POLICY, cacheTokenInEndpoint);
    }

    protected static void configureStsWithPolicyForClient(STSClient stsClient, Client client, String policyReference,
                                                          boolean cacheTokenInEndpoint) {
        String location = System.getenv("SECURITYTOKENSERVICE_URL");
        String username = System.getenv("SRV_USERNAME");
        String password = System.getenv("SRV_PASSWORD");

        configureSTSClient(stsClient, location, username, password);

        client.getRequestContext().put(org.apache.cxf.rt.security.SecurityConstants.STS_CLIENT, stsClient);
        client.getRequestContext().put(org.apache.cxf.rt.security.SecurityConstants.CACHE_ISSUED_TOKEN_IN_ENDPOINT,
                cacheTokenInEndpoint);
        setEndpointPolicyReference(client, policyReference);
    }

    /**
     * Creating custom STS client because the STS on Datapower requires KeyType as a child to RequestSecurityToken and
     * TokenType as a child to SecondaryParameters. Standard CXF client put both elements in SecondaryParameters. By
     * overriding the useSecondaryParameters method you can exactly specify the request in the
     * RequestSecurityTokenTemplate in the policy.
     *
     * @param bus
     * @return
     */
    protected static STSClient createCustomSTSClient(Bus bus) {
        return new STSClientWSTrust13and14(bus);
    }

    protected static STSClient configureSTSClient(STSClient stsClient, String location, String username,
                                                  String password) {

        stsClient.setEnableAppliesTo(false);
        stsClient.setAllowRenewing(false);
        stsClient.setLocation(location);

        HashMap<String, Object> properties = new HashMap<>();
        properties.put(org.apache.cxf.rt.security.SecurityConstants.USERNAME, username);
        properties.put(org.apache.cxf.rt.security.SecurityConstants.PASSWORD, password);

        stsClient.setProperties(properties);

        // used for the STS client to authenticate itself to the STS provider.
        stsClient.setPolicy(STS_CLIENT_AUTHENTICATION_POLICY);
        return stsClient;
    }

    protected static void setEndpointPolicyReference(Client client, String uri) {
        Policy policy = resolvePolicyReference(client, uri);
        setClientEndpointPolicy(client, policy);
    }

    private static String requireProperty(String key) {
        String property = System.getenv(key);
        return property != null ? property : systemProperty(key);
    }

    private static String systemProperty(String key) {
        String property = System.getProperty(key);
        if (property == null) {
            throw new IllegalStateException("Required property " + key + " not available.");
        }
        return property;
    }

    private static Policy resolvePolicyReference(Client client, String uri) {
        PolicyBuilder policyBuilder = client.getBus().getExtension(PolicyBuilder.class);
        ReferenceResolver resolver = new RemoteReferenceResolver("", policyBuilder);
        return resolver.resolveReference(uri);
    }

    private static void setClientEndpointPolicy(Client client, Policy policy) {
        Endpoint endpoint = client.getEndpoint();
        EndpointInfo endpointInfo = endpoint.getEndpointInfo();

        PolicyEngine policyEngine = client.getBus().getExtension(PolicyEngine.class);
        SoapMessage message = new SoapMessage(Soap12.getInstance());
        EndpointPolicy endpointPolicy = policyEngine.getClientEndpointPolicy(endpointInfo, null, message);
        policyEngine.setClientEndpointPolicy(endpointInfo, endpointPolicy.updatePolicy(policy, message));
    }
}
