from flask import Blueprint, render_template, request, redirect, url_for, session, flash
from utils.db import db
from models.pet import Pet
from models.product import Product
from models.playdate import Playdate
from models.user import User
from models.lost_pet import LostPet
from models.sighting import Sighting
from models.pricing import PricingRule
from models.sitter import Sitter, SitterReview
from models.availability import Availability
from models.booking import Booking
from models.order import Order
from models.ProductReview import ProductReview
from config import UPLOAD_FOLDER
from utils.helpers import save_file
import os
from datetime import datetime

owner_bp = Blueprint("owner", __name__, url_prefix="/owner")

def check_login():
    user_id = session.get("user_id")
    role = session.get("user_role")
    if not user_id:
        flash("Please log in first!", "danger")
        return None, None
    return int(user_id), role

@owner_bp.route("/dashboard")
def dashboard():
    user_id, role = check_login()
    if not user_id:
        return redirect(url_for("auth.login"))
    my_pets = Pet.query.filter_by(owner_id=user_id).all()
    other_pets = Pet.query.filter(Pet.owner_id != user_id).all()
    return render_template("dashboard_owner.html", my_pets=my_pets, other_pets=other_pets, role=role)

@owner_bp.route("/pets")
def pets():
    user_id, role = check_login()
    if not user_id:
        return redirect(url_for("auth.login"))
    pets = Pet.query.filter_by(owner_id=user_id).all()
    return render_template("owner_pets.html", pets=pets, role=role)

@owner_bp.route("/pet/<int:pet_id>")
def pet_detail(pet_id):
    user_id, role = check_login()
    if not user_id:
        return redirect(url_for("auth.login"))
    pet = Pet.query.filter_by(id=pet_id, owner_id=user_id).first_or_404()
    return render_template("owner_pet_detail.html", pet=pet, role=role)

@owner_bp.route("/pet/add", methods=["GET", "POST"])
def add_pet():
    user_id, role = check_login()
    if not user_id:
        return redirect(url_for("auth.login"))
    if request.method == "POST":
        name = request.form["name"]
        species = request.form.get("species", "Other")
        breed = request.form["breed"]
        age = request.form["age"]
        medical_history = request.form.get("medical_history")
        image_file = request.files.get("image")
        filename = None
        if image_file and image_file.filename:
            os.makedirs(UPLOAD_FOLDER, exist_ok=True)
            filename = save_file(image_file, UPLOAD_FOLDER)
        pet = Pet(name=name, species=species, breed=breed, age=age, medical_history=medical_history, image=filename, owner_id=user_id)
        db.session.add(pet)
        db.session.commit()
        flash("Pet added successfully!", "success")
        return redirect(url_for("owner.pets"))
    return render_template("owner_edit_pet.html", pet=None, role=role)

@owner_bp.route("/pet/edit/<int:pet_id>", methods=["GET", "POST"])
def edit_pet(pet_id):
    user_id, role = check_login()
    if not user_id:
        return redirect(url_for("auth.login"))
    pet = Pet.query.filter_by(id=pet_id, owner_id=user_id).first_or_404()
    if request.method == "POST":
        pet.name = request.form["name"]
        pet.species = request.form.get("species", pet.species)
        pet.breed = request.form["breed"]
        pet.age = request.form["age"]
        pet.medical_history = request.form.get("medical_history")
        image_file = request.files.get("image")
        if image_file and image_file.filename:
            os.makedirs(UPLOAD_FOLDER, exist_ok=True)
            pet.image = save_file(image_file, UPLOAD_FOLDER)
        db.session.commit()
        flash("Pet updated successfully!", "success")
        return redirect(url_for("owner.pets"))
    return render_template("owner_edit_pet.html", pet=pet, role=role)

@owner_bp.route("/pet/delete/<int:pet_id>")
def delete_pet(pet_id):
    user_id, role = check_login()
    if not user_id:
        return redirect(url_for("auth.login"))
    pet = Pet.query.filter_by(id=pet_id, owner_id=user_id).first_or_404()
    db.session.delete(pet)
    db.session.commit()
    flash("Pet deleted successfully!", "success")
    return redirect(url_for("owner.pets"))

@owner_bp.route("/playdates", methods=["GET", "POST"])
def playdates():
    user_id, role = check_login()
    if not user_id:
        return redirect(url_for("auth.login"))
    my_pets = Pet.query.filter_by(owner_id=user_id).all()
    other_pets = Pet.query.filter(Pet.owner_id != user_id).all()
    if request.method == "POST":
        try:
            pet_id = int(request.form["pet_id"])
            invitee_pet_id = int(request.form["invitee_pet_id"])
            date = request.form["date"]
            time = request.form["time"]
            location = request.form["location"]
            invitee_pet = Pet.query.get(invitee_pet_id)
            if not invitee_pet:
                flash("Selected pet does not exist!", "danger")
                return redirect(url_for("owner.playdates"))
            new_playdate = Playdate(owner_id=user_id, pet_id=pet_id, invitee_owner_id=invitee_pet.owner_id, invitee_pet_id=invitee_pet_id, date=date, time=time, location=location, status="Pending")
            db.session.add(new_playdate)
            db.session.commit()
            flash("Playdate requested successfully!", "success")
        except Exception as e:
            db.session.rollback()
            flash(f"Error creating playdate: {str(e)}", "danger")
        return redirect(url_for("owner.playdates"))
    my_playdates = Playdate.query.filter((Playdate.owner_id == user_id) | (Playdate.invitee_owner_id == user_id)).order_by(Playdate.date.asc(), Playdate.time.asc()).all()
    return render_template("owner_playdates.html", my_pets=my_pets, other_pets=other_pets, playdates=my_playdates, user_id=user_id, role=role)

@owner_bp.route("/playdates/accept/<int:playdate_id>")
def accept_playdate(playdate_id):
    user_id, role = check_login()
    if not user_id:
        return redirect(url_for("auth.login"))
    playdate = Playdate.query.get_or_404(playdate_id)
    if user_id != playdate.invitee_owner_id:
        flash("You cannot accept this playdate.", "danger")
        return redirect(url_for("owner.playdates"))
    playdate.status = "Accepted"
    db.session.commit()
    flash("Playdate accepted!", "success")
    return redirect(url_for("owner.playdates"))

@owner_bp.route("/playdates/reject/<int:playdate_id>")
def reject_playdate(playdate_id):
    user_id, role = check_login()
    if not user_id:
        return redirect(url_for("auth.login"))
    playdate = Playdate.query.get_or_404(playdate_id)
    if user_id != playdate.invitee_owner_id:
        flash("You cannot reject this playdate.", "danger")
        return redirect(url_for("owner.playdates"))
    playdate.status = "Declined"
    db.session.commit()
    flash("Playdate declined.", "warning")
    return redirect(url_for("owner.playdates"))

@owner_bp.route("/playdates/delete/<int:playdate_id>")
def delete_playdate(playdate_id):
    user_id, role = check_login()
    if not user_id:
        return redirect(url_for("auth.login"))
    playdate = Playdate.query.filter_by(id=playdate_id, owner_id=user_id).first()
    if not playdate:
        flash("Playdate not found or you are not authorized!", "danger")
        return redirect(url_for("owner.playdates"))
    if playdate.status != "Pending":
        flash("You can only delete pending requests!", "warning")
        return redirect(url_for("owner.playdates"))
    db.session.delete(playdate)
    db.session.commit()
    return redirect(url_for("owner.playdates"))

@owner_bp.route("/lost-found")
def lost_found():
    user_id, role = check_login()
    if not user_id: return redirect(url_for("auth.login"))
    my_lost_pets = LostPet.query.filter_by(owner_id=user_id).order_by(LostPet.created_at.desc()).all()
    other_lost_pets = LostPet.query.filter(LostPet.owner_id != user_id).order_by(LostPet.created_at.desc()).all()
    return render_template("owner_lost_found.html", my_lost_pets=my_lost_pets, other_lost_pets=other_lost_pets, role=role)

@owner_bp.route("/lost-pet-alerts")
def lost_pet_alerts():
    user_id, role = check_login()
    if not user_id: return redirect(url_for("auth.login"))
    alerts = LostPet.query.filter_by(owner_id=user_id).order_by(LostPet.created_at.desc()).all()
    for pet in alerts:
        sightings_list = pet.sightings.filter_by(status="Pending").order_by(Sighting.created_at.desc()).all()
        pet.sightings_count = len(sightings_list)
        pet.sightings_to_show = sightings_list
    return render_template("owner_lost_pet_alerts.html", alerts=alerts, role=role)

@owner_bp.route("/report-lost-pet", methods=["GET", "POST"])
def report_lost_pet():
    user_id, role = check_login()
    if not user_id: return redirect(url_for("auth.login"))
    if request.method == "POST":
        image_file = request.files.get("image")
        filename = save_file(image_file, UPLOAD_FOLDER) if image_file and image_file.filename else None
        new_lost_pet = LostPet(owner_id=user_id, name=request.form.get("pet_name"), type=request.form.get("pet_type"), breed=request.form.get("breed"), color=request.form.get("color"), last_seen=request.form.get("last_seen"), description=request.form.get("description"), status="Lost", reward=request.form.get("reward", type=float), image=filename, created_at=datetime.utcnow())
        db.session.add(new_lost_pet)
        db.session.commit()
        return redirect(url_for("owner.lost_found"))
    return render_template("owner_report_lost_pet.html", role=role)

@owner_bp.route("/lost-pet/<int:pet_id>")
def view_lost_pet(pet_id):
    user_id, role = check_login()
    if not user_id: return redirect(url_for("auth.login"))
    pet = LostPet.query.get_or_404(pet_id)
    sightings = Sighting.query.filter_by(pet_id=pet_id).order_by(Sighting.created_at.desc()).all()
    return render_template("owner_view_lost_pet.html", pet=pet, sightings=sightings, role=role)

@owner_bp.route("/mark-found/<int:pet_id>", methods=["POST"])
def mark_found(pet_id):
    user_id, role = check_login()
    if not user_id: return redirect(url_for("auth.login"))
    pet = LostPet.query.filter_by(id=pet_id, owner_id=user_id).first_or_404()
    pet.status = "Found"
    db.session.commit()
    return redirect(url_for("owner.lost_found"))

@owner_bp.route("/reviews")
def reviews():
    user_id, role = check_login()
    if not user_id: return redirect(url_for("auth.login"))
    return render_template("owner_reviews.html", sitter_reviews=SitterReview.query.join(Sitter).all(), role=role)

@owner_bp.route("/subscription")
def subscription():
    user_id, role = check_login()
    if not user_id: return redirect(url_for("auth.login"))
    subscriptions = [{"plan":"Basic","price":0,"status":"Active"},{"plan":"Premium","price":49,"status":"Inactive"},{"plan":"Gold","price":99,"status":"Inactive"}]
    return render_template("owner_subscriptions.html", subscriptions=subscriptions, role=role)

@owner_bp.route("/sitters")
def find_sitters():
    user_id, role = check_login()
    if not user_id: return redirect(url_for("auth.login"))
    sitter_data=[]
    for sitter in Sitter.query.filter_by(verification_status="approved").all():
        availabilities=Availability.query.filter_by(sitter_id=sitter.id).all()
        for slot in availabilities:
            slot.start_dt=datetime.combine(slot.date,slot.start_time); slot.end_dt=datetime.combine(slot.date,slot.end_time)
            slot.booking=Booking.query.filter(Booking.sitter_id==sitter.id,Booking.start_date<slot.end_dt,Booking.end_date>slot.start_dt).first()
            slot.start_dt_str=slot.start_dt.strftime("%Y-%m-%d %H:%M"); slot.end_dt_str=slot.end_dt.strftime("%Y-%m-%d %H:%M")
        sitter_data.append({"sitter":sitter,"availabilities":availabilities})
    return render_template("owner_sitters.html", sitter_data=sitter_data, pets=Pet.query.filter_by(owner_id=user_id).all(), role=role)

@owner_bp.route("/sitter/<int:sitter_id>")
def view_sitter_profile(sitter_id):
    user_id, role=check_login()
    if not user_id:return redirect(url_for("auth.login"))
    sitter=Sitter.query.get_or_404(sitter_id); reviews=SitterReview.query.filter_by(sitter_id=sitter_id).all(); availabilities=Availability.query.filter_by(sitter_id=sitter.id).all()
    for slot in availabilities:
        slot.start_dt=datetime.combine(slot.date,slot.start_time); slot.end_dt=datetime.combine(slot.date,slot.end_time); slot.start_dt_str=slot.start_dt.strftime("%Y-%m-%d %H:%M"); slot.end_dt_str=slot.end_dt.strftime("%Y-%m-%d %H:%M")
        slot.booking=Booking.query.filter(Booking.sitter_id==sitter.id,Booking.start_date<slot.end_dt,Booking.end_date>slot.start_dt).first()
    return render_template("owner_sitter_profile.html",sitter=sitter,reviews=reviews,availabilities=availabilities,role=role)

@owner_bp.route("/book_sitter",methods=["POST"])
def book_sitter():
    user_id,role=check_login()
    if not user_id:return redirect(url_for("auth.login"))
    try:
        sitter_id=int(request.form.get("sitter_id")); pet_id=int(request.form.get("pet_id")); start_dt=datetime.strptime(request.form.get("start_date"),"%Y-%m-%d %H:%M"); end_dt=datetime.strptime(request.form.get("end_date"),"%Y-%m-%d %H:%M")
    except (ValueError,TypeError):
        flash("Invalid booking data.","danger"); return redirect(url_for("owner.find_sitters"))
    pet=Pet.query.filter_by(id=pet_id,owner_id=user_id).first()
    if not pet or end_dt <= start_dt:
        flash("Invalid pet or booking time.","danger"); return redirect(url_for("owner.find_sitters"))
    if Booking.query.filter(Booking.sitter_id==sitter_id,Booking.start_date<end_dt,Booking.end_date>start_dt).first():
        flash("This slot is already booked!","warning"); return redirect(url_for("owner.find_sitters"))
    db.session.add(Booking(pet_id=pet_id,sitter_id=sitter_id,start_date=start_dt,end_date=end_dt,status="pending")); db.session.commit()
    return redirect(url_for("owner.find_sitters"))

@owner_bp.route("/my_bookings")
def my_bookings():
    user_id,role=check_login()
    if not user_id:return redirect(url_for("auth.login"))
    return render_template("owner_my_bookings.html",bookings=Booking.query.join(Pet).filter(Pet.owner_id==user_id).all(),role=role)

@owner_bp.route("/rate_sitter/<int:sitter_id>")
def rate_sitter(sitter_id):
    user_id,role=check_login()
    if not user_id:return redirect(url_for("auth.login"))
    sitter=Sitter.query.get_or_404(sitter_id); existing=SitterReview.query.filter_by(sitter_id=sitter_id,owner_id=user_id).first()
    return render_template("owner_rate_sitter.html",sitter=sitter,current_rating=existing.rating if existing else 0,current_review_text=existing.review_text if existing else "",role=role)

@owner_bp.route("/submit_rating/<int:sitter_id>",methods=["POST"])
def submit_rating(sitter_id):
    user_id,role=check_login()
    if not user_id:return redirect(url_for("auth.login"))
    try: rating=int(request.form.get("rating",0))
    except ValueError: rating=0
    if rating not in range(1,6):return redirect(url_for("owner.rate_sitter",sitter_id=sitter_id))
    review=SitterReview.query.filter_by(sitter_id=sitter_id,owner_id=user_id).first()
    if review: review.rating=rating; review.review_text=request.form.get("review","").strip()
    else: db.session.add(SitterReview(sitter_id=sitter_id,owner_id=user_id,rating=rating,review_text=request.form.get("review","").strip()))
    db.session.commit(); return redirect(url_for("owner.find_sitters"))

@owner_bp.route("/shop")
def shop():
    user_id,role=check_login()
    if not user_id:return redirect(url_for("auth.login"))
    return render_template("owner_shop.html",products=Product.query.all(),role=role)

@owner_bp.route("/product/<int:product_id>")
def product_detail(product_id):
    user_id,role=check_login()
    if not user_id:return redirect(url_for("auth.login"))
    return render_template("owner_product_detail.html",product=Product.query.get_or_404(product_id),role=role)

@owner_bp.route("/add-to-cart/<int:product_id>",methods=["POST"])
def add_to_cart(product_id):
    user_id,role=check_login()
    if not user_id:return redirect(url_for("auth.login"))
    product=Product.query.get_or_404(product_id)
    try: quantity=max(1,int(request.form.get("quantity",1)))
    except ValueError: quantity=1
    cart_item=Order.query.filter_by(buyer_id=user_id,product_id=product_id,status="cart").first(); new_quantity=quantity+(cart_item.quantity if cart_item else 0)
    if new_quantity>product.stock: flash("Quantity exceeds available stock!","danger"); return redirect(url_for("owner.product_detail",product_id=product_id))
    if cart_item: cart_item.quantity=new_quantity; cart_item.total_price=new_quantity*product.price
    else: db.session.add(Order(buyer_id=user_id,product_id=product_id,quantity=quantity,total_price=quantity*product.price,status="cart"))
    db.session.commit(); return redirect(url_for("owner.cart"))

@owner_bp.route("/cart")
def cart():
    user_id,role=check_login()
    if not user_id:return redirect(url_for("auth.login"))
    return render_template("cart.html",cart_items=Order.query.filter_by(buyer_id=user_id,status="cart").all(),role=role)

@owner_bp.route("/remove-from-cart/<int:product_id>",methods=["POST"])
def remove_from_cart(product_id):
    user_id,role=check_login()
    if not user_id:return redirect(url_for("auth.login"))
    item=Order.query.filter_by(buyer_id=user_id,product_id=product_id,status="cart").first()
    if item: db.session.delete(item); db.session.commit()
    return redirect(url_for("owner.cart"))

@owner_bp.route("/checkout",methods=["POST"])
def checkout():
    user_id,role=check_login()
    if not user_id:return redirect(url_for("auth.login"))
    items=Order.query.filter_by(buyer_id=user_id,status="cart").all()
    if not items:return redirect(url_for("owner.cart"))
    for item in items: item.status="pending"; item.ordered_at=datetime.utcnow()
    db.session.commit(); return redirect(url_for("owner.orders"))

@owner_bp.route("/place-order/<int:product_id>",methods=["POST"])
def place_order(product_id):
    user_id,role=check_login()
    if not user_id:return redirect(url_for("auth.login"))
    product=Product.query.get_or_404(product_id); db.session.add(Order(buyer_id=user_id,product_id=product.id,quantity=1,total_price=product.price,status="ordered")); db.session.commit(); return redirect(url_for("owner.orders"))

@owner_bp.route("/orders")
def orders():
    user_id,role=check_login()
    if not user_id:return redirect(url_for("auth.login"))
    return render_template("orders.html",orders=Order.query.filter_by(buyer_id=user_id).filter(Order.status!="cart").all(),role=role)

@owner_bp.route("/product_reviews",methods=["GET","POST"])
def product_reviews():
    user_id,role=check_login()
    if not user_id:return redirect(url_for("auth.login"))
    purchased_orders=Order.query.filter(Order.buyer_id==user_id,Order.status!="cart").all()
    for order in purchased_orders:
        order.reviews=ProductReview.query.filter_by(product_id=order.product_id).all(); order.user_review=ProductReview.query.filter_by(product_id=order.product_id,user_id=user_id).first(); order.show_form=False
    if request.method=="POST":
        action=request.form.get("action")
        try: product_id=int(request.form.get("product_id"))
        except (ValueError,TypeError): return redirect(url_for("owner.product_reviews"))
        if action=="show_form":
            for order in purchased_orders: order.show_form=(order.product_id==product_id)
        elif action=="submit_review":
            try: rating=int(request.form.get("rating",0))
            except ValueError: rating=0
            if rating not in range(1,6): return redirect(url_for("owner.product_reviews"))
            review=ProductReview.query.filter_by(product_id=product_id,user_id=user_id).first()
            if review: review.rating=rating; review.review_text=request.form.get("review","").strip()
            else: db.session.add(ProductReview(product_id=product_id,user_id=user_id,rating=rating,review_text=request.form.get("review","").strip()))
            db.session.commit(); return redirect(url_for("owner.product_reviews"))
    return render_template("owner_product_reviews.html",orders=purchased_orders,role=role)
