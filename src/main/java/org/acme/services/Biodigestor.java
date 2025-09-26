package org.acme.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.concurrent.CompletionStage;

import org.eclipse.microprofile.reactive.messaging.Acknowledgment;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;

import io.smallrye.common.annotation.Blocking;
import io.smallrye.common.annotation.Identifier;

@Identifier("channel-a")
@ApplicationScoped
public class Biodigestor {

    @Inject
    Logger log;
    



    // @Incoming("my-queue")
    // @Blocking
    // @Acknowledgment(Acknowledgment.Strategy.MANUAL)
    
    public CompletionStage<Void> glubglub(Message<String> message) throws Exception {
        var msg = message.getPayload();
        log.info("processei ["+msg+"]");
        if("bug".equals(msg)){
            throw new Exception("Isso mata o consumer, mas a mensagem sai da fila porque estou com AUTO_ACKNOWLEDGE.");
        }
        // outra estratégia: a gente deixa o smallrye-messaging falhar,
        // mas fica checando se ele matou o canal em um multi/thread
        return message.ack();

        // quando o método .acknowledge() do delegate de dentro do Message
        // lança exceção, ela aqui dentro do exceptionally.
        // however, uma exceção também pode ser lançada pelo .receive() na classe
        // JmsPublisher , dentro do metodo enqueue, por exemplo, em caso de
        // reconexão.
        // Essa a gente não consegue capturar, e ela gera uma falha que é contabilizada
        // pelo Multi source da classe JmsSource. 
        // Quando esgotam os retries, o fluxo é interrompido permanentemente
        // e não tem o que ser feito. Não tem jeito fácil de capturar isso.
        // Não é possível recriar esse consumidor.
        // A ideia é que quando esse tipo de falha ocorre, o endpoint 
        // do /q/health vai automagicamente passar a retornar 503, indicando que
        // o pod deve ser reiniciado.
        // Em um certo lugar, há programas que retornam status 200 no /q/health independente
        // de o consumo ter sido interrompido.
        // Uma solução de contorno é ficar chamando o método getLiveness().isOk() de uma classe
        // do smallrye-messaging chamada HealthCenter. Se existem 3 canais e um deles morre,
        // o método .isOk() vai retornar falso.

        // return message.ack().exceptionally(t -> {
        //     log.error("que que deu? "+t.getMessage());
        //     return null;
        // });
    }
}