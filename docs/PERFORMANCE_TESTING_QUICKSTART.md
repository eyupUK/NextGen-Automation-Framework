# Performance Testing Quick Start Guide

## 🚀 Quick Start

### Prerequisites
- Java 21
- Maven 3.x
- Internet connection (for API calls)

### Step 1: Download Dependencies
```bash
mvn clean install -DskipTests
```

### Step 2: Run Your First Performance Test

#### Option A: Using the Convenience Script (Recommended)
```bash
# Make script executable (first time only)
chmod +x run-performance-tests.sh

# Run a simple load test
./run-performance-tests.sh weather-api

# Run with custom parameters
./run-performance-tests.sh -u 20 -t stress weather-api
```

#### Option B: Using Maven Directly
```bash
# Run Gatling performance test
mvn gatling:test

# Run JUnit-based performance tests
mvn test -Dtest=WeatherApiPerformanceTest
```

### Step 3: View Results

#### Gatling Reports
```bash
# Open the latest HTML report
./run-performance-tests.sh report

# Or manually
open target/gatling-results/*/index.html
```

#### JUnit Performance Reports
```bash
# View CSV results
cat target/performance-results/weather-api-metrics.csv
```

## 📊 Available Test Scenarios

### 1. Weather API Performance Test (Gatling)
Tests the Weather API with realistic load patterns.

**Run Commands:**
```bash
# Load test (default): 10 users, 10s ramp-up
./run-performance-tests.sh weather-api

# Stress test: 50 users over 30 seconds
./run-performance-tests.sh -u 50 -t stress weather-api

# Spike test: Sudden burst of 100 users
./run-performance-tests.sh -u 100 -t spike weather-api

# Custom: 30 users, 20s ramp-up, 120s duration
./run-performance-tests.sh -u 30 -r 20 -d 120 weather-api
```

### 2. E-commerce API Performance Test (Gatling)
Tests the FakeStore API with mixed scenarios.

**Run Command:**
```bash
./run-performance-tests.sh ecommerce-api
```

### 3. JUnit Performance Tests
Detailed performance tests with custom metrics.

**Run Commands:**
```bash
# Run all performance tests
./run-performance-tests.sh junit-perf

# Run specific test
mvn test -Dtest=WeatherApiPerformanceTest#testCurrentWeatherEndpointUnderLoad

# Run endurance test
mvn test -Dtest=WeatherApiPerformanceTest#testEndurance
```

### 4. Performance Examples (Standalone)
Educational examples demonstrating various patterns.

**Run Command:**
```bash
mvn exec:java -Dexec.mainClass="com.example.performance.examples.PerformanceTestingExamples"
```

## 🎯 Understanding Results

### Key Metrics

#### Response Times
- **Mean**: Average response time
- **Median (P50)**: Middle value - typical user experience
- **P95**: 95% of requests faster than this
- **P99**: 99% of requests faster than this
- **Max**: Worst case response time

#### Throughput
- **Requests/sec**: Number of requests processed per second
- Higher is better

#### Error Rate
- **Percentage**: Failed requests / total requests × 100
- Should be < 1% for healthy systems

### Example Gatling Report
```
================================================================================
---- Global Information --------------------------------------------------------
> request count                                        500 (OK=498    KO=2     )
> min response time                                     45 (OK=45     KO=5000  )
> max response time                                   1234 (OK=1200   KO=5001  )
> mean response time                                   246 (OK=245    KO=5000  )
> std deviation                                        156 (OK=148    KO=1     )
> response time 50th percentile                        198 (OK=198    KO=5000  )
> response time 75th percentile                        342 (OK=340    KO=5000  )
> response time 95th percentile                        521 (OK=520    KO=5001  )
> response time 99th percentile                        892 (OK=890    KO=5001  )
> mean requests/sec                                   8.33 (OK=8.30   KO=0.03  )
---- Response Time Distribution ------------------------------------------------
> t < 800 ms                                           492 ( 98%)
> 800 ms < t < 1200 ms                                   6 (  1%)
> t > 1200 ms                                            0 (  0%)
> failed                                                 2 (  0%)
================================================================================
```

### What to Look For

✅ **Good Performance:**
- P95 < 2 seconds
- Error rate < 1%
- Throughput meets requirements

⚠️ **Warning Signs:**
- P95 > 3 seconds
- Error rate 1-5%
- High standard deviation

❌ **Poor Performance:**
- P95 > 5 seconds
- Error rate > 5%
- Many timeouts

## 🔧 Configuration

### Customize Performance Parameters

#### Via Configuration File
Edit `configuration.properties`:
```properties
perf.users=10
perf.rampup=10
perf.duration=60
perf.spike.users=50
perf.stress.users=100
```

#### Via Command Line
```bash
mvn gatling:test -Dperf.users=50 -Dperf.rampup=30
```

#### Via Script
```bash
./run-performance-tests.sh -u 50 -r 30 -d 120 weather-api
```

### Adjust SLA Thresholds
Edit `src/test/java/com/example/performance/config/PerformanceConfig.java`:
```java
public static final int RESPONSE_TIME_P95_THRESHOLD = 2000; // 2 seconds
public static final int RESPONSE_TIME_P99_THRESHOLD = 5000; // 5 seconds
public static final double SUCCESS_RATE_THRESHOLD = 99.0;   // 99%
```

## 📈 Test Types Explained

### Load Testing
**Purpose**: Verify normal performance under expected load
**When to use**: Before releases, regularly in CI/CD
```bash
./run-performance-tests.sh -u 20 weather-api
```

### Stress Testing
**Purpose**: Find system breaking point
**When to use**: Capacity planning, identifying limits
```bash
./run-performance-tests.sh -u 100 -t stress weather-api
```

### Spike Testing
**Purpose**: Test recovery from sudden traffic bursts
**When to use**: Black Friday preparation, viral event scenarios
```bash
./run-performance-tests.sh -u 200 -t spike weather-api
```

### Endurance Testing
**Purpose**: Verify stability over time
**When to use**: Checking for memory leaks, resource degradation
```bash
mvn test -Dtest=WeatherApiPerformanceTest#testEndurance
```

## 🐛 Troubleshooting

### Problem: High Error Rates

**Check:**
1. API rate limits
2. Valid API keys
3. Network connectivity

**Solution:**
```bash
# Reduce concurrent users
./run-performance-tests.sh -u 5 weather-api
```

### Problem: Tests Take Too Long

**Solution:**
```bash
# Reduce duration and users
./run-performance-tests.sh -u 5 -d 30 weather-api
```

### Problem: Dependencies Not Found

**Solution:**
```bash
# Download all dependencies
mvn clean install -DskipTests
```

### Problem: Can't Open Gatling Report

**Solution:**
```bash
# Find and open manually
find target/gatling-results -name "index.html" | head -1 | xargs open
```

## 📚 Next Steps

1. **Review Documentation**: Check `docs/PERFORMANCE_TESTING.md` for detailed guide
2. **Customize Tests**: Modify simulations in `src/test/java/com/example/performance/simulations/`
3. **Add Scenarios**: Create new test scenarios based on your APIs
4. **Integrate CI/CD**: Add performance tests to your pipeline
5. **Set Baselines**: Establish performance baselines for comparison

## 🤝 Integration Examples

### Run in CI/CD
```yaml
# GitHub Actions example
- name: Performance Tests
  run: |
    mvn gatling:test -Dperf.users=10 -Dperf.duration=60
    
- name: Archive Reports
  uses: actions/upload-artifact@v3
  with:
    name: performance-reports
    path: target/gatling-results/
```

### Compare Results
```bash
# Export to CSV for tracking over time
mvn test -Dtest=WeatherApiPerformanceTest
cat target/performance-results/weather-api-metrics.csv
```

## 💡 Pro Tips

1. **Start Small**: Begin with 5-10 users and scale up
2. **Monitor Systems**: Watch CPU, memory, network during tests
3. **Test Regularly**: Include in CI/CD for early detection
4. **Baseline First**: Establish baseline metrics before optimization
5. **Test Realistically**: Use production-like data and patterns
6. **Avoid Rate Limits**: Coordinate with API providers for testing

---

**Need Help?** Check the full documentation in `docs/PERFORMANCE_TESTING.md`

