package simulations;

import io.gatling.javaapi.core.PopulationBuilder;

import java.time.Duration;

import static io.gatling.javaapi.core.CoreDsl.*;

/**
 * Load simulation: ramps to 20 VUs over 30 s, sustains for 5 min.
 *
 * <p>Models normal expected production traffic. Assertions enforce
 * that 95% of responses complete under 2 s and error rate stays below 1%.
 *
 * <p>Run: {@code mvn gatling:test -Dgatling.simulationClass=simulations.LoadSimulation}
 *
 * <p>Override defaults:
 * {@code mvn gatling:test -Dgatling.simulationClass=simulations.LoadSimulation
 *   -Dload.users=50 -Dload.rampSeconds=60 -Dload.durationSeconds=600}
 */
public class LoadSimulation extends BaseSimulation {

    private static final int USERS =
            Integer.getInteger("load.users", 20);
    private static final int RAMP_SECONDS =
            Integer.getInteger("load.rampSeconds", 30);
    private static final int DURATION_SECONDS =
            Integer.getInteger("load.durationSeconds", 300);

    {
        PopulationBuilder population = ReqresScenarios.loadScenario
                .injectOpen(
                        rampUsers(USERS).during(Duration.ofSeconds(RAMP_SECONDS))
                );

        setUp(population)
                .protocols(HTTP_PROTOCOL)
                .maxDuration(Duration.ofSeconds(DURATION_SECONDS + RAMP_SECONDS))
                .assertions(
                        global().successfulRequests().percent().gte(99.0),
                        global().responseTime().percentile(95).lte(2000),
                        global().responseTime().percentile(99).lte(3000),
                        global().requestsPerSec().gte(5.0)
                );
    }
}
