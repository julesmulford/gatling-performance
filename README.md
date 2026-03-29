# gatling-performance

> **GitHub repo description:** Gatling performance test suite — Java DSL, smoke/load/stress/spike simulations targeting reqres.in REST API with HTML reporting and CI/CD.

A professional-grade performance testing suite using Gatling 3.10 (Java DSL) demonstrating enterprise patterns: scenario composition, parameterised injection profiles, JSON path extraction chaining, multi-stage ramp-up, reusable scenario chains, and threshold-driven CI assertions.

## Tech Stack

| Component      | Technology                               |
|----------------|------------------------------------------|
| Load Tool      | Gatling 3.10.5 (Java DSL)               |
| Build / Runner | Maven 3.9 + gatling-maven-plugin 4.9.6  |
| Target         | reqres.in (public REST API)              |
| Reporting      | Gatling HTML Report                      |
| CI             | GitHub Actions                           |

## Project Structure

```
gatling-performance/
├── .github/
│   └── workflows/
│       └── ci.yml
├── scripts/
│   ├── run-smoke.sh
│   ├── run-load.sh
│   ├── run-stress.sh
│   └── run-spike.sh
├── src/
│   └── test/
│       ├── java/
│       │   └── simulations/
│       │       ├── BaseSimulation.java      # Shared HTTP protocol config
│       │       ├── ReqresScenarios.java     # Reusable chains & scenarios
│       │       ├── SmokeSimulation.java     # 1 VU, single pass
│       │       ├── LoadSimulation.java      # 20 VUs, 5 min sustained
│       │       ├── StressSimulation.java    # 100 VUs, 10 min sustained
│       │       └── SpikeSimulation.java     # 150 VU burst then recover
│       └── resources/
│           └── gatling.conf
├── pom.xml
└── README.md
```

## Prerequisites

- [Java 17+](https://adoptium.net/)
- [Maven 3.9+](https://maven.apache.org/download.cgi)

> Gatling is downloaded automatically via Maven — no separate installation required.

## Running Simulations

### Via Maven

```bash
# Smoke — 1 VU, single pass through all endpoints
mvn gatling:test -Dgatling.simulationClass=simulations.SmokeSimulation

# Load — 20 VUs, 5 min sustained
mvn gatling:test -Dgatling.simulationClass=simulations.LoadSimulation

# Stress — 100 VUs, 10 min sustained
mvn gatling:test -Dgatling.simulationClass=simulations.StressSimulation

# Spike — sudden 150 VU burst then recover
mvn gatling:test -Dgatling.simulationClass=simulations.SpikeSimulation

# Override VU count and duration at runtime
mvn gatling:test \
    -Dgatling.simulationClass=simulations.LoadSimulation \
    -Dload.users=50 \
    -Dload.rampSeconds=60 \
    -Dload.durationSeconds=600
```

### Via scripts

```bash
chmod +x scripts/*.sh

bash scripts/run-smoke.sh
bash scripts/run-load.sh
bash scripts/run-stress.sh
bash scripts/run-spike.sh

# Override via environment variables
LOAD_USERS=50 LOAD_RAMP=60 LOAD_DURATION=600 bash scripts/run-load.sh
```

## Simulations

| Simulation | VUs | Injection Profile | Duration | p95 SLO |
|------------|-----|-------------------|----------|---------|
| `SmokeSimulation` | 1 | At once | 1 loop | 2 000 ms |
| `LoadSimulation` | 20 | Ramp over 30 s | 5 min | 2 000 ms |
| `StressSimulation` | 100 | Two-stage ramp over 60 s | 10 min | 5 000 ms |
| `SpikeSimulation` | 150 peak | Instant burst + cool-down | 4 min | 5 000 ms |

## Assertions

Each simulation enforces threshold assertions — the build fails if any are breached:

| Simulation | Success Rate | p95 | p99 | Mean |
|------------|-------------|-----|-----|------|
| Smoke | ≥ 100% | ≤ 2 000 ms | — | — |
| Load | ≥ 99% | ≤ 2 000 ms | ≤ 3 000 ms | — |
| Stress | ≥ 95% | ≤ 5 000 ms | — | ≤ 2 000 ms |
| Spike | ≥ 90% | ≤ 5 000 ms | — | — |

## API Endpoints Under Test

| Request | Method | Endpoint | Expected |
|---------|--------|----------|----------|
| List Users | GET | `/api/users?page={1|2}` | 200, `$.data` array |
| Get User | GET | `/api/users/{id}` | 200, `$.data.id` |
| Create User | POST | `/api/users` | 201, `$.id` present |
| Update User | PUT | `/api/users/{id}` | 200, `$.updatedAt` present |
| Login | POST | `/api/login` | 200, `$.token` present |

## Reports

After a simulation run, the HTML report is at:

```
target/gatling/<SimulationName>-<timestamp>/index.html
```

Open `index.html` in a browser for response time graphs, throughput charts, percentile breakdowns, and per-request statistics.

## CI/CD

GitHub Actions workflow (`.github/workflows/ci.yml`):

1. **smoke-tests** — runs `SmokeSimulation` on every push; fails CI on assertion breach
2. **load-tests** — runs after smoke passes with reduced VU count (5 VUs, 60 s) to keep CI fast
3. Both jobs upload the Gatling HTML report as an artifact for review
