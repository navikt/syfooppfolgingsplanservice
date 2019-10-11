package no.nav.syfo.mq.mock;

import org.springframework.jms.JmsException;
import org.springframework.jms.UncategorizedJmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import javax.jms.JMSException;
import javax.jms.TextMessage;

public class JmsTemplateMock extends JmsTemplate {
    private String name;

    public JmsTemplateMock(String name) {
        super(new ConnectionFactoryMock());
        this.name = name;
    }

    @Override
    public void send(MessageCreator messageCreator) throws JmsException {
        try {
            TextMessage message = (TextMessage) messageCreator.createMessage(new SessionMock());
        } catch (JMSException e) {
            throw new UncategorizedJmsException(e);
        }
    }
}
