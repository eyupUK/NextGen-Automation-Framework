### Step 1: Add Secret to GitHub Repository

1. **Go to your GitHub repository**
2. Click **Settings** tab
3. In the left sidebar, click **Secrets and variables** → **Actions**
4. Click **"New repository secret"**
5. Fill in:
   - **Name:** `WEATHER_API_KEY`
   - **Secret:** `your api key here`
6. Click **"Add secret"**

**Screenshot Guide:**
```
Settings → Secrets and variables → Actions → New repository secret
┌─────────────────────────────────────────┐
│ Name: WEATHER_API_KEY                   │
│ Secret: `your api key here` │
│                                         │
│ [ Add secret ]                          │
└─────────────────────────────────────────┘
```

---

## 🔧 How to Run Tests Locally

### Option 1: Set Environment Variable (Recommended)

**On macOS/Linux:**
```bash
# Set for current session
export WEATHER_API_KEY=`your api key here`

# Run tests
mvn test
./run-performance-tests.sh weather-api
```

**On macOS/Linux (permanent):**
Add to `~/.zshrc` or `~/.bash_profile`:
```bash
export WEATHER_API_KEY=`your api key here`
```
Then: `source ~/.zshrc`

**On Windows (PowerShell):**
```powershell
$env:WEATHER_API_KEY=`your api key here`
mvn test
```

**On Windows (Command Prompt):**
```cmd
set WEATHER_API_KEY=`your api key here`
mvn test
```

### Option 2: Use System Property
```bash
mvn test -DWEATHER_API_KEY=`your api key here`
```

### Option 3: Create Local Configuration (Not Recommended)
```bash
# Create a local file (already in .gitignore)
cp configuration-test.properties.template configuration-test.properties.local
# Add WEATHER_API_KEY=your_key_here to the file
```

---

## ✅ Verification

### Test Locally:
```bash
# Set the environment variable
export WEATHER_API_KEY=`your api key here`

# Run a quick test
mvn test -Dtest=WeatherApiSteps

# Or run performance test
./run-performance-tests.sh weather-api
```

### Test on GitHub:
1. Push your code to GitHub
2. Go to **Actions** tab
3. Run **"Performance Tests"** workflow
4. It will automatically use the secret from repository settings

---

## 🎯 How It Works

### Local Development:
```
ConfigurationReader.get("WEATHER_API_KEY")
  ↓
1. Checks environment variable WEATHER_API_KEY ← SET THIS
  ↓ (if not found)
2. Checks system property -DWEATHER_API_KEY=...
  ↓ (if not found)
3. Checks configuration.properties (now empty)
```

### GitHub Actions:
```
Workflow runs
  ↓
env:
  WEATHER_API_KEY: ${{ secrets.WEATHER_API_KEY }}
  ↓
Tests run with environment variable set automatically
  ↓
ConfigurationReader finds it immediately
```

---

## 🔒 Security Benefits

✅ **API key no longer in version control**
✅ **Safe to push to public GitHub**
✅ **Each developer uses their own key locally**
✅ **GitHub Actions uses repository secret**
✅ **No risk of accidental exposure**
✅ **Easy to rotate keys (just update secret)**

---

## 📝 What Stays in configuration.properties

All non-sensitive configuration remains:
- ✅ `browser=chrome`
- ✅ `sauceDemoUrl=https://www.saucedemo.com/`
- ✅ `weather_api_base_url=http://api.weatherapi.com/v1`
- ✅ `fakestore_api_base_url=https://fakestoreapi.com`
- ✅ `STANDARD_USER=standard_user`
- ✅ `STANDARD_PASSWORD=secret_sauce`
- ✅ All performance test parameters

**Only removed:** `WEATHER_API_KEY` (moved to environment variable)

---

## 🔐 Securing OAuth Client Credentials

OAuth client credentials must never be committed. Use environment variables or CI secrets.

- Required keys (client-credentials flow):
  - `OAUTH_TOKEN_URL` (e.g., Duende: `https://demo.duendesoftware.com/connect/token`)
  - `OAUTH_CLIENT_ID`
  - `OAUTH_CLIENT_SECRET`
  - `OAUTH_SCOPE` (optional; empty for Spotify)
  - `OAUTH_PROBE_URL` (protected resource to call)

- Resolution order used by the framework (`OAuthConfig` helper):
  1. Environment variables: `OAUTH_*`
  2. System properties: `-Doauth.token_url=...` etc.
  3. `configuration.properties`: `oauth.token_url=...` etc.

### Local setup
```bash
# Duende (JWT tokens)
export OAUTH_TOKEN_URL="https://demo.duendesoftware.com/connect/token"
export OAUTH_CLIENT_ID="m2m.short"
export OAUTH_CLIENT_SECRET="secret"
export OAUTH_SCOPE="api"
export OAUTH_PROBE_URL="https://demo.duendesoftware.com/api/test"

# Spotify (opaque tokens)
export OAUTH_TOKEN_URL="https://accounts.spotify.com/api/token"
export OAUTH_CLIENT_ID="<your_spotify_client_id>"
export OAUTH_CLIENT_SECRET="<your_spotify_client_secret>"
export OAUTH_SCOPE=""
export OAUTH_PROBE_URL="https://api.spotify.com/v1/search?q=daft%20punk&type=artist&limit=1"
```

### GitHub Actions secrets
Add these repository secrets as needed:
- `OAUTH_TOKEN_URL`
- `OAUTH_CLIENT_ID`
- `OAUTH_CLIENT_SECRET`
- `OAUTH_SCOPE`
- `OAUTH_PROBE_URL`

Then expose them to jobs as env vars:
```yaml
env:
  OAUTH_TOKEN_URL: ${{ secrets.OAUTH_TOKEN_URL }}
  OAUTH_CLIENT_ID: ${{ secrets.OAUTH_CLIENT_ID }}
  OAUTH_CLIENT_SECRET: ${{ secrets.OAUTH_CLIENT_SECRET }}
  OAUTH_SCOPE: ${{ secrets.OAUTH_SCOPE }}
  OAUTH_PROBE_URL: ${{ secrets.OAUTH_PROBE_URL }}
```

### Running OAuth security tests
```bash
# All OAuth scenarios (Duende defaults if not overridden)
mvn clean test -Dtest=SecurityRunner -Dcucumber.filter.tags='@oauth'

# Spotify-only scenarios (opaque tokens)
mvn clean test -Dtest=SecurityRunner -Dcucumber.filter.tags='@spotify'
```

> Note: The `@spotify` opaque-token assertion is skipped automatically if your `OAUTH_TOKEN_URL` is not Spotify (to avoid false failures when using JWT providers like Duende).

---

## 🚨 Important: Before Pushing to GitHub

1. **Add the GitHub Secret** (see Step 1 above)
2. **Test locally first:**
   ```bash
   export WEATHER_API_KEY=`your api key here`
   mvn clean test
   ```
3. **Commit and push:**
   ```bash
   git add .
   git commit -m "Secure API key using GitHub Secrets"
   git push
   ```
4. **Verify on GitHub Actions** - run a workflow to confirm it works

---

## 🔄 Rotating API Keys

When you need to change your API key:

**For GitHub:**
1. Go to Settings → Secrets → Actions
2. Click on `WEATHER_API_KEY`
3. Click **Update secret**
4. Enter new key
5. Save

**For Local Development:**
```bash
# Update your environment variable
export WEATHER_API_KEY=new_key_here
```

---

## 💡 Pro Tips

1. **Never commit the actual key** - it's now safe in GitHub Secrets
2. **Share template file** - `configuration.properties.template` with team
3. **Document in README** - Tell team members to set `WEATHER_API_KEY` and `OAUTH_*` env vars
4. **Use .env files** - For local development (already in .gitignore)
5. **Rotate regularly** - Update API keys and OAuth client secrets periodically

---

## 🎉 Summary

Your configuration is now secure! Here's the complete setup:

✅ **ConfigurationReader** reads from env/system/properties for general keys
✅ **OAuthConfig** prefers `OAUTH_*` env vars for oauth.* keys
✅ **GitHub Actions** can inject secrets via `env` → `secrets.*`
✅ **Template file** includes commented examples for Duende/Spotify
✅ **Documentation** includes complete setup for Weather API and OAuth

**Next Steps:**
1. Set up `WEATHER_API_KEY` locally
2. Set up `OAUTH_*` variables (Duende or Spotify)
3. Run `@oauth` or `@spotify` tests via `SecurityRunner`
