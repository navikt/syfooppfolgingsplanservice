package no.nav.syfo.mq.mock

import java.io.Serializable
import java.util.*
import javax.jms.*
import javax.jms.Queue

class SessionMock : Session {
    private var textMessage: String? = null
    private var callId: String? = null

    @Throws(JMSException::class)
    override fun createBytesMessage(): BytesMessage? {
        return null
    }

    @Throws(JMSException::class)
    override fun createMapMessage(): MapMessage? {
        return null
    }

    @Throws(JMSException::class)
    override fun createMessage(): Message? {
        return null
    }

    @Throws(JMSException::class)
    override fun createObjectMessage(): ObjectMessage? {
        return null
    }

    @Throws(JMSException::class)
    override fun createObjectMessage(serializable: Serializable): ObjectMessage? {
        return null
    }

    @Throws(JMSException::class)
    override fun createStreamMessage(): StreamMessage? {
        return null
    }

    @Throws(JMSException::class)
    override fun createTextMessage(): TextMessage? {
        return null
    }

    @Throws(JMSException::class)
    override fun createTextMessage(s: String): TextMessage? {
        textMessage = s
        return object : TextMessage {
            @Throws(JMSException::class)
            override fun setText(s: String) {
            }

            @Throws(JMSException::class)
            override fun getText(): String {
                return textMessage!!
            }

            @Throws(JMSException::class)
            override fun getJMSMessageID(): String? {
                return null
            }

            @Throws(JMSException::class)
            override fun setJMSMessageID(s: String) {
            }

            @Throws(JMSException::class)
            override fun getJMSTimestamp(): Long {
                return 0
            }

            @Throws(JMSException::class)
            override fun setJMSTimestamp(l: Long) {
            }

            @Throws(JMSException::class)
            override fun getJMSCorrelationIDAsBytes(): ByteArray {
                return ByteArray(0)
            }

            @Throws(JMSException::class)
            override fun setJMSCorrelationIDAsBytes(bytes: ByteArray) {
            }

            @Throws(JMSException::class)
            override fun setJMSCorrelationID(s: String) {
            }

            @Throws(JMSException::class)
            override fun getJMSCorrelationID(): String? {
                return null
            }

            @Throws(JMSException::class)
            override fun getJMSReplyTo(): Destination? {
                return null
            }

            @Throws(JMSException::class)
            override fun setJMSReplyTo(destination: Destination) {
            }

            @Throws(JMSException::class)
            override fun getJMSDestination(): Destination? {
                return null
            }

            @Throws(JMSException::class)
            override fun setJMSDestination(destination: Destination) {
            }

            @Throws(JMSException::class)
            override fun getJMSDeliveryMode(): Int {
                return 0
            }

            @Throws(JMSException::class)
            override fun setJMSDeliveryMode(i: Int) {
            }

            @Throws(JMSException::class)
            override fun getJMSRedelivered(): Boolean {
                return false
            }

            @Throws(JMSException::class)
            override fun setJMSRedelivered(b: Boolean) {
            }

            @Throws(JMSException::class)
            override fun getJMSType(): String? {
                return null
            }

            @Throws(JMSException::class)
            override fun setJMSType(s: String) {
            }

            @Throws(JMSException::class)
            override fun getJMSExpiration(): Long {
                return 0
            }

            @Throws(JMSException::class)
            override fun setJMSExpiration(l: Long) {
            }

            @Throws(JMSException::class)
            override fun getJMSDeliveryTime(): Long {
                return 0
            }

            @Throws(JMSException::class)
            override fun setJMSDeliveryTime(l: Long) {
            }

            @Throws(JMSException::class)
            override fun getJMSPriority(): Int {
                return 0
            }

            @Throws(JMSException::class)
            override fun setJMSPriority(i: Int) {
            }

            @Throws(JMSException::class)
            override fun clearProperties() {
            }

            @Throws(JMSException::class)
            override fun propertyExists(s: String): Boolean {
                return false
            }

            @Throws(JMSException::class)
            override fun getBooleanProperty(s: String): Boolean {
                return false
            }

            @Throws(JMSException::class)
            override fun getByteProperty(s: String): Byte {
                return 0
            }

            @Throws(JMSException::class)
            override fun getShortProperty(s: String): Short {
                return 0
            }

            @Throws(JMSException::class)
            override fun getIntProperty(s: String): Int {
                return 0
            }

            @Throws(JMSException::class)
            override fun getLongProperty(s: String): Long {
                return 0
            }

            @Throws(JMSException::class)
            override fun getFloatProperty(s: String): Float {
                return 0F
            }

            @Throws(JMSException::class)
            override fun getDoubleProperty(s: String): Double {
                return 0.0
            }

            @Throws(JMSException::class)
            override fun getStringProperty(s: String): String {
                return callId!!
            }

            @Throws(JMSException::class)
            override fun getObjectProperty(s: String): Any? {
                return null
            }

            @Throws(JMSException::class)
            override fun getPropertyNames(): Enumeration<*>? {
                return null
            }

            @Throws(JMSException::class)
            override fun setBooleanProperty(s: String, b: Boolean) {
            }

            @Throws(JMSException::class)
            override fun setByteProperty(s: String, b: Byte) {
            }

            @Throws(JMSException::class)
            override fun setShortProperty(s: String, i: Short) {
            }

            @Throws(JMSException::class)
            override fun setIntProperty(s: String, i: Int) {
            }

            @Throws(JMSException::class)
            override fun setLongProperty(s: String, l: Long) {
            }

            @Throws(JMSException::class)
            override fun setFloatProperty(s: String, v: Float) {
            }

            @Throws(JMSException::class)
            override fun setDoubleProperty(s: String, v: Double) {
            }

            @Throws(JMSException::class)
            override fun setStringProperty(s: String, s1: String) {
                callId = s1
            }

            @Throws(JMSException::class)
            override fun setObjectProperty(s: String, o: Any) {
            }

            @Throws(JMSException::class)
            override fun acknowledge() {
            }

            @Throws(JMSException::class)
            override fun clearBody() {
            }

            @Throws(JMSException::class)
            override fun <T> getBody(aClass: Class<T>): T? {
                return null
            }

            @Throws(JMSException::class)
            override fun isBodyAssignableTo(aClass: Class<*>?): Boolean {
                return false
            }
        }
    }

    @Throws(JMSException::class)
    override fun getTransacted(): Boolean {
        return false
    }

    @Throws(JMSException::class)
    override fun getAcknowledgeMode(): Int {
        return 0
    }

    @Throws(JMSException::class)
    override fun commit() {
    }

    @Throws(JMSException::class)
    override fun rollback() {
    }

    @Throws(JMSException::class)
    override fun close() {
    }

    @Throws(JMSException::class)
    override fun recover() {
    }

    @Throws(JMSException::class)
    override fun getMessageListener(): MessageListener? {
        return null
    }

    @Throws(JMSException::class)
    override fun setMessageListener(messageListener: MessageListener) {
    }

    override fun run() {}

    @Throws(JMSException::class)
    override fun createProducer(destination: Destination): MessageProducer? {
        return null
    }

    @Throws(JMSException::class)
    override fun createConsumer(destination: Destination): MessageConsumer? {
        return null
    }

    @Throws(JMSException::class)
    override fun createConsumer(destination: Destination, s: String): MessageConsumer? {
        return null
    }

    @Throws(JMSException::class)
    override fun createConsumer(destination: Destination, s: String, b: Boolean): MessageConsumer? {
        return null
    }

    @Throws(JMSException::class)
    override fun createSharedConsumer(topic: Topic, s: String): MessageConsumer? {
        return null
    }

    @Throws(JMSException::class)
    override fun createSharedConsumer(topic: Topic, s: String, s1: String): MessageConsumer? {
        return null
    }

    @Throws(JMSException::class)
    override fun createQueue(s: String): Queue? {
        return null
    }

    @Throws(JMSException::class)
    override fun createTopic(s: String): Topic? {
        return null
    }

    @Throws(JMSException::class)
    override fun createDurableSubscriber(topic: Topic, s: String): TopicSubscriber? {
        return null
    }

    @Throws(JMSException::class)
    override fun createDurableSubscriber(topic: Topic, s: String, s1: String, b: Boolean): TopicSubscriber? {
        return null
    }

    @Throws(JMSException::class)
    override fun createDurableConsumer(topic: Topic, s: String): MessageConsumer? {
        return null
    }

    @Throws(JMSException::class)
    override fun createDurableConsumer(topic: Topic, s: String, s1: String, b: Boolean): MessageConsumer? {
        return null
    }

    @Throws(JMSException::class)
    override fun createSharedDurableConsumer(topic: Topic, s: String): MessageConsumer? {
        return null
    }

    @Throws(JMSException::class)
    override fun createSharedDurableConsumer(topic: Topic, s: String, s1: String): MessageConsumer? {
        return null
    }

    @Throws(JMSException::class)
    override fun createBrowser(queue: Queue): QueueBrowser? {
        return null
    }

    @Throws(JMSException::class)
    override fun createBrowser(queue: Queue, s: String): QueueBrowser? {
        return null
    }

    @Throws(JMSException::class)
    override fun createTemporaryQueue(): TemporaryQueue? {
        return null
    }

    @Throws(JMSException::class)
    override fun createTemporaryTopic(): TemporaryTopic? {
        return null
    }

    @Throws(JMSException::class)
    override fun unsubscribe(s: String) {
    }
}
