#!/usr/bin/env bash
set -euo pipefail

USERS="${STRESS_USERS:-100}"
RAMP="${STRESS_RAMP:-60}"
DURATION="${STRESS_DURATION:-600}"

echo "Running Gatling stress simulation (users=${USERS}, ramp=${RAMP}s, duration=${DURATION}s)..."
mvn gatling:test \
    -Dgatling.simulationClass=simulations.StressSimulation \
    -Dstress.users="${USERS}" \
    -Dstress.rampSeconds="${RAMP}" \
    -Dstress.durationSeconds="${DURATION}"
echo "Stress simulation complete. Results: target/gatling/"
