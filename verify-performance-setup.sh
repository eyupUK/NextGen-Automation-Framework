#!/bin/bash

###############################################################################
# Performance Testing Verification Script
# Verifies that all performance testing components are properly set up
###############################################################################

echo "╔════════════════════════════════════════════════════════════════╗"
echo "║  Performance Testing Framework - Verification Script          ║"
echo "╚════════════════════════════════════════════════════════════════╝"
echo ""

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

ERRORS=0

# Check 1: Maven
echo -n "Checking Maven installation... "
if command -v mvn &> /dev/null; then
    echo -e "${GREEN}✓${NC}"
else
    echo -e "${RED}✗ Maven not found${NC}"
    ERRORS=$((ERRORS + 1))
fi

# Check 2: Java
echo -n "Checking Java installation... "
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | head -n 1)
    echo -e "${GREEN}✓${NC} ($JAVA_VERSION)"
else
    echo -e "${RED}✗ Java not found${NC}"
    ERRORS=$((ERRORS + 1))
fi

# Check 3: Performance test files
echo ""
echo "Checking performance test files:"

FILES=(
    "src/test/java/com/example/performance/config/PerformanceConfig.java"
    "src/test/java/com/example/performance/gatling/simulations/WeatherApiPerformanceSimulation.java"
    "src/test/java/com/example/performance/gatling/simulations/EcommerceApiPerformanceSimulation.java"
    "src/test/java/com/example/performance/tests/WeatherApiPerformanceTest.java"
    "src/test/java/com/example/performance/utils/PerformanceMetricsCollector.java"
    "src/test/java/com/example/performance/utils/LoadGenerator.java"
    "src/test/java/com/example/performance/examples/PerformanceTestingExamples.java"
    "docs/PERFORMANCE_TESTING.md"
    "PERFORMANCE_TESTING_QUICKSTART.md"
    "run-performance-tests.sh"
)

for file in "${FILES[@]}"; do
    echo -n "  $file ... "
    if [ -f "$file" ]; then
        echo -e "${GREEN}✓${NC}"
    else
        echo -e "${RED}✗ Missing${NC}"
        ERRORS=$((ERRORS + 1))
    fi
done

# Check 4: Configuration
echo ""
echo -n "Checking configuration.properties... "
if grep -q "perf.users" configuration-test.properties 2>/dev/null; then
    echo -e "${GREEN}✓${NC}"
else
    echo -e "${YELLOW}⚠ Performance config not found${NC}"
fi

# Check 5: Compile test
echo ""
echo "Verifying compilation..."
mvn test-compile -DskipTests -q
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ All classes compiled successfully${NC}"
else
    echo -e "${RED}✗ Compilation failed${NC}"
    ERRORS=$((ERRORS + 1))
fi

# Summary
echo ""
echo "═══════════════════════════════════════════════════════════════"
if [ $ERRORS -eq 0 ]; then
    echo -e "${GREEN}✓ All checks passed! Performance testing is ready to use.${NC}"
    echo ""
    echo "Next steps:"
    echo "  1. Run: ./run-performance-tests.sh weather-api"
    echo "  2. View report: ./run-performance-tests.sh report"
    echo "  3. Read guide: cat PERFORMANCE_TESTING_QUICKSTART.md"
else
    echo -e "${RED}✗ $ERRORS check(s) failed. Please fix the issues above.${NC}"
fi
echo "═══════════════════════════════════════════════════════════════"

