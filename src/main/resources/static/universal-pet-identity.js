(()=>{
const esc=s=>String(s??'').replace(/[&<>"']/g,m=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[m]));
let active=null;
function fallback(p){try{return typeof petEmoji==='function'?petEmoji(p?.species):'🐾'}catch{return'🐾'}}
function avatar(p){return p?.avatarUrl?`<img src="${p.avatarUrl}" alt="${esc(p.name||'Pet')} profile photo" style="width:100%;height:100%;object-fit:cover;border-radius:inherit">`:fallback(p)}
async function load(){try{const r=await fetch('/api/auth/session',{credentials:'same-origin'});if(!r.ok)return;const s=await r.json(),id=Number(s.activePetId);active=(s.pets||[]).find(p=>Number(p.id)===id)||null;if(!active)return;window.happyTailsActivePet=active;apply()}catch(e){console.warn('active pet identity sync',e)}}
function apply(){const p=active;if(!p)return;
 document.querySelectorAll('.pet-chip .pet-avatar').forEach(el=>el.innerHTML=avatar(p));
 const n=document.getElementById('sideName');if(n)n.textContent=(p.name||'My Pet')+' ✓';const h=document.getElementById('sideHandle');if(h)h.textContent=p.handle||'';
 document.querySelectorAll('[data-active-pet-avatar]').forEach(el=>el.innerHTML=avatar(p));
 document.querySelectorAll('[data-active-pet-name]').forEach(el=>el.textContent=p.name||'My Pet');
 document.querySelectorAll('#profileView .bigpet,#homeProfile .bigpet').forEach(el=>el.innerHTML=avatar(p));
 document.querySelectorAll('#profileView .profile-cover,#homeProfile .profile-cover').forEach(el=>{if(p.avatarUrl)el.style.backgroundImage=`linear-gradient(120deg,rgba(90,43,214,.26),rgba(255,79,135,.16)),url("${p.avatarUrl}")`});
}
window.refreshActivePetIdentity=async()=>{await load();if(typeof window.renderEnhancedFeed==='function')window.renderEnhancedFeed('for-you').catch(()=>{});if(typeof window.renderProfile==='function')window.renderProfile()};
const obs=new MutationObserver(()=>apply());document.addEventListener('DOMContentLoaded',()=>{load();obs.observe(document.body,{childList:true,subtree:true});setInterval(load,15000)});if(document.readyState!=='loading'){load();obs.observe(document.body,{childList:true,subtree:true})}
})();