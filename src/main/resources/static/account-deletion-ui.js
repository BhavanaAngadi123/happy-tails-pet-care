(()=>{
 const $=id=>document.getElementById(id);
 function install(){
  const stage=$('securityStage');
  if(!stage||$('deleteAccountPanel'))return;
  const panel=document.createElement('div');
  panel.id='deleteAccountPanel';
  panel.style.marginTop='22px';
  panel.innerHTML=`<div style="border-top:1px solid #eee;padding-top:18px"><h3 style="margin:0 0 6px;color:#b42318">Delete Happy Tails account</h3><p class="muted">This permanently deletes the private owner account, every pet profile, posts, memories, messages, health data, bookings and related activity. This cannot be undone.</p><div class="error" id="deleteAccountError"></div><form id="deleteAccountForm" class="form"><label>Current password<input id="deleteAccountPassword" type="password" required autocomplete="current-password"></label><label>Type <b>DELETE MY ACCOUNT</b> to confirm<input id="deleteAccountPhrase" required autocomplete="off" placeholder="DELETE MY ACCOUNT"></label><button class="btn" style="background:#b42318;color:#fff" type="submit">Permanently delete account</button></form></div>`;
  stage.appendChild(panel);
  $('deleteAccountForm').onsubmit=async e=>{
   e.preventDefault();
   const box=$('deleteAccountError');box.style.display='none';
   try{
    const r=await fetch('/api/auth/account',{method:'DELETE',credentials:'same-origin',headers:{'Content-Type':'application/json'},body:JSON.stringify({password:$('deleteAccountPassword').value,confirmPhrase:$('deleteAccountPhrase').value})});
    let d={};try{d=await r.json()}catch{}
    if(!r.ok)throw new Error(d.error||'Could not delete account.');
    location.href='/login.html?accountDeleted=1';
   }catch(err){box.textContent=err.message;box.style.display='block';}
  };
 }
 function showDeleted(){
  if(!new URLSearchParams(location.search).has('accountDeleted'))return;
  const auth=$('authStage');if(!auth)return;
  const n=document.createElement('div');n.className='success';n.style.display='block';n.textContent='Your Happy Tails account and pet data were permanently deleted.';auth.prepend(n);
 }
 setTimeout(()=>{install();showDeleted()},0);
})();