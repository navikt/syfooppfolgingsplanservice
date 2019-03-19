package no.nav.syfo.config.mq;

import com.ibm.mq.jms.MQQueue;
import com.ibm.mq.jms.MQXAConnectionFactory;
import no.nav.syfo.config.mqconfigs.mq.UserCredentialsXaConnectionFactoryAdapter;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.jms.XAConnectionFactoryWrapper;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.destination.DestinationResolver;

import javax.jms.*;

import static com.ibm.mq.constants.CMQC.MQENC_NATIVE;
import static com.ibm.msg.client.jms.JmsConstants.JMS_IBM_CHARACTER_SET;
import static com.ibm.msg.client.jms.JmsConstants.JMS_IBM_ENCODING;
import static com.ibm.msg.client.wmq.common.CommonConstants.WMQ_CM_CLIENT;
import static java.lang.System.getenv;

@Configuration
@EnableJms
@Profile({"remote"})
public class MQConfig {

    private static final int UTF_8_WITH_PUA = 1208;

    @Value("${syfooppfolgingsplanservice.channel.name}")
    private String channelName;
    @Value("${mqgateway03.hostname}")
    private String gatewayHostname;
    @Value("${mqgateway03.name}")
    private String gatewayName;
    @Value("${mqgateway03.port}")
    private int gatewayPort;
    @Value("${srvappserver.username:srvappserver}")
    private String srvAppserverUsername;
    @Value("${srvappserver.password:}")
    private String srvAppserverPassword;

    private static final String VARSELPRODUKSJON_VARSLINGER_QUEUENAME = "VARSELPRODUKSJON_VARSLINGER_QUEUENAME";
    private static final String VARSELPRODUKSJON_BEST_SRVMLD_M_KONTAKT_QUEUENAME = "VARSELPRODUKSJON_BEST_SRVMLD_M_KONTAKT_QUEUENAME";

    @Bean
    public DestinationResolver destinationResolver(ApplicationContext context) {
        return (session, destinationName, pubSubDomain) -> context.getBean(destinationName, Queue.class);
    }

    @Bean(name = "serviceVarselDestination")
    public Queue serviceVarselDestination() throws JMSException {
        return new MQQueue(getenv(VARSELPRODUKSJON_VARSLINGER_QUEUENAME));
    }


    @Bean(name = "servicevarselqueue")
    public JmsTemplate servicevarselqueue(
            @Autowired @Qualifier("serviceVarselDestination") Queue serviceVarselDestination,
            ConnectionFactory connectionFactory
    ) {
        JmsTemplate jmsTemplate = new JmsTemplate();
        jmsTemplate.setDefaultDestination(serviceVarselDestination);
        jmsTemplate.setConnectionFactory(connectionFactory);
        jmsTemplate.setSessionTransacted(true);
        return jmsTemplate;
    }

    @Bean(name = "tredjepartsVarselDestination")
    public Queue tredjepartsVarselDestination() throws JMSException {
        return new MQQueue(getenv(VARSELPRODUKSJON_BEST_SRVMLD_M_KONTAKT_QUEUENAME));
    }

    @Bean(name = "tredjepartsvarselqueue")
    public JmsTemplate tredjepartsvarselqueue(
            @Autowired @Qualifier("tredjepartsVarselDestination") Queue tredjepartsVarselDestination,
            ConnectionFactory connectionFactory
    ) {
        JmsTemplate jmsTemplate = new JmsTemplate();
        jmsTemplate.setDefaultDestination(tredjepartsVarselDestination);
        jmsTemplate.setConnectionFactory(connectionFactory);
        jmsTemplate.setSessionTransacted(true);
        return jmsTemplate;
    }

    @Bean
    public ConnectionFactory connectionFactory(XAConnectionFactoryWrapper xaConnectionFactoryWrapper) throws Exception {
        MQXAConnectionFactory connectionFactory = new MQXAConnectionFactory();
        connectionFactory.setHostName(gatewayHostname);
        connectionFactory.setPort(gatewayPort);
        connectionFactory.setChannel(channelName);
        connectionFactory.setQueueManager(gatewayName);
        connectionFactory.setTransportType(WMQ_CM_CLIENT);
        connectionFactory.setCCSID(UTF_8_WITH_PUA);
        connectionFactory.setIntProperty(JMS_IBM_ENCODING, MQENC_NATIVE);
        connectionFactory.setIntProperty(JMS_IBM_CHARACTER_SET, UTF_8_WITH_PUA);
        UserCredentialsXaConnectionFactoryAdapter adapter = new UserCredentialsXaConnectionFactoryAdapter();
        adapter.setTargetConnectionFactory(connectionFactory);
        adapter.setUsername(srvAppserverUsername);
        adapter.setPassword(srvAppserverPassword);
        return xaConnectionFactoryWrapper.wrapConnectionFactory(adapter);
    }
}

