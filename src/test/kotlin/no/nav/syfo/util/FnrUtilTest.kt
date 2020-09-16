package no.nav.syfo.util

import org.assertj.core.api.Assertions
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
class FnrUtilTest {
    @Test
    fun fnrDayOfBirthGreatherThan15ShouldReturnTrue() {
        val fnr = "16123456789"
        val thresholdDay = 15

        val shouldBeTrue = FnrUtil.fodtEtterDagIMaaned(fnr, thresholdDay)
        Assertions.assertThat(shouldBeTrue).isEqualTo(true)
    }

    @Test
    fun fnrDayOfBirthLessThanOrEqualTo15ShouldReturnFalse() {
        val fnr1 = "15123456789"
        val fnr2 = "14123456789"
        val thresholdDay = 15

        val shouldBeFalseForFnr1 = FnrUtil.fodtEtterDagIMaaned(fnr1, thresholdDay)
        val shouldBeFalseForFnr2 = FnrUtil.fodtEtterDagIMaaned(fnr2, thresholdDay)
        Assertions.assertThat(shouldBeFalseForFnr1 || shouldBeFalseForFnr2).isEqualTo(false)
    }
}
