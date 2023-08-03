package no.nav.syfo.tokenx

import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.security.token.support.core.jwt.JwtTokenClaims
import no.nav.syfo.domain.Fodselsnummer
import javax.ws.rs.ForbiddenException

object TokenXUtil {
    @Throws(ForbiddenException::class)
    fun validateTokenXClaims(
        contextHolder: TokenValidationContextHolder,
        vararg requestedClientId: String,
    ): JwtTokenClaims {
        val context = contextHolder.tokenValidationContext
        val claims = context.getClaims(TokenXIssuer.TOKENX)
        val clientId = claims.getStringClaim("client_id")

        if (!requestedClientId.toList().contains(clientId)) {
            throw ForbiddenException("Uventet client id $clientId")
        }
        return claims
    }

    fun JwtTokenClaims.fnrFromIdportenTokenX(): Fodselsnummer {
        return Fodselsnummer(this.getStringClaim("pid"))
    }

    fun fnrFromIdportenTokenX(contextHolder: TokenValidationContextHolder): Fodselsnummer {
        val context = contextHolder.tokenValidationContext
        val claims = context.getClaims(TokenXIssuer.TOKENX)
        return Fodselsnummer(claims.getStringClaim("pid"))
    }

    object TokenXIssuer {
        const val TOKENX = "tokenx"
    }
}
