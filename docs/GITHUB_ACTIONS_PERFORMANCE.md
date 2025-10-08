# GitHub Actions - Performance Testing Workflows

## 📋 Overview

This project includes **4 comprehensive GitHub Actions workflows** for automated performance testing in CI/CD pipelines.

---

## 🔧 Available Workflows

### 1. **Performance Tests** (`performance-tests.yml`)
**Primary workflow for flexible performance testing**

**Triggers:**
- ✅ Manual trigger (workflow_dispatch) with customizable parameters
- ✅ Weekly schedule (Sundays at 2 AM UTC)
- 🔄 Optional: On push to main/master (commented out by default)

**Features:**
- Run Weather API or E-commerce API tests
- Choose test type: Load, Stress, or Spike
- Configure users, duration dynamically
- Automatic report generation and artifact upload
- PR comments with results
- Performance summary in workflow output

**How to Run Manually:**
1. Go to **Actions** tab in GitHub
2. Select **"Performance Tests"**
3. Click **"Run workflow"**
4. Configure parameters:
   - Test Type: `load`, `stress`, or `spike`
   - Users: Number of concurrent users (e.g., `10`)
   - Duration: Test duration in seconds (e.g., `60`)
   - Simulation: `weather`, `ecommerce`, or `all`
5. Click **"Run workflow"**

**Command Line (GitHub CLI):**
```bash
gh workflow run performance-tests.yml \
  -f test_type=load \
  -f users=20 \
  -f duration=120 \
  -f simulation=weather
```

---

### 2. **Nightly Performance Tests** (`nightly-performance.yml`)
**Automated comprehensive testing every night**

**Triggers:**
- 🌙 Daily at 2 AM UTC (automatic)
- ✅ Manual trigger with notification options

**Features:**
- Extended duration tests (5 minutes)
- Higher user loads (20 users for Weather API, 15 for E-commerce)
- Performance trend tracking (365 days retention)
- Automatic issue creation on failure
- Baseline comparison
- Long-term artifact storage

**What It Tests:**
- Weather API: 20 users, 5-minute load test
- E-commerce API: 15 users, standard test
- Extracts and stores performance trends

**Notifications:**
- Creates GitHub issue automatically if tests fail
- Labels: `performance`, `automated-test`, `needs-investigation`

**How to Run Manually:**
```bash
gh workflow run nightly-performance.yml \
  -f notify_on_failure=true
```

---

### 3. **PR Performance Check** (`pr-performance.yml`)
**Quick performance validation for pull requests**

**Triggers:**
- 🔀 Automatically on PR to main/master/develop
- Only when relevant files change (src/, pom.xml)

**Features:**
- Quick 30-second performance check
- 5 concurrent users (lightweight)
- Automatic PR comments with results
- Performance comparison with baseline
- No merge blocking (informational only)
- 14-day artifact retention

**What It Does:**
1. Runs quick load test on PR code
2. Extracts key metrics
3. Comments results on PR
4. Uploads reports for review
5. Compares with baseline performance

**PR Comment Example:**
```
🎯 Performance Test Results

Quick Performance Check Completed

Configuration:
- Users: 5
- Duration: 30 seconds
- Test Type: Load

Results:
✅ Performance test executed successfully

Next Steps:
1. Download the artifacts to view detailed reports
2. Compare metrics with baseline
3. Ensure no significant performance regression

Workflow Run: [View Details](...)
```

---

### 4. **Stress Test Suite** (`stress-test.yml`)
**Intensive stress and spike testing**

**Triggers:**
- ✅ Manual trigger only (resource-intensive)

**Features:**
- Configurable maximum users (default: 100)
- Extended duration (default: 3 minutes)
- Two test types: Stress Test + Spike Test
- Comprehensive analysis of breaking points
- 60-day artifact retention
- Optional Slack notifications

**Tests Included:**
1. **Stress Test** - Pushes system beyond normal capacity
2. **Spike Test** - Sudden traffic burst testing

**How to Run:**
```bash
gh workflow run stress-test.yml \
  -f max_users=100 \
  -f duration=180 \
  -f notify_slack=false
```

**Use Cases:**
- Capacity planning
- Finding system limits
- Testing auto-scaling
- Validating error handling under extreme load

---

## 📊 Artifacts & Reports

### Generated Artifacts

| Artifact | Contents | Retention | Workflow |
|----------|----------|-----------|----------|
| **gatling-reports** | Interactive HTML reports with charts | 30 days | All |
| **junit-performance-results** | JUnit test results & logs | 30 days | Main |
| **performance-metrics-csv** | CSV files for trend analysis | 90 days | Main |
| **nightly-performance-reports** | Comprehensive nightly results | 90 days | Nightly |
| **performance-trends** | Long-term trend data | 365 days | Nightly |
| **pr-performance-report** | Quick PR check results | 14 days | PR |
| **stress-test-reports** | Stress & spike test results | 60 days | Stress |

### How to Download Artifacts

**Via GitHub UI:**
1. Go to **Actions** tab
2. Click on a workflow run
3. Scroll to **Artifacts** section
4. Click to download

**Via GitHub CLI:**
```bash
# List artifacts
gh run view <run-id> --log

# Download specific artifact
gh run download <run-id> -n gatling-reports-123
```

---

## 🎯 Usage Examples

### Example 1: Quick Performance Check Before Release
```bash
# Run load test with 20 users for 2 minutes
gh workflow run performance-tests.yml \
  -f test_type=load \
  -f users=20 \
  -f duration=120 \
  -f simulation=all
```

### Example 2: Stress Test for Capacity Planning
```bash
# Test with 100 users for 5 minutes
gh workflow run stress-test.yml \
  -f max_users=100 \
  -f duration=300
```

### Example 3: Pre-Production Validation
```bash
# Run comprehensive tests on all APIs
gh workflow run performance-tests.yml \
  -f test_type=load \
  -f users=50 \
  -f duration=180 \
  -f simulation=all
```

### Example 4: Manual Nightly Test
```bash
# Trigger nightly test manually with notifications
gh workflow run nightly-performance.yml \
  -f notify_on_failure=true
```

---

## 🔔 Notifications & Alerts

### Automatic Notifications

**Issue Creation (Nightly Tests):**
- Created automatically on test failure
- Labels: `performance`, `automated-test`, `needs-investigation`
- Contains run details and action items

**PR Comments:**
- Posted automatically on PR performance checks
- Includes test configuration and results
- Links to detailed reports

### Manual Notifications

Add these secrets to enable additional notifications:

**Slack Integration:**
```yaml
# Add to repository secrets:
SLACK_WEBHOOK_URL=your-webhook-url
```

**Email Notifications:**
Configure in repository settings under Notifications

---

## 📈 Performance Trend Tracking

### Viewing Trends

1. **Download historical data:**
   ```bash
   gh run download --pattern "performance-trends-*"
   ```

2. **Analyze trends:**
   - Compare `stats.json` files over time
   - Track response time changes
   - Monitor error rate trends
   - Identify performance regressions

3. **Create dashboards:**
   - Import CSV data into tools like Grafana
   - Use Excel/Google Sheets for visualization
   - Build custom tracking dashboards

---

## ⚙️ Configuration

### Customizing Workflows

#### Adjust Schedule
Edit the cron expression:
```yaml
schedule:
  - cron: '0 2 * * *'  # Daily at 2 AM UTC
  - cron: '0 2 * * 0'  # Weekly on Sundays
```

#### Change Retention Days
```yaml
retention-days: 30  # Adjust as needed (1-90 days)
```

#### Add Branches
```yaml
branches:
  - main
  - master
  - develop
  - staging  # Add custom branches
```

#### Modify User Counts
```yaml
-Dperf.users=20  # Adjust concurrent users
-Dperf.duration=120  # Adjust duration (seconds)
```

---

## 🚀 Best Practices

### 1. **Start Small**
- Begin with manual workflow runs
- Use lower user counts initially
- Gradually increase load

### 2. **Schedule Wisely**
- Run nightly tests during off-peak hours
- Avoid overlapping with other intensive jobs
- Consider API rate limits

### 3. **Monitor Costs**
- GitHub Actions has usage limits
- Self-hosted runners for intensive tests
- Archive old artifacts regularly

### 4. **Set Baselines**
- Run tests consistently for comparison
- Document expected performance
- Alert on significant deviations

### 5. **Review Regularly**
- Download and analyze reports weekly
- Track performance trends
- Act on degradation early

---

## 🛠️ Troubleshooting

### Common Issues

**Issue: Workflow fails to start**
- Check if workflows are enabled in repo settings
- Verify Java 21 compatibility
- Ensure all required files exist

**Issue: Tests timeout**
- Increase `timeout-minutes` in workflow
- Reduce user count or duration
- Check API availability

**Issue: Artifacts not uploaded**
- Verify artifact path exists
- Check file size limits (max 2GB)
- Ensure workflow has upload permissions

**Issue: PR comments not posted**
- Check repository permissions
- Verify GitHub token has write access
- Review workflow logs

---

## 📚 Additional Resources

### Documentation Files
- `PERFORMANCE_TESTING_QUICKSTART.md` - Getting started
- `docs/PERFORMANCE_TESTING.md` - Comprehensive guide
- `DEMO_RESULTS.md` - Example test results

### GitHub Actions Documentation
- [Workflow syntax](https://docs.github.com/en/actions/reference/workflow-syntax-for-github-actions)
- [GitHub CLI](https://cli.github.com/)
- [Artifacts](https://docs.github.com/en/actions/guides/storing-workflow-data-as-artifacts)

### Gatling Documentation
- [Gatling Official Docs](https://gatling.io/docs/)
- [Maven Plugin](https://gatling.io/docs/gatling/reference/current/extensions/maven_plugin/)

---

## 📋 Workflow Summary

| Workflow | Trigger | Duration | Users | Frequency | Purpose |
|----------|---------|----------|-------|-----------|---------|
| **Performance Tests** | Manual/Weekly | Configurable | 5-50+ | On-demand | Flexible testing |
| **Nightly Performance** | Scheduled | 5 min | 15-20 | Daily | Baseline tracking |
| **PR Performance** | Automatic | 30 sec | 5 | Per PR | Quick validation |
| **Stress Test** | Manual | 3+ min | 50-100+ | On-demand | Capacity testing |

---

## ✅ Quick Start Checklist

- [ ] Review all workflow files in `.github/workflows/`
- [ ] Understand artifact retention policies
- [ ] Configure notification preferences
- [ ] Run first manual test: `gh workflow run performance-tests.yml`
- [ ] Download and review generated reports
- [ ] Set up baseline metrics
- [ ] Schedule regular reviews of nightly results
- [ ] Document performance SLAs
- [ ] Configure issue labels if needed
- [ ] Train team on interpreting results

---

**🎉 GitHub Actions Performance Testing Workflows Ready!**

All workflows are configured and ready to use. Start with a manual run of the main performance test workflow to see it in action!

