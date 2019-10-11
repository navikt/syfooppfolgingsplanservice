package no.nav.syfo.mq.mock;

import javax.jms.*;
import java.io.Serializable;
import java.util.Enumeration;

public class SessionMock implements Session {
    private String textMessage;
    private String callId;

    @Override
    public BytesMessage createBytesMessage() throws JMSException {
        return null;
    }

    @Override
    public MapMessage createMapMessage() throws JMSException {
        return null;
    }

    @Override
    public Message createMessage() throws JMSException {
        return null;
    }

    @Override
    public ObjectMessage createObjectMessage() throws JMSException {
        return null;
    }

    @Override
    public ObjectMessage createObjectMessage(Serializable serializable) throws JMSException {
        return null;
    }

    @Override
    public StreamMessage createStreamMessage() throws JMSException {
        return null;
    }

    @Override
    public TextMessage createTextMessage() throws JMSException {
        return null;
    }

    @Override
    public TextMessage createTextMessage(String s) throws JMSException {
        this.textMessage = s;
        return new TextMessage() {
            @Override
            public void setText(String s) throws JMSException {

            }

            @Override
            public String getText() throws JMSException {
                return SessionMock.this.textMessage;
            }

            @Override
            public String getJMSMessageID() throws JMSException {
                return null;
            }

            @Override
            public void setJMSMessageID(String s) throws JMSException {

            }

            @Override
            public long getJMSTimestamp() throws JMSException {
                return 0;
            }

            @Override
            public void setJMSTimestamp(long l) throws JMSException {

            }

            @Override
            public byte[] getJMSCorrelationIDAsBytes() throws JMSException {
                return new byte[0];
            }

            @Override
            public void setJMSCorrelationIDAsBytes(byte[] bytes) throws JMSException {

            }

            @Override
            public void setJMSCorrelationID(String s) throws JMSException {

            }

            @Override
            public String getJMSCorrelationID() throws JMSException {
                return null;
            }

            @Override
            public Destination getJMSReplyTo() throws JMSException {
                return null;
            }

            @Override
            public void setJMSReplyTo(Destination destination) throws JMSException {

            }

            @Override
            public Destination getJMSDestination() throws JMSException {
                return null;
            }

            @Override
            public void setJMSDestination(Destination destination) throws JMSException {

            }

            @Override
            public int getJMSDeliveryMode() throws JMSException {
                return 0;
            }

            @Override
            public void setJMSDeliveryMode(int i) throws JMSException {

            }

            @Override
            public boolean getJMSRedelivered() throws JMSException {
                return false;
            }

            @Override
            public void setJMSRedelivered(boolean b) throws JMSException {

            }

            @Override
            public String getJMSType() throws JMSException {
                return null;
            }

            @Override
            public void setJMSType(String s) throws JMSException {

            }

            @Override
            public long getJMSExpiration() throws JMSException {
                return 0;
            }

            @Override
            public void setJMSExpiration(long l) throws JMSException {

            }

            @Override
            public long getJMSDeliveryTime() throws JMSException {
                return 0;
            }

            @Override
            public void setJMSDeliveryTime(long l) throws JMSException {

            }

            @Override
            public int getJMSPriority() throws JMSException {
                return 0;
            }

            @Override
            public void setJMSPriority(int i) throws JMSException {

            }

            @Override
            public void clearProperties() throws JMSException {

            }

            @Override
            public boolean propertyExists(String s) throws JMSException {
                return false;
            }

            @Override
            public boolean getBooleanProperty(String s) throws JMSException {
                return false;
            }

            @Override
            public byte getByteProperty(String s) throws JMSException {
                return 0;
            }

            @Override
            public short getShortProperty(String s) throws JMSException {
                return 0;
            }

            @Override
            public int getIntProperty(String s) throws JMSException {
                return 0;
            }

            @Override
            public long getLongProperty(String s) throws JMSException {
                return 0;
            }

            @Override
            public float getFloatProperty(String s) throws JMSException {
                return 0;
            }

            @Override
            public double getDoubleProperty(String s) throws JMSException {
                return 0;
            }

            @Override
            public String getStringProperty(String s) throws JMSException {
                return SessionMock.this.callId;
            }

            @Override
            public Object getObjectProperty(String s) throws JMSException {
                return null;
            }

            @Override
            public Enumeration getPropertyNames() throws JMSException {
                return null;
            }

            @Override
            public void setBooleanProperty(String s, boolean b) throws JMSException {

            }

            @Override
            public void setByteProperty(String s, byte b) throws JMSException {

            }

            @Override
            public void setShortProperty(String s, short i) throws JMSException {

            }

            @Override
            public void setIntProperty(String s, int i) throws JMSException {

            }

            @Override
            public void setLongProperty(String s, long l) throws JMSException {

            }

            @Override
            public void setFloatProperty(String s, float v) throws JMSException {

            }

            @Override
            public void setDoubleProperty(String s, double v) throws JMSException {

            }

            @Override
            public void setStringProperty(String s, String s1) throws JMSException {
                SessionMock.this.callId = s1;
            }

            @Override
            public void setObjectProperty(String s, Object o) throws JMSException {

            }

            @Override
            public void acknowledge() throws JMSException {

            }

            @Override
            public void clearBody() throws JMSException {

            }

            @Override
            public <T> T getBody(Class<T> aClass) throws JMSException {
                return null;
            }

            @Override
            public boolean isBodyAssignableTo(Class aClass) throws JMSException {
                return false;
            }
        };
    }

    @Override
    public boolean getTransacted() throws JMSException {
        return false;
    }

    @Override
    public int getAcknowledgeMode() throws JMSException {
        return 0;
    }

    @Override
    public void commit() throws JMSException {

    }

    @Override
    public void rollback() throws JMSException {

    }

    @Override
    public void close() throws JMSException {

    }

    @Override
    public void recover() throws JMSException {

    }

    @Override
    public MessageListener getMessageListener() throws JMSException {
        return null;
    }

    @Override
    public void setMessageListener(MessageListener messageListener) throws JMSException {

    }

    @Override
    public void run() {

    }

    @Override
    public MessageProducer createProducer(Destination destination) throws JMSException {
        return null;
    }

    @Override
    public MessageConsumer createConsumer(Destination destination) throws JMSException {
        return null;
    }

    @Override
    public MessageConsumer createConsumer(Destination destination, String s) throws JMSException {
        return null;
    }

    @Override
    public MessageConsumer createConsumer(Destination destination, String s, boolean b) throws JMSException {
        return null;
    }

    @Override
    public MessageConsumer createSharedConsumer(Topic topic, String s) throws JMSException {
        return null;
    }

    @Override
    public MessageConsumer createSharedConsumer(Topic topic, String s, String s1) throws JMSException {
        return null;
    }

    @Override
    public Queue createQueue(String s) throws JMSException {
        return null;
    }

    @Override
    public Topic createTopic(String s) throws JMSException {
        return null;
    }

    @Override
    public TopicSubscriber createDurableSubscriber(Topic topic, String s) throws JMSException {
        return null;
    }

    @Override
    public TopicSubscriber createDurableSubscriber(Topic topic, String s, String s1, boolean b) throws JMSException {
        return null;
    }

    @Override
    public MessageConsumer createDurableConsumer(Topic topic, String s) throws JMSException {
        return null;
    }

    @Override
    public MessageConsumer createDurableConsumer(Topic topic, String s, String s1, boolean b) throws JMSException {
        return null;
    }

    @Override
    public MessageConsumer createSharedDurableConsumer(Topic topic, String s) throws JMSException {
        return null;
    }

    @Override
    public MessageConsumer createSharedDurableConsumer(Topic topic, String s, String s1) throws JMSException {
        return null;
    }

    @Override
    public QueueBrowser createBrowser(Queue queue) throws JMSException {
        return null;
    }

    @Override
    public QueueBrowser createBrowser(Queue queue, String s) throws JMSException {
        return null;
    }

    @Override
    public TemporaryQueue createTemporaryQueue() throws JMSException {
        return null;
    }

    @Override
    public TemporaryTopic createTemporaryTopic() throws JMSException {
        return null;
    }

    @Override
    public void unsubscribe(String s) throws JMSException {

    }
}
