# Troubleshooting Performance Tests

This document provides guidance on troubleshooting common performance test failures.

## Common Issues

### 1. HTTP Status Code Mismatches

**Symptom:**
```
> status.find.is(200), but actually found 201
```

**Root Cause:**
The test expects HTTP 200 (OK) but the API correctly returns HTTP 201 (Created) for POST requests that create resources.

**Solution:**
Update the status check in the Gatling simulation to match the actual HTTP status code returned by the API:

```java
// Before (incorrect)
.check(status().is(200))

// After (correct for POST requests that create resources)
.check(status().is(201))
```

**Example:**
See commit history for `EcommerceApiPerformanceSimulation.java` where we fixed this issue for:
- `/auth/login` endpoint
- `/carts` endpoint

### 2. Network/API Availability Issues

**Symptom:**
```
> j.n.UnknownHostException: fakestoreapi.com
> All requests fail with network errors
```

**Root Cause:**
The external API is unavailable or network connectivity issues exist.

**Solution:**
1. Check API status (e.g., https://fakestoreapi.com)
2. Verify network connectivity
3. Check for DNS resolution issues
4. Consider implementing retry logic or circuit breakers

### 3. Performance Threshold Violations

**Symptom:**
```
Global: percentage of successful events is greater than or equal to 95.0 : false (actual : 86.11)
```

**Root Cause:**
The actual success rate falls below the configured threshold (95% in this case).

**Investigation Steps:**
1. Check error logs in the Gatling report
2. Identify which endpoints are failing
3. Analyze response times and error patterns
4. Review recent changes to the API or test configuration

**Common Causes:**
- HTTP status code mismatches (see #1 above)
- API rate limiting
- API performance degradation
- Test configuration issues (too many concurrent users)

### 4. Response Time Threshold Violations

**Symptom:**
```
Global: 95th percentile of response time is less than or equal to 2000.0 : false (actual : 2500)
```

**Root Cause:**
The API response times exceed the configured SLA thresholds.

**Investigation Steps:**
1. Download and analyze the Gatling HTML reports
2. Check response time distribution across percentiles (P50, P75, P95, P99)
3. Identify slow endpoints
4. Review API logs for performance issues
5. Check for resource constraints (CPU, memory, database connections)

**Solution Options:**
1. Optimize the API performance
2. Adjust SLA thresholds if they're unrealistic
3. Reduce load (fewer concurrent users)
4. Investigate infrastructure scaling needs

## Debugging Workflow Failures

### Step 1: Review the Workflow Logs

1. Navigate to the failed workflow run in GitHub Actions
2. Expand the failed step (e.g., "Run E-commerce API Load Test")
3. Look for error messages in the logs

### Step 2: Download Artifacts

1. Scroll to the bottom of the workflow run page
2. Download the performance reports artifact
3. Extract and open `index.html` in the Gatling results folder
4. Analyze the detailed metrics, charts, and error logs

### Step 3: Identify the Root Cause

Common patterns to look for:
- **Status code mismatches**: Look for `status.find.is(X), but actually found Y`
- **Network errors**: Look for `UnknownHostException`, `ConnectException`, timeouts
- **Performance degradation**: Compare P95/P99 response times with historical data
- **High error rates**: Check the error percentage in the summary

### Step 4: Apply the Fix

1. Update test assertions if the API behavior is correct
2. Fix API issues if the test expectations are correct
3. Adjust performance thresholds if needed
4. Update test configuration (users, duration, ramp-up)

## Best Practices

### 1. Use Correct HTTP Status Codes

| Method | Action | Expected Status |
|--------|--------|----------------|
| POST | Create resource | 201 Created |
| POST | Action/Operation | 200 OK or 202 Accepted |
| GET | Read resource | 200 OK |
| PUT | Update resource | 200 OK or 204 No Content |
| PATCH | Partial update | 200 OK or 204 No Content |
| DELETE | Delete resource | 204 No Content or 200 OK |

### 2. Set Realistic Thresholds

- Start with conservative thresholds based on baseline measurements
- Gradually tighten thresholds as performance improves
- Consider different thresholds for different test types (load vs. stress)

### 3. Monitor Trends

- Keep performance trend data for at least 90 days
- Compare current results with historical baselines
- Alert on significant deviations (e.g., >20% increase in P95 response time)

### 4. Document Known Issues

- Maintain a list of known intermittent failures
- Document expected failure scenarios
- Use appropriate `if` conditions in workflows to handle expected failures

## Related Documentation

- [GitHub Actions Performance Testing](GITHUB_ACTIONS_PERFORMANCE.md)
- [Performance Testing Guide](PERFORMANCE_TESTING.md)
- [GitHub Actions Setup Complete](GITHUB_ACTIONS_SETUP_COMPLETE.md)

## Support

For issues not covered in this guide:
1. Check the GitHub Issues for similar problems
2. Review recent commits for related changes
3. Contact the test automation team
4. Open a new issue with detailed logs and steps to reproduce
