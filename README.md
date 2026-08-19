# Happy Tails 🐾

A Java full-stack pet care application built with Spring Boot. The project demonstrates layered backend design, REST APIs, relational persistence, validation, automated testing, Docker packaging, and CI.

## What it does

- Create, view, update, delete, and filter pet profiles.
- Store pet information through Spring Data JPA.
- Expose REST endpoints under `/api/pets`.
- Serve a lightweight browser UI from Spring Boot.
- Run locally with H2 or connect to PostgreSQL using environment variables.
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

## PostgreSQL

Set these environment variables before starting the app:

```text
DATABASE_URL=jdbc:postgresql://localhost:5432/happytails
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=your_password
```

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
- Environment-driven database configuration for local and hosted environments.
- Integration testing with Spring Boot, JUnit, MockMvc, and H2.
- Multi-stage Docker build for portable deployment.
- GitHub Actions CI runs Maven tests on pushes and pull requests.

## Author

**Bhavana Angadi**

Portfolio project focused on practical Java backend and full-stack software engineering.
