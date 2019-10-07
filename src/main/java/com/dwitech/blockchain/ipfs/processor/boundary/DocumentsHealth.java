package com.dwitech.blockchain.ipfs.processor.boundary;

import org.eclipse.microprofile.health.*;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import java.util.Date;

import static java.lang.System.currentTimeMillis;
import static org.eclipse.microprofile.health.HealthCheckResponse.named;

@ApplicationScoped
public class DocumentsHealth {

    @Produces @Liveness
    HealthCheck live() {
        return () ->  named("IB24-Priceloader-Service: " + new Date(currentTimeMillis()))
                       .withData("prices","GET /prices?estate_type=<long>&estate_size=<long>&estate_visit_reason=<long>")
                       .up()
                       .build();
    }

    @Produces @Readiness
    HealthCheck ready() {
        HealthCheckResponseBuilder responseBuilder = HealthCheckResponse.named("IB24-Priceloader-Service : Database connection health check");
        responseBuilder.state(true);
        return responseBuilder::build;
    }
}