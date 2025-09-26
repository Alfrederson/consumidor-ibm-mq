package org.acme;
import java.time.Duration;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.health.Liveness;
import org.eclipse.microprofile.reactive.messaging.spi.Connector;
import org.jboss.logging.Logger;

import io.quarkus.runtime.StartupEvent;
import io.smallrye.mutiny.Multi;
import io.smallrye.health.api.AsyncHealthCheck;
import io.smallrye.reactive.messaging.jms.JmsConnector;
import io.smallrye.reactive.messaging.providers.extension.HealthCenter;
import io.smallrye.reactive.messaging.providers.extension.MediatorManager;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/")
public class Apizinha {

    @Inject
    Logger log;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String root() {
        var ok = health.getLiveness().isOk();
        log.warn("O smallrye messaging está ok? "+ok);
        return "Esta é nossa apizinha! O smallrye está: "+ok;
    }

    @Inject
    @Connector("smallrye-jms")
    JmsConnector connector;

    @Inject
    HealthCenter health;

    @Inject
    MediatorManager manager;

    void onStart(@Observes StartupEvent ev) {
        // cria um Multi que checa de 5 em 5 segundos se o 
        // liveness do reactive messaging indica que ele morreu.
        // usar isso só se por algum motivo o /q/health precisar dar
        // sempre status 200.
        // também pode ser feito com new Thread(){ ... }.start();
        log.warn("ativando reiniciador...");
        Multi.createFrom().ticks().every(Duration.ofSeconds(5))
            .onItem().transform(tick -> {
                if (health.getLiveness().isOk()) {
                    return tick;
                }
                health.getLiveness().getChannels().forEach(channelInfo -> {
                    log.errorf("SmallRye fechou o consumidor: %s", channelInfo.getChannel());
                    log.errorf("Motivo: %s", channelInfo.getMessage());
                });

                Multi.createFrom().publisher(
                    connector.getPublisher(ConfigProvider.getConfig())
                ).onItem().invoke(item ->{
                    var x = item.getPayload();
                    log.info("ZUMBI "+x);
                    item.ack();
                });
                
                

                log.error("eu  preciso ser reiniciado!");
                //Quarkus.asyncExit(1);
                return null;
            })
            .onFailure().recoverWithCompletion()
            .subscribe().with(item -> {});    }

}

