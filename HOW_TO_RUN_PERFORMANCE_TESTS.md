# 🚀 Running Performance Tests Locally - Quick Guide

## ⚡ Quick Start (Easiest Way)

I've created a simplified script for you. Just run:

```bash
cd /Users/mac/IdeaProjects/qa-assessment-Eyup-Tozcu

# Set the API key first
export WEATHER_API_KEY=31ea33c30d254920977133231250909

# Run the quick test
./quick-perf-test.sh
```

This will:
- ✅ Set the API key automatically
- ✅ Run a quick 30-second test with 5 users
- ✅ Open the HTML report when done

---

## 🔧 Alternative: Direct Maven Commands

If the script doesn't work, run these commands directly:

### 1. Set the API Key (REQUIRED - Must be done first!)
```bash
export WEATHER_API_KEY=31ea33c30d254920977133231250909
```

**⚠️ Important:** The API key MUST be set as an environment variable BEFORE running the test. The performance tests require it to be available at runtime.

### 2. Run the Performance Test
```bash
cd /Users/mac/IdeaProjects/qa-assessment-Eyup-Tozcu

mvn gatling:test \
  -Dgatling.simulationClass=com.example.performance.simulations.WeatherApiPerformanceSimulation \
  -Dperf.users=5 \
  -Dperf.rampup=5 \
  -Dperf.duration=30 \
  -Dperf.type=load
```

### 3. View the Report
```bash
open target/gatling-results/*/index.html
```

---

## 🐛 Common Issues & Solutions

### Issue 1: "WEATHER_API_KEY is not set"
**Error Message:** `IllegalStateException: WEATHER_API_KEY is not set. Please set it as an environment variable or system property.`

**Solution:**
```bash
# Set it in your current terminal session (MUST DO THIS FIRST!)
export WEATHER_API_KEY=31ea33c30d254920977133231250909

# Verify it's set
echo $WEATHER_API_KEY

# Then run the test
mvn gatling:test -Dgatling.simulationClass=com.example.performance.simulations.WeatherApiPerformanceSimulation
```

**Permanent Setup (recommended):**
```bash
# Add to your ~/.zshrc for permanent setup
echo 'export WEATHER_API_KEY=31ea33c30d254920977133231250909' >> ~/.zshrc
source ~/.zshrc

# Verify
echo $WEATHER_API_KEY
```

### Issue 2: "Permission denied"
**Solution:**
```bash
chmod +x quick-perf-test.sh
chmod +x run-performance-tests.sh
```

### Issue 3: "No tests found" or "Cannot find symbol"
**Solution:**
```bash
# Make sure dependencies are installed
mvn clean install -DskipTests
```

### Issue 4: Maven not found
**Solution:**
```bash
# Check if Maven is installed
mvn --version

# If not installed, install it:
brew install maven
```

### Issue 5: NullPointerException during test initialization
**This has been fixed!** The issue was that the API key wasn't available during Gatling class initialization. The fix uses `PerformanceConfig.getWeatherApiKey()` which evaluates at runtime.

**If you still see this:**
1. Make sure you've pulled the latest code
2. Clean and recompile: `mvn clean compile test-compile`
3. Ensure WEATHER_API_KEY is exported BEFORE running Maven

---

## 📊 Different Test Configurations

**Remember:** Always set `export WEATHER_API_KEY=31ea33c30d254920977133231250909` first!

### Quick Test (30 seconds, 5 users)
```bash
export WEATHER_API_KEY=31ea33c30d254920977133231250909
mvn gatling:test \
  -Dgatling.simulationClass=com.example.performance.simulations.WeatherApiPerformanceSimulation \
  -Dperf.users=5 \
  -Dperf.duration=30
```

### Load Test (1 minute, 10 users)
```bash
export WEATHER_API_KEY=31ea33c30d254920977133231250909
mvn gatling:test \
  -Dgatling.simulationClass=com.example.performance.simulations.WeatherApiPerformanceSimulation \
  -Dperf.users=10 \
  -Dperf.duration=60 \
  -Dperf.type=load
```

### Stress Test (2 minutes, 50 users)
```bash
export WEATHER_API_KEY=31ea33c30d254920977133231250909
mvn gatling:test \
  -Dgatling.simulationClass=com.example.performance.simulations.WeatherApiPerformanceSimulation \
  -Dperf.users=50 \
  -Dperf.duration=120 \
  -Dperf.type=stress
```

### Spike Test (sudden burst of 100 users)
```bash
export WEATHER_API_KEY=31ea33c30d254920977133231250909
mvn gatling:test \
  -Dgatling.simulationClass=com.example.performance.simulations.WeatherApiPerformanceSimulation \
  -Dperf.users=100 \
  -Dperf.type=spike
```

### E-commerce API Test
```bash
# E-commerce API doesn't require WEATHER_API_KEY
mvn gatling:test \
  -Dgatling.simulationClass=com.example.performance.simulations.EcommerceApiPerformanceSimulation \
  -Dperf.users=10
```

---

## 🎯 Using the Full Script

If you want to use the full `run-performance-tests.sh` script:

```bash
# Set API key first!
export WEATHER_API_KEY=31ea33c30d254920977133231250909

# Make sure it's executable
chmod +x run-performance-tests.sh

# View help
./run-performance-tests.sh --help

# Run tests
./run-performance-tests.sh weather-api
./run-performance-tests.sh -u 20 -t stress weather-api
./run-performance-tests.sh -u 30 -r 20 -d 120 weather-api
```

---

## ✅ Verification Steps

### 1. Check if dependencies are installed
```bash
mvn dependency:tree | grep gatling
```

### 2. Check if API key is set (CRITICAL!)
```bash
echo $WEATHER_API_KEY
# Should output: 31ea33c30d254920977133231250909
```

### 3. Check if Maven can compile
```bash
mvn clean test-compile -DskipTests
```

### 4. Run a simple test
```bash
export WEATHER_API_KEY=31ea33c30d254920977133231250909
./quick-perf-test.sh
```

---

## 📈 What Happens During the Test

1. **Environment variable check** - Tests verify WEATHER_API_KEY is set
2. **Maven downloads dependencies** (first time only)
3. **Code compilation** - Tests compile successfully
4. **Gatling starts** and creates virtual users
5. **Users make API calls** to Weather API with your key
6. **Metrics are collected** (response times, throughput, errors)
7. **HTML report is generated** in `target/gatling-results/`
8. **Report opens automatically** in your browser

---

## 📊 Understanding the Results

After the test completes, you'll see output like:
```
================================================================================
---- Global Information --------------------------------------------------------
> request count                                         50 (OK=50     KO=0     )
> min response time                                     18 (OK=18     KO=-     )
> max response time                                    105 (OK=105    KO=-     )
> mean response time                                    38 (OK=38     KO=-     )
> response time 95th percentile                         83 (OK=83     KO=-     )
> response time 99th percentile                        101 (OK=101    KO=-     )
================================================================================
```

**Key Metrics:**
- ✅ **OK**: Successful requests
- ❌ **KO**: Failed requests (should be 0)
- **Mean**: Average response time (lower is better)
- **P95**: 95% of requests were faster than this
- **P99**: 99% of requests were faster than this

---

## 🎉 Success Indicators

You'll know the test succeeded when:
- ✅ No "WEATHER_API_KEY is not set" error
- ✅ Maven completes with `BUILD SUCCESS`
- ✅ All requests show `OK` (no failures)
- ✅ HTML report is generated
- ✅ Report opens in your browser automatically

---

## 📞 Quick Troubleshooting

**If the test fails:**
1. ⚠️ **FIRST**: Check API key is set: `echo $WEATHER_API_KEY`
2. Check Maven is installed: `mvn --version`
3. Check dependencies: `mvn clean install -DskipTests`
4. Try the simple script: `./quick-perf-test.sh`
5. Try direct Maven command (see section above)

**If you see compilation errors:**
```bash
mvn clean compile test-compile -DskipTests
```

**If report doesn't open:**
```bash
# Find and open manually
find target/gatling-results -name "index.html" -type f -exec open {} \;
```

---

## 🚀 Next Steps After Successful Test

1. ✅ View the HTML report (opens automatically)
2. 📊 Review the metrics and charts
3. 🔄 Run more tests with different configurations
4. 📈 Track performance over time
5. 🎯 Customize simulations for your needs

---

## 🔑 Important Notes

### API Key Requirements:
- **Local Development**: MUST set `export WEATHER_API_KEY=...` before running tests
- **GitHub Actions**: Automatically provided from repository secrets
- **Configuration**: Uses `PerformanceConfig.getWeatherApiKey()` which validates at runtime

### How It Works:
1. `PerformanceConfig.getWeatherApiKey()` checks for the key in this order:
   - Environment variable `WEATHER_API_KEY`
   - System property `-DWEATHER_API_KEY=...`
   - Configuration file (fallback)
2. If not found, throws `IllegalStateException` with clear error message
3. Gatling simulation uses the retrieved key for all API calls

---

**Ready to test? Run this now:**
```bash
export WEATHER_API_KEY=31ea33c30d254920977133231250909
./quick-perf-test.sh
```

Good luck! 🎉
