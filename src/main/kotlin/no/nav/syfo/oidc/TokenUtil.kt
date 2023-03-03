package no.nav.syfo.oidc

import no.nav.security.token.support.core.context.TokenValidationContextHolder

object TokenUtil {

    @JvmStatic
    fun getIssuerToken(contextHolder: TokenValidationContextHolder, issuer: String): String {
        val context = contextHolder.tokenValidationContext
        return context.getJwtToken(issuer)?.tokenAsString ?: throw RuntimeException("Klarte ikke hente token fra issuer: $issuer")
    }

}
