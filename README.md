## UH Groupings Project
### The web API for UH Groupings.

Manage your groupings in one place, use them in many.

A grouping is a collection of members (e.g., all full-time Hilo faculty). UH Groupings allows you to manage grouping memberships, control members' self-service options, designate sync destinations, and more.

Groupings can be integrated with one or more of the following: email LISTSERV lists, permissions and privilege assignments for access control via CAS, etc, and this list will continue to grow.  Additionally, UH Groupings allows you to leverage existing membership collections, which can substantially reduce the manual overhead of membership management.

UH Groupings utilizes the Internet2 Grouper project.  Grouper is an enterprise access management system designed for the highly distributed management environment and heterogeneous information technology environment common to universities.

[![Build and Test](https://github.com/uhawaii-system-its-ti-iam/uh-groupings-api/actions/workflows/build_badge.yml/badge.svg)](https://github.com/uhawaii-system-its-ti-iam/uh-groupings-api/actions/workflows/build_badge.yml)
[![Coverage Status](https://github.com/uhawaii-system-its-ti-iam/uh-groupings-api/blob/badges/jacoco.svg)](https://github.com/uhawaii-system-its-ti-iam/uh-groupings-api/actions/workflows/coverage.yml)
[![Known Vulnerabilities](https://snyk.io/test/github/uhawaii-system-its-ti-iam/uh-groupings-api/badge.svg)](https://snyk.io/test/github/uhawaii-system-its-ti-iam/uh-groupings-api)
[![CodeQL](https://github.com/yertsti/uh-groupings-api/actions/workflows/codeql.yml/badge.svg)](https://github.com/uhawaii-system-its-ti-iam/uh-groupings-api/actions/workflows/codeql.yml)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/cd7ffbd709394483a61472a2c87b0aaf)](https://www.codacy.com/gh/uhawaii-system-its-ti-iam/uh-groupings-api/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=uhawaii-system-its-ti-iam/uh-groupings-api&amp;utm_campaign=Badge_Grade)

## Getting Started

Local development quick start. **Deploying to AWS?** See [docs/AWS_QUICKSTART.md](docs/AWS_QUICKSTART.md).

### Prerequisites

- **Docker Desktop** running locally
- **Make** (standard on macOS and Linux)
- **Git** with access to `https://github.com/uhawaii-system-its-ti-iam/uh-groupings-api`
- **Java JDK 21** — only if you intend to build outside Docker (see [Building from Source](#building-from-source) below); the Docker quick start does not need it

### Run locally with Docker

1. **Clone the repo:**
   ```bash
   git clone https://github.com/uhawaii-system-its-ti-iam/uh-groupings-api.git
   cd uh-groupings-api
   ```

2. **Create your local properties file:**
   ```bash
   mkdir -p ~/.$(whoami)-conf
   nano ~/.$(whoami)-conf/uh-groupings-api-overrides.properties
   chmod 600 ~/.$(whoami)-conf/uh-groupings-api-overrides.properties
   ```
   Paste the template from [docs/DEV_QUICKSTART.md](docs/DEV_QUICKSTART.md#2-create-configuration-file) and fill in your Grouper credentials, JWT key, and email.

3. **Start the application:**
   ```bash
   docker-compose up
   ```

4. **Verify:**
   ```bash
   curl http://localhost:8081/uhgroupingsapi/actuator/health
   # → {"status":"UP"}
   ```
   Swagger UI: <http://localhost:8081/uhgroupingsapi/swagger-ui.html>

## Building from Source

For non-Docker builds. Requires Java JDK 21.

### Building
To run the Application from the Command Line:

    $ ./mvnw clean spring-boot:run

To build a deployable war file for local development, if preferred:

    $ ./mvnw clean package

You should have a deployable war file in the target directory.
Deploy as usual in a servlet container, e.g. tomcat.

### Running Unit Tests
The project includes Unit Tests for various parts of the system.
For this project, Unit Tests are defined as those tests that will
rely on only the local development computer.
A development build of the application will run the Unit Tests.
A test and production build of the application will run both the
Unit Tests and the System Tests (which may require network access).
You can also run specific Unit Tests using the appropriate command
line arguments.

To run the Unit Tests with a standard build:

    $ ./mvnw clean test

To run a test class:

    $ ./mvnw clean test -Dtest=StringsTest

To run a single method in a test class:

    $ ./mvnw clean test -Dtest=StringsTest#trunctate

### Running System Tests
The project files include a handful of System Tests.
For this project, System Tests are defined as those tests that may
call live remote systems, such as a search against the production
LDAP server. A standard build of the application will exclude the
System Tests, but you can explicitly run them by specifying the
appropriate command line argument.

To run the System Tests:

    $ ./mvnw -Dtest=*SystemTest clean test
