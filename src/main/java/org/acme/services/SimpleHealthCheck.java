package org.acme.services;

import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSException;

@ApplicationScoped  
@Liveness
public class SimpleHealthCheck implements HealthCheck {

    @Inject
    ConnectionFactory cf;

    AtomicBoolean morto = new AtomicBoolean(false);
    public void morreu(){
        morto.set(true);
    }
    
    @Override
    public HealthCheckResponse call() {
        return HealthCheckResponse.builder().status(!morto.get()).name("emissor").build();
    }
}