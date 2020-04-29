package no.nav.syfo.api.intern

import no.nav.security.oidc.api.ProtectedWithClaims
import no.nav.syfo.domain.GodkjentPlan
import no.nav.syfo.oidc.OIDCIssuer
import no.nav.syfo.repository.dao.GodkjentplanDAO
import no.nav.syfo.service.DokumentService
import no.nav.syfo.service.PdfService
import no.nav.syfo.veiledertilgang.VeilederTilgangConsumer
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.io.IOException
import java.io.Serializable
import javax.inject.Inject
import javax.ws.rs.NotFoundException

@RestController
@ProtectedWithClaims(issuer = OIDCIssuer.AZURE)
@RequestMapping(value = ["/api/internad/dokument/{oppfoelgingsdialogId}"])
class DokumentADController @Inject constructor(
        private val dokumentService: DokumentService,
        private val godkjentplanDAO: GodkjentplanDAO,
        private val pdfService: PdfService,
        private val veilederTilgangConsumer: VeilederTilgangConsumer
) {
    @GetMapping
    @RequestMapping(value = ["/side/{side}"])
    @Throws(IOException::class)
    fun bilde(@PathVariable("oppfoelgingsdialogId") oppfoelgingsdialogId: Long, @PathVariable("side") side: Int): ResponseEntity<*> {
        veilederTilgangConsumer.throwExceptionIfVeilederWithoutAccessToSYFO()
        return try {
            val pdf = getPdf(oppfoelgingsdialogId)
            ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(pdfService.pdf2image(pdf, side))
        } catch (e: IndexOutOfBoundsException) {
            LOG.error("Fikk IndexOutOfBoundsException ved henting av side {} for oppfoelgingsplan {} ", side, oppfoelgingsdialogId)
            throw e
        }
    }

    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    @RequestMapping(value = ["/dokumentinfo"])
    fun dokumentinfo(@PathVariable("oppfoelgingsdialogId") oppfoelgingsdialogId: Long): Dokumentinfo {
        veilederTilgangConsumer.throwExceptionIfVeilederWithoutAccessToSYFO()
        val pdf = getPdf(oppfoelgingsdialogId)
        return Dokumentinfo(
                antallSider = pdfService.hentAntallSiderIDokument(pdf)
        )
    }

    @GetMapping
    fun dokument(@PathVariable("oppfoelgingsdialogId") oppfoelgingsdialogId: Long): ResponseEntity<*> {
        veilederTilgangConsumer.throwExceptionIfVeilederWithoutAccessToSYFO()
        val pdf = getPdf(oppfoelgingsdialogId)
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf)
    }

    private fun getPdf(oppfoelgingsdialogId: Long): ByteArray {
        return godkjentplanDAO.godkjentPlanByOppfolgingsplanId(oppfoelgingsdialogId)
                .map { obj: GodkjentPlan -> obj.dokumentUuid }
                .map { dokumentUuid: String -> dokumentService.hentDokument(dokumentUuid) }
                .orElseThrow { NotFoundException("Klarte ikke å hente ut godkjent plan for oppfølgingsdialogId $oppfoelgingsdialogId") }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(DokumentADController::class.java)
    }
}

data class Dokumentinfo(
        val antallSider: Int
) : Serializable
