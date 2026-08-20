(()=>{
const esc=s=>String(s??'').replace(/[&<>"']/g,m=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[m]));
let active=null,observer=null,scheduled=false,applying=false;
function fallback(p){try{return typeof petEmoji==='function'?petEmoji(p?.species):'🐾'}catch{return'🐾'}}
function avatar(p){return p?.avatarUrl?`<img src="${p.avatarUrl}" alt="${esc(p.name||'Pet')} profile photo" style="width:100%;height:100%;object-fit:cover;border-radius:inherit">`:fallback(p)}
function setHtml(el,html,key){if(!el)return;if(el.dataset.petIdentityKey===key)return;el.dataset.petIdentityKey=key;el.innerHTML=html}
async function load(){try{const r=await fetch('/api/auth/session',{credentials:'same-origin'});if(!r.ok)return;const s=await r.json(),id=Number(s.activePetId);active=(s.pets||[]).find(p=>Number(p.id)===id)||null;if(!active)return;window.happyTailsActivePet=active;apply()}catch(e){console.warn('active pet identity sync',e)}}
function apply(){if(applying)return;const p=active;if(!p)return;applying=true;try{
 const key=`${p.id||''}|${p.name||''}|${p.handle||''}|${p.avatarUrl||''}`;
 document.querySelectorAll('.pet-chip .pet-avatar').forEach(el=>setHtml(el,avatar(p),key));
 const n=document.getElementById('sideName');if(n&&n.textContent!==(p.name||'My Pet')+' ✓')n.textContent=(p.name||'My Pet')+' ✓';
 const h=document.getElementById('sideHandle');if(h&&h.textContent!==(p.handle||''))h.textContent=p.handle||'';
 document.querySelectorAll('[data-active-pet-avatar]').forEach(el=>setHtml(el,avatar(p),key));
 document.querySelectorAll('[data-active-pet-name]').forEach(el=>{if(el.textContent!==(p.name||'My Pet'))el.textContent=p.name||'My Pet'});
 document.querySelectorAll('#profileView .bigpet,#homeProfile .bigpet').forEach(el=>setHtml(el,avatar(p),key));
 document.querySelectorAll('#profileView .profile-cover,#homeProfile .profile-cover').forEach(el=>{const bg=p.avatarUrl?`linear-gradient(120deg,rgba(90,43,214,.26),rgba(255,79,135,.16)),url("${p.avatarUrl}")`:'';if(el.dataset.petIdentityBg!==key){el.dataset.petIdentityBg=key;if(bg)el.style.backgroundImage=bg;}});
 }finally{applying=false}}
function scheduleApply(){if(scheduled)return;scheduled=true;requestAnimationFrame(()=>{scheduled=false;apply()})}
window.refreshActivePetIdentity=async()=>{await load();if(typeof window.renderEnhancedFeed==='function')window.renderEnhancedFeed('for-you').catch(()=>{});if(typeof window.renderProfile==='function')window.renderProfile()};
function start(){load();if(observer)return;observer=new MutationObserver(mutations=>{if(applying)return;for(const m of mutations){if(m.addedNodes&&m.addedNodes.length){scheduleApply();break}}});observer.observe(document.body,{childList:true,subtree:true});setInterval(load,15000)}
if(document.readyState==='loading')document.addEventListener('DOMContentLoaded',start,{once:true});else start();
})();