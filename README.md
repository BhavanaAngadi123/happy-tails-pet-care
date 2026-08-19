# Happy Tails 🐾

A Java full-stack pet care application built with Spring Boot. The project demonstrates layered backend design, REST APIs, relational persistence, validation, automated testing, Docker packaging, CI, and production deployment configuration.

## What it does

- Create, view, update, delete, and filter pet profiles.
- Store pet information through Spring Data JPA.
- Expose REST endpoints under `/api/pets`.
- Serve a lightweight browser UI from Spring Boot.
- Run locally with H2 or in production with PostgreSQL.
- Verify the API with Spring Boot integration tests.

## Tech Stack

- Java 17
- Spring Boot
- Spring Web / REST
- Spring Data JPA / Hibernate
- H2 and PostgreSQL
- Maven
- HTML, CSS, JavaScript
- JUnit / MockMvc
- Docker
- GitHub Actions
- Render Blueprint

## Architecture

```text
src/main/java/com/happytails/
├── HappyTailsApplication.java
└── pet/
    ├── Pet.java
    ├── PetController.java
    ├── PetRepository.java
    └── PetService.java
```

The API follows a controller → service → repository structure so HTTP handling, business logic, and persistence remain separated.

## REST API

| Method | Endpoint | Purpose |
| --- | --- | --- |
| GET | `/api/pets` | List pets |
| GET | `/api/pets?species=Dog` | Filter by species |
| GET | `/api/pets/{id}` | Get one pet |
| POST | `/api/pets` | Create a pet |
| PUT | `/api/pets/{id}` | Update a pet |
| DELETE | `/api/pets/{id}` | Delete a pet |

## Run locally

```bash
git clone https://github.com/BhavanaAngadi123/happy-tails-pet-care.git
cd happy-tails-pet-care
mvn spring-boot:run
```

Open `http://localhost:8080`.

The default profile uses an in-memory H2 database, so the project runs without external setup.

## Local PostgreSQL

Set these environment variables before starting the app:

```text
DATABASE_URL=jdbc:postgresql://localhost:5432/happytails
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=your_password
```

## Production Deployment on Render

The repository contains a `render.yaml` Blueprint that provisions both the Spring Boot web service and a PostgreSQL database.

1. Make this repository public, or authorize Render to access the private repository.
2. In Render, create a new Blueprint and select this repository.
3. Render reads `render.yaml`, builds the included Dockerfile, creates PostgreSQL, injects database credentials, and starts the application with the `prod` Spring profile.
4. Render verifies the deployment through `/actuator/health`.

Production database credentials are supplied by Render and are not committed to source control.

## Test

```bash
mvn clean test
```

## Docker

```bash
docker build -t happy-tails .
docker run -p 8080:8080 happy-tails
```

## Engineering Highlights

- Layered Spring Boot design with controller, service, repository, and entity components.
- RESTful CRUD API with Jakarta Bean Validation.
- Separate local and production database configuration.
- PostgreSQL production profile with secrets supplied through environment variables.
- Integration testing with Spring Boot, JUnit, MockMvc, and H2.
- Multi-stage Docker build for portable deployment.
- GitHub Actions CI runs Maven tests on pushes and pull requests.
- Render Blueprint provisions the application and PostgreSQL infrastructure together.
- Spring Boot Actuator provides a production health-check endpoint.

## Author

**Bhavana Angadi**

Portfolio project focused on practical Java backend and full-stack software engineering.
