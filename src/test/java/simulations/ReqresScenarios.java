package simulations;

import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

/**
 * Reusable chains and scenarios for the reqres.in API.
 *
 * <p>Chains are composed into ScenarioBuilders that the individual
 * simulation classes reference — keeping each simulation class focused
 * on thread-group shape rather than request detail.
 */
public final class ReqresScenarios {

    private ReqresScenarios() {}

    // ─── Chains ────────────────────────────────────────────────────────────

    /** Fetch page 1 or 2 of users and extract the first user's id. */
    public static final ChainBuilder listUsers =
            exec(http("GET List Users")
                    .get("/api/users")
                    .queryParam("page", "#{page}")
                    .check(status().is(200))
                    .check(jsonPath("$.data[0].id").saveAs("userId"))
                    .check(responseTimeInMillis().lte(2000)));

    /** Fetch a single user by extracted id. */
    public static final ChainBuilder getUser =
            exec(http("GET Single User")
                    .get("/api/users/#{userId}")
                    .check(status().is(200))
                    .check(jsonPath("$.data.id").exists())
                    .check(responseTimeInMillis().lte(2000)));

    /** Create a new user — validates 201 and returned id. */
    public static final ChainBuilder createUser =
            exec(http("POST Create User")
                    .post("/api/users")
                    .body(StringBody(
                            "{\"name\":\"perf-user-#{userId}\",\"job\":\"load-tester\"}"))
                    .check(status().is(201))
                    .check(jsonPath("$.id").exists())
                    .check(responseTimeInMillis().lte(3000)));

    /** Update an existing user. */
    public static final ChainBuilder updateUser =
            exec(http("PUT Update User")
                    .put("/api/users/#{userId}")
                    .body(StringBody(
                            "{\"name\":\"updated-user\",\"job\":\"senior-tester\"}"))
                    .check(status().is(200))
                    .check(jsonPath("$.updatedAt").exists())
                    .check(responseTimeInMillis().lte(3000)));

    /** Login and capture the auth token. */
    public static final ChainBuilder login =
            exec(http("POST Login")
                    .post("/api/login")
                    .body(StringBody(
                            "{\"email\":\"eve.holt@reqres.in\",\"password\":\"cityslicka\"}"))
                    .check(status().is(200))
                    .check(jsonPath("$.token").saveAs("authToken"))
                    .check(responseTimeInMillis().lte(2000)));

    // ─── Scenarios ─────────────────────────────────────────────────────────

    /**
     * Smoke scenario: single pass through every endpoint.
     */
    public static final ScenarioBuilder smokeScenario =
            scenario("Smoke Scenario")
                    .feed(feeder())
                    .exec(login)
                    .pause(1)
                    .exec(listUsers)
                    .pause(1)
                    .exec(getUser)
                    .pause(1)
                    .exec(createUser)
                    .pause(1)
                    .exec(updateUser);

    /**
     * Read-heavy scenario: 80% reads, 20% writes — typical production traffic mix.
     */
    public static final ScenarioBuilder loadScenario =
            scenario("Load Scenario")
                    .feed(feeder())
                    .exec(listUsers)
                    .pause(1, 2)
                    .exec(getUser)
                    .pause(1, 2)
                    .randomSwitch()
                        .on(
                            percent(80.0).then(exec(listUsers).pause(1)),
                            percent(20.0).then(exec(createUser).pause(1)));

    /**
     * Stress scenario: maximum throughput with minimal pauses.
     */
    public static final ScenarioBuilder stressScenario =
            scenario("Stress Scenario")
                    .feed(feeder())
                    .exec(listUsers)
                    .pause(0, 1)
                    .exec(getUser)
                    .pause(0, 1)
                    .exec(createUser);

    // ─── Private helpers ───────────────────────────────────────────────────

    private static io.gatling.javaapi.core.FeederBuilder<Object> feeder() {
        return listFeeder(java.util.List.of(
                java.util.Map.<String, Object>of("page", 1, "userId", 1),
                java.util.Map.<String, Object>of("page", 1, "userId", 2),
                java.util.Map.<String, Object>of("page", 2, "userId", 3),
                java.util.Map.<String, Object>of("page", 2, "userId", 4),
                java.util.Map.<String, Object>of("page", 1, "userId", 5),
                java.util.Map.<String, Object>of("page", 2, "userId", 6)
        )).random();
    }
}
