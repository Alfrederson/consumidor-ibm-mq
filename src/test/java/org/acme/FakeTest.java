package org.acme;

import org.junit.jupiter.api.Test;

import io.smallrye.mutiny.Uni;
import jakarta.jms.JMSException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Metadata;
import org.acme.services.Biodigestor;

public class FakeTest {
    @Test
    public void testHelloEndpoint() throws Exception {

    }
}