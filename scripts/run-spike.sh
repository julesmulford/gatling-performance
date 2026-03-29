#!/usr/bin/env bash
set -euo pipefail

echo "Running Gatling spike simulation..."
mvn gatling:test -Dgatling.simulationClass=simulations.SpikeSimulation
echo "Spike simulation complete. Results: target/gatling/"
