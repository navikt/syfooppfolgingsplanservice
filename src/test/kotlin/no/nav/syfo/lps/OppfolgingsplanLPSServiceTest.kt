package no.nav.syfo.lps

import com.fasterxml.jackson.module.kotlin.readValue
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import no.nav.helse.op2016.Oppfoelgingsplan4UtfyllendeInfoM
import no.nav.syfo.LocalApplication
import no.nav.syfo.dialogmelding.DialogmeldingService
import no.nav.syfo.domain.Fodselsnummer
import no.nav.syfo.lps.OppfolgingsplanLPSService.Companion.METRIKK_BISTAND_FRA_NAV
import no.nav.syfo.lps.OppfolgingsplanLPSService.Companion.METRIKK_DELT_MED_FASTLEGE
import no.nav.syfo.lps.OppfolgingsplanLPSService.Companion.METRIKK_LPS_RETRY
import no.nav.syfo.lps.OppfolgingsplanLPSService.Companion.METRIKK_OLD_FNR
import no.nav.syfo.lps.OppfolgingsplanLPSService.Companion.METRIKK_PROSSESERING_VELLYKKET
import no.nav.syfo.lps.OppfolgingsplanLPSService.Companion.METRIKK_TAG_BISTAND
import no.nav.syfo.lps.database.OppfolgingsplanLPSDAO
import no.nav.syfo.lps.kafka.OppfolgingsplanLPSProducer
import no.nav.syfo.metric.Metrikk
import no.nav.syfo.pdl.*
import no.nav.syfo.service.FeiletSendingService
import no.nav.syfo.service.JournalforOPService
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.web.client.HttpServerErrorException
import java.util.*

@RunWith(SpringRunner::class)
@SpringBootTest(classes = [LocalApplication::class])
@DirtiesContext
class OppfolgingsplanLPSServiceTest {
    private val dialogmeldingService = mockk<DialogmeldingService>()
    private val journalforOPService = mockk<JournalforOPService>()
    private val oppfolgingsplanLPSProducer = mockk<OppfolgingsplanLPSProducer>()
    private val oppfolgingplanLPSDAO = mockk<OppfolgingsplanLPSDAO>()
    private val oppfolgingsplanLPSRetryService = mockk<OppfolgingsplanLPSRetryService>()
    private val opPdfGenConsumer = mockk<OPPdfGenConsumer>()
    private val metrikk = mockk<Metrikk>()
    private val pdlConsumer = mockk<PdlConsumer>()
    private val feiletSendingService = mockk<FeiletSendingService>()
    private val oppfolgingsplanLPSService = OppfolgingsplanLPSService(
        dialogmeldingService,
        journalforOPService,
        oppfolgingsplanLPSProducer,
        metrikk,
        oppfolgingplanLPSDAO,
        oppfolgingsplanLPSRetryService,
        opPdfGenConsumer,
        pdlConsumer,
        feiletSendingService,
    )

    val pdfByteArray = "<PDF_INNHOLD>".toByteArray()

    @Before
    fun setUp() {
        val rowId = 1L
        justRun { metrikk.tellHendelse(any()) }
        justRun { metrikk.tellHendelseMedTag(any(), any(), any()) }
        justRun { oppfolgingplanLPSDAO.updatePdf(rowId, pdfByteArray) }
        justRun { oppfolgingplanLPSDAO.updateSharedWithFastlege(rowId) }
        justRun { oppfolgingsplanLPSProducer.sendOppfolgingsLPSTilNAV(any()) }
        justRun { dialogmeldingService.sendOppfolgingsplanLPSTilFastlege(any(), pdfByteArray) }
        every { oppfolgingplanLPSDAO.create(any(), any(), any(), any(), any(), any(), any()) } returns Pair(
            rowId,
            UUID.randomUUID(),
        )
        every { opPdfGenConsumer.pdfgenResponse(any()) } returns pdfByteArray
    }

//    @Test
//    fun receivePlanFromLPSWithBistandFraNAVAndShareWithFastlegeSet() {
//        val (archiveReference, arbeidstakerFnr, lpsXml) = receiveLPS()
//
//        every { pdlConsumer.person(arbeidstakerFnr) } returns
//            PdlHentPerson(
//                PdlPerson(
//                    listOf(PdlPersonNavn("Fornavn", null, "Etternavn")),
//                    listOf(Adressebeskyttelse(Gradering.UGRADERT)),
//                ),
//            )
//        every { pdlConsumer.gjeldendeFnr(arbeidstakerFnr) } returns arbeidstakerFnr
//
//        oppfolgingsplanLPSService.receivePlan(archiveReference, lpsXml, false)
//
//        verify(exactly = 1) { metrikk.tellHendelse(METRIKK_DELT_MED_FASTLEGE) }
//        verify(exactly = 1) { metrikk.tellHendelseMedTag(METRIKK_BISTAND_FRA_NAV, METRIKK_TAG_BISTAND, any()) }
//        verify(exactly = 1) { metrikk.tellHendelse(METRIKK_PROSSESERING_VELLYKKET) }
//    }

    @Test
    fun receivePlanFromLPSWithUserDiskresjonsmerket() {
        val (archiveReference, arbeidstakerFnr, lpsXml) = receiveLPS()

        every { pdlConsumer.person(arbeidstakerFnr) } returns
            PdlHentPerson(
                PdlPerson(
                    listOf(PdlPersonNavn("Fornavn", null, "Etternavn")),
                    listOf(Adressebeskyttelse(Gradering.STRENGT_FORTROLIG)),
                ),
            )

        every { pdlConsumer.gjeldendeFnr(arbeidstakerFnr) } returns arbeidstakerFnr
        every {
            oppfolgingplanLPSDAO.create(
                Fodselsnummer(arbeidstakerFnr),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
            )
        } returns Pair(1L, UUID.randomUUID())

        oppfolgingsplanLPSService.receivePlan(archiveReference, lpsXml, false)

        verify(exactly = 1) { metrikk.tellHendelse(METRIKK_PROSSESERING_VELLYKKET) }
    }

    @Test
    fun receivePlanFromLPSWithoutSharingWithNAVAndFastlege() {
        val (archiveReference, arbeidstakerFnr, lpsXml) = receiveLPSDelingFieldsUnset()

        every { pdlConsumer.person(arbeidstakerFnr) } returns
            PdlHentPerson(
                PdlPerson(
                    listOf(PdlPersonNavn("Fornavn", null, "Etternavn")),
                    listOf(Adressebeskyttelse(Gradering.UGRADERT)),
                ),
            )
        every { pdlConsumer.gjeldendeFnr(arbeidstakerFnr) } returns arbeidstakerFnr

        oppfolgingsplanLPSService.receivePlan(archiveReference, lpsXml, false)

        verify(exactly = 0) { metrikk.tellHendelse(METRIKK_DELT_MED_FASTLEGE) }
        verify(exactly = 0) { metrikk.tellHendelseMedTag(METRIKK_BISTAND_FRA_NAV, METRIKK_TAG_BISTAND, any()) }
        verify(exactly = 1) { metrikk.tellHendelse(METRIKK_PROSSESERING_VELLYKKET) }
    }

    @Test
    fun arbeidstakerHasFnrDifferentFromLPSForm() {
        val (archiveReference, arbeidstakerFnr, lpsXml) = receiveLPS()
        val currentFnr = arbeidstakerFnr.reversed()

        every { pdlConsumer.person(arbeidstakerFnr) } returns
            PdlHentPerson(
                PdlPerson(
                    listOf(PdlPersonNavn("Fornavn", null, "Etternavn")),
                    listOf(Adressebeskyttelse(Gradering.UGRADERT)),
                ),
            )

        every { pdlConsumer.gjeldendeFnr(arbeidstakerFnr) } returns currentFnr

        oppfolgingsplanLPSService.receivePlan(archiveReference, lpsXml, false)

        verify(exactly = 1) { metrikk.tellHendelse(METRIKK_OLD_FNR) }
        verify(exactly = 1) {
            oppfolgingplanLPSDAO.create(
                Fodselsnummer(currentFnr),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
            )
        }
        verify(exactly = 0) {
            oppfolgingplanLPSDAO.create(
                Fodselsnummer(arbeidstakerFnr),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
            )
        }
        verify(exactly = 1) { metrikk.tellHendelse(METRIKK_PROSSESERING_VELLYKKET) }
    }

    @Test
    fun lpsPlanIsStoredInRetryTableWhenPDLHentPersonFails() {
        val (archiveReference, _, lpsXml) = receiveLPS()
        every { pdlConsumer.person(any()) } throws HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR)
        every { oppfolgingsplanLPSRetryService.getOrCreate(archiveReference, lpsXml) } returns 1L

        oppfolgingsplanLPSService.receivePlan(archiveReference, lpsXml, false)

        verify(exactly = 1) { metrikk.tellHendelse(METRIKK_LPS_RETRY) }
    }

    @Test
    fun lpsPlanIsStoredInRetryTableWhenPDLHentIdenterFails() {
        val (archiveReference, arbeidstakerFnr, lpsXml) = receiveLPS()

        every { pdlConsumer.person(arbeidstakerFnr) } returns
            PdlHentPerson(
                PdlPerson(
                    listOf(PdlPersonNavn("Fornavn", null, "Etternavn")),
                    listOf(Adressebeskyttelse(Gradering.UGRADERT)),
                ),
            )
        every { pdlConsumer.gjeldendeFnr(any()) } throws RuntimeException()
        every { oppfolgingsplanLPSRetryService.getOrCreate(archiveReference, lpsXml) } returns 1L

        oppfolgingsplanLPSService.receivePlan(archiveReference, lpsXml, false)

        verify(exactly = 1) { metrikk.tellHendelse(METRIKK_LPS_RETRY) }
    }

    private fun receiveLPS(): Triple<String, String, String> {
        val (fnr, payload) = loadXML("/lps/lps_test.xml")
        return Triple(archiveReference1, fnr, payload)
    }

    private fun receiveLPSDelingFieldsUnset(): Triple<String, String, String> {
        val (fnr, payload) = loadXML("/lps/lps_test_ingen_deling.xml")
        return Triple(archiveReference2, fnr, payload)
    }

    private fun loadXML(resourcePath: String): Pair<String, String> {
        val payload = this::class.java.getResource(resourcePath).readText()
        val fnr = xmlMapper.readValue<Oppfoelgingsplan4UtfyllendeInfoM>(payload).skjemainnhold.sykmeldtArbeidstaker.fnr
        return Pair(fnr, payload)
    }

    companion object {
        val archiveReference1 = "AR0000000"
        val archiveReference2 = "AR0000001"
    }
}
