package org.acme.services;

import java.time.Duration;

import org.jboss.logging.Logger;
import com.ibm.mq.jakarta.jms.MQQueueConnectionFactory;
import com.ibm.msg.client.jakarta.wmq.WMQConstants;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.ExceptionListener;
import jakarta.jms.JMSContext;
import jakarta.jms.JMSException;
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
            //@Override
            public JMSContext createContextff(int arg0) {
                // TODO Auto-generated method stub
                log.warn("criando o context do jeito malucado!");
                var ctx = super.createContext(arg0);
                
                Uni.createFrom().voidItem()
                        .onItem().delayIt().by(Duration.ofSeconds(2))
                        .subscribe().with(s->{
                            ctx.setExceptionListener(new ExceptionListener() {
                                @Override
                                public void onException(JMSException t){
                                    log.error("fuck!");
                                    t.printStackTrace();
                                }
                            });

                        });

                return ctx;
            }
            {                
                // ajustar esses valores pra conseguir conectar no seu
                // mq
                log.info("criando connection factory");
                setHostName("172.17.0.2");
                setPort(1414);
                setChannel("TOTO");
                setQueueManager("QM1");
                
                // setIntProperty(WMQConstants.WMQ_CLIENT_RECONNECT_OPTIONS, WMQConstants.WMQ_CLIENT_RECONNECT);
                setBooleanProperty(WMQConstants.WMQ_VERBOSE_RECONNECT, true);
                //setAsyncExceptions(WMQConstants.ASYNC_EXCEPTIONS_ALL);
                setTransportType(WMQConstants.WMQ_CM_CLIENT);
                setStringProperty(WMQConstants.USERID, "bebezao");

            }
        };
    }
}
