package simulations;

import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import static io.gatling.javaapi.http.HttpDsl.http;

/**
 * Base class providing shared HTTP protocol configuration for all simulations.
 * Targets reqres.in — a publicly accessible REST API.
 */
public abstract class BaseSimulation extends Simulation {

    protected static final String BASE_URL =
            System.getProperty("base.url", "https://reqres.in");

    protected static final HttpProtocolBuilder HTTP_PROTOCOL = http
            .baseUrl(BASE_URL)
            .acceptHeader("application/json")
            .contentTypeHeader("application/json")
            .userAgentHeader("Gatling/3.10 Performance Suite")
            .shareConnections();
}
