"""Create the HappyTails database schema.

Run this deliberately against a configured DATABASE_URL. This imports every
model module before db.create_all() so SQLAlchemy knows about the full schema.
For long-lived production changes, use Flask-Migrate migrations instead.
"""

import os

if not os.getenv("DATABASE_URL"):
    raise RuntimeError("DATABASE_URL must be set before running init_db.py")

from app import app
from utils.db import db

# Import every model module so all SQLAlchemy tables are registered.
from models.ProductReview import ProductReview  # noqa: F401,E402
from models.availability import Availability  # noqa: F401,E402
from models.booking import Booking  # noqa: F401,E402
from models.campaign import Campaign  # noqa: F401,E402
from models.cart import Cart  # noqa: F401,E402
from models.lost_pet import LostPet  # noqa: F401,E402
from models.message import Message  # noqa: F401,E402
from models.order import Order  # noqa: F401,E402
from models.pet import Pet  # noqa: F401,E402
from models.playdate import Playdate  # noqa: F401,E402
from models.pricing import PricingRule  # noqa: F401,E402
from models.product import Product  # noqa: F401,E402
from models.sighting import Sighting  # noqa: F401,E402
from models.sitter import Sitter, SitterReview  # noqa: F401,E402
from models.user import User  # noqa: F401,E402

with app.app_context():
    db.create_all()
    print("Database tables created successfully.")
