package no.nav.syfo.oidc;

import no.nav.security.token.support.core.context.TokenValidationContext;
import no.nav.security.token.support.core.context.TokenValidationContextHolder;
import no.nav.syfo.service.ws.OnBehalfOfOutInterceptor;
import org.apache.cxf.endpoint.Client;


public class OIDCUtil {

    public static void leggTilOnBehalfOfOutInterceptorForOIDC(Client client, String OIDCToken) {
        client.getRequestContext().put(OnBehalfOfOutInterceptor.REQUEST_CONTEXT_ONBEHALFOF_TOKEN_TYPE, OnBehalfOfOutInterceptor.TokenType.OIDC);
        client.getRequestContext().put(OnBehalfOfOutInterceptor.REQUEST_CONTEXT_ONBEHALFOF_TOKEN, OIDCToken);
    }

    public static String getSubjectEksternMedThrows(TokenValidationContextHolder contextHolder) {
        try {
            TokenValidationContext context = contextHolder.getTokenValidationContext();
            return context.getClaims(OIDCIssuer.EKSTERN).getSubject();
        } catch (NullPointerException e) {
            throw new RuntimeException("Fant ikke subject for OIDCIssuer Ekstern");
        }
    }

    public static String getIssuerToken(TokenValidationContextHolder contextHolder, String issuer) {
        TokenValidationContext context = contextHolder.getTokenValidationContext();
        return context.getJwtToken(issuer).getTokenAsString();
    }
}
