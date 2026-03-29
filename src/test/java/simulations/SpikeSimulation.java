package simulations;

import io.gatling.javaapi.core.PopulationBuilder;

import java.time.Duration;

import static io.gatling.javaapi.core.CoreDsl.*;

/**
 * Spike simulation: sudden burst of 150 VUs, hold for 2 min, drop back to 20.
 *
 * <p>Models flash-sale or viral traffic patterns. Verifies that the system
 * recovers gracefully once the spike subsides.
 *
 * <p>Run: {@code mvn gatling:test -Dgatling.simulationClass=simulations.SpikeSimulation}
 */
public class SpikeSimulation extends BaseSimulation {

    {
        PopulationBuilder population = ReqresScenarios.stressScenario
                .injectOpen(
                        atOnceUsers(20),
                        nothingFor(Duration.ofSeconds(30)),
                        atOnceUsers(150),
                        nothingFor(Duration.ofSeconds(120)),
                        rampUsers(20).during(Duration.ofSeconds(30))
                );

        setUp(population)
                .protocols(HTTP_PROTOCOL)
                .maxDuration(Duration.ofSeconds(240))
                .assertions(
                        global().successfulRequests().percent().gte(90.0),
                        global().responseTime().percentile(95).lte(5000)
                );
    }
}
