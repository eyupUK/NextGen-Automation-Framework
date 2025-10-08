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
USERS=10
RAMPUP=10
DURATION=60
TEST_TYPE="load"

# Print colored message
print_message() {
    color=$1
    message=$2
    echo -e "${color}${message}${NC}"
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
    -u, --users NUM       Number of concurrent users (default: 10)
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

# Check if command is provided
if [ -z "$COMMAND" ]; then
    print_message "$RED" "Error: No command specified"
    usage
    exit 1
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

        mvn gatling:test \
            -Dgatling.simulationClass=com.example.performance.simulations.WeatherApiPerformanceSimulation \
            -Dperf.users=$USERS \
            -Dperf.rampup=$RAMPUP \
            -Dperf.duration=$DURATION \
            -Dperf.type=$TEST_TYPE

        print_message "$GREEN" "\n✓ Performance test completed!"
        print_message "$YELLOW" "Report location: target/gatling-results/"
        ;;

    ecommerce-api)
        print_message "$BLUE" "========================================="
        print_message "$BLUE" "Running E-commerce API Performance Test"
        print_message "$BLUE" "========================================="
        print_message "$YELLOW" "Configuration:"
        echo "  Users: $USERS"
        echo "  Ramp-up: ${RAMPUP}s"
        print_message "$BLUE" "========================================="

        mvn gatling:test \
            -Dgatling.simulationClass=com.example.performance.simulations.EcommerceApiPerformanceSimulation \
            -Dperf.users=$USERS \
            -Dperf.rampup=$RAMPUP

        print_message "$GREEN" "\n✓ Performance test completed!"
        ;;

    junit-perf)
        print_message "$BLUE" "========================================="
        print_message "$BLUE" "Running JUnit Performance Tests"
        print_message "$BLUE" "========================================="

        mvn test -Dtest=WeatherApiPerformanceTest \
            -Dperf.users=$USERS \
            -Dperf.duration=$DURATION

        print_message "$GREEN" "\n✓ JUnit performance tests completed!"
        print_message "$YELLOW" "Results: target/performance-results/"
        ;;

    all-gatling)
        print_message "$BLUE" "========================================="
        print_message "$BLUE" "Running All Gatling Simulations"
        print_message "$BLUE" "========================================="

        mvn gatling:test -Dperf.users=$USERS

        print_message "$GREEN" "\n✓ All simulations completed!"
        ;;

    report)
        print_message "$BLUE" "Opening latest Gatling report..."

        # Find the latest report
        LATEST_REPORT=$(find target/gatling-results -name "index.html" -type f -print0 | xargs -0 ls -t | head -n 1)

        if [ -z "$LATEST_REPORT" ]; then
            print_message "$RED" "No Gatling reports found. Run a test first."
            exit 1
        fi

        print_message "$GREEN" "Opening: $LATEST_REPORT"

        # Open report based on OS
        if [[ "$OSTYPE" == "darwin"* ]]; then
            open "$LATEST_REPORT"
        elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
            xdg-open "$LATEST_REPORT"
        else
            print_message "$YELLOW" "Please open manually: $LATEST_REPORT"
        fi
        ;;
esac

print_message "$GREEN" "\nDone! 🚀"

