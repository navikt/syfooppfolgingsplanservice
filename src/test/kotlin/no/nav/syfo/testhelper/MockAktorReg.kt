package no.nav.syfo.testhelper

private const val MOCK_AKTORID_PREFIX = "10"

fun getFnrFromMockedAktorId(aktorId: String): String {
    return aktorId.replace(MOCK_AKTORID_PREFIX, "")
}

fun mockAktorId(fnr: String): String {
    return "$MOCK_AKTORID_PREFIX$fnr"
}
