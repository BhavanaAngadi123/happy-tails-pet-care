import os

os.environ.setdefault("DATABASE_URL", "sqlite:///:memory:")
os.environ.setdefault("SECRET_KEY", "test-secret-key")

from app import app
from models.user import User
from routes.admin import build_dashboard_analytics
from utils.db import db


def setup_function():
    app.config.update(TESTING=True, SQLALCHEMY_DATABASE_URI="sqlite:///:memory:")
    with app.app_context():
        db.drop_all()
        db.create_all()


def teardown_function():
    with app.app_context():
        db.session.remove()
        db.drop_all()


def test_login_page_loads():
    client = app.test_client()
    response = client.get("/auth/login")
    assert response.status_code == 200


def test_public_registration_cannot_create_admin():
    client = app.test_client()
    response = client.post(
        "/auth/register",
        data={
            "name": "Test User",
            "email": "test@example.com",
            "password": "strong-password",
            "role": "admin",
        },
        follow_redirects=False,
    )
    assert response.status_code == 302
    with app.app_context():
        user = User.query.filter_by(email="test@example.com").first()
        assert user is not None
        assert user.role == "owner"


def test_admin_dashboard_requires_login():
    client = app.test_client()
    response = client.get("/admin/dashboard", follow_redirects=False)
    assert response.status_code == 302
    assert "/auth/login" in response.headers["Location"]


def test_empty_dashboard_analytics_are_safe():
    analytics = build_dashboard_analytics([], [], [], [], [])
    assert analytics["kpis"]["owners"] == 0
    assert analytics["kpis"]["bookings"] == 0
    assert analytics["kpis"]["completion_rate"] == 0
    assert analytics["monthly_bookings"]["labels"] == []
