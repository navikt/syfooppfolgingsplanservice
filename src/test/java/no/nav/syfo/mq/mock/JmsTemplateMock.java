package no.nav.syfo.mq.mock;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.JmsException;
import org.springframework.jms.UncategorizedJmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import javax.jms.JMSException;
import javax.jms.TextMessage;

@Slf4j
public class JmsTemplateMock extends JmsTemplate {
    private String name;

    public JmsTemplateMock(String name) {
        super(new ConnectionFactoryMock());
        this.name = name;
    }

    @Override
    public void send(MessageCreator messageCreator) throws JmsException {
        log.info("Sender melding til {}", name);
        try {
            TextMessage message = (TextMessage) messageCreator.createMessage(new SessionMock());
            log.info("Call id: {}", message.getStringProperty("callId"));
            log.info("Text:\n{}", message.getText());
        } catch (JMSException e) {
            throw new UncategorizedJmsException(e);
        }
    }
}
