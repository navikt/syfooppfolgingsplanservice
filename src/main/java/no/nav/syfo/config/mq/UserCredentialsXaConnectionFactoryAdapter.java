package no.nav.syfo.config.mq;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.NamedThreadLocal;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.jms.*;

public class UserCredentialsXaConnectionFactoryAdapter implements XAConnectionFactory, ConnectionFactory, QueueConnectionFactory, TopicConnectionFactory, InitializingBean {

    private ConnectionFactory targetConnectionFactory;
    private String username;
    private String password;

    private final ThreadLocal<JmsUserCredentials> threadBoundCredentials =
            new NamedThreadLocal<>("Current JMS user credentials");

    public XAConnection createXAConnection() throws JMSException {
        JmsUserCredentials threadCredentials = this.threadBoundCredentials.get();
        if (threadCredentials != null) {
            return doCreateXaConnection(threadCredentials.username, threadCredentials.password);
        } else {
            return doCreateXaConnection(this.username, this.password);
        }
    }

    public XAConnection createXAConnection(String username, String password) throws JMSException {
        return this.doCreateXaConnection(username, password);
    }

    private XAConnection doCreateXaConnection(String username, String password) throws JMSException {
        Assert.state(this.targetConnectionFactory != null, "'targetConnectionFactory' is required");
        if (!(this.targetConnectionFactory instanceof XAConnectionFactory)) {
            throw new javax.jms.IllegalStateException("'targetConnectionFactory' is not a XAConnectionFactory");
        }
        XAConnectionFactory xaConnectionFactory = (XAConnectionFactory) this.targetConnectionFactory;
        if (StringUtils.hasLength(username)) {
            return xaConnectionFactory.createXAConnection(username, password);
        } else {
            return xaConnectionFactory.createXAConnection();
        }
    }

    public void setTargetConnectionFactory(ConnectionFactory targetConnectionFactory) {
        Assert.notNull(targetConnectionFactory, "'targetConnectionFactory' must not be null");
        this.targetConnectionFactory = targetConnectionFactory;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void afterPropertiesSet() {
        if (this.targetConnectionFactory == null) {
            throw new IllegalArgumentException("Property 'targetConnectionFactory' is required");
        }
    }

    public final Connection createConnection() throws JMSException {
        JmsUserCredentials threadCredentials = this.threadBoundCredentials.get();
        if (threadCredentials != null) {
            return doCreateConnection(threadCredentials.username, threadCredentials.password);
        } else {
            return doCreateConnection(this.username, this.password);
        }
    }


    public Connection createConnection(String username, String password) throws JMSException {
        return doCreateConnection(username, password);
    }

    protected Connection doCreateConnection(String username, String password) throws JMSException {
        Assert.state(this.targetConnectionFactory != null, "'targetConnectionFactory' is required");
        if (StringUtils.hasLength(username)) {
            return this.targetConnectionFactory.createConnection(username, password);
        } else {
            return this.targetConnectionFactory.createConnection();
        }
    }


    public final QueueConnection createQueueConnection() throws JMSException {
        JmsUserCredentials threadCredentials = this.threadBoundCredentials.get();
        if (threadCredentials != null) {
            return doCreateQueueConnection(threadCredentials.username, threadCredentials.password);
        } else {
            return doCreateQueueConnection(this.username, this.password);
        }
    }


    public QueueConnection createQueueConnection(String username, String password) throws JMSException {
        return doCreateQueueConnection(username, password);
    }

    protected QueueConnection doCreateQueueConnection(String username, String password) throws JMSException {
        Assert.state(this.targetConnectionFactory != null, "'targetConnectionFactory' is required");
        if (!(this.targetConnectionFactory instanceof QueueConnectionFactory)) {
            throw new javax.jms.IllegalStateException("'targetConnectionFactory' is not a QueueConnectionFactory");
        }
        QueueConnectionFactory queueFactory = (QueueConnectionFactory) this.targetConnectionFactory;
        if (StringUtils.hasLength(username)) {
            return queueFactory.createQueueConnection(username, password);
        } else {
            return queueFactory.createQueueConnection();
        }
    }

    public final TopicConnection createTopicConnection() throws JMSException {
        JmsUserCredentials threadCredentials = this.threadBoundCredentials.get();
        if (threadCredentials != null) {
            return doCreateTopicConnection(threadCredentials.username, threadCredentials.password);
        } else {
            return doCreateTopicConnection(this.username, this.password);
        }
    }

    public TopicConnection createTopicConnection(String username, String password) throws JMSException {
        return doCreateTopicConnection(username, password);
    }

    protected TopicConnection doCreateTopicConnection(String username, String password) throws JMSException {
        Assert.state(this.targetConnectionFactory != null, "'targetConnectionFactory' is required");
        if (!(this.targetConnectionFactory instanceof TopicConnectionFactory)) {
            throw new javax.jms.IllegalStateException("'targetConnectionFactory' is not a TopicConnectionFactory");
        }
        TopicConnectionFactory queueFactory = (TopicConnectionFactory) this.targetConnectionFactory;
        if (StringUtils.hasLength(username)) {
            return queueFactory.createTopicConnection(username, password);
        } else {
            return queueFactory.createTopicConnection();
        }
    }

    private static class JmsUserCredentials {
        public final String username;
        public final String password;

        private JmsUserCredentials(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public String toString() {
            return "JmsUserCredentials[username='" + this.username + "',password='" + this.password + "']";
        }
    }

}
