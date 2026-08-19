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
})();
</script>
""";
    html=html.replace("</body>",hardening+"</body>");
    return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(html);
  }
}
