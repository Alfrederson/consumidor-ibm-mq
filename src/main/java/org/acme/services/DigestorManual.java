package org.acme.services;

import org.jboss.logging.Logger;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSException;
import jakarta.jms.MessageConsumer;
import jakarta.jms.Queue;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;

@ApplicationScoped
public class DigestorManual {
    @Inject
    Logger log;

    @Inject
    private ConnectionFactory connectionFactory;


    public void onStart(@Observes StartupEvent ev){
        log.info("iniciando consumidor manual...");
        int falhas = 0;
        while(true){
            try(Connection connection = connectionFactory.createConnection();
                Session session = connection.createSession(false,Session.AUTO_ACKNOWLEDGE)){

                Queue queue = session.createQueue("queue:///lixo");
                MessageConsumer consumer = session.createConsumer(queue);  

                connection.start();

                log.info("conectado, consumidor criado iniciando ingestão");
                while(true){
                    var msg = consumer.receive();
                    if(msg == null)
                        continue;
                    if (msg instanceof TextMessage txt){
                        falhas=0;
                        log.info("<<<" + txt.getText());
                    }else{
                        log.info("recebi mensagem que não sei o que é...");
                    }
                }
            }catch(JMSException e){
                falhas++;
                log.error("já falhei "+falhas+" vezes, na terceira eu morro");
                log.error("alguma coisa na conexão deu errado",e);
                if(falhas==3){
                    Quarkus.asyncExit(3);
                }
            }
        }


    }
}
