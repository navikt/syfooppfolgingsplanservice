package no.nav.syfo.api.gcp.util

import java.util.regex.Pattern

fun fodselsnummerValid(fnr: String): Boolean = Pattern.compile("\\d{11}").matcher(fnr).matches()

fun fodselsnummerInvalid(fnr: String): Boolean = !fodselsnummerValid(fnr)

