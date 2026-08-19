package com.happytails.social;

import jakarta.servlet.http.HttpSession;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Controller
public class AppPageController {
  @GetMapping({"/","/app"})
  public ResponseEntity<?> app(HttpSession session) throws IOException {
    Object owner=session.getAttribute("ownerId");
    if(owner==null)return ResponseEntity.status(HttpStatus.FOUND).header(HttpHeaders.LOCATION,"/login.html").build();
    Object active=session.getAttribute("activePetId");
    if(!(active instanceof Long))return ResponseEntity.status(HttpStatus.FOUND).header(HttpHeaders.LOCATION,"/login.html?manage=1").build();
    String html=new String(new ClassPathResource("static/index.html").getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    html=html.replace("const API='/api/social';let me=1,","const API='/api/social';let me="+active+",");
    html=html.replace("<div class=\"top-actions\">","<div class=\"top-actions\"><button class=\"icon-btn\" title=\"Switch pet\" onclick=\"location.href='/login.html?manage=1'\">🐾</button><button class=\"icon-btn\" title=\"Log out\" onclick=\"fetch('/api/auth/logout',{method:'POST'}).then(()=>location.href='/login.html')\">↪</button>");
    String hardening="""
<style>
.shop-hero{background:linear-gradient(120deg,#5f31d9,#8c4de9 48%,#ff6e9b);color:#fff;border-radius:24px;padding:28px;margin-bottom:18px;display:grid;grid-template-columns:1fr auto;align-items:center;gap:20px;overflow:hidden;position:relative}.shop-hero:after{content:'🐾';position:absolute;right:90px;top:-50px;font-size:190px;opacity:.08;transform:rotate(-18deg)}.shop-hero h2{font-size:30px;margin:0 0 8px}.shop-hero p{margin:0;opacity:.9;max-width:650px}.shop-hero .cart-btn{background:#fff;color:#5f31d9;border:0;border-radius:14px;padding:13px 18px;font-weight:900;cursor:pointer;position:relative;z-index:1}.shop-tools{display:flex;gap:10px;flex-wrap:wrap;align-items:center;margin:15px 0}.shop-search{flex:1;min-width:230px;padding:12px 14px;border:1px solid #ddd9ea;border-radius:13px;background:#fff;outline:0}.shop-search:focus{border-color:#9b7aec;box-shadow:0 0 0 3px #eee8ff}.cat-chip{border:1px solid #e7e1f5;background:#fff;color:#5c5671;border-radius:999px;padding:9px 13px;font-weight:750;cursor:pointer;transition:.2s}.cat-chip:hover,.cat-chip.active{background:#6d36e3;color:#fff;border-color:#6d36e3;transform:translateY(-1px)}.market-grid{display:grid;grid-template-columns:repeat(4,minmax(0,1fr));gap:15px}.market-card{padding:0;overflow:hidden;position:relative}.market-card:hover{transform:translateY(-5px)}.market-img{height:165px;display:grid;place-items:center;font-size:72px;background:linear-gradient(145deg,#f6f1ff,#fff3f7);position:relative}.market-badge{position:absolute;left:10px;top:10px;background:#fff;color:#6d36e3;padding:5px 8px;border-radius:999px;font-size:10px;font-weight:900;box-shadow:0 5px 16px #43256e1c}.wish{position:absolute;right:10px;top:10px;width:34px;height:34px;border:0;border-radius:50%;background:#fff;cursor:pointer;font-size:18px;box-shadow:0 5px 16px #43256e1c}.market-info{padding:15px}.market-info h3{font-size:16px;margin:5px 0 4px}.rating{font-size:12px;color:#8a819a}.market-price{display:flex;align-items:end;justify-content:space-between;margin:12px 0}.market-price b{font-size:21px;color:#5f31d9}.old-price{text-decoration:line-through;color:#aaa;font-size:12px;margin-left:6px}.product-actions{display:grid;grid-template-columns:1fr auto;gap:8px}.product-actions button{border:0;border-radius:11px;padding:10px;cursor:pointer;font-weight:800}.add-cart{background:linear-gradient(90deg,#6d36e3,#5568ed);color:#fff}.view-product{background:#f2edff;color:#6d36e3}.recommend-strip{display:flex;gap:10px;overflow:auto;margin:15px 0 20px}.recommend-pill{min-width:max-content;padding:10px 14px;background:#fff7df;border:1px solid #ffe5a0;border-radius:13px;font-size:12px}.cart-line{display:grid;grid-template-columns:46px 1fr auto;gap:10px;align-items:center;padding:12px 0;border-bottom:1px solid #eeeaf5}.cart-icon{width:46px;height:46px;border-radius:12px;background:#f5efff;display:grid;place-items:center;font-size:26px}.qty{display:flex;gap:6px;align-items:center}.qty button{width:28px;height:28px;border:0;border-radius:8px;background:#f1ecfb;cursor:pointer}.checkout-summary{background:#f8f5ff;padding:14px;border-radius:14px;margin-top:14px}.detail-hero{height:190px;border-radius:18px;background:linear-gradient(145deg,#f4edff,#fff0f5);display:grid;place-items:center;font-size:100px;margin-bottom:15px}.stock-ok{color:#18875d;font-weight:800;font-size:12px}@media(max-width:1200px){.market-grid{grid-template-columns:repeat(3,1fr)}}@media(max-width:850px){.market-grid{grid-template-columns:repeat(2,1fr)}.shop-hero{grid-template-columns:1fr}.shop-hero .cart-btn{justify-self:start}}@media(max-width:540px){.market-grid{grid-template-columns:1fr}}
</style>
<script>
(function(){
  const samePet=p=>p&&Number(p.id)===Number(me);
  const petPic=p=>p&&p.avatarUrl?`<img src="${p.avatarUrl}" alt="${esc(p.name)}" style="width:100%;height:100%;object-fit:cover;border-radius:inherit">`:petEmoji(p?.species);
  const originalFollow=followPet, originalFriend=friendPet;
  followPet=async function(id,b){if(Number(id)===Number(me)){toast('You cannot follow your own pet profile.');return;}return originalFollow(id,b)};
  friendPet=async function(id){if(Number(id)===Number(me)){toast('You cannot send a friend request to your own pet profile.');return;}return originalFriend(id)};
  renderDiscover=function(list){
    const safe=(list||profiles).filter(p=>!samePet(p));
    discoverGrid.innerHTML=safe.map(p=>`<div class="card pet-card"><div class="pet-hero" style="overflow:hidden">${petPic(p)}</div><h3>${esc(p.name)} ✓</h3><div class="handle">${esc(p.handle)}</div><p>${esc(p.breed||p.species)} · ${esc(p.location||'Nearby')}</p><span class="pill">${esc(p.species||'Pet')}</span>${p.age!=null?`<span class="pill">${p.age} yrs</span>`:''}<span class="pill">${p.followers} followers</span><div class="pet-actions"><button class="btn ghost" onclick="followPet(${p.id},this)">Follow</button><button class="btn primary" onclick="friendPet(${p.id})">Friend Request</button></div></div>`).join('')||'<div class="card empty">No other pets match your search.</div>';
  };
  openPlayDate=function(){
    const options=profiles.filter(p=>!samePet(p)).map(p=>`<option value="${p.id}">${esc(p.name)} · ${esc(p.breed||p.species)}</option>`).join('');
    if(!options){toast('Add or discover another pet before scheduling a play date.');return;}
    openModal('Schedule Play Date',`<form class="form" onsubmit="createPlayDate(event)"><select id="playGuest" required>${options}</select><input id="playPlace" required placeholder="Pet-friendly park or venue"><input id="playTime" type="datetime-local" required><button class="btn primary">Schedule Play Date 🎾</button></form>`);
  };
  showPet=function(id){
    if(Number(id)===Number(me)){showView('profile');return;}
    const p=profile(id);openModal(p.name+' · '+p.handle,`<div class="pet-hero" style="overflow:hidden">${petPic(p)}</div><h2>${esc(p.name)} ✓</h2><p>${esc(p.bio||'Happy Tails pet profile')}</p><span class="pill">${esc(p.species)}</span><span class="pill">${esc(p.breed||'')}</span>${p.age!=null?`<span class="pill">${p.age} yrs</span>`:''}<span class="pill">${p.followers} followers</span><div class="pet-actions"><button class="btn ghost" onclick="followPet(${id},this)">Follow</button><button class="btn primary" onclick="friendPet(${id});closeModal()">Friend Request</button></div>`);
  };
  const oldRenderProfile=renderProfile;
  renderProfile=function(){oldRenderProfile();const p=profile(me);const card=document.querySelector('#profileView .card.sidecard');if(card){const h=card.querySelector('.handle');if(h){const details=[];if(p.birthday)details.push('🎂 '+p.birthday);if(p.age!=null)details.push(p.age+' years old');if(details.length)h.insertAdjacentHTML('afterend',`<div class="handle" style="text-align:center;margin-top:7px">${details.join(' · ')}</div>`);}const big=card.querySelector('.bigpet');if(big&&p.avatarUrl)big.innerHTML=petPic(p);}}

  const catalog=[
    {id:1,name:'Bouncy Rope Ball',icon:'🎾',cat:'Toys',price:12.99,old:16.99,rating:4.8,reviews:328,badge:'BEST SELLER',pets:'Dogs',desc:'Durable rope-and-ball toy for fetch, tug and active play.'},
    {id:2,name:'Interactive Treat Puzzle',icon:'🧩',cat:'Toys',price:18.50,rating:4.7,reviews:214,badge:'SMART PLAY',pets:'Dogs & Cats',desc:'A boredom-busting puzzle that rewards curious pets with treats.'},
    {id:3,name:'Cat Feather Wand Set',icon:'🪶',cat:'Toys',price:10.49,rating:4.9,reviews:441,badge:'CAT FAVORITE',pets:'Cats',desc:'Colorful feather wands for chase, jump and bonding time.'},
    {id:4,name:'Salmon Training Bites',icon:'🐟',cat:'Snacks',price:11.99,rating:4.8,reviews:506,badge:'NATURAL',pets:'Dogs & Cats',desc:'Soft bite-sized salmon treats made for training and rewards.'},
    {id:5,name:'Crunchy Veggie Nibbles',icon:'🥕',cat:'Snacks',price:8.75,rating:4.6,reviews:178,badge:'SMALL PETS',pets:'Rabbits & Hamsters',desc:'Crunchy vegetable snacks for small-pet enrichment.'},
    {id:6,name:'Birthday Pupcake Kit',icon:'🧁',cat:'Snacks',price:19.99,rating:4.9,reviews:97,badge:'CELEBRATE',pets:'Dogs',desc:'Pet-friendly birthday cake mix with a party topper.'},
    {id:7,name:'Cozy Knit Pet Hoodie',icon:'🧥',cat:'Fashion',price:26.99,rating:4.7,reviews:264,badge:'NEW',pets:'Dogs & Cats',desc:'Soft stretch hoodie for chilly walks and photo-ready outings.'},
    {id:8,name:'Rainy Day Pet Jacket',icon:'🌧️',cat:'Fashion',price:31.50,rating:4.6,reviews:131,badge:'WEATHER READY',pets:'Dogs',desc:'Lightweight water-resistant jacket with reflective trim.'},
    {id:9,name:'Bow Tie & Bandana Set',icon:'🎀',cat:'Fashion',price:14.99,rating:4.8,reviews:290,badge:'PHOTO READY',pets:'Dogs & Cats',desc:'A playful matching set for birthdays, meetups and social posts.'},
    {id:10,name:'Happy Tails Gift Box',icon:'🎁',cat:'Gifts',price:34.99,rating:4.9,reviews:188,badge:'GIFT PICK',pets:'All Pets',desc:'A curated surprise box with toy, treat, keepsake and birthday card.'},
    {id:11,name:'Custom Paw Print Frame',icon:'🖼️',cat:'Gifts',price:24.00,rating:4.8,reviews:154,badge:'MEMORY GIFT',pets:'All Pets',desc:'Create a keepsake frame for a favorite paw print and photo.'},
    {id:12,name:'Pet Birthday Party Pack',icon:'🎉',cat:'Gifts',price:22.50,rating:4.7,reviews:87,badge:'PARTY',pets:'All Pets',desc:'Hat, banner, photo props and pet-safe celebration accessories.'},
    {id:13,name:'Gentle Grooming Kit',icon:'🧴',cat:'Care',price:27.75,rating:4.8,reviews:352,badge:'CARE ESSENTIAL',pets:'Dogs & Cats',desc:'Brush, shampoo, paw balm and grooming wipes in one kit.'},
    {id:14,name:'Dental Care Starter Set',icon:'🪥',cat:'Care',price:16.99,rating:4.6,reviews:245,badge:'VET PICK',pets:'Dogs & Cats',desc:'Pet toothbrush, finger brush and pet-safe dental gel.'},
    {id:15,name:'Travel Water Bottle',icon:'💧',cat:'Travel',price:17.49,rating:4.9,reviews:603,badge:'TRAVEL PICK',pets:'Dogs',desc:'Leak-resistant portable bottle with an attached drinking tray.'},
    {id:16,name:'Airline Style Pet Carrier',icon:'👜',cat:'Travel',price:49.00,rating:4.7,reviews:201,badge:'TRAVEL',pets:'Cats & Small Dogs',desc:'Ventilated soft carrier with padded shoulder strap and pockets.'},
    {id:17,name:'Cloud Comfort Bed',icon:'🛏️',cat:'Home',price:44.99,old:54.99,rating:4.9,reviews:712,badge:'TOP RATED',pets:'Dogs & Cats',desc:'Washable plush donut bed made for curling, napping and comfort.'},
    {id:18,name:'Bird Foraging Activity Set',icon:'🦜',cat:'Small Pets',price:18.25,rating:4.7,reviews:96,badge:'ENRICHMENT',pets:'Birds',desc:'Colorful climbing and foraging toys designed for active birds.'},
    {id:19,name:'Rabbit Tunnel & Hideaway',icon:'🐇',cat:'Small Pets',price:21.99,rating:4.8,reviews:142,badge:'BUNNY PICK',pets:'Rabbits',desc:'Foldable tunnel for hiding, zooming and enrichment.'},
    {id:20,name:'Aquarium Adventure Decor',icon:'🐠',cat:'Small Pets',price:15.99,rating:4.6,reviews:83,badge:'AQUATIC',pets:'Fish',desc:'Pet-safe colorful aquarium hideouts and decorative plants.'}
  ];
  let shopCategory='All',shopQuery='',cart=JSON.parse(localStorage.getItem('happyTailsCart')||'[]'),wishlist=new Set(JSON.parse(localStorage.getItem('happyTailsWishlist')||'[]'));
  const saveCart=()=>localStorage.setItem('happyTailsCart',JSON.stringify(cart));
  const saveWish=()=>localStorage.setItem('happyTailsWishlist',JSON.stringify([...wishlist]));
  const cartCount=()=>cart.reduce((n,x)=>n+x.qty,0);
  const filteredProducts=()=>catalog.filter(p=>(shopCategory==='All'||p.cat===shopCategory)&&(!shopQuery||[p.name,p.cat,p.pets,p.desc].join(' ').toLowerCase().includes(shopQuery)));
  window.setShopCategory=function(cat){shopCategory=cat;renderShop()};
  window.searchShop=function(v){shopQuery=v.trim().toLowerCase();renderShop(false)};
  window.toggleWish=function(id){wishlist.has(id)?wishlist.delete(id):wishlist.add(id);saveWish();renderShop(false);toast(wishlist.has(id)?'Saved to favorites 💜':'Removed from favorites')};
  window.addToCart=function(id){const p=catalog.find(x=>x.id===id);const found=cart.find(x=>x.id===id);found?found.qty++:cart.push({id,qty:1});saveCart();renderShop(false);toast(p.name+' added to cart 🛍')};
  window.changeQty=function(id,d){const x=cart.find(x=>x.id===id);if(!x)return;x.qty+=d;if(x.qty<=0)cart=cart.filter(y=>y.id!==id);saveCart();openCart()};
  window.productDetails=function(id){const p=catalog.find(x=>x.id===id);openModal(p.name,`<div class="detail-hero">${p.icon}</div><span class="pill">${p.cat}</span><span class="pill">For ${p.pets}</span><h2>${esc(p.name)}</h2><div class="rating">⭐ ${p.rating} · ${p.reviews} verified reviews</div><p>${esc(p.desc)}</p><p class="stock-ok">✓ In stock · Happy Tails marketplace item</p><div class="market-price"><div><b>$${p.price.toFixed(2)}</b>${p.old?`<span class="old-price">$${p.old.toFixed(2)}</span>`:''}</div></div><button class="btn primary" style="width:100%" onclick="addToCart(${p.id});closeModal()">Add to Cart 🛍</button>`)};
  window.openCart=function(){const total=cart.reduce((sum,x)=>{const p=catalog.find(y=>y.id===x.id);return sum+(p?p.price*x.qty:0)},0);const body=cart.length?cart.map(x=>{const p=catalog.find(y=>y.id===x.id);return `<div class="cart-line"><div class="cart-icon">${p.icon}</div><div><b>${esc(p.name)}</b><div class="handle">$${p.price.toFixed(2)} each</div></div><div class="qty"><button onclick="changeQty(${p.id},-1)">−</button><b>${x.qty}</b><button onclick="changeQty(${p.id},1)">＋</button></div></div>`}).join('')+`<div class="checkout-summary"><div style="display:flex;justify-content:space-between"><b>Total</b><b>$${total.toFixed(2)}</b></div><p class="handle">Demo marketplace checkout. Orders are saved to your pet account.</p><button class="btn primary" style="width:100%" onclick="checkoutCart()">Place Order</button></div>`:'<div class="empty"><div class="emoji">🛒</div>Your cart is empty.</div>';openModal('Shopping Cart · '+cartCount()+' items',body)};
  window.checkoutCart=async function(){if(!cart.length)return;try{for(const x of cart){const p=catalog.find(y=>y.id===x.id);await req('/orders',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({petProfileId:me,itemName:p.name,quantity:x.qty,totalAmount:Number((p.price*x.qty).toFixed(2)),status:'PLACED'})})}cart=[];saveCart();closeModal();renderShop();toast('Order placed for '+profile(me).name+'! 📦🐾')}catch(e){toast(e.message)}};
  window.renderShop=function(){const shop=document.getElementById('view-shop');if(!shop)return;const mine=profile(me);const recommended=catalog.filter(p=>p.pets==='All Pets'||(mine.species&&p.pets.toLowerCase().includes(mine.species.toLowerCase()))).slice(0,4);shop.innerHTML=`<div class="shop-hero"><div><h2>🛍 Happy Tails Marketplace</h2><p>Toys, snacks, outfits, gifts, care essentials and pet-friendly finds — curated for ${esc(mine.name||'your pet')}.</p></div><button class="cart-btn" onclick="openCart()">🛒 Cart <span>${cartCount()}</span></button></div><div class="recommend-strip">${recommended.map(p=>`<div class="recommend-pill">✨ Recommended for ${esc(mine.name)} · ${esc(p.name)}</div>`).join('')}</div><div class="shop-tools"><input class="shop-search" value="${esc(shopQuery)}" oninput="searchShop(this.value)" placeholder="Search toys, snacks, dresses, gifts..."><button class="btn ghost" onclick="showOrders()">📦 My Orders</button></div><div class="shop-tools">${['All','Toys','Snacks','Fashion','Gifts','Care','Travel','Home','Small Pets'].map(c=>`<button class="cat-chip ${shopCategory===c?'active':''}" onclick="setShopCategory('${c}')">${c}</button>`).join('')}</div><div class="market-grid">${filteredProducts().map(p=>`<article class="card market-card"><div class="market-img"><span class="market-badge">${p.badge}</span><button class="wish" onclick="toggleWish(${p.id})">${wishlist.has(p.id)?'💜':'♡'}</button>${p.icon}</div><div class="market-info"><span class="pill">${p.cat}</span><h3>${esc(p.name)}</h3><div class="rating">⭐ ${p.rating} (${p.reviews}) · ${esc(p.pets)}</div><div class="market-price"><div><b>$${p.price.toFixed(2)}</b>${p.old?`<span class="old-price">$${p.old.toFixed(2)}</span>`:''}</div></div><div class="product-actions"><button class="add-cart" onclick="addToCart(${p.id})">Add to Cart</button><button class="view-product" onclick="productDetails(${p.id})">View</button></div></div></article>`).join('')||'<div class="card empty">No marketplace items match your search.</div>'}</div>`};
  window.showOrders=async function(){const os=await req(`/profiles/${me}/orders`);openModal('My Orders',os.length?os.map(o=>`<div class="notice"><span>📦</span><div><b>${esc(o.itemName)}</b><div class="handle">Qty ${o.quantity} · $${Number(o.totalAmount).toFixed(2)} · ${esc(o.status)}</div></div></div>`).join(''):'<div class="empty">No orders yet.</div>')};
})();
</script>
""";
    html=html.replace("</body>",hardening+"</body>");
    html=html.replace("</head>","<link rel=\"stylesheet\" href=\"/product-ux.css\"></head>");
    html=html.replace("</body>","<script src=\"/product-ux.js\"></script></body>");
    return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(html);
  }
}
