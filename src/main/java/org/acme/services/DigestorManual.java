package org.acme.services;

import org.jboss.logging.Logger;

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

    // a gente pode misturar a leitura em loop com esses
    // canais do reactive messaging
    //@Incoming("canal")
    public void processar(String msg){
        log.info("processei["+msg+"]");
    }

    // private final SubmissionPublisher<String> publisher = new SubmissionPublisher<>();

    // @Outgoing("canal")
    // public Flow.Publisher<String> stream(){
    //     return publisher;
    // }

    @Inject
    private ConnectionFactory connectionFactory;

    public void comecarDigestao(){
        int falhas = 0;
        while(falhas < 3){
            try(Connection connection = connectionFactory.createConnection();
                Session session = connection.createSession(false,Session.AUTO_ACKNOWLEDGE)){
                Queue queue = session.createQueue("queue:///lixo");

                connection.start();

                try(MessageConsumer consumer = session.createConsumer(queue)){
                    log.info("conectado, consumidor criado iniciando ingestão");
                    falhas=0;
                    while(true){
                        var msg = consumer.receive();
                        // chega aqui só se fizer consumer.close
                        if(msg == null){
                            throw new Exception("mensagem nula! provavelmente houve uma desconexão...");
                        }

                        if (msg instanceof TextMessage txt){
                            if(txt.getText().trim().equalsIgnoreCase("bum")){
                                // esse exception vai cair no catch(Exception e) lá embaixo
                                // e vai ser contabilizado como falha, mas a mensagem não vai
                                // ser processada novamente
                                throw new Exception("Falha simulada");
                            }
                            processar(txt.getText());
                        }else{
                            log.info("recebi mensagem que não sei o que é...");
                        }
                    }
                }
            }catch(JMSException e){
                // cai aqui se acontecer uma exceção em qualquer uma das chamadas
                // do JMS, inclusive o receive()
                log.error("Exception do JMS:" + e.getMessage());
            }catch(Exception e){
                // só vai cair aqui se a gente não especificar WMQ_CLIENT_RECONNECT
                // no factory
                log.error("Exception que eu lancei:"+e.getMessage());
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
            ++falhas;
            log.error("já falhei "+falhas+" vezes, na terceira eu morro");
            try {
                Thread.sleep(2500);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }                    
        }
        //Quarkus.asyncExit(3);
    }

    public boolean funciona=false;
    public void onStart(@Observes StartupEvent ev) throws InterruptedException{
        if(!funciona) return;

        log.info("iniciando consumidor manual...");
        new Thread(this::comecarDigestao,"loop digestão").start();
    }
}
