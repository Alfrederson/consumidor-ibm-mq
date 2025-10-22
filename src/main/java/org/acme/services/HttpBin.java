package org.acme.services;

import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.core.Response;

@RegisterRestClient(baseUri = "http://192.168.15.19:9999/delay/5/80",configKey="httpbin")
public interface HttpBin {
    @GET
    @Retry(
        maxRetries = 3,              // try up to 3 times
        delay = 500,                 // wait 500 ms between retries
        jitter = 200                 // random extra wait (0–200 ms)
    )
    Response delay();
}
