package org.acme;
import java.time.Duration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.acme.services.HttpBin;
import org.acme.services.emissor.Emissor;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import io.quarkus.runtime.StartupEvent;
import io.smallrye.mutiny.Multi;
import io.smallrye.reactive.messaging.providers.extension.HealthCenter;
import io.smallrye.reactive.messaging.providers.impl.ConfiguredChannelFactory;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

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
    HealthCenter health;

    @Inject
    @RestClient
    HttpBin httpBin;

    @Inject
    ConfiguredChannelFactory manager;
    
    static {
        // java.util.logging.Logger logger = java.util.logging.Logger.getLogger("com.ibm.mq");
        // logger.setLevel(Level.FINEST);
        // ConsoleHandler handler = new ConsoleHandler();
        // handler.setLevel(Level.FINEST);
        // logger.addHandler(handler);
    }

    final int POOL_SIZE = 20;

    ExecutorService pool = Executors.newFixedThreadPool(POOL_SIZE);
    // Semaphore sem = new Semaphore(POOL_SIZE);

    void fazerOChamado(int num) throws InterruptedException{

        // sem.acquire();
        // log.info(sem.availablePermits() + " spots");
        pool.submit(()->{
            try{
                long start = System.nanoTime();
                Response response = httpBin.delay();
                long durationMs = (System.nanoTime() - start) / 1_000_000;
                log.info(num + " => " 
                        + response.getStatus() + " (" + durationMs + " ms)");
            }catch(Exception e){
                log.info(num + " ups "+e.getMessage());
                e.printStackTrace();
            }finally{
                // sem.release();
            }
        });
    }
    void chamarHttpBinEternamente(){
        log.info("iniciando o esgotador de pool");
        new Thread(){
            @Override
            public void run() {
                int num = 0;
                while(num < 1000){
                    num++;
                    try {
                        Thread.sleep(100);
                        fazerOChamado(num);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }


    AtomicInteger contador = new AtomicInteger(0);

    @Inject
    Emissor emissor;
    
    void onStart(@Observes StartupEvent ev) {
        // vamos ver se tem a ver...
        // chamarHttpBinEternamente();

        Multi.createFrom().ticks().every(Duration.ofSeconds(2))
            .subscribe().with(item -> {
                emissor.enviar("msg "+contador.incrementAndGet());
            });

        // cria um Multi que checa de 5 em 5 segundos se o 
        // liveness do reactive messaging indica que ele morreu.
        // usar isso só se por algum motivo o /q/health precisar dar
        // sempre status 200.
        // também pode ser feito com new Thread(){ ... }.start();
        // log.warn("ativando reiniciador...");
        // Multi.createFrom().ticks().every(Duration.ofSeconds(10))
        //     .onItem().transform(tick -> {
        //         if (health.getLiveness().isOk()) {
        //             // log.info("tudo ok!");
        //             return tick;
        //         }
        //         health.getLiveness().getChannels().forEach(channelInfo -> {
        //             log.errorf("SmallRye fechou o consumidor: %s", channelInfo.getChannel());
        //             log.errorf("Motivo: %s", channelInfo.getMessage());
        //         });                                
        //         log.error("eu  preciso ser reiniciado!");

        //         return tick;
        //     })
        //     .onFailure().retry().indefinitely()
        //     .subscribe().with(item -> {});
        // }
    }
}

