package no.nav.syfo.lps.mq

import no.nav.altinnkanal.avro.ExternalAttachment
import org.slf4j.LoggerFactory
import net.sf.saxon.TransformerFactoryImpl
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.jms.core.JmsTemplate
import org.springframework.jms.core.MessageCreator
import org.springframework.stereotype.Service
import java.io.StringReader
import java.io.StringWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.jms.Session
import javax.xml.transform.stream.StreamSource
import javax.xml.transform.stream.StreamResult
import javax.transaction.Transactional

@Service
@Transactional
class EiaMottakProducer @Autowired constructor(
    @Qualifier("eiaMottakQueue") private val eiaMottakQueue: JmsTemplate
) {
    private val log = LoggerFactory.getLogger(EiaMottakProducer::class.java)

    private val xsltFilePath = "/altinn2eifellesformat2018_03_16.xsl"
    private val xFactory = TransformerFactoryImpl()
    private val xslt = xFactory
        .newTransformer(
            StreamSource(
                EiaMottakProducer::class.java.getResourceAsStream(xsltFilePath)))

    fun sendOppfolgingsplanLPS(
        externalAttachment: ExternalAttachment,
        payload: String,
        virksomhetsnummer: String
    ) {
        val archiveReference = externalAttachment.getArchiveReference() // already in avro msg
        val ediLogId = "${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm"))}-kafka-$archiveReference"
        xslt.apply {
            setParameter("ArchiveReference", archiveReference)
            setParameter("ServiceCode", externalAttachment.getServiceCode())
            setParameter("FormData", payload)
            setParameter("ArchiveReferenceFile", "")
            setParameter("FileName", "")
            setParameter("FileContent", "")
            setParameter("OrgNo", virksomhetsnummer)
            setParameter("EdiLogId", ediLogId)
        }
        eiaMottakQueue.send(xslTransform(externalAttachment.getBatch()))

        log.info("LPS-TRACE: Sent xmlBatch for LPSPlan to Eia by MQ")
    }

    private fun xslTransform(xml: String): MessageCreator {
        val resultWriter = StringWriter()
        return try {
            xslt.transform(
                StreamSource(StringReader(xml)),
                StreamResult(resultWriter))
            MessageCreator { session: Session ->
                session.createTextMessage().apply {
                    this.text = resultWriter.toString()
                }
            }
        } catch (e: Exception) {
            log.error("Exception during transform", e)
            MessageCreator { session: Session ->
                session.createTextMessage().apply { this.text = "Transform exception!" }
            }
        }
    }
}
