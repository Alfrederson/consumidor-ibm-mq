package org.acme.services.emissor;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.jboss.logging.Logger;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSContext;
import jakarta.jms.JMSProducer;
import jakarta.jms.Queue;

@ApplicationScoped
public class Emissor {
    @Inject
    Logger log;
    
    BlockingQueue<String> fila = new LinkedBlockingQueue<>();
    public void enviar(String msg){
        fila.offer(msg);
        if(fila.size() % 10 == 0){
            log.warn("!!! tem "+fila.size()+"mensagens na fila !!!");
        }
    }

    @Inject
    ConnectionFactory cf;

    private String output;
    public void setOutput(String s){
        output = s;
    }
    
    public void inicio(@Observes StartupEvent ev){
        var worker = new Thread(()-> {
            log.info("iniciando enviador");
            while(true){
                try(JMSContext ctx = cf.createContext(JMSContext.AUTO_ACKNOWLEDGE)){
                    Queue queue = ctx.createQueue(output);
                    JMSProducer producer = ctx.createProducer();
                    while(!Thread.currentThread().isInterrupted()){
                        var msg = fila.take();
                        try {
                            producer.send(queue, msg);
                        }catch(Exception e){
                            fila.offer(msg); // devolve pra fila (interna)
                            throw(e);
                        }
                    }
                    break;
                }catch(Exception e){
                    log.warn("deu alguma coisa ruim no loop de envio de mensagens: "+e.getMessage());
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            log.info("terminando enviador");
        });
        worker.setDaemon(true);
        worker.start();

    }
}
