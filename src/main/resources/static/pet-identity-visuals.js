(()=>{
const esc=s=>String(s??'').replace(/[&<>"']/g,m=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[m]));
function current(){try{return typeof profile==='function'?profile(me):null}catch{return null}}
function img(url,name){return `<img class="pet-identity-img" src="${url}" alt="${esc(name||'Pet')} profile photo">`}
function applyIdentity(){const p=current();if(!p?.avatarUrl)return;
  document.querySelectorAll('.pet-chip .pet-avatar').forEach(el=>{el.classList.add('pet-has-photo');el.innerHTML=img(p.avatarUrl,p.name)});
  document.querySelectorAll('#profileView .bigpet,#homeProfile .bigpet').forEach(el=>{el.classList.add('pet-has-photo');el.innerHTML=img(p.avatarUrl,p.name)});
  document.querySelectorAll('#profileView .profile-cover,#homeProfile .profile-cover').forEach(el=>{el.classList.add('pet-photo-cover');el.style.backgroundImage=`linear-gradient(120deg,rgba(90,43,214,.28),rgba(255,79,135,.18)),url("${p.avatarUrl}")`});
  const hint=document.getElementById('quickPhotoHint');if(hint){hint.className='quick-photo-hint identity-photo-live';hint.innerHTML=`<div class="identity-thumb">${img(p.avatarUrl,p.name)}</div><div><b>${esc(p.name)}'s profile photo is live</b><div class="handle">This is the photo other pets see across Happy Tails. Changing it can also appear as a social activity.</div></div><button type="button" class="btn ghost">Change photo</button>`;hint.querySelector('button').onclick=()=>window.openPetProfileEditor?.();}
}
async function ensureActivity(){const p=current();if(!p?.avatarUrl||window.__identityActivityChecked)return;window.__identityActivityChecked=true;try{
  const r=await fetch(`/api/social/profiles/${me}/posts`);if(!r.ok)return;const posts=await r.json();
  const has=posts.some(x=>String(x.caption||'').toLowerCase().includes('updated their profile picture')&&x.mediaUrl);
  if(!has){const c=await fetch('/api/social/posts',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({caption:`${p.name} updated their profile picture 📸`,mediaUrl:p.avatarUrl})});if(c.ok){sessionStorage.setItem('happyTailsIdentityBackfill','1');setTimeout(()=>location.reload(),350);return;}}
  if(typeof window.renderEnhancedFeed==='function')window.renderEnhancedFeed('for-you').catch(()=>{});
}catch(e){console.warn('profile activity sync',e)}}
function boot(){applyIdentity();ensureActivity()}
setTimeout(boot,600);setTimeout(boot,1500);setInterval(()=>{if(document.getElementById('view-profile')?.classList.contains('active')||document.getElementById('view-home')?.classList.contains('active'))applyIdentity()},1800);
})();