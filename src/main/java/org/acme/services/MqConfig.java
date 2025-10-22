package org.acme.services;

import org.jboss.logging.Logger;
import com.ibm.mq.jakarta.jms.MQQueueConnectionFactory;
import com.ibm.msg.client.jakarta.wmq.WMQConstants;

import jakarta.jms.ConnectionFactory;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

@ApplicationScoped
public class MqConfig {
    @Inject
    Logger log;
    
    @Produces
    public ConnectionFactory connectionFactory() throws Exception {
        return new MQQueueConnectionFactory(){
            {                

                // ajustar esses valores pra conseguir conectar no seu
                // mq
                log.info("criando connection factory");
                setHostName("172.17.0.2");
                setPort(1414);
                setChannel("CANAL.FAKE");
                setQueueManager("QM1");
                
                // setClientReconnectOptions(WMQConstants.WMQ_CLIENT_RECONNECT);
                setBooleanProperty(WMQConstants.WMQ_VERBOSE_RECONNECT, true);
                
                // setIntProperty(WMQConstants.WMQ_CLIENT_RECONNECT_TIMEOUT, 35);
                setFailIfQuiesce(WMQConstants.WMQ_FIQ_DEFAULT);
                //setAsyncExceptions(WMQConstants.ASYNC_EXCEPTIONS_ALL);
                setTransportType(WMQConstants.WMQ_CM_CLIENT);
                setStringProperty(WMQConstants.USERID, "app");
                setStringProperty(WMQConstants.PASSWORD, "1234");
            }
        };
    }
}
