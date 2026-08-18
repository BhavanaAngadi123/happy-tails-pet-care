# Happy Tails 🐾

### Pet Care Management System

Happy Tails is a full-stack web application designed to bring common pet-care activities into one platform. It supports pet owners, pet sitters, sellers, and community members through pet profiles, social interactions, sitter bookings, shopping, and lost-and-found workflows.

This project demonstrates backend development with Python and Flask, relational database design with MySQL and SQLAlchemy, server-rendered web interfaces, authentication-oriented application structure, email integration, and modular application design.

## Key Features

- **Pet Profiles & Social Feed** — Create pet profiles and share community posts.
- **Pet Sitting & Bookings** — Support sitter availability and pet-care booking workflows.
- **Pet Commerce** — Browse products and manage shopping/order workflows.
- **Lost & Found** — Report missing pets and share sightings with the community.
- **Email Notifications** — SMTP-based application notifications.
- **Modular Architecture** — Separate routes, models, templates, static resources, configuration, and utilities for maintainability.

## Tech Stack

| Area | Technologies |
| --- | --- |
| Backend | Python, Flask |
| Database | MySQL, SQLAlchemy |
| Frontend | HTML, CSS, JavaScript, Jinja Templates |
| Email | Flask-Mail, SMTP |
| Configuration | python-dotenv |
| Database Management | Flask-Migrate |
| Version Control | Git, GitHub |

## Architecture

The application follows a modular Flask structure with dedicated components for routing, database models, reusable utilities, templates, and static assets.

```text
Angadi_Bhavana_COMP_699_C/
├── app.py
├── config.py
├── init_db.py
├── models/
├── routes/
├── static/
├── templates/
├── utils/
├── requirements.txt
└── .env.example
```

### Application Package Diagram

<img width="1199" height="778" alt="Happy Tails application package diagram" src="https://github.com/user-attachments/assets/9f0617b1-52d4-4e2a-a398-becf7b48b44d" />

## Getting Started

### Prerequisites

Install:

- Python 3.10+
- pip
- MySQL Server 8.0+
- Git

### 1. Clone the repository

```bash
git clone https://github.com/BhavanaAngadi123/Angadi_Bhavana_COMP_699_C.git
cd Angadi_Bhavana_COMP_699_C
```

### 2. Create a virtual environment

**Windows**

```bash
python -m venv venv
venv\Scripts\activate
```

**macOS/Linux**

```bash
python3 -m venv venv
source venv/bin/activate
```

### 3. Install dependencies

```bash
pip install -r requirements.txt
```

### 4. Configure environment variables

Copy `.env.example` to `.env` and replace the placeholder values with your local configuration.

```bash
cp .env.example .env
```

Example database configuration:

```text
DATABASE_URL=mysql+pymysql://USERNAME:PASSWORD@127.0.0.1:3306/happytails
```

Never commit your `.env` file or real credentials to source control.

### 5. Create the database

In MySQL:

```sql
CREATE DATABASE happytails;
```

Initialize the application tables:

```bash
python init_db.py
```

### 6. Run the application

```bash
python app.py
```

Open `http://127.0.0.1:5000` in your browser.

## Engineering Highlights

- Organized Flask application into reusable models, routes, utilities, templates, and configuration modules.
- Used SQLAlchemy for relational data access and MySQL persistence.
- Implemented environment-based configuration to keep application settings separate from source code.
- Integrated email functionality through Flask-Mail and SMTP.
- Designed the system around multiple user workflows rather than a single-purpose CRUD interface.

## Security

Sensitive values such as database credentials, application secrets, and email passwords must be stored locally in `.env`. The repository includes `.env.example` only as a configuration template.

## Future Improvements

- Add automated unit and integration tests.
- Add CI checks with GitHub Actions.
- Containerize the application with Docker.
- Add production deployment configuration.
- Expand API documentation and application screenshots.

## Author

**Bhavana Angadi**

Graduate software development project focused on building a practical, multi-module pet-care web platform with Python, Flask, and MySQL.
