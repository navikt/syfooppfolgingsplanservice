package no.nav.syfo.util

import no.nav.syfo.testhelper.UserConstants
import java.util.*

fun encodedJWTTokenFromFnrAndIssuer(issuer: String, fnr: String): String {
    val encoder = Base64.getEncoder().withoutPadding()
    val jwtHeader = "{" +
        "\"alg\":\"HS256\"," +
        "\"typ\":\"JWT\"" +
        "}".trim();
    val jwtPayload = "{" +
        "  \"sub\": \"${UserConstants.ARBEIDSTAKER_FNR}\"," +
        "  \"name\": \"Ola Nordmann\"," +
        "  \"iat\": 1516239022" +
        "}".trim()
    val jwtSignature = "hemmelig"

    return "${encoder.encodeToString(jwtHeader.toByteArray())}.${encoder.encodeToString(jwtPayload.toByteArray())}.${encoder.encodeToString(jwtSignature.toByteArray())}"
}