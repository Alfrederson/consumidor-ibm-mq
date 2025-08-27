package org.acme.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.jms.JMSException;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

import org.acme.Application;
import org.eclipse.microprofile.reactive.messaging.Acknowledgment;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.reactive.messaging.ChannelRegistry;

@ApplicationScoped
public class Biodigestor {
    @Inject
    ChannelRegistry registry;

    @Incoming("my-queue")
    @Blocking
    @Acknowledgment(Acknowledgment.Strategy.MANUAL)
    public CompletionStage<Void> glubglub(Message<String> message) throws Exception {
        if(message.getPayload()=="morre"){
            Application.finished=true;
        }
        System.out.print("glub("+message.getPayload()+")");
        // com a configuração desse projeto é para
        // esse exceptionally não rodar praticamente nunca no mundo real
        return message.ack().exceptionally(t -> {
            System.out.println("deu esse erro: "+t.getMessage());
            return null;
        }).thenRun(()->{
            System.out.print("(ack -> thenRun)\n");
        });
    }
}