package org.acme.services;

import org.jboss.logging.Logger;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.StartupEvent;
import io.vertx.mutiny.core.eventbus.Message;
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
                connection.start();

                try(MessageConsumer consumer = session.createConsumer(queue)){
                    log.info("conectado, consumidor criado iniciando ingestão");
                    while(true){
                        var msg = consumer.receive();
                        // chega aqui só se fizer consumer.close
                        if(msg == null){
                            throw new Exception("mensagem nula! provavelmente houve uma desconeão...");
                        }
                        falhas=0;

                        if (msg instanceof TextMessage txt){
                            if(txt.getText().trim().equalsIgnoreCase("bum")){
                                // esse exception vai cair no catch(Exception e) lá embaixo
                                // e vai ser contabilizado como falha, mas a mensagem não vai
                                // ser processada novamente
                                throw new Exception("Falha simulada");
                            }
                            log.info("<<<" + txt.getText());
                        }else{
                            log.info("recebi mensagem que não sei o que é...");
                        }
                    }
                }
            }catch(JMSException e){
                // cai aqui se acontecer uma exceção em qualquer uma das chamadas
                // do JMS, inclusive o receive()
                log.error("Exception do JMS:",e);
            }catch(Exception e){
                // só vai cair aqui se a gente não especificar WMQ_CLIENT_RECONNECT
                // no factory
                log.error("Exception que eu lancei:",e);
            }

            // a gente conta falhas, mas reseta o contador
            // no primeiro sucesso. desse jeito, a gente consegue
            // fazer o programa consumir a fila eternamente enquanto 
            // ele conseguir reconectar.
            // se a gente está testando com a imagem do ibmmq, ela pode demorar
            // um pouco para começar a funcionar e esse container vai acabar
            // falhando.
            // em um kubernetes da vida, isso causaria um crash loop, indicando
            // algum problema mais grave
            if(++falhas==3){
                Quarkus.asyncExit(3);
                return;
            }else{
                log.error("já falhei "+falhas+" vezes, na terceira eu morro");
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }        
        }


    }
}
