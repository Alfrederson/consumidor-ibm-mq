package org.acme.services;

import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;

import io.smallrye.common.annotation.Identifier;

import io.smallrye.reactive.messaging.IncomingInterceptor;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@Identifier("my-queue")
@ApplicationScoped
public class Interceptador implements IncomingInterceptor {
    @Inject
    Logger log;
    
    @Override
    public Message<?> afterMessageReceive(Message <?> message){
        log.info("recebi ["+message.getPayload()+"]");
        return message;
    }

    @Override
    public void onMessageAck(Message<?> message) {
        log.info("ack ["+message.getPayload()+"]");
    }

    @Override
    public void onMessageNack(Message<?> message, Throwable failure) {
        log.info("nack ["+message.getPayload()+"]");
    }
}
