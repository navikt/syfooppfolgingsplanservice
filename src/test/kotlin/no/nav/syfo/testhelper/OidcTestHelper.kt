package no.nav.syfo.testhelper

import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import no.nav.security.token.support.core.context.TokenValidationContext
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.security.token.support.core.jwt.JwtToken
import no.nav.security.token.support.test.JwtTokenGenerator
import no.nav.syfo.oidc.OIDCIssuer.EKSTERN
import no.nav.syfo.oidc.OIDCIssuer.INTERN_AZUREAD_V2

object OidcTestHelper {
    @JvmStatic
    fun loggInnBruker(contextHolder: TokenValidationContextHolder, subject: String?) {
        //OIDC-hack - legg til token og oidcclaims for en test-person
        val jwt = JwtTokenGenerator.createSignedJWT(subject)
        settOIDCValidationContext(contextHolder, jwt, EKSTERN)
    }

    @JvmStatic
    fun loggUtAlle(contextHolder: TokenValidationContextHolder) {
        contextHolder.tokenValidationContext = null
    }
}

fun loggInnVeilederAzureADV2(contextHolder: TokenValidationContextHolder, veilederIdent: String) {
    val claimsSet = JWTClaimsSet.parse("{\"NAVident\":\"$veilederIdent\"}")
    val jwt = JwtTokenGenerator.createSignedJWT(claimsSet)
    settOIDCValidationContext(contextHolder, jwt, INTERN_AZUREAD_V2)
}

fun settOIDCValidationContext(contextHolder: TokenValidationContextHolder, jwt: SignedJWT, issuer: String) {
    val jwtToken = JwtToken(jwt.serialize())
    val issuerTokenMap: MutableMap<String, JwtToken> = HashMap()
    issuerTokenMap[issuer] = jwtToken
    val tokenValidationContext = TokenValidationContext(issuerTokenMap)
    contextHolder.tokenValidationContext = tokenValidationContext
}
