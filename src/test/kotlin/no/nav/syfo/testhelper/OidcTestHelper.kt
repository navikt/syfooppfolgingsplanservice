package no.nav.syfo.testhelper

import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import no.nav.security.oidc.context.*
import no.nav.security.oidc.test.support.JwtTokenGenerator
import no.nav.syfo.oidc.OIDCIssuer

object OidcTestHelper {
    @JvmStatic
    fun loggInnBruker(oidcRequestContextHolder: OIDCRequestContextHolder, subject: String?) {
        //OIDC-hack - legg til token og oidcclaims for en test-person
        val jwt = JwtTokenGenerator.createSignedJWT(subject)
        val issuer = OIDCIssuer.EKSTERN
        val tokenContext = TokenContext(issuer, jwt.serialize())
        val oidcClaims = OIDCClaims(jwt)
        val oidcValidationContext = OIDCValidationContext()
        oidcValidationContext.addValidatedToken(issuer, tokenContext, oidcClaims)
        oidcRequestContextHolder.oidcValidationContext = oidcValidationContext
    }

    @JvmStatic
    fun loggUtAlle(oidcRequestContextHolder: OIDCRequestContextHolder) {
        oidcRequestContextHolder.oidcValidationContext = null
    }
}

fun loggInnVeilederAzure(oidcRequestContextHolder: OIDCRequestContextHolder, veilederIdent: String) {
    val claimsSet = JWTClaimsSet.parse("{\"NAVident\":\"$veilederIdent\"}")
    val jwt = JwtTokenGenerator.createSignedJWT(claimsSet)
    settOIDCValidationContext(oidcRequestContextHolder, jwt, OIDCIssuer.AZURE)
}

fun settOIDCValidationContext(oidcRequestContextHolder: OIDCRequestContextHolder, jwt: SignedJWT, issuer: String) {
    val tokenContext = TokenContext(issuer, jwt.serialize())
    val oidcClaims = OIDCClaims(jwt)
    val oidcValidationContext = OIDCValidationContext()
    oidcValidationContext.addValidatedToken(issuer, tokenContext, oidcClaims)
    oidcRequestContextHolder.oidcValidationContext = oidcValidationContext
}
