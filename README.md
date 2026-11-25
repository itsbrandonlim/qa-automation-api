# QA Automation API Framework

API automation portfolio project with Java 25, REST Assured, JUnit 5, and Maven.

The suite tests the [DummyJSON REST API](https://dummyjson.com), a local in-memory todo API, and the product client against WireMock. It covers products, authentication, an authenticated user workflow, independent test-data setup and cleanup, and simulated dependency failures.

## Project Status

**Current milestone: Phase 5 — Checkpoint 5.5, mock services.**

| Phase / checkpoint | Scope | Implementation |
| --- | --- | --- |
| 1–2 | Product tests, configuration, request specifications, API clients | Implemented |
| 3 | Authentication, Java models, parameterized tests, JSON Schema validation | Implemented |
| 4 | Allure reporting and GitHub Actions | Implemented |
| 5.1 | Login → profile → user todos workflow | Implemented |
| 5.2 | Local test-data setup and cleanup | Implemented |
| 5.3 | Environment profiles and runtime overrides | Implemented |
| 5.4 | Parallel execution | Implemented |
| 5.5 | WireMock success, errors, delays, and read timeouts | Implemented |
| 5.6 | Contract-testing concepts and consumer/provider verification | Planned |

Implementation status is separate from execution results: use the latest Maven output and CI run to confirm whether tests pass.

## Tech Stack

| Technology | Purpose |
| --- | --- |
| Java 25 | Test and framework implementation |
| Maven | Dependencies and test execution |
| REST Assured / Hamcrest | HTTP requests and response assertions |
| JUnit 5 | Tests, lifecycle hooks, parameterized scenarios |
| Jackson | Request/response JSON mapping |
| JSON Schema Validator | Login response contract checks |
| Allure | Test results and HTML reports |
| GitHub Actions | CI execution and downloadable reports |
| Java HTTP server | Local in-memory todo test fixture |
| WireMock | Local HTTP stubs and request verification for product-client tests |

Dependency versions and build plugins are defined in `pom.xml`.

## Framework Structure

Java source paths are relative to `src/test/java/com/brandon/qa/`.

| Location | Responsibility |
| --- | --- |
| `config/ConfigManager.java` | Selects a profile, resolves overrides, validates base URLs |
| `specification/RequestSpecFactory.java` | Creates a fresh request specification for a configured or explicit URL |
| `client/` | `ProductClient`, `AuthClient`, `TodoClient`, and `LocalTodoClient` encapsulate HTTP operations |
| `model/` | `LoginRequest` and `LoginResponse` represent authentication data |
| `support/LocalTodoApi.java` | Starts an in-memory API on an available loopback port |
| `testdata/TodoDataFactory.java` | Creates unique todo payloads |
| `tests/products/productTests.java` | Contains the `ProductTests` class |
| `tests/auth/AuthTests.java` | Authentication and schema tests |
| `tests/workflows/UserWorkflowTests.java` | Login, retrieve profile, and verify user todos |
| `tests/lifecycle/TodoLifecycleTests.java` | Create, retrieve, delete, and clean up local todos |
| `tests/mocks/ProductMockTests.java` | Product-client request matching, HTTP errors, delays, and read timeouts |

| Project-relative path | Purpose |
| --- | --- |
| `src/test/resources/environments/dummyjson.properties` | Default API URL and public demo credentials |
| `src/test/resources/environments/local.properties` | Local profile; server URL is supplied at runtime |
| `src/test/resources/schemas/login-response-schema.json` | Login response contract |
| `src/test/resources/allure.properties` | Allure results directory |
| `src/test/resources/junit-platform.properties` | Parallel execution modes and worker limit |
| `.github/workflows/api-tests.yml` | Java 25 CI, test execution, report generation, artifact upload |

Tests describe behavior; clients handle requests. Both remote and local clients use `RequestSpecFactory`. Local clients explicitly supply the server-generated URL, so global base-URL overrides cannot redirect their setup or cleanup requests.

## Prerequisites

- JDK 25 and Maven available on `PATH`
- Git
- Internet access for dependency downloads and DummyJSON tests

Check your tools:

```bash
java -version
mvn -version
git --version
```

**Maven must report Java 25.** If it reports Java 21, point `JAVA_HOME` to your JDK 25 installation before running Maven. Otherwise compilation fails with `invalid target release: 25`.

## Run Tests

```bash
git clone https://github.com/itsbrandonlim/qa-automation-api.git
cd qa-automation-api
mvn clean test
```

Run commands from the directory containing `pom.xml`.

| Scope | Command |
| --- | --- |
| Full suite, explicit default profile | `mvn "-Denv=dummyjson" clean test` |
| Products | `mvn "-Dtest=ProductTests" test` |
| Authentication | `mvn "-Dtest=AuthTests" test` |
| User workflow | `mvn "-Dtest=UserWorkflowTests" test` |
| Local lifecycle | `mvn "-Denv=local" "-Dtest=TodoLifecycleTests" test` |
| Mock services | `mvn "-Denv=local" "-Dtest=ProductMockTests" test` |

`-Denv` selects configuration; `-Dtest` selects tests. The `local` profile does not select local tests automatically. Running the entire suite with `-Denv=local` fails because the local API has no DummyJSON authentication or product endpoints.

## Environment Configuration

Profile selection: **Java system property → environment variable → default `dummyjson`**.

Setting precedence: **Java system property → environment variable → selected profile file**.

| Setting | System property | Environment variable | Profile key / default |
| --- | --- | --- | --- |
| Profile | `env` | `TEST_ENV` | `dummyjson` |
| Base URL | `baseUrl` | `API_BASE_URL` | `base.url` |
| Username | `apiUsername` | `API_USERNAME` | `auth.username` |
| Password | `apiPassword` | `API_PASSWORD` | `auth.password` |

Profiles are loaded from `src/test/resources/environments/<profile>.properties`. The former `config.properties` file is no longer used.

- The profile is selected once per JVM. Set it before starting tests.
- Profile names are normalized to lowercase and allow letters, numbers, and hyphens.
- Blank overrides are treated as absent; passwords are preserved without trimming.
- Unknown profiles fail during configuration initialization.
- Missing settings fail when requested. Local tests do not request login credentials.
- Base URLs require HTTP(S) and a host, without embedded credentials, a query, or a fragment.
- An explicit local-server URL takes precedence over all base-URL settings.
- Changing the target URL requires an API with the endpoints and contracts expected by the selected tests.

Override the configured URL:

```bash
mvn "-Denv=dummyjson" "-DbaseUrl=https://dummyjson.com" "-Dtest=UserWorkflowTests" test
```

Use an environment variable in PowerShell (restore any prior value afterward):

```powershell
$previousTestEnv = $env:TEST_ENV
try {
    $env:TEST_ENV = "local"
    mvn "-Dtest=TodoLifecycleTests" test

    # The system property overrides TEST_ENV.
    mvn "-Denv=dummyjson" "-Dtest=UserWorkflowTests" test
} finally {
    $env:TEST_ENV = $previousTestEnv
}
```

On macOS/Linux:

```bash
TEST_ENV=local mvn "-Dtest=TodoLifecycleTests" test
TEST_ENV=local mvn "-Denv=dummyjson" "-Dtest=UserWorkflowTests" test
```

Valid-login tests read configured credentials and compare the returned identity with that account. Negative tests retain deliberately invalid inputs. Only public DummyJSON demo credentials are stored in the profile; use environment variables or CI secrets for private credentials.

### Checkpoint 5.3 verification

The following checks intentionally include failed Maven runs:

| Check | Command | Expected result |
| --- | --- | --- |
| Default profile | `mvn clean test` | Full suite passes when no overriding environment is set |
| Explicit profile | `mvn "-Denv=dummyjson" clean test` | Full suite passes |
| Unknown profile | `mvn "-Denv=does-not-exist" "-Dtest=UserWorkflowTests" test` | Fails with `Environment file not found: environments/does-not-exist.properties` |
| Invalid configured URL | `mvn "-Denv=dummyjson" "-DbaseUrl=not-a-url" "-Dtest=UserWorkflowTests" test` | Fails with a base-URL validation error |
| Local URL isolation | `mvn "-Denv=local" "-DbaseUrl=not-a-url" "-Dtest=TodoLifecycleTests" test` | Passes using the local server URL |

Static initialization errors may wrap the underlying configuration error in `ExceptionInInitializerError`. Finish verification with a successful full clean run so reports reflect the normal suite.

## Parallel Execution (Checkpoint 5.4)

JUnit runs test classes and test methods concurrently by default, using a fixed pool of **two workers**. The maximum pool size is also two, keeping concurrency modest against the public demo API. These settings live in `src/test/resources/junit-platform.properties` and apply to Maven, CI, and IDE runs that load the test resources.

The existing tests already isolate their state:

- JUnit creates a new test instance for each test method.
- Each request receives a fresh request specification; no test mutates REST Assured global configuration.
- Tokens, payloads, and responses belong to individual tests.
- Each local lifecycle test owns its server, dynamically allocated port, unique payload, and cleanup.
- Environment configuration is shared read-only. Set overrides before the run, never from concurrent tests.

Run the parallel suite:

```bash
mvn "-Denv=dummyjson" clean test
```

For debugging, disable parallel execution for one run:

```bash
mvn "-Denv=dummyjson" "-Djunit.jupiter.execution.parallel.enabled=false" clean test
```

Verify local setup and cleanup still use their own servers while running concurrently:

```bash
mvn "-Denv=local" "-DbaseUrl=not-a-url" "-Dtest=TodoLifecycleTests" clean test
```

For a higher-concurrency check, override both limits together:

```bash
mvn "-Denv=dummyjson" "-Djunit.jupiter.execution.parallel.config.fixed.parallelism=4" "-Djunit.jupiter.execution.parallel.config.fixed.max-pool-size=4" clean test
```

Use a clean run before generating Allure reports. In Allure's **Timeline**, overlapping tests on different worker threads demonstrate actual concurrency; a passing suite alone does not prove parallel execution. The suite should retain all 21 executions with no failures, errors, or skipped tests. Repeat parallel runs when checking for intermittent state-sharing failures.

The existing CI test command uses the same parallel defaults. A branch push alone does not trigger the current workflow; use manual dispatch or a pull request targeting `master` to run hosted CI without merging.

Configuration reference: [JUnit parallel execution](https://docs.junit.org/5.14.0/user-guide/index.html#writing-tests-parallel-execution).

## Mock Services (Checkpoint 5.5)

`ProductMockTests` runs the existing `ProductClient` against an embedded WireMock server. This lets us reproduce dependency responses without relying on DummyJSON availability or making the public service fail on demand.

Each test registers an **instance-scoped** `WireMockExtension`. JUnit starts a separate loopback server on a dynamic port for each test and stops it afterward, including when assertions fail. Stubs and request journals belong to that test. All registrations and verifications use `mockApi.stubFor(...)` and `mockApi.verify(...)`; the shared static WireMock client is not configured. Unmatched requests fail the test.

`ProductClient()` still uses the selected environment. `ProductClient(baseUrl)` uses an explicit URL, and `ProductClient(baseUrl, readTimeoutMillis)` also sets a positive socket read timeout. The default read timeout is 10 seconds. Each call creates a fresh request specification and client configuration; no test changes global REST Assured settings or system properties. A socket read timeout limits waiting for response data; it is not a total request deadline or connection timeout.

| Scenario | What the test checks |
| --- | --- |
| Product success | Product ID in the path, JSON Accept header, response fields, one request |
| Result limit | Query parameter and returned collection size |
| Category filter | Category path and response category |
| Missing product | Explicit stubbed 404 and error message, rather than WireMock's unmatched-request 404 |
| Unavailable service | 503, error body, Retry-After header, and a single request |
| Delayed success | A 200 ms delayed response succeeds within the default timeout |
| Read timeout | A 3-second delayed response raises `SocketTimeoutException` with a 500 ms read timeout |

The timeout test checks the exception type rather than an exact elapsed duration. HTTP errors remain responses that tests can inspect; a read timeout is a transport exception with no HTTP status. The client does not implement a retry policy for these cases.

Run only the mocks:

```bash
mvn "-Denv=local" "-Dtest=ProductMockTests" clean test
```

Verify isolation from a global URL override, including four concurrent workers:

```bash
mvn "-Denv=local" "-DbaseUrl=not-a-url" "-Dtest=ProductMockTests" "-Djunit.jupiter.execution.parallel.config.fixed.parallelism=4" "-Djunit.jupiter.execution.parallel.config.fixed.max-pool-size=4" clean test
```

The mock tests need no external API access after Maven dependencies are cached. They run automatically in the existing full-suite CI command. No WireMock process, Docker container, fixed port, or new environment profile is needed.

For study, trace `stubFor → ProductClient → RequestSpecFactory → HTTP response/assertion → verify`. Change the requested path or query parameter to see an unmatched-request failure, then restore it. Explain why a 503 response and a socket timeout require different assertions.

These tests demonstrate client behavior against responses we define. They do not prove that DummyJSON implements those responses, validate a production application's recovery behavior, or complete Checkpoint 5.6. The local todo API remains a stateful fixture for data lifecycle tests; WireMock supplies controlled dependency responses.

The test-scoped `wiremock-standalone` dependency shades its server dependencies to avoid clashes with REST Assured and Jackson. References: [WireMock installation](https://wiremock.org/docs/download-and-installation/), [JUnit extension lifecycle](https://wiremock.org/docs/junit-jupiter/), and [simulating delays and faults](https://wiremock.org/docs/simulating-faults/).

## Test Coverage

The API suite contains **21 test executions**:

| Area | Executions | Coverage |
| --- | --- | --- |
| Products | 4 | Product by ID, missing product, category filter, result limit |
| Authentication | 7 | Valid login, three invalid credential cases, current user with/without token, login schema |
| User workflow | 1 | Login → authenticated profile → associated todos |
| Local lifecycle | 2 | Retrieve created todo; delete and confirm absence |
| Mock services | 7 | Product, limit, category, 404, 503, delayed success, and read timeout |

Each local test starts its own server and creates unique data in `@BeforeEach`. Cleanup in `@AfterEach` tolerates an already-deleted todo, confirms it is absent, and stops the server in a `finally` block. These tests use real persistence across requests within their local server instance.

Authenticated tests obtain their own tokens. The workflow checks the logged-in user's ID and username and verifies that every returned todo belongs to that user; an empty todo collection is valid.

## Reports and Continuous Integration

Surefire results: `target/surefire-reports/`

Allure raw results: `target/allure-results/`

After a test run, generate the Allure HTML report:

```bash
mvn allure:report
```

Report output: `target/site/allure-maven/`. To generate and view a report locally:

```bash
mvn allure:serve
```

GitHub Actions runs on pushes to `master`, pull requests targeting `master`, and manual dispatch. It sets up Java 25 and runs:

```bash
mvn --batch-mode --no-transfer-progress "-Denv=dummyjson" clean test
```

The same job includes local lifecycle and mock-service tests because they supply their own URLs. It attempts Allure report generation when result files exist and uploads Surefire results, Allure results, and the HTML report as the `api-test-reports` artifact, retained for 14 days.

Generated reports and build output are ignored by Git. Running the workflow locally does not verify a hosted GitHub Actions run.

## Design and Limitations

- Keep the framework simple: tests, clients, models, a configuration utility, and shared request specifications.
- Tests prepare their own prerequisites and do not rely on execution order.
- Assertions check behavior; schemas check response structure.
- Public API tests depend on network access and DummyJSON availability.
- WireMock tests check the product client against authored stubs; provider compatibility remains separate work in Checkpoint 5.6.
- Authentication coverage does not yet include role-based authorization, token expiry, or token refresh.
- Parallel execution uses two workers by default; it is functional test concurrency, not a load or performance test.
