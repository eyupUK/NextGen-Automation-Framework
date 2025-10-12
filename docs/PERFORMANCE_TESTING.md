# Performance Testing Guide

## Overview

This framework includes comprehensive performance testing capabilities using industry best practices. The performance testing module supports multiple testing approaches and provides detailed metrics and reporting.

## Performance Testing Tools

### 1. Gatling (Primary Tool)
- **Purpose**: High-performance load testing with realistic user behavior simulation
- **Best For**: API load testing, stress testing, spike testing
- **Reports**: HTML reports with detailed metrics, charts, and percentiles

### 2. REST Assured + Dropwizard Metrics
- **Purpose**: Custom performance testing with detailed metrics
- **Best For**: Integration with existing test suite, custom scenarios
- **Reports**: CSV exports, console reports with SLA validation

## Test Types Implemented

### 1. Load Testing
**Purpose**: Verify system behavior under expected load
- Gradual ramp-up of concurrent users
- Realistic user behavior patterns
- Validates response times meet SLAs

### 2. Stress Testing
**Purpose**: Find system breaking points
- Heavy sustained load beyond normal capacity
- Identifies resource limits
- Tests error handling and recovery

### 3. Spike Testing
**Purpose**: Test system resilience to sudden traffic bursts
- Sudden increase in concurrent users
- Tests auto-scaling and rate limiting
- Validates system recovery

### 4. Endurance Testing
**Purpose**: Verify system stability over time
- Sustained load for extended periods
- Identifies memory leaks and resource degradation
- Tests long-term reliability

## Running Performance Tests

### Using Gatling

#### Run Weather API Performance Test
```bash
# Basic load test (default)
mvn gatling:test -Dgatling.simulationClass=com.example.performance.gatling.simulations.WeatherApiPerformanceSimulation

# Stress test
mvn gatling:test -Dgatling.simulationClass=com.example.performance.gatling.simulations.WeatherApiPerformanceSimulation -Dperf.type=stress

# Spike test
mvn gatling:test -Dgatling.simulationClass=com.example.performance.gatling.simulations.WeatherApiPerformanceSimulation -Dperf.type=spike

# Custom parameters
mvn gatling:test -Dgatling.simulationClass=com.example.performance.gatling.simulations.WeatherApiPerformanceSimulation \
  -Dperf.users=50 \
  -Dperf.rampup=30 \
  -Dperf.duration=120
```

#### Run E-commerce API Performance Test
```bash
mvn gatling:test -Dgatling.simulationClass=com.example.performance.gatling.simulations.EcommerceApiPerformanceSimulation
```

### Using JUnit Performance Tests

```bash
# Run all performance tests
mvn test -Dtest=WeatherApiPerformanceTest

# Run specific test
mvn test -Dtest=WeatherApiPerformanceTest#testCurrentWeatherEndpointUnderLoad

# With custom parameters
mvn test -Dtest=WeatherApiPerformanceTest -Dperf.users=20 -Dperf.duration=90
```

## Configuration

### Performance Parameters

Edit `PerformanceConfig.java` or override via system properties:

| Parameter | Default | Description | System Property |
|-----------|---------|-------------|-----------------|
| Users | 10 | Number of concurrent users | `-Dperf.users=20` |
| Ramp-up Time | 10s | Time to reach max users | `-Dperf.rampup=30` |
| Duration | 60s | Test duration | `-Dperf.duration=120` |
| Spike Users | 50 | Users for spike test | `-Dperf.spike=100` |

### SLA Thresholds

Default SLA thresholds in `PerformanceConfig.java`:

```java
RESPONSE_TIME_P95_THRESHOLD = 2000ms  // 95th percentile
RESPONSE_TIME_P99_THRESHOLD = 5000ms  // 99th percentile
SUCCESS_RATE_THRESHOLD = 99.0%        // Success rate
```

## Reports and Metrics

### Gatling Reports

After running tests, Gatling generates HTML reports in:
```
target/gatling-results/<timestamp>/
```

**Metrics Included:**
- Response time distribution
- Percentiles (min, p50, p75, p95, p99, max)
- Requests per second (throughput)
- Success/failure rates
- Active users over time
- Interactive charts and graphs

Open the report:
```bash
open target/gatling-results/*/index.html
```

### JUnit Performance Reports

CSV reports are generated in:
```
target/performance-results/weather-api-metrics.csv
```

**Metrics Included:**
- Total requests and errors
- Mean, median, P95, P99 response times
- Throughput (requests/second)
- Error rate percentage
- SLA validation results

### Console Reports

Real-time metrics printed during test execution:
```
================================================================================
PERFORMANCE TEST REPORT: Weather API Performance Test
================================================================================
Total Requests: 500
Total Errors: 2 (0.40%)
Mean Response Time: 245.67 ms
Median Response Time: 198.32 ms
95th Percentile: 521.45 ms
99th Percentile: 892.33 ms
Max Response Time: 1234.56 ms
Throughput: 8.33 req/sec
Active Requests: 0
================================================================================
```

## Best Practices Implemented

### 1. Realistic User Behavior
- Think time between requests (1-3 seconds)
- Varied request patterns
- Multiple scenario execution

### 2. Comprehensive Metrics
- Response time percentiles (P50, P95, P99)
- Throughput measurements
- Error rate tracking
- Active user monitoring

### 3. SLA Validation
- Automated assertion checks
- Performance threshold validation
- Success rate verification

### 4. Proper Resource Management
- Thread pool management
- Connection pooling
- Graceful shutdown

### 5. Detailed Reporting
- HTML reports with visualizations
- CSV exports for analysis
- Real-time console feedback

## Performance Test Scenarios

### Weather API Scenarios

1. **Current Weather Lookup** (Most common)
   - Multiple city queries
   - Response time: < 2000ms (P95)
   
2. **Forecast Retrieval** (Medium frequency)
   - 3-day forecasts
   - Response time: < 3000ms (P95)

### E-commerce API Scenarios

1. **Browse Products** (High frequency)
   - List all products
   - Category filtering
   - Product details

2. **Shopping Cart** (Medium frequency)
   - View cart
   - Add items
   - Update quantities

3. **User Authentication** (Low frequency)
   - Login
   - User profile retrieval

## Analyzing Results

### Key Metrics to Monitor

1. **Response Time Percentiles**
   - P50 (Median): Typical user experience
   - P95: Most users' experience
   - P99: Worst-case scenarios
   - Max: Absolute worst case

2. **Throughput**
   - Requests per second
   - System capacity indicator
   - Scalability measurement

3. **Error Rate**
   - Should be < 1% under normal load
   - < 5% acceptable under stress
   - > 10% indicates system issues

4. **Active Users**
   - Concurrent connections
   - Resource utilization indicator

### Performance Tuning Tips

1. **API Server**: Check CPU, memory, database connections
2. **Network**: Monitor latency and bandwidth
3. **Database**: Query optimization, connection pooling
4. **Caching**: Implement for frequently accessed data
5. **Rate Limiting**: Protect against overload

## Continuous Integration

### GitHub Actions Integration

Performance tests can run in CI/CD:

```yaml
- name: Run Performance Tests
  run: mvn gatling:test -Dgatling.simulationClass=com.example.performance.gatling.simulations.WeatherApiPerformanceSimulation

- name: Archive Performance Reports
  uses: actions/upload-artifact@v3
  with:
    name: gatling-reports
    path: target/gatling-results/
```

## Troubleshooting

### Common Issues

1. **High Error Rates**
   - Check API rate limits
   - Verify API keys are valid
   - Reduce concurrent users

2. **Slow Response Times**
   - Check network connectivity
   - Monitor server resources
   - Review database performance

3. **Test Failures**
   - Adjust SLA thresholds
   - Increase timeouts
   - Check test data validity

## Examples

### Example 1: Quick Load Test
```bash
mvn gatling:test -Dperf.users=10 -Dperf.duration=30
```

### Example 2: Extended Stress Test
```bash
mvn gatling:test -Dperf.type=stress -Dperf.users=100 -Dperf.duration=300
```

### Example 3: Spike Test with Monitoring
```bash
mvn test -Dtest=WeatherApiPerformanceTest#testSpikeLoad
```

## Additional Resources

- [Gatling Documentation](https://gatling.io/docs/gatling/)
- [REST Assured Performance Testing](https://rest-assured.io/)
- [Dropwizard Metrics](https://metrics.dropwizard.io/)
- Performance testing best practices: [Web Performance Testing Guide]

---

**Note**: Always run performance tests in non-production environments. Coordinate with your infrastructure team before running large-scale tests.

