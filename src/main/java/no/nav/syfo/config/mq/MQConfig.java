package no.nav.syfo.config.mq;

import com.ibm.mq.jms.MQQueue;
import com.ibm.mq.jms.MQXAConnectionFactory;
import com.ibm.msg.client.wmq.WMQConstants;
import com.ibm.msg.client.wmq.v6.base.internal.MQC;
import no.nav.syfo.service.ServiceVarselService;
import no.nav.syfo.service.TredjepartsvarselService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.destination.DestinationResolver;
import org.springframework.transaction.PlatformTransactionManager;

import javax.inject.Named;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Queue;

import static java.lang.System.getProperty;

@Configuration
@EnableJms
public class MQConfig {

    private static final int UTF_8_WITH_PUA = 1208;

    private static final String MQ_HOSTNAME = "MQGATEWAY03_HOSTNAME";
    private static final String MQ_PORT = "MQGATEWAY03_PORT";
    private static final String MQ_NAME = "MQGATEWAY03_NAME";
    private static final String MQ_CHANNEL = "SYFOOPPFOLGINGSPLANSERVICE_CHANNEL_NAME";
    private static final String MQ_USERNAME = "srvappserver";
    private static final String MQ_PASSOWORD = "";

    private static final String VARSELPRODUKSJON_VARSLINGER_QUEUENAME = "VARSELPRODUKSJON_VARSLINGER_QUEUENAME";
    private static final String VARSELPRODUKSJON_BEST_SRVMLD_M_KONTAKT_QUEUENAME = "VARSELPRODUKSJON_BEST_SRVMLD_M_KONTAKT_QUEUENAME";

    @Bean(name = "jmsListenerContainerFactory")
    public DefaultJmsListenerContainerFactory jmsListenerContainerFactory(ConnectionFactory connectionFactory, DestinationResolver destinationResolver, PlatformTransactionManager transactionManager) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setDestinationResolver(destinationResolver);
        factory.setConcurrency("3-10");
        factory.setTransactionManager(transactionManager);
        return factory;
    }

    @Bean
    public ConnectionFactory connectionFactory() throws JMSException {
        MQXAConnectionFactory connectionFactory = new MQXAConnectionFactory();
        connectionFactory.setHostName(getProperty(MQ_HOSTNAME));
        connectionFactory.setPort(Integer.valueOf(getProperty(MQ_PORT)));
        connectionFactory.setChannel(getProperty(MQ_CHANNEL));
        connectionFactory.setQueueManager(getProperty(MQ_NAME));
        connectionFactory.setTransportType(WMQConstants.WMQ_CM_CLIENT);
        connectionFactory.setCCSID(UTF_8_WITH_PUA);
        connectionFactory.setIntProperty(WMQConstants.JMS_IBM_ENCODING, MQC.MQENC_NATIVE);
        connectionFactory.setIntProperty(WMQConstants.JMS_IBM_CHARACTER_SET, UTF_8_WITH_PUA);
        UserCredentialsXaConnectionFactoryAdapter adapter = new UserCredentialsXaConnectionFactoryAdapter();
        adapter.setTargetConnectionFactory(connectionFactory);
        adapter.setUsername(MQ_USERNAME);
        adapter.setPassword(MQ_PASSOWORD);
        return adapter;
    }

    @Bean
    public DestinationResolver destinationResolver(ApplicationContext context) {
        return (session, destinationName, pubSubDomain) -> context.getBean(destinationName, Queue.class);
    }

    @Bean
    @Named("serviceVarselDestination")
    public Destination serviceVarselDestination() throws JMSException {
        return new MQQueue(getProperty(VARSELPRODUKSJON_VARSLINGER_QUEUENAME));
    }


    @Bean(name = "servicevarselqueue")
    public JmsTemplate servicevarselqueue() throws JMSException {
        JmsTemplate jmsTemplate = new JmsTemplate();
        jmsTemplate.setDefaultDestination(serviceVarselDestination());
        jmsTemplate.setConnectionFactory(connectionFactory());
        jmsTemplate.setSessionTransacted(true);
        return jmsTemplate;
    }

    @Bean
    @Named("tredjepartsVarselDestination")
    public Destination tredjepartsVarselDestination() throws JMSException {
        return new MQQueue(getProperty(VARSELPRODUKSJON_BEST_SRVMLD_M_KONTAKT_QUEUENAME));
    }

    @Bean(name = "tredjepartsvarselqueue")
    public JmsTemplate tredjepartsvarselqueue() throws JMSException {
        JmsTemplate jmsTemplate = new JmsTemplate();
        jmsTemplate.setDefaultDestination(tredjepartsVarselDestination());
        jmsTemplate.setConnectionFactory(connectionFactory());
        jmsTemplate.setSessionTransacted(true);
        return jmsTemplate;
    }

    @Bean
    public ServiceVarselService serviceVarselService() {
        return new ServiceVarselService();
    }

    @Bean
    public TredjepartsvarselService tredjepartsvarselService() {
        return new TredjepartsvarselService();
    }
}

