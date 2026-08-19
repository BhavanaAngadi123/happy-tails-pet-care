from flask import Blueprint, flash, redirect, render_template, request, session, url_for

from models.user import User
from utils.db import db

auth_bp = Blueprint("auth", __name__, url_prefix="/auth")


@auth_bp.route("/register", methods=["GET", "POST"])
def register():
    if request.method == "POST":
        name = request.form["name"].strip()
        email = request.form["email"].strip().lower()
        password = request.form["password"]
        role = request.form.get("role", "owner")

        # Public registration must never be able to create an admin account.
        if role not in {"owner", "sitter"}:
            role = "owner"

        if User.query.filter_by(email=email).first():
            flash("Email already registered!", "danger")
            return redirect(url_for("auth.register"))

        user = User(name=name, email=email, role=role)
        user.set_password(password)
        db.session.add(user)
        db.session.flush()

        if role == "sitter":
            from models.sitter import Sitter

            sitter = Sitter(id=user.id, name=name, email=email)
            db.session.add(sitter)

        db.session.commit()

        flash("Registered successfully! Please login.", "success")
        return redirect(url_for("auth.login"))

    return render_template("register.html")


@auth_bp.route("/login", methods=["GET", "POST"])
def login():
    if request.method == "POST":
        email = request.form["email"].strip().lower()
        password = request.form["password"]

        user = User.query.filter_by(email=email).first()
        if user and user.check_password(password):
            session.clear()
            session["user_id"] = user.id
            session["user_role"] = user.role
            # Backward compatibility for older routes that still read "role".
            session["role"] = user.role
            session["user_name"] = user.name
            flash(f"Welcome {user.name}!", "success")

            if user.role == "admin":
                return redirect(url_for("admin.dashboard"))
            if user.role == "sitter":
                return redirect(url_for("sitter.dashboard"))
            if user.role == "owner":
                return redirect(url_for("owner.dashboard"))
            return redirect(url_for("main.dashboard"))

        flash("Invalid email or password!", "danger")
        return redirect(url_for("auth.login"))

    return render_template("login.html")


@auth_bp.route("/logout")
def logout():
    session.clear()
    flash("Logged out successfully!", "success")
    return redirect(url_for("auth.login"))


@auth_bp.route("/forgot-password", methods=["GET", "POST"])
def forgot_password():
    if request.method == "POST":
        # Password-reset email delivery is not implemented yet. Do not claim
        # that a reset link was sent or reveal whether an account exists.
        flash(
            "Password reset is currently under development. Please contact support for account access.",
            "info",
        )
        return redirect(url_for("auth.login"))

    return render_template("forgot_password.html")
