package simulations;

import io.gatling.javaapi.core.PopulationBuilder;

import static io.gatling.javaapi.core.CoreDsl.*;

/**
 * Smoke simulation: 1 virtual user, 1 iteration.
 *
 * <p>Purpose: verify that all endpoints are reachable and return
 * expected responses before running heavier load scenarios.
 *
 * <p>Run: {@code mvn gatling:test -Dgatling.simulationClass=simulations.SmokeSimulation}
 */
public class SmokeSimulation extends BaseSimulation {

    private static final int USERS =
            Integer.getInteger("smoke.users", 1);

    {
        PopulationBuilder population =
                ReqresScenarios.smokeScenario.injectOpen(atOnceUsers(USERS));

        setUp(population)
                .protocols(HTTP_PROTOCOL)
                .assertions(
                        global().successfulRequests().percent().gte(100.0),
                        global().responseTime().percentile(95).lte(2000)
                );
    }
}
