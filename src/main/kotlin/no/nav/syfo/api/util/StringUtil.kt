package no.nav.syfo.api.util

import java.util.regex.Pattern

fun fodselsnummerValid(fnr: String): Boolean = Pattern.compile("\\d{11}").matcher(fnr).matches()

fun fodselsnummerInvalid(fnr: String): Boolean = !fodselsnummerValid(fnr)

fun virksomhetsnummerValid(virksomhetsnummer: String): Boolean = Pattern.compile("\\d{9}").matcher(virksomhetsnummer).matches()

fun virksomhetsnummerInvalid(virksomhetsnummer: String): Boolean = !virksomhetsnummerValid(virksomhetsnummer)
