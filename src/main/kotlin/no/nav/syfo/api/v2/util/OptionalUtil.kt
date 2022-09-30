package no.nav.syfo.api.v2.util

import java.util.*

fun <T> Optional<T>.unwrap(): T? = orElse(null)
