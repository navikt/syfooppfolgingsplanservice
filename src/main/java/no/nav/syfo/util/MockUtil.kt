package no.nav.syfo.util

private const val defaultOrgNumber = "910067494"
private const val vismaTestUserOrgNumberInNAV = "972674818"
private const val vismaTestUserOrgNumberInAltinn = "215918912"
private val allowedOrgnumbersToBeSentToAltinn = listOf(defaultOrgNumber, vismaTestUserOrgNumberInAltinn)

fun getOrgnummerForSendingTilAltinn(orgnumber: String): String {
    if (!isDev()) return orgnumber

    return when {
        vismaTestUserOrgNumberInNAV == orgnumber -> vismaTestUserOrgNumberInAltinn
        allowedOrgnumbersToBeSentToAltinn.contains(orgnumber) -> orgnumber
        else -> defaultOrgNumber
    }

}

private fun isDev(): Boolean = System.getenv("ENVIRONMENT") == "dev"
