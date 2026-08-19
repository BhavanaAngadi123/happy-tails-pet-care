# Happy Tails 🐾

Happy Tails is a Java full-stack pet social and care platform built with Spring Boot. The core idea is simple: the **pet owns the digital identity**. Each pet has a profile, social graph, posts, memories, care history, reminders, play dates, communities, sitter bookings, and personalized discovery.

This repository has evolved from a basic pet CRUD application into a broader product prototype designed around one persistent pet identity.

## Product vision

A pet profile can become the center of the pet's digital life:

- Create a pet-first social profile with photo, username, species, breed, birthday, adoption date, location, personality, activities, and preferences.
- Publish posts and build followers and pet-friend relationships.
- Discover compatible pets using species, breed, location, age, personality, activities, and user preferences.
- Send friend requests and pet-to-pet messages.
- Schedule play-date requests and accept, decline, cancel, or message around confirmed plans.
- Join species, breed, local, and mixed-pet communities.
- Discover pet-friendly meetups.
- Maintain private health and care information through a Pet Health Passport.
- Track vaccinations, vet visits, medications, grooming, dental care, and future reminders.
- Book pet sitters for the active pet and track booking status.
- Save permanent memories, birthdays, Gotcha Days, anniversaries, and "On This Day" moments.
- Browse a lightweight pet marketplace with toys, snacks, clothing, gifts, care items, travel items, and small-pet products.
- Control account privacy, location visibility, messaging permissions, play-date permissions, blocking, and reporting.

## Core product principle

Happy Tails is not an owner profile with a pet attached to it.

The **pet is the social identity**. The owner account is used for authentication and management, while posts, follows, friendships, messaging, play dates, communities, memories, care, and recommendations revolve around the active pet.

## Tech stack

- Java 17
- Spring Boot 3
- Spring Web / REST
- Spring Data JPA / Hibernate
- PostgreSQL in production
- H2 for local development and tests
- HTML, CSS, and JavaScript frontend served by Spring Boot
- Maven
- JUnit 5 / MockMvc
- GitHub Actions CI
- Docker
- Render deployment
- Spring Boot Actuator health checks

## Architecture

```text
src/main/java/com/happytails/
├── HappyTailsApplication.java
└── social/
    ├── AppPageController.java
    ├── AuthController.java
    ├── SocialController.java
    ├── SocialDomain.java
    ├── SocialRepositories.java
    ├── PetMessaging.java
    ├── PetHealthPassport.java
    ├── PetCommunities.java
    ├── SafetyController.java
    ├── SitterBooking.java
    └── SocialSeed.java

src/main/resources/static/
├── index.html
├── login.html
├── profile-editor.js
├── social-experience.js
├── messaging.js
├── playdate-flow.js
├── health-passport.js
├── sitter-booking.js
├── memories-milestones.js
├── communities.js
├── smart-discovery.js
├── trust-safety.js
├── qa-polish.js
├── journey-flow.js
└── visual-system.css
```

The backend uses Spring MVC controllers, JPA entities/repositories, server-side HTTP sessions, and PostgreSQL persistence. The frontend is a lightweight JavaScript application served from the Spring Boot service.

## Main API areas

| Area | Example endpoints |
| --- | --- |
| Authentication | `/api/auth/signup`, `/api/auth/login`, `/api/auth/session` |
| Pet management | `/api/auth/pets`, `/api/auth/select-pet/{petId}` |
| Social profiles | `/api/social/profiles` |
| Posts | `/api/social/posts` |
| Follows | `/api/social/follows` |
| Friend requests | `/api/social/friend-requests` |
| Messaging | `/api/messages/...` |
| Play dates | `/api/social/play-dates` |
| Meetups | `/api/social/meetups` |
| Health reminders | `/api/social/reminders` |
| Health Passport | private health APIs under the health module |
| Memories | `/api/social/memories` |
| Communities | community APIs under the communities module |
| Safety | block/report APIs under the safety module |
| Sitter bookings | sitter-booking APIs |

Private pet data is protected using the active pet stored in the authenticated HTTP session. Sensitive actions do not trust arbitrary pet IDs supplied by the browser.

## Privacy and safety

Happy Tails currently includes prototype-level controls for:

- Public or private pet accounts
- Location visibility
- Messaging permissions
- Play-date request permissions
- Blocking another pet
- Reporting a pet profile
- Private reminders, memories, orders, play dates, and health information
- Server-side active-pet identity enforcement for sensitive actions

The health area is intentionally separate from public social profile data.

## Testing and QA

Run the complete test suite:

```bash
mvn clean test
```

The repository includes a Spring Boot / MockMvc smoke test that covers a core multi-pet journey including signup, pet creation, posts, follows, friend requests, messaging, play dates, private data protection, and identity spoofing protection.

GitHub Actions runs Maven tests on pushes and pull requests.

## Run locally

```bash
git clone https://github.com/BhavanaAngadi123/happy-tails-pet-care.git
cd happy-tails-pet-care
mvn spring-boot:run
```

Open:

```text
http://localhost:8080
```

The default local configuration uses an in-memory H2 database.

## Local PostgreSQL

You can also run against PostgreSQL by supplying the required datasource environment variables or adapting the local Spring profile.

Production uses Render-provided PostgreSQL connection details and does not commit database credentials to the repository.

## Production deployment

The project includes `render.yaml` for Render deployment.

Render provisions:

- Spring Boot web service
- PostgreSQL database
- Production environment variables
- `/actuator/health` health check
- Automatic deployments from the repository

The production Spring profile also enables secure session cookies and disables the H2 console.

## Current prototype boundaries

This is a working full-stack product prototype, not a finished commercial marketplace. A few areas are intentionally simplified:

- Sitter confirmation is prototype-driven; sitters do not yet have a separate authentication system.
- Shopping uses a sample in-app catalog rather than live merchant/payment integrations.
- Compatibility scoring is transparent rule-based matching rather than machine learning.
- Real-time messaging currently uses periodic refresh rather than WebSockets.
- Moderation/reporting is stored and enforced at a basic application level; there is no full admin moderation console yet.

These boundaries are deliberate. The focus is demonstrating product architecture and the complete pet-identity concept without pretending unfinished infrastructure already exists.

## Engineering highlights

- Pet-first identity model with owner authentication separated from the public pet persona
- Persistent PostgreSQL-backed social and care data
- Server-side session ownership checks for sensitive actions
- Privacy-aware profile and post access
- Pet-to-pet messaging and unread state
- Play-date request lifecycle
- Private Pet Health Passport
- Communities and meetup discovery
- Compatibility-based discovery
- Block/report safety controls
- End-to-end backend smoke testing
- Responsive frontend UX and unified design system
- Docker and Render production configuration
- GitHub Actions CI

## Author

**Bhavana Angadi**

Java full-stack project focused on building a cohesive pet social, care, and community platform around a persistent pet identity.
