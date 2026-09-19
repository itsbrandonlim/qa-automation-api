# QA Automation API Framework

API automation portfolio project built to demonstrate practical Quality Engineering and test automation skills using Java, REST Assured, JUnit, and Maven.

The project currently tests the DummyJSON REST API and will evolve into a maintainable API automation framework with reusable API clients, configuration management, test data handling, reporting, and CI/CD execution.

## Tech Stack

* Java 21
* Maven
* REST Assured
* JUnit 5
* Hamcrest
* Git


Planned additions:

* Jackson
* JSON Schema Validation
* Allure Report
* GitHub Actions
* API client layer
* Test data factories
* Environment configuration

## Current Test Coverage

### Products API

The current automated scenarios include:

* Retrieve a product by ID
* Verify product response data
* Request a non-existing product
* Validate HTTP 404 responses
* Validate product pagination/limits
* Retrieve products by category
* Validate collection response data

The test suite includes both positive and negative API scenarios.

## Project Structure

```text
qa-automation-api/

├── src/
│   └── test/
│       └── java/
│           └── com/
│               └── brandon/
│                   └── qa/
│                       └── tests/
│                           └── products/
│                               └── ProductTests.java
│
├── .gitignore
├── pom.xml
└── README.md
```

The architecture will be expanded as additional API domains and reusable framework components are introduced.

## Test Design

The project aims to demonstrate more than HTTP status-code checking.

Tests are designed to cover areas including:

* Positive scenarios
* Negative scenarios
* Response-body validation
* Path parameters
* Query parameters
* HTTP status codes
* Collection validation
* Pagination
* API business rules

Future iterations will introduce authentication, reusable request specifications, serialization/deserialization, schema validation, test data generation, and API workflows.

## Learning Objectives

This project is being built brick by brick for learning and understanding of both test automation and framework design.

Key learning areas include:

* REST API testing
* HTTP concepts
* Test design techniques
* Java automation development
* REST Assured
* JUnit
* Maven dependency management
* Framework architecture
* Clean test design
* CI/CD
* Test reporting
* Git workflow
