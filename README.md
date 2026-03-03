# Spribe Player API Test Automation Framework

## Overview
API test automation framework for Player Controller using Java, RestAssured, and TestNG.

## Technology Stack
- **Java**: 11+
- **Build Tool**: Gradle 9.0
- **Testing Framework**: TestNG 7.9.0
- **API Client**: RestAssured 5.4.0
- **Assertions**: TestNG Assert/SoftAssert
- **Reporting**: Allure 2.25.0
- **Logging**: Log4j2 2.22.1

## Project Structure
```
spribe-test-task/
├── src/
│   ├── main/
│   │   ├── java/com/spribe/api/
│   │   │   ├── clients/          # API client classes
│   │   │   │   ├── BaseClient.java
│   │   │   │   └── PlayerClient.java
│   │   │   ├── models/
│   │   │   │   └── # DTOs/POJOs
│   │   │   │   
│   │   │   ├── specs/            # Request/Response specifications
│   │   │   │   └── BaseSpec.java
│   │   │   ├── utils/            # Utilities
│   │   │   │   ├── ConfigManager.java
│   │   │   │   └── TestDataGenerator.java
│   │   │   └── listeners/        # TestNG listeners
│   │   │       └── TestListener.java
│   │   └── resources/
│   │       ├── env.properties    # Configuration
│   │       └── log4j2.xml        # Logging config
│   └── test/
│       ├── java/com/spribe/api/tests/
│       │   ├── BaseTest.java
│       │   ├── PlayerCreateTest.java
│       │   ├── PlayerDeleteTest.java
│       │   ├── PlayerGetTest.java
│       │   ├── PlayerGetAllTest.java
│       │   └── PlayerUpdateTest.java
│       └── resources/
│           └── testng-suites/
│               └── player-controller.xml
└── build.gradle
```

## Key Features

### 1. Thread-Safe Parallel Execution
- **3 threads** configured via TestNG suite
- `parallel="methods"` execution mode
- Thread-local test data generation (unique logins, screenNames per thread)
- No shared mutable state

### 2. Comprehensive Logging
- Request/response logging via RestAssured filters
- Test lifecycle logging via custom TestNG listener
- Log4j2 configuration with console and file appenders
- Detailed failure logging

### 3. Allure Reporting
- Integrated AllureRestAssured filter
- Request/response attachments
- Test descriptions and severity levels
- Feature-based organization

### 4. Production-Grade Design
- Centralized configuration via `ConfigManager`
- Builder pattern for test data via `TestDataGenerator`
- Reusable `BaseClient` with HTTP method wrappers
- Clean separation: models, clients, specs, tests, utilities

## Prerequisites
- **JDK**: 11 or higher
- **Gradle**: 9.0+ (or use included Gradle wrapper)
- **Allure CLI**: For report generation (optional)

Install Allure (if needed):
```bash
# macOS
brew install allure

# Windows (via Scoop)
scoop install allure

# Or download from: https://github.com/allure-framework/allure2/releases
```

## Configuration

The framework uses `env.properties` file for environment configuration. Edit `src/main/resources/env.properties`:
```properties
base.url=http://3.68.165.45
threads=3
parallel=methods
```

## Running Tests

### Run all tests (parallel, 3 threads):
```bash
./gradlew clean test
```

### Run a specific test class:
```bash
./gradlew test --tests tests.com.spribe.PlayerCreateTest
```

### Run a specific test method:
```bash
./gradlew test --tests tests.com.spribe.PlayerCreateTest.testCreatePlayersAllRolesAsSupervisor
```

## Generating Allure Reports

### Generate and open the report:
```bash
allure generate build/allure-results --clean
allure serve build/allure-results
```

## Bug Reports & Observations

All discovered bugs, improvements, and architectural observations are documented in **`FOUND_BUGS.md`**.

## Future Framework Improvements

1. Customizations for Allure Reporting (Environment properties, steps annotations, etc)
2. Logging on the test steps level
3. CI/CD Integration
4. Framework Enhancements (API Mocking, Multi-Environment Support, Profile-based configuration (dev, staging, prod), Test Tagging
