#!/usr/bin/env bash
set -euo pipefail

USERS="${LOAD_USERS:-20}"
RAMP="${LOAD_RAMP:-30}"
DURATION="${LOAD_DURATION:-300}"

echo "Running Gatling load simulation (users=${USERS}, ramp=${RAMP}s, duration=${DURATION}s)..."
mvn gatling:test \
    -Dgatling.simulationClass=simulations.LoadSimulation \
    -Dload.users="${USERS}" \
    -Dload.rampSeconds="${RAMP}" \
    -Dload.durationSeconds="${DURATION}"
echo "Load simulation complete. Results: target/gatling/"
