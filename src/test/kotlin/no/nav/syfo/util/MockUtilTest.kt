package no.nav.syfo.util


import no.nav.syfo.util.MockUtil.getOrgnummerForSendingTilAltinn
import no.nav.syfo.util.PropertyUtil.ALTINN_TEST_WHITELIST_ORGNR
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import uk.org.webcompere.systemstubs.rules.EnvironmentVariablesRule


class MockUtilTest {

    @JvmField
    @Rule
    var environmentVariablesRule: EnvironmentVariablesRule =
        EnvironmentVariablesRule()

    @Test
    fun returnInputWhenNoEnvVariable() {
        val orgnummer = "1234565"
        val orgnummerForSendingTilAltinn = getOrgnummerForSendingTilAltinn(orgnummer)
        assertThat(orgnummerForSendingTilAltinn).isEqualTo(orgnummer)
    }

    @Test
    fun returnInputWhenEnvVariable() {
        environmentVariablesRule.set(ALTINN_TEST_WHITELIST_ORGNR, "98765")
        val orgnummer = "98765"
        val orgnummerForSendingTilAltinn = getOrgnummerForSendingTilAltinn(orgnummer)
        assertThat(orgnummerForSendingTilAltinn).isEqualTo(orgnummer)
    }

    @Test
    fun returnEnvVariableWhenWhenEnvVariableIsSetAndNotInList() {
        environmentVariablesRule.set(ALTINN_TEST_WHITELIST_ORGNR, "98765")
        val orgnummer = "32456"
        val orgnummerForSendingTilAltinn = getOrgnummerForSendingTilAltinn(orgnummer)
        assertThat(orgnummerForSendingTilAltinn).isEqualTo("98765")
    }

    @Test
    fun returFirstOrgnummerWhenEnvVariableContainsSeveralOrgnummersAndNooneMatches() {
        environmentVariablesRule.set(ALTINN_TEST_WHITELIST_ORGNR, "11111,22222")
        val orgnummer = "33333"
        val orgnummerForSendingTilAltinn = getOrgnummerForSendingTilAltinn(orgnummer)
        assertThat(orgnummerForSendingTilAltinn).isEqualTo("11111")
    }

    @Test
    fun returnInputWhenEnvVariableContainsSeveralOrgnummers() {
        environmentVariablesRule.set(ALTINN_TEST_WHITELIST_ORGNR, "11111,22222")
        val orgnummer = "22222"
        val orgnummerForSendingTilAltinn = getOrgnummerForSendingTilAltinn(orgnummer)
        assertThat(orgnummerForSendingTilAltinn).isEqualTo(orgnummer)
    }

}
