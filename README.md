# QA Automation API Framework

API automation portfolio project demonstrating practical Quality Engineering and test automation skills using Java, REST Assured, JUnit 5, and Maven.

The framework tests the [DummyJSON REST API](https://dummyjson.com) through reusable API clients, shared request specifications, configurable environment settings, and request/response models. It covers product endpoints and authentication workflows with positive, negative, data-driven, and JSON Schema tests.

## Project Status

**Current milestone: Phase 3 — Authentication and stronger API tests.**

| Phase | Scope | Status |
| --- | --- | --- |
| 1 | Project setup and initial Product API tests | Complete |
| 2 | Configuration management, shared request specifications, and API client layer | Complete |
| 3 | Authentication, Java models, Jackson mapping, parameterized tests, and JSON Schema validation | Complete |
| 4 | Allure reporting and GitHub Actions continuous integration | Planned |

## Tech Stack

| Technology | Purpose |
| --- | --- |
| Java 21 | Test and framework implementation |
| Maven | Dependency management and test execution |
| REST Assured | HTTP requests and API response validation |
| JUnit 5 | Test execution and parameterized scenarios |
| Hamcrest | Readable response assertions |
| Jackson | Java-to-JSON serialization and JSON-to-Java deserialization |
| REST Assured JSON Schema Validator | Response structure and type validation |
| Git and GitHub | Version control and pull request workflow |

Dependency versions are defined in `pom.xml`.

## Framework Architecture

Tests define expected behavior and call API clients. Clients handle HTTP methods, endpoints, request bodies, and authentication headers. `RequestSpecFactory` provides shared request configuration using the base URL supplied by `ConfigManager`.

Request and response models represent authentication data, while schema files define the response structure checked by contract tests.

| Component | Responsibility |
| --- | --- |
| `ProductTests` | Validates product retrieval, missing products, categories, and limits |
| `AuthTests` | Validates login, authenticated access, missing authentication, and the login response schema |
| `ProductClient` | Encapsulates Product API endpoints and HTTP operations |
| `AuthClient` | Encapsulates login and current-user requests |
| `LoginRequest` | Represents the username and password sent during login |
| `LoginResponse` | Represents selected login response fields, including the access token |
| `RequestSpecFactory` | Creates shared REST Assured request specifications |
| `ConfigManager` | Loads the default base URL and supports a JVM system-property override |
| `config.properties` | Stores non-sensitive default configuration |
| `login-response-schema.json` | Defines required login response fields, types, and constraints |

## Project Structure

Java source paths below are relative to `src/test/java/com/brandon/qa/`.

| Location | File |
| --- | --- |
| `config/` | `ConfigManager.java` |
| `specification/` | `RequestSpecFactory.java` |
| `client/` | `ProductClient.java`, `AuthClient.java` |
| `model/` | `LoginRequest.java`, `LoginResponse.java` |
| `tests/products/` | Product test class: `ProductTests` |
| `tests/auth/` | `AuthTests.java` |

| Project-relative path | Purpose |
| --- | --- |
| `src/test/resources/config.properties` | Default API configuration |
| `src/test/resources/schemas/login-response-schema.json` | Login response schema |
| `pom.xml` | Maven dependencies and build plugins |
| `.gitignore` | Excludes generated files and local tooling output |
| `README.md` | Project overview and execution instructions |

## Prerequisites

- JDK 21
- Maven installed and available on `PATH`
- Git to clone the repository
- Internet access to download dependencies and reach DummyJSON

Verify the installed tools:

```bash
java -version
mvn -version
git --version
```

Ensure `mvn -version` reports Java 21.

## Getting Started

Clone the repository and open the project directory:

```bash
git clone https://github.com/itsbrandonlim/qa-automation-api.git
cd qa-automation-api
```

Run Maven commands from the directory containing `pom.xml`.

### Run all tests

```bash
mvn clean test
```

### Run only product tests

```bash
mvn "-Dtest=ProductTests" test
```

### Run only authentication tests

```bash
mvn "-Dtest=AuthTests" test
```

### Override the API base URL

The default configuration in `src/test/resources/config.properties` is:

```properties
base.url=https://dummyjson.com
```

Override it for a test run using the `baseUrl` JVM system property:

```bash
mvn clean test "-DbaseUrl=https://dummyjson.com"
```

An alternative base URL must expose the endpoints and response contracts expected by this suite.

### Inspect test results

Maven prints the execution summary in the terminal. Surefire writes detailed test results to:

```text
target/surefire-reports/
```

Generated files under `target/` should not be committed. Allure reports and automated GitHub Actions runs are planned for Phase 4.

## Test Coverage

### Products API

| Scenario | Validation |
| --- | --- |
| Retrieve a product by ID | HTTP 200, expected ID, and presence of title and price |
| Request a nonexistent product | HTTP 404 |
| Retrieve products by category | Nonempty collection with the requested category on every item |
| Limit the product collection | Returned collection size and response limit match the requested value |

### Authentication API

| Scenario | Validation |
| --- | --- |
| Login with valid credentials | HTTP 200, expected username, user ID, and nonblank access token |
| Login with an incorrect password | HTTP 400 and invalid-credentials message |
| Login with an unknown username | HTTP 400 and invalid-credentials message |
| Login with both credentials incorrect | HTTP 400 and invalid-credentials message |
| Request the current user with a valid token | HTTP 200 and identity matching the logged-in user |
| Request the current user without a token | HTTP 401 and an error message |
| Validate the login response schema | Required fields, field types, and nonempty token strings |

The Phase 3 suite defines **11 test executions**: four product scenarios and seven authentication executions. The invalid-credentials parameterized test contributes three executions.

This describes the suite's coverage; the latest Maven output is the source of truth for pass/fail results.

## Authentication and Data Mapping

Authentication tests send a `LoginRequest` to `POST /auth/login`. Jackson serializes that Java object into JSON and deserializes the successful response into `LoginResponse`.

The authenticated-user test obtains its own access token and sends it to `GET /auth/me` through an `Authorization: Bearer <access-token>` header. Tests do not depend on another test running first.

The suite uses DummyJSON's public demonstration credentials. These are sample API data, not personal account credentials. Access tokens are obtained during execution rather than stored in source files.

Schema validation checks the response's structure. Value assertions separately check behavior, such as whether the returned username matches the account that logged in. The login schema permits additional response fields while requiring the fields used by its contract checks.

## Test Design Principles

- **Separation of concerns:** tests express expectations; clients handle HTTP operations.
- **Reusable configuration:** shared request specifications keep request setup consistent.
- **Independent tests:** each authenticated scenario prepares its own login prerequisite.
- **Data-driven coverage:** parameterized tests exercise multiple invalid inputs without duplicating test logic.
- **Behavior and contract validation:** assertions check status codes and values; schemas check required fields and types.
- **Maintainable models:** request/response classes make authentication data explicit.

The suite exercises a public external service. Network failures, service outages, or API changes can affect results. Current authentication coverage does not include role-based authorization, token expiration, or token refresh.

## Learning Objectives

This project is built incrementally to develop practical understanding of:

- REST API and HTTP concepts
- Positive and negative test design
- Path parameters, query parameters, and collection validation
- Java classes, constructors, and request/response models
- REST Assured clients and request specifications
- Jackson serialization and deserialization
- Bearer-token authentication
- JUnit parameterized testing
- JSON Schema validation
- Maven dependency management and test execution
- Git feature branches and pull requests

## Next Milestone — Phase 4

- Integrate Allure reporting with readable test names and failure details.
- Run the suite through GitHub Actions on pull requests and updates to `master`.
- Preserve test results and generated reports as downloadable CI artifacts.
- Document local report viewing and CI execution.

Further enhancements may include test data factories, additional API workflows, and broader environment configuration.
