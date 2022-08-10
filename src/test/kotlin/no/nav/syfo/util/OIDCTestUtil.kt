package no.nav.syfo.util

import java.util.*

fun encodedJWTToken(fnr: String): String {
    val encoder = Base64.getEncoder().withoutPadding()
    val jwtHeader = "{" +
            "\"alg\":\"HS256\"," +
            "\"typ\":\"JWT\"" +
            "}".trim()
    val jwtPayload = "{" +
            "  \"pid\": \"${fnr}\"," +
            "  \"name\": \"Ola Nordmann\"," +
            "  \"iat\": 1516239022" +
            "}".trim()
    val jwtSignature = "hemmelig"

    return "${encoder.encodeToString(jwtHeader.toByteArray())}.${encoder.encodeToString(jwtPayload.toByteArray())}.${
        encoder.encodeToString(
            jwtSignature.toByteArray()
        )
    }"
}

fun encodedJWTTokenX(fnr: String): String {
    val encoder = Base64.getEncoder().withoutPadding()
    val jwtHeader = "{" +
            "\"alg\":\"HS256\"," +
            "\"typ\":\"JWT\"" +
            "}".trim()
    val jwtPayload = "{" +
            "  \"pid\": \"${fnr}\"," +
            "  \"name\": \"Ola Nordmann\"," +
            "  \"iat\": 1516239022" +
            "  \"aud\": localhost:teamsykmelding:syfosmregister" +
            "}".trim()
    val jwtSignature = "hemmelig"

    return "${encoder.encodeToString(jwtHeader.toByteArray())}.${encoder.encodeToString(jwtPayload.toByteArray())}.${
        encoder.encodeToString(
            jwtSignature.toByteArray()
        )
    }"
}