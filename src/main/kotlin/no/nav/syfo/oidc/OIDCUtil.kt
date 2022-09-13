package no.nav.syfo.oidc

import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.security.token.support.core.jwt.JwtTokenClaims
import no.nav.syfo.oidc.OIDCIssuer.EKSTERN
import no.nav.syfo.tokenx.TokenXUtil.TokenXIssuer.TOKENX
import java.text.ParseException

object OIDCUtil {

    private val PID_CLAIM = "pid"

    @JvmStatic
    fun getSubjectEksternMedThrows(contextHolder: TokenValidationContextHolder): String {
        val issuer = EKSTERN
        val context = contextHolder.tokenValidationContext
        return context.getClaims(issuer)?.let {
            getPidClaimFromClaimsSet(it, issuer)
        } ?: throw RuntimeException("Fant ikke 'subject'-claim i token fra issuer: $issuer")
    }

    @JvmStatic
    fun getIssuerToken(contextHolder: TokenValidationContextHolder, issuer: String): String {
        val context = contextHolder.tokenValidationContext
        return context.getJwtToken(issuer)?.tokenAsString ?: throw RuntimeException("Klarte ikke hente token fra issuer: $issuer")
    }

    @JvmStatic
    fun getSluttbrukerToken(contextHolder: TokenValidationContextHolder): String {
        val context = contextHolder.tokenValidationContext
        val eksternToken = context.getJwtToken(EKSTERN)
        val tokenxToken = context.getJwtToken(TOKENX)
        return when {
            eksternToken != null ->
                eksternToken.tokenAsString
            tokenxToken != null ->
                tokenxToken.tokenAsString
            else ->
                throw RuntimeException("Klarte ikke hente token fra issuer $EKSTERN eller $TOKENX")
        }
    }

    @JvmStatic
    fun getPidClaimFromClaimsSet(jwtTokenClaims: JwtTokenClaims, issuer: String): String {
        return try {
            jwtTokenClaims.getStringClaim(PID_CLAIM)
        } catch (e: ParseException) {
            throw RuntimeException("Fant ikke pid-claim i tokenet fra $issuer")
        }
    }

}