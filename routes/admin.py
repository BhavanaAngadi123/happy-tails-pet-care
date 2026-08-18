from collections import Counter
from functools import wraps

from flask import Blueprint, flash, redirect, render_template, session, url_for
from sqlalchemy.orm import joinedload

from models.booking import Booking
from models.pet import Pet
from models.product import Product
from models.sitter import Sitter
from models.user import User
from utils.db import db

admin_bp = Blueprint("admin", __name__, url_prefix="/admin")


def admin_required(f):
    @wraps(f)
    def decorated(*args, **kwargs):
        if "user_id" not in session or session.get("user_role") != "admin":
            flash("Please login as admin first!", "danger")
            return redirect(url_for("auth.login"))
        return f(*args, **kwargs)

    return decorated


def build_dashboard_analytics(users, pets, products, bookings, sitters):
    """Create presentation-ready KPIs and chart data from application records."""
    booking_status_counts = Counter((booking.status or "unknown").lower() for booking in bookings)
    pet_species_counts = Counter((pet.species or "Unknown").title() for pet in pets)
    sitter_status_counts = Counter(
        (sitter.verification_status or "pending").lower() for sitter in sitters
    )

    monthly_bookings = Counter()
    for booking in bookings:
        if booking.created_at:
            monthly_bookings[booking.created_at.strftime("%Y-%m")] += 1

    recent_months = sorted(monthly_bookings.keys())[-6:]
    completed_bookings = booking_status_counts.get("completed", 0)
    total_bookings = len(bookings)
    completion_rate = round((completed_bookings / total_bookings) * 100, 1) if total_bookings else 0

    return {
        "kpis": {
            "owners": sum(1 for user in users if user.role == "owner"),
            "sitters": len(sitters),
            "pets": len(pets),
            "products": len(products),
            "bookings": total_bookings,
            "completion_rate": completion_rate,
        },
        "booking_status": {
            "labels": [label.title() for label in booking_status_counts.keys()],
            "values": list(booking_status_counts.values()),
        },
        "pet_species": {
            "labels": list(pet_species_counts.keys()),
            "values": list(pet_species_counts.values()),
        },
        "sitter_status": {
            "labels": [label.title() for label in sitter_status_counts.keys()],
            "values": list(sitter_status_counts.values()),
        },
        "monthly_bookings": {
            "labels": recent_months,
            "values": [monthly_bookings[month] for month in recent_months],
        },
    }


@admin_bp.route("/dashboard")
@admin_required
def dashboard():
    users = User.query.all()
    pets = Pet.query.options(joinedload(Pet.owner)).all()
    products = Product.query.options(joinedload(Product.seller)).all()
    bookings = Booking.query.options(joinedload(Booking.pet), joinedload(Booking.sitter)).all()
    sitters = Sitter.query.all()
    analytics = build_dashboard_analytics(users, pets, products, bookings, sitters)

    return render_template(
        "dashboard_admin.html",
        users=users,
        pets=pets,
        products=products,
        bookings=bookings,
        sitters=sitters,
        analytics=analytics,
    )


@admin_bp.route("/sitters")
@admin_required
def sitter_list():
    sitters = Sitter.query.all()
    return render_template("admin_sitter_list.html", sitters=sitters)


@admin_bp.route("/sitter/<int:sitter_id>")
@admin_required
def sitter_profile(sitter_id):
    sitter = Sitter.query.get_or_404(sitter_id)
    return render_template("admin_sitter_profile.html", sitter=sitter)


@admin_bp.route("/verify-sitter/<int:sitter_id>/<action>", methods=["POST"])
@admin_required
def verify_sitter(sitter_id, action):
    sitter = Sitter.query.get_or_404(sitter_id)
    if action not in {"approved", "rejected"}:
        flash("Invalid action", "danger")
        return redirect(url_for("admin.sitter_list"))
    sitter.verification_status = action
    sitter.verified = action == "approved"
    db.session.commit()
    flash(f"Sitter {action} successfully!", "success")
    return redirect(url_for("admin.sitter_profile", sitter_id=sitter.id))


@admin_bp.route("/delete-user/<int:user_id>", methods=["POST"])
@admin_required
def delete_user(user_id):
    user = User.query.get_or_404(user_id)
    if user.id == session.get("user_id"):
        flash("You cannot delete your own admin account.", "danger")
        return redirect(url_for("admin.dashboard"))
    db.session.delete(user)
    db.session.commit()
    flash("User deleted successfully!", "success")
    return redirect(url_for("admin.dashboard"))


@admin_bp.route("/delete-pet/<int:pet_id>", methods=["POST"])
@admin_required
def delete_pet(pet_id):
    pet = Pet.query.get_or_404(pet_id)
    db.session.delete(pet)
    db.session.commit()
    flash("Pet deleted successfully!", "success")
    return redirect(url_for("admin.dashboard"))


@admin_bp.route("/delete-product/<int:product_id>", methods=["POST"])
@admin_required
def delete_product(product_id):
    product = Product.query.get_or_404(product_id)
    db.session.delete(product)
    db.session.commit()
    flash("Product deleted successfully!", "success")
    return redirect(url_for("admin.dashboard"))


@admin_bp.route("/delete-sitter/<int:sitter_id>", methods=["POST"])
@admin_required
def delete_sitter(sitter_id):
    sitter = Sitter.query.get_or_404(sitter_id)
    db.session.delete(sitter)
    db.session.commit()
    flash("Sitter deleted successfully!", "success")
    return redirect(url_for("admin.dashboard"))
