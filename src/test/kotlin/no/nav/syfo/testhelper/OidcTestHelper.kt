package no.nav.syfo.testhelper

import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import no.nav.security.token.support.core.context.TokenValidationContext
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.security.token.support.core.jwt.JwtToken
import no.nav.security.token.support.test.JwtTokenGenerator.createSignedJWT
import no.nav.syfo.oidc.OIDCIssuer.INTERN_AZUREAD_V2
import no.nav.syfo.tokenx.TokenXUtil

object OidcTestHelper {

    @JvmStatic
    fun loggUtAlle(contextHolder: TokenValidationContextHolder) {
        contextHolder.tokenValidationContext = null
    }
}

fun loggInnBrukerTokenX(contextHolder: TokenValidationContextHolder, brukerFnr: String, clientId: String) {
    val claimsSet = JWTClaimsSet.Builder()
        .claim("pid", brukerFnr)
        .claim("client_id", clientId)
        .build()

    val jwt = createSignedJWT(claimsSet)
    settOIDCValidationContext(contextHolder, jwt, TokenXUtil.TokenXIssuer.TOKENX)
}

fun loggInnVeilederAzureADV2(contextHolder: TokenValidationContextHolder, veilederIdent: String) {
    val claimsSet = JWTClaimsSet.parse("{\"NAVident\":\"$veilederIdent\"}")
    val jwt = createSignedJWT(claimsSet)
    settOIDCValidationContext(contextHolder, jwt, INTERN_AZUREAD_V2)
}

fun settOIDCValidationContext(contextHolder: TokenValidationContextHolder, jwt: SignedJWT, issuer: String) {
    val jwtToken = JwtToken(jwt.serialize())
    val issuerTokenMap: MutableMap<String, JwtToken> = HashMap()
    issuerTokenMap[issuer] = jwtToken
    val tokenValidationContext = TokenValidationContext(issuerTokenMap)
    contextHolder.tokenValidationContext = tokenValidationContext
}
