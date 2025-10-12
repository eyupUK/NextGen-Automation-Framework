#!/bin/bash

###############################################################################
# Quick Performance Test Runner
# Simplified version for direct execution
###############################################################################

echo "🚀 Starting Performance Test..."
echo ""

# Prepare optional API key flag (use array for safe quoting)
mvn_args=()
if [[ -n "$WEATHER_API_KEY" ]]; then
  mvn_args+=( -DWEATHER_API_KEY="$WEATHER_API_KEY" )
else
  echo "ℹ️ WEATHER_API_KEY env var not set. Tests will fallback to configuration.properties if configured."
fi

# Run the test
echo "Running Weather API Performance Test with 5 users for 30 seconds..."
mvn "${mvn_args[@]}" gatling:test \
  -Dgatling.simulationClass=com.example.performance.simulations.WeatherApiPerformanceSimulation \
  -Dperf.users=5 \
  -Dperf.rampup=5 \
  -Dperf.duration=30 \
  -Dperf.type=load

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Performance test completed successfully!"
    echo ""
    echo "📊 Opening HTML report..."
    LATEST_REPORT=$(find target/gatling-results -name "index.html" -type f | sort | tail -1)
    if [ -n "$LATEST_REPORT" ]; then
        open "$LATEST_REPORT"
        echo "Report opened: $LATEST_REPORT"
    else
        echo "No report found. Check target/gatling-results/"
    fi
else
    echo ""
    echo "❌ Performance test failed. Check the output above for errors."
    exit 1
fi
