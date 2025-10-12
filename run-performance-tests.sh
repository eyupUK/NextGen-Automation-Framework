#!/bin/bash

###############################################################################
# Performance Testing Runner Script
#
# This script provides easy commands to run different types of performance tests
###############################################################################

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Default values
USERS=20
RAMPUP=10
DURATION=60
TEST_TYPE="load"

# Optional API key flag for Maven (use array for safe quoting)
mvn_args=()
if [[ -n "$WEATHER_API_KEY" ]]; then
  mvn_args+=( -DWEATHER_API_KEY="$WEATHER_API_KEY" )
else
  echo -e "${YELLOW}ℹ️ WEATHER_API_KEY not set in environment. Tests will fallback to configuration.properties if configured.${NC}"
fi

# Print colored message
print_message() {
    color=$1
    message=$2
    echo -e "${color}${message}${NC}"
}

# Open the latest Gatling report (helper)
open_latest_report() {
    # Give filesystem a brief moment to flush
    sleep 1
    local latest
    latest=$(find target/gatling-results -name "index.html" -type f -print0 2>/dev/null | xargs -0 ls -t 2>/dev/null | head -n 1)

    if [[ -z "$latest" ]]; then
        print_message "$RED" "No Gatling reports found. Run a test first."
        return 1
    fi

    print_message "$GREEN" "Opening: $latest"

    if command -v open >/dev/null 2>&1; then
        open "$latest"
    elif command -v xdg-open >/dev/null 2>&1; then
        xdg-open "$latest"
    else
        print_message "$YELLOW" "Please open manually: $latest"
    fi
}

# Display usage
usage() {
    cat << EOF
Usage: $0 [OPTIONS] COMMAND

Performance Testing Runner

COMMANDS:
    weather-api       Run Weather API performance test (Gatling)
    ecommerce-api     Run E-commerce API performance test (Gatling)
    junit-perf        Run JUnit-based performance tests
    all-gatling       Run all Gatling simulations
    report            Open last Gatling report

OPTIONS:
    -u, --users NUM       Number of concurrent users (default: 20)
    -r, --rampup SEC      Ramp-up time in seconds (default: 10)
    -d, --duration SEC    Test duration in seconds (default: 60)
    -t, --type TYPE       Test type: load|stress|spike (default: load)
    -h, --help            Show this help message

EXAMPLES:
    # Run basic load test with 10 users
    $0 weather-api

    # Run stress test with 50 users
    $0 -u 50 -t stress weather-api

    # Run spike test
    $0 -u 100 -t spike weather-api

    # Run JUnit performance tests
    $0 junit-perf

    # Open latest report
    $0 report
EOF
}

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        -u|--users)
            USERS="$2"
            shift 2
            ;;
        -r|--rampup)
            RAMPUP="$2"
            shift 2
            ;;
        -d|--duration)
            DURATION="$2"
            shift 2
            ;;
        -t|--type)
            TEST_TYPE="$2"
            shift 2
            ;;
        -h|--help)
            usage
            exit 0
            ;;
        weather-api|ecommerce-api|junit-perf|all-gatling|report)
            COMMAND="$1"
            shift
            ;;
        *)
            print_message "$RED" "Unknown option: $1"
            usage
            exit 1
            ;;
    esac
done

# If no command provided, default to weather-api for convenience
if [ -z "$COMMAND" ]; then
    print_message "$YELLOW" "No command specified. Defaulting to 'weather-api'. Use -h for help."
    print_message "$BLUE" "COMMANDS:
                               weather-api       Run Weather API performance test (Gatling)
                               ecommerce-api     Run E-commerce API performance test (Gatling)
                               junit-perf        Run JUnit-based performance tests
                               all-gatling       Run all Gatling simulations
                               report            Open last Gatling report"
    COMMAND="weather-api"
fi

# Execute command
case $COMMAND in
    weather-api)
        print_message "$BLUE" "========================================="
        print_message "$BLUE" "Running Weather API Performance Test"
        print_message "$BLUE" "========================================="
        print_message "$YELLOW" "Configuration:"
        echo "  Users: $USERS"
        echo "  Ramp-up: ${RAMPUP}s"
        echo "  Duration: ${DURATION}s"
        echo "  Test Type: $TEST_TYPE"
        print_message "$BLUE" "========================================="

        mvn "${mvn_args[@]}" gatling:test \
            -Dgatling.simulationClass=com.example.performance.gatling.simulations.WeatherApiPerformanceSimulation \
            -Dperf.users="$USERS" \
            -Dperf.rampup="$RAMPUP" \
            -Dperf.duration="$DURATION" \
            -Dperf.type="$TEST_TYPE"

        print_message "$GREEN" "\n✔ Performance test completed!"
        print_message "$YELLOW" "Report location: target/gatling-results/"
        open_latest_report || true
        ;;

    ecommerce-api)
        print_message "$BLUE" "========================================="
        print_message "$BLUE" "Running E-commerce API Performance Test"
        print_message "$BLUE" "========================================="
        print_message "$YELLOW" "Configuration:"
        echo "  Users: $USERS"
        echo "  Ramp-up: ${RAMPUP}s"
        echo "  Duration: ${DURATION}s"
        echo "  Test Type: $TEST_TYPE"
        print_message "$BLUE" "========================================="

        mvn gatling:test \
            -Dgatling.simulationClass=com.example.performance.gatling.simulations.EcommerceApiPerformanceSimulation \
            -Dperf.users="$USERS" \
            -Dperf.rampup="$RAMPUP" \
            -Dperf.duration="$DURATION" \
            -Dperf.type="$TEST_TYPE"

        print_message "$GREEN" "\n✔ Performance test completed!"
        open_latest_report || true
        ;;

    junit-perf)
        print_message "$BLUE" "========================================="
        print_message "$BLUE" "Running JUnit Performance Tests"
        print_message "$BLUE" "========================================="

        mvn "${mvn_args[@]}" test -Dtest=WeatherApiPerformanceTest \
            -Dperf.users="$USERS" \
            -Dperf.duration="$DURATION"

        print_message "$GREEN" "\n✔ JUnit performance tests completed!"
        print_message "$YELLOW" "Results: target/performance-results/"
        ;;

    all-gatling)
        print_message "$BLUE" "========================================="
        print_message "$BLUE" "Running All Gatling Simulations"
        print_message "$BLUE" "========================================="

        mvn gatling:test -Dperf.users="$USERS" -Dperf.rampup="$RAMPUP" -Dperf.duration="$DURATION" -Dperf.type="$TEST_TYPE"

        print_message "$GREEN" "\n✔ All simulations completed!"
        open_latest_report || true
        ;;

    report)
        print_message "$BLUE" "Opening latest Gatling report..."
        open_latest_report
        ;;
 esac

print_message "$GREEN" "\nDone! 🚀"
