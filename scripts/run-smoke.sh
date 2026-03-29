#!/usr/bin/env bash
set -euo pipefail

echo "Running Gatling smoke simulation..."
mvn gatling:test -Dgatling.simulationClass=simulations.SmokeSimulation
echo "Smoke simulation complete. Results: target/gatling/"
