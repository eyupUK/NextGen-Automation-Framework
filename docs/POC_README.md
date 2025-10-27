PoC: Demo E2E Checkout Flow

Goal
----
Provide a minimal, stakeholder-friendly proof-of-concept that demonstrates the full UI checkout flow executing in CI, showing a passing Cucumber scenario and generated artifacts (HTML/JSON/Allure).

What I added
------------
- `@demo` tagged scenario in `src/test/resources/features/ui/ui_checkout.feature` which performs a checkout by selecting the highest-priced item.
- GitHub Actions workflow: `.github/workflows/demo-ci.yml` — runs the `@demo` scenario headlessly on `ubuntu-latest` with JDK 21 and uploads UI reports as artifacts.

How to run locally (quick)
--------------------------
1. Ensure Java 21 and Maven are installed and available (JAVA_HOME points to JDK 21).
2. Create local configuration from the template:

```bash
cp configuration-test.properties.template configuration-test.properties
```

3. Run only the demo scenario locally (headless Chrome):

```bash
./mvnw -Dheadless=true -Dbrowser=chrome -Dcucumber.filter.tags="@demo" test
```

CI notes
--------
- The workflow is triggered manually via GitHub "Run workflow" or on push to `main`/`master`.
- The job runs single-threaded headless Chrome on the hosted runner and uploads reports as an artifact called `demo-reports`.
- Artifacts include `target/ui-api-report.html`, Allure results, and screenshots.