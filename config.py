import os
import secrets

from dotenv import load_dotenv

load_dotenv()

SECRET_KEY = os.getenv("SECRET_KEY") or secrets.token_hex(32)

# Database: use a managed PostgreSQL DATABASE_URL in production.
database_url = os.getenv("DATABASE_URL", "sqlite:///instance/happy.db")
# Some providers still expose the legacy postgres:// scheme.
if database_url.startswith("postgres://"):
    database_url = database_url.replace("postgres://", "postgresql://", 1)
SQLALCHEMY_DATABASE_URI = database_url
SQLALCHEMY_TRACK_MODIFICATIONS = False
SQLALCHEMY_ENGINE_OPTIONS = {
    "pool_pre_ping": True,
}

# Local uploads remain supported for development. Production deployments should
# move user uploads to durable object storage rather than ephemeral app disk.
UPLOAD_FOLDER = os.getenv(
    "UPLOAD_FOLDER",
    os.path.join(os.getcwd(), "static", "uploads"),
)

MAIL_SERVER = os.getenv("MAIL_SERVER", "smtp.gmail.com")
MAIL_PORT = int(os.getenv("MAIL_PORT", 587))
MAIL_USE_TLS = os.getenv("MAIL_USE_TLS", "True").lower() == "true"
MAIL_USE_SSL = os.getenv("MAIL_USE_SSL", "False").lower() == "true"
MAIL_USERNAME = os.getenv("MAIL_USERNAME")
MAIL_PASSWORD = os.getenv("MAIL_PASSWORD")
MAIL_DEFAULT_SENDER = os.getenv("MAIL_DEFAULT_SENDER", MAIL_USERNAME)
