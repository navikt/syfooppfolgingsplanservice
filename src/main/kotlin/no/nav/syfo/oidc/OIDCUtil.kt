package no.nav.syfo.oidc

import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.syfo.service.ws.OnBehalfOfOutInterceptor
import no.nav.syfo.oidc.OIDCIssuer.EKSTERN
import org.apache.cxf.endpoint.Client

object OIDCUtil {

    @JvmStatic
    fun leggTilOnBehalfOfOutInterceptorForOIDC(client: Client, OIDCToken: String) {
        client.requestContext.put(OnBehalfOfOutInterceptor.REQUEST_CONTEXT_ONBEHALFOF_TOKEN_TYPE, OnBehalfOfOutInterceptor.TokenType.OIDC)
        client.requestContext.put(OnBehalfOfOutInterceptor.REQUEST_CONTEXT_ONBEHALFOF_TOKEN, OIDCToken)
    }

    @JvmStatic
    fun getSubjectEksternMedThrows(contextHolder: TokenValidationContextHolder) : String {
        val issuer = EKSTERN
        val context = contextHolder.tokenValidationContext
        return context.getClaims(issuer)?.subject ?: throw RuntimeException("Fant ikke 'subject'-claim i token fra issuer: $issuer")
    }

    @JvmStatic
    fun getIssuerToken(contextHolder: TokenValidationContextHolder, issuer: String) : String {
        val context = contextHolder.tokenValidationContext
        return context.getJwtToken(issuer)?.tokenAsString ?: throw RuntimeException("Klarte ikke hente token fra issuer: $issuer")
    }

}