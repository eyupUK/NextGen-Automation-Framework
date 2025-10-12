#!/bin/bash

###############################################################################
# Quick Performance Test Runner
# Simplified version for direct execution
###############################################################################


set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Performance test parameters
USERS=10
RAMPUP=5
DURATION=20
TEST_TYPE="load"

# Print colored message
print_message() {
    color=$1
    message=$2
    echo -e "${color}${message}${NC}"
}

echo ""
print_message "$YELLOW" "🚀 Starting Performance Test..."
echo ""

# Prepare optional API key flag (use array for safe quoting)
mvn_args=()
if [[ -n "$WEATHER_API_KEY" ]]; then
  mvn_args+=( -DWEATHER_API_KEY="$WEATHER_API_KEY" -Dgatling.simulationClass=com.example.performance.gatling.simulations.WeatherApiPerformanceSimulation )
else
#  echo -e "${YELLOW}ℹ️ WEATHER_API_KEY env var not set. Tests will fallback to configuration.properties if configured.${NC}"
  print_message "$YELLOW" "ℹ️ WEATHER_API_KEY env var not set. Tests will fallback to configuration.properties if configured."
fi

# Run the test
echo -e "${BLUE}Running Weather API ${RED}${TEST_TYPE} ${BLUE}Test with ${RED}${USERS} users${NC} ${BLUE}for ${RED}${DURATION} seconds${NC}..."
#print_message "$BLUE" "Running Weather API Performance Test with 5 users for 30 seconds..."
echo ""

mvn "${mvn_args[@]}" gatling:test \
  -Dperf.users="$USERS" \
  -Dperf.rampup="$RAMPUP" \
  -Dperf.duration="$DURATION"\
  -Dperf.type="$TEST_TYPE"

if [ $? -eq 0 ]; then
    echo ""
    print_message "$GREEN" "✅ Performance test completed successfully!"
    echo ""
    print_message "$BLUE" "📊 Opening HTML report..."

    LATEST_REPORT=$(find target/gatling-results -name "index.html" -type f | sort | tail -1)
    if [ -n "$LATEST_REPORT" ]; then
        open "$LATEST_REPORT"
        echo "Report opened: $LATEST_REPORT"
    else
        print_message "$RED" "❌ Performance test failed. Check the output above for errors."

    fi
else
    echo ""
    print_message "$RED" "❌ Performance test failed. Check the output above for errors."
    exit 1
fi

print_message "$GREEN" "\nDone! 🚀"
