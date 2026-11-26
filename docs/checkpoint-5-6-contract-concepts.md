# Checkpoint 5.6: Contract concepts

The learning goal is to explain what schema validation, mocks, and consumer-driven contracts each prove, then demonstrate a provider verification that fails when an agreed interaction changes.

## Three different checks

| Check | Example in this repository | Evidence and limits |
| --- | --- | --- |
| JSON Schema | `TodoSchemaContractTests` checks required fields and types | Detects missing/renamed fields and a string ID. Allows additive fields. Does not prove that the returned todo is the one requested. |
| HTTP mock | `ProductMockTests` calls WireMock through `ProductClient` | Reproduces controlled responses and verifies outgoing requests. A passing stub test alone says nothing about the real provider. |
| Consumer/provider contract | `TodoConsumerContractTests` generates a Pact; `TodoProviderContractIT` replays it against `LocalTodoApi` | Checks the real client's requests and the real local provider's responses for the selected interactions. Does not replace lifecycle, security, or performance testing. |

The schema examples are compatibility exercises over JSON fixtures. The existing authentication test remains a live response schema check. The Pact tests use their own interaction definitions, not the JSON Schema file.

## Follow the contract through the build

```text
Surefire (test)
  LocalTodoClient -> Pact mock -> consumer assertions
                               -> target/pacts/LocalTodoClient-LocalTodoApi.json
Failsafe (integration-test, verify)
  Generated pact -> provider state setup -> real LocalTodoApi -> Pact comparison
```

The consumer is `LocalTodoClient`; the provider is `LocalTodoApi`. This is a local teaching example with both sides in one repository. No broker, account, external service, or Docker installation is required.

The consumer test calls the actual client rather than constructing HTTP requests separately. Pact checks the requests and writes the interactions after successful consumer testing. The provider test reads that generated artifact; it does not duplicate the expected response or route verification through WireMock.

| Interaction | Provider state | Expected response |
| --- | --- | --- |
| POST `/todos` | No todos exist | 201, integer ID, supplied todo text and completion flag |
| GET `/todos/1` | Todo 1 exists | 200, ID 1, expected text and completion flag |
| GET `/todos/9999` | No todos exist | 404 and `Todo not found` error |
| DELETE `/todos/1` | Todo 1 exists | 204 |

The generated ID in the create response uses an integer matcher instead of requiring an arbitrary generated value. The retrieval interaction requires ID 1 because it requests that specific resource. Exact text and boolean expectations make changes visible. Matcher choices describe what the consumer relies on; overly broad matchers can hide incompatible behavior.

Each provider interaction starts a new `LocalTodoApi` on a dynamic loopback port. A provider-state method creates todo 1 only when needed. The provider is stopped after each interaction, including failures. No test depends on another interaction having run first. Contract methods are sequential within each class; other test classes retain the existing parallel configuration.

## Run and inspect

From the repository root, with Maven using JDK 25:

```bash
# Complete portfolio: 30 Surefire tests + 4 Failsafe interactions.
mvn "-Denv=dummyjson" clean verify

# Only the schema and consumer tests, followed by provider verification.
mvn "-Denv=local" "-Dtest=Todo*ContractTests" clean verify

# The generated server URLs must win over the invalid global override.
mvn "-Denv=local" "-DbaseUrl=not-a-url" "-Dtest=Todo*ContractTests" clean verify

# Generate the combined consumer and provider report after a full verify.
mvn allure:report
```

The contract-only command performs 9 Surefire executions and 4 Failsafe verifications. After dependencies are cached, it needs only local networking. `-Dtest` selects Surefire tests; Failsafe selects the `*IT` provider class independently. `-Denv` only selects configuration.

Inspect:

- `target/pacts/LocalTodoClient-LocalTodoApi.json`: methods, paths, provider states, response fields and matching rules.
- `target/surefire-reports/`: schema and consumer results.
- `target/failsafe-reports/`: one verification per generated interaction.
- `target/site/allure-maven/`: combined report after generation.

Always use `clean verify` for acceptance. It removes stale artifacts, regenerates the Pact, then verifies the provider. Generated contracts are ignored under `target/` and uploaded as CI artifacts. Running a provider class directly in an IDE requires the consumer-generated Pact first, with the repository root as the working directory. Missing contracts must fail verification, not silently skip it.

## Learning exercise: make compatibility fail

On a disposable working change, change the response status for an existing todo from 200 to 202 in `LocalTodoApi`, while leaving the consumer contract unchanged. Run the contract-only `clean verify` command. Consumer tests should still pass against their Pact mock; provider verification should reject the changed status. Restore the provider and run `clean verify` again.

For an artifact-only exercise, change the generated GET `/todos/1` response status from 200 to 202 in the Pact JSON, then run:

```bash
mvn "-Denv=local" failsafe:integration-test failsafe:verify
```

That must fail because the actual provider still returns 200. This deliberately bypasses contract regeneration for the exercise. Finish with the contract-only `clean verify` command to recreate the correct artifact. Never treat edited generated contracts as the source of truth.

## Explain it in an interview

“I first validated JSON shapes and used WireMock to simulate dependency responses. Then I added consumer-driven contract tests: my actual todo client exercises Pact's mock, generating four interactions. A separate Maven phase starts my real local todo API and verifies those interactions. This catches mismatches that a passing mock alone would miss.”

Be ready to explain:

- Why an extra field is compatible for this schema, while a renamed required field is not.
- Why a structurally valid response can still contain the wrong ID.
- Why consumer tests passing is only half the contract workflow.
- How provider states remove execution-order dependencies.
- Why `mvn test` does not include provider verification, and why CI uses `verify`.
- Why an integer matcher is useful for a generated ID, but exact ID matching matters for a retrieval.

The four interactions do not cover authorization, all validation errors, or deployment compatibility across independently released services. There is no Pact Broker publication or deployment gate here. Those can be explored later if needed; Checkpoint 5.6 completes the local contract-concepts exercise.

References: [Pact consumer JUnit 5](https://docs.pact.io/implementation_guides/jvm/consumer/junit5), [Pact provider JUnit 5](https://docs.pact.io/implementation_guides/jvm/provider/junit5), [Maven Failsafe lifecycle](https://maven.apache.org/surefire/maven-failsafe-plugin/).
