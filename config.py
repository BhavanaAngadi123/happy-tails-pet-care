import os
import secrets

from dotenv import load_dotenv

load_dotenv()

SECRET_KEY = os.getenv("SECRET_KEY") or secrets.token_hex(32)

# Database: use a managed PostgreSQL DATABASE_URL in production.
# Vercel's application directory is read-only, so the development fallback
# must use /tmp rather than Flask's default instance directory.
database_url = os.getenv("DATABASE_URL")
if not database_url:
    if os.getenv("VERCEL"):
        database_url = "sqlite:////tmp/happy.db"
    else:
        database_url = "sqlite:///instance/happy.db"

# Normalize PostgreSQL URLs to the installed psycopg v3 driver.
if database_url.startswith("postgres://"):
    database_url = database_url.replace("postgres://", "postgresql+psycopg://", 1)
elif database_url.startswith("postgresql://"):
    database_url = database_url.replace("postgresql://", "postgresql+psycopg://", 1)

SQLALCHEMY_DATABASE_URI = database_url
SQLALCHEMY_TRACK_MODIFICATIONS = False
SQLALCHEMY_ENGINE_OPTIONS = {
    "pool_pre_ping": True,
}

# Local uploads remain supported for development. Production deployments should
# use Supabase Storage through the upload helper.
UPLOAD_FOLDER = os.getenv(
    "UPLOAD_FOLDER",
    "/tmp/uploads" if os.getenv("VERCEL") else os.path.join(os.getcwd(), "static", "uploads"),
)

MAIL_SERVER = os.getenv("MAIL_SERVER", "smtp.gmail.com")
MAIL_PORT = int(os.getenv("MAIL_PORT", 587))
MAIL_USE_TLS = os.getenv("MAIL_USE_TLS", "True").lower() == "true"
MAIL_USE_SSL = os.getenv("MAIL_USE_SSL", "False").lower() == "true"
MAIL_USERNAME = os.getenv("MAIL_USERNAME")
MAIL_PASSWORD = os.getenv("MAIL_PASSWORD")
MAIL_DEFAULT_SENDER = os.getenv("MAIL_DEFAULT_SENDER", MAIL_USERNAME)
