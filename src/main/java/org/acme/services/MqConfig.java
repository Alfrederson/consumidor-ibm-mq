package org.acme.services;

import com.ibm.mq.jakarta.jms.MQQueueConnectionFactory;
import com.ibm.msg.client.jakarta.wmq.WMQConstants;

import jakarta.jms.ConnectionFactory;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

@ApplicationScoped
public class MqConfig {
    public MqConfig(){
        System.out.println("Criando MqConfig...");
    }
    @Produces
    public ConnectionFactory connectionFactory() throws Exception {
        return new MQQueueConnectionFactory(){
            {
                setHostName("localhost");
                setPort(1414);
                setChannel("TOTO");
                setQueueManager("QM1");
                setIntProperty(WMQConstants.WMQ_CLIENT_RECONNECT_OPTIONS, WMQConstants.WMQ_CLIENT_RECONNECT);
                setTransportType(WMQConstants.WMQ_CM_CLIENT);
                setIntProperty(WMQConstants.ASYNC_EXCEPTIONS, WMQConstants.ASYNC_EXCEPTIONS_ALL);
            }
        };
    }
}
