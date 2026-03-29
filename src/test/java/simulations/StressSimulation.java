package simulations;

import io.gatling.javaapi.core.PopulationBuilder;

import java.time.Duration;

import static io.gatling.javaapi.core.CoreDsl.*;

/**
 * Stress simulation: ramps to 100 VUs over 60 s, sustains for 10 min.
 *
 * <p>Pushes the API beyond normal load to identify breaking points
 * and degradation patterns. Assertions use relaxed thresholds (5 s p95)
 * to observe behaviour rather than enforce SLOs.
 *
 * <p>Run: {@code mvn gatling:test -Dgatling.simulationClass=simulations.StressSimulation}
 *
 * <p>Override defaults:
 * {@code mvn gatling:test -Dgatling.simulationClass=simulations.StressSimulation
 *   -Dstress.users=200 -Dstress.rampSeconds=120 -Dstress.durationSeconds=900}
 */
public class StressSimulation extends BaseSimulation {

    private static final int USERS =
            Integer.getInteger("stress.users", 100);
    private static final int RAMP_SECONDS =
            Integer.getInteger("stress.rampSeconds", 60);
    private static final int DURATION_SECONDS =
            Integer.getInteger("stress.durationSeconds", 600);

    {
        PopulationBuilder population = ReqresScenarios.stressScenario
                .injectOpen(
                        nothingFor(Duration.ofSeconds(5)),
                        rampUsers(USERS / 2).during(Duration.ofSeconds(RAMP_SECONDS / 2)),
                        rampUsers(USERS).during(Duration.ofSeconds(RAMP_SECONDS))
                );

        setUp(population)
                .protocols(HTTP_PROTOCOL)
                .maxDuration(Duration.ofSeconds(DURATION_SECONDS + RAMP_SECONDS + 5))
                .assertions(
                        global().successfulRequests().percent().gte(95.0),
                        global().responseTime().percentile(95).lte(5000),
                        global().responseTime().mean().lte(2000)
                );
    }
}
