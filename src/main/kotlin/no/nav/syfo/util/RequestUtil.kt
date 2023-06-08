package no.nav.syfo.util

import java.util.*

const val NAV_CONSUMER_ID_HEADER = "Nav-Consumer-Id"
const val APP_CONSUMER_ID = "srvsyfooppfolgings"
const val NAV_CALL_ID_HEADER = "Nav-Call-Id"

const val NAV_PERSONIDENT_HEADER = "nav-personident"
const val NAV_PERSONIDENTER_HEADER = "Nav-Personidenter"

const val NAV_CONSUMER_TOKEN_HEADER = "Nav-Consumer-Token"

const val PDL_BEHANDLINGSNUMMER_HEADER = "behandlingsnummer"
// https://behandlingskatalog.nais.adeo.no/process/team/6a3b85e0-0e06-4f58-95bb-4318e31c4b2b/cca7c846-e5a5-4a10-bc7e-6abd6fc1b0f5
const val BEHANDLINGSNUMMER_OPPFOLGINGSPLAN = "B275"

fun createCallId(): String = UUID.randomUUID().toString()

