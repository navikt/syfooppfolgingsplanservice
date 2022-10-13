package no.nav.syfo.api.util

import java.util.*

fun <T> Optional<T>.unwrap(): T? = orElse(null)
