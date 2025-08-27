package org.acme.services;

import org.eclipse.microprofile.reactive.messaging.Message;

import io.smallrye.common.annotation.Identifier;
import io.smallrye.reactive.messaging.IncomingInterceptor;
import jakarta.enterprise.context.ApplicationScoped;

@Identifier("my-queue")
@ApplicationScoped
public class Interceptador implements IncomingInterceptor {

    @Override
    public Message<?> afterMessageReceive(Message <?> message){
        System.out.print("receive ->");
        return message;
    }

    @Override
    public void onMessageAck(Message<?> message) {
        // Called after message ack
        System.out.print("interceptor ack("+message.getPayload()+")");
    }

    @Override
    public void onMessageNack(Message<?> message, Throwable failure) {
        // Called after message nack
        System.out.print("interceptor_nack("+message.getPayload()+")");
    }
}
