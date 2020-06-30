package no.nav.syfo.mq.mock

import javax.jms.*

class ConnectionFactoryMock : ConnectionFactory {
    @Throws(JMSException::class)
    override fun createConnection(): Connection? {
        return null
    }

    @Throws(JMSException::class)
    override fun createConnection(s: String, s1: String): Connection? {
        return null
    }

    override fun createContext(): JMSContext? {
        return null
    }

    override fun createContext(s: String, s1: String): JMSContext? {
        return null
    }

    override fun createContext(s: String, s1: String, i: Int): JMSContext? {
        return null
    }

    override fun createContext(i: Int): JMSContext? {
        return null
    }
}
