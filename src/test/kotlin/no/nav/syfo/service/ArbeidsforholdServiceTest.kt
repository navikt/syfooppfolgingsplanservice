package no.nav.syfo.service

import no.nav.syfo.aareg.AaregConsumer
import no.nav.syfo.aareg.AaregUtils
import no.nav.syfo.aareg.Arbeidsforhold
import no.nav.syfo.aareg.utils.AaregConsumerTestUtils
import no.nav.syfo.fellesKodeverk.FellesKodeverkConsumer
import no.nav.syfo.model.Stilling
import no.nav.syfo.pdl.PdlConsumer
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import java.time.LocalDate.now

@RunWith(MockitoJUnitRunner::class)
class ArbeidsforholdServiceTest {

    @Mock
    private lateinit var aaregConsumer: AaregConsumer

    @Mock
    private lateinit var fellesKodeverkConsumer: FellesKodeverkConsumer

    @Mock
    private lateinit var pdlConsumer: PdlConsumer

    @InjectMocks
    private lateinit var arbeidsforholdService: ArbeidsforholdService

    @Before
    fun setup() {
        `when`(fellesKodeverkConsumer.stillingsnavnFromKode(ArgumentMatchers.anyString())).thenReturn(AaregConsumerTestUtils.YRKESNAVN)
    }

    @Test
    fun arbeidstakersStillingerForOrgnummerShouldOnlyReturnStillingerWithTypeOrganization() {
        val arbeidsforholdList = listOf(AaregConsumerTestUtils.validArbeidsforhold(), AaregConsumerTestUtils.arbeidsforholdTypePerson())

        test_arbeidstakersStillingerForOrgnummer(arbeidsforholdList)
    }

    @Test
    fun arbeidstakersStillingerForOrgnummerShouldOnlyReturnStillingerValidOnDate() {
        val arbeidsforholdList = listOf(AaregConsumerTestUtils.validArbeidsforhold(), AaregConsumerTestUtils.arbeidsforholdWithPassedDate())

        test_arbeidstakersStillingerForOrgnummer(arbeidsforholdList)
    }

    @Test
    fun arbeidstakersStillingerForOrgnummerShouldOnlyReturnStillingerWithOrgnummer() {
        val arbeidsforholdList = listOf(AaregConsumerTestUtils.validArbeidsforhold(), AaregConsumerTestUtils.arbeidsforholdWithWrongOrgnummer())

        test_arbeidstakersStillingerForOrgnummer(arbeidsforholdList)
    }

    @Test
    fun arbeidstakersStillingerForOrgnummerShouldReturnEmptyListWhenNoValidArbeidsforhold() {
        val arbeidsforholdList = listOf(
            AaregConsumerTestUtils.arbeidsforholdTypePerson(),
            AaregConsumerTestUtils.arbeidsforholdWithPassedDate(),
            AaregConsumerTestUtils.arbeidsforholdWithWrongOrgnummer()
        )

        `when`(aaregConsumer.arbeidsforholdArbeidstaker(AaregConsumerTestUtils.AT_FNR)).thenReturn(arbeidsforholdList)
        `when`(pdlConsumer.fnr(AaregConsumerTestUtils.AT_AKTORID)).thenReturn(AaregConsumerTestUtils.AT_FNR)

        val actualStillingList = arbeidsforholdService.arbeidstakersStillingerForOrgnummer(AaregConsumerTestUtils.AT_AKTORID, now(), AaregConsumerTestUtils.ORGNUMMER)

        assertThat(actualStillingList).isEmpty()
    }

    private fun test_arbeidstakersStillingerForOrgnummer(arbeidsforholdList: List<Arbeidsforhold>) {
        `when`(aaregConsumer.arbeidsforholdArbeidstaker(AaregConsumerTestUtils.AT_FNR)).thenReturn(arbeidsforholdList)
        `when`(pdlConsumer.fnr(AaregConsumerTestUtils.AT_AKTORID)).thenReturn(AaregConsumerTestUtils.AT_FNR)
        val actualStillingList =
            arbeidsforholdService.arbeidstakersStillingerForOrgnummer(AaregConsumerTestUtils.AT_AKTORID, now(), AaregConsumerTestUtils.ORGNUMMER)
        verifyStilling(actualStillingList)
    }

    private fun verifyStilling(stillingList: List<Stilling>) {
        assertThat(stillingList.size).isEqualTo(1)
        val stilling = stillingList[0]
        assertThat(stilling.yrke).isEqualTo(AaregConsumerTestUtils.YRKESNAVN)
        assertThat(stilling.prosent).isEqualTo(AaregUtils.stillingsprosentWithMaxScale(AaregConsumerTestUtils.STILLINGSPROSENT))
    }
}
