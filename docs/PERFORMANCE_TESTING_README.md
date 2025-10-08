
# E-commerce API test
./run-performance-tests.sh ecommerce-api

# JUnit performance tests
mvn test -Dtest=WeatherApiPerformanceTest

# View examples
mvn exec:java -Dexec.mainClass="com.example.performance.examples.PerformanceTestingExamples"
```

### 🔍 Key Metrics

- **Response Time**: Mean, Median, P95, P99, Max
- **Throughput**: Requests per second
- **Error Rate**: Percentage of failed requests
- **Active Users**: Concurrent connections

### 💡 Use Cases

- **Pre-Release Testing**: Validate performance before deployments
- **Capacity Planning**: Determine system limits
- **SLA Validation**: Ensure performance targets are met
- **Regression Testing**: Detect performance degradations
- **Scalability Analysis**: Test system under various loads

---

For detailed documentation, see `PERFORMANCE_TESTING_QUICKSTART.md`
# 🚀 Performance Testing Module - README Addition

## Performance Testing Overview

This framework now includes a comprehensive **Performance Testing Module** implementing industry best practices for load, stress, spike, and endurance testing.

### 📊 What's Included

#### 🎯 Two Performance Testing Approaches

1. **Gatling Simulations** (Recommended for API load testing)
   - Professional HTML reports with interactive charts
   - Realistic user behavior modeling
   - Support for multiple load patterns
   
2. **JUnit + REST Assured** (For integration with existing tests)
   - Custom metrics collection
   - CSV reports for trend analysis
   - SLA validation

### 🏃 Quick Start

```bash
# 1. Install dependencies
mvn clean install -DskipTests

# 2. Run your first performance test
./run-performance-tests.sh weather-api

# 3. View the report
./run-performance-tests.sh report
```

### 📁 Performance Test Structure

```
src/test/java/com/example/performance/
├── config/PerformanceConfig.java          # Configuration & SLA thresholds
├── simulations/
│   ├── WeatherApiPerformanceSimulation.java      # Gatling load tests
│   └── EcommerceApiPerformanceSimulation.java    # E-commerce scenarios
├── tests/WeatherApiPerformanceTest.java   # JUnit performance tests
├── utils/
│   ├── PerformanceMetricsCollector.java   # Metrics tracking
│   └── LoadGenerator.java                 # Load generation
└── examples/PerformanceTestingExamples.java      # 5 example patterns
```

### 🎓 Test Types Available

| Type | Purpose | Command |
|------|---------|---------|
| **Load** | Expected traffic patterns | `./run-performance-tests.sh weather-api` |
| **Stress** | Find breaking points | `./run-performance-tests.sh -t stress weather-api` |
| **Spike** | Sudden traffic bursts | `./run-performance-tests.sh -t spike weather-api` |
| **Endurance** | Long-term stability | `mvn test -Dtest=WeatherApiPerformanceTest#testEndurance` |

### 🔧 Configuration Options

```bash
# Customize via command line
./run-performance-tests.sh -u 50 -r 30 -d 120 weather-api

# Or via configuration.properties
perf.users=50
perf.rampup=30
perf.duration=120
```

### 📈 Reports Generated

- **Gatling HTML Reports**: `target/gatling-results/*/index.html`
  - Response time percentiles (P50, P95, P99)
  - Throughput graphs
  - Active users over time
  - Success/failure rates

- **CSV Metrics**: `target/performance-results/*.csv`
  - Exportable data for tracking
  - Trend analysis over time

### 📚 Documentation

- **Quick Start**: `PERFORMANCE_TESTING_QUICKSTART.md`
- **Comprehensive Guide**: `docs/PERFORMANCE_TESTING.md`
- **Implementation Summary**: `PERFORMANCE_TESTING_SUMMARY.md`

### ✅ Best Practices Implemented

- ✅ Realistic user behavior with think time
- ✅ Comprehensive metrics (P50, P95, P99 percentiles)
- ✅ SLA validation and assertions
- ✅ Proper resource management
- ✅ Multiple load patterns
- ✅ CI/CD integration ready
- ✅ Professional reporting

### 🎯 Example Commands

```bash
# Basic load test (10 users)
./run-performance-tests.sh weather-api

# Stress test (50 users)
./run-performance-tests.sh -u 50 -t stress weather-api

# Extended test (30 users, 2 minutes)
./run-performance-tests.sh -u 30 -d 120 weather-api

