package no.nav.syfo.mq.mock

import org.springframework.jms.JmsException
import org.springframework.jms.UncategorizedJmsException
import org.springframework.jms.core.JmsTemplate
import org.springframework.jms.core.MessageCreator
import javax.jms.JMSException
import javax.jms.TextMessage

class JmsTemplateMock(private val name: String) : JmsTemplate(ConnectionFactoryMock()) {
    @Throws(JmsException::class)
    override fun send(messageCreator: MessageCreator) {
        try {
            messageCreator.createMessage(SessionMock()) as TextMessage
        } catch (e: JMSException) {
            throw UncategorizedJmsException(e)
        }
    }
}
