(()=>{
function installQuickPhoto(){
  if(typeof window.openPetProfileEditor!=='function') return;
  const view=document.getElementById('profileView');
  if(!view) return;

  const card=view.querySelector('.card.sidecard')||view.querySelector('.profile-card')||view.querySelector('.card');
  if(!card) return;

  let avatar=card.querySelector('.avatar,.pet-avatar,.profile-avatar,.pet-hero');
  if(!avatar){
    avatar=[...card.querySelectorAll('div')].find(el=>{
      const s=getComputedStyle(el);
      return (parseFloat(s.borderRadius)>=40||s.borderRadius==='50%')&&el.offsetWidth>=60&&el.offsetWidth<=180&&el.offsetHeight>=60&&el.offsetHeight<=180;
    });
  }
  if(!avatar||avatar.dataset.quickPhoto==='1') return;
  avatar.dataset.quickPhoto='1';
  avatar.classList.add('quick-photo-target');
  avatar.setAttribute('role','button');
  avatar.setAttribute('tabindex','0');
  avatar.setAttribute('aria-label','Edit pet profile photo');
  avatar.title='Change pet profile photo';
  avatar.addEventListener('click',()=>window.openPetProfileEditor());
  avatar.addEventListener('keydown',e=>{if(e.key==='Enter'||e.key===' '){e.preventDefault();window.openPetProfileEditor();}});

  const badge=document.createElement('button');
  badge.type='button';
  badge.className='quick-photo-badge';
  badge.innerHTML='📷 <span>Edit photo</span>';
  badge.onclick=e=>{e.stopPropagation();window.openPetProfileEditor();};
  avatar.appendChild(badge);

  if(!document.getElementById('quickPhotoHint')){
    const hint=document.createElement('div');
    hint.id='quickPhotoHint';
    hint.className='quick-photo-hint';
    hint.innerHTML='📷 <b>Add a real photo for your pet</b><span>Tap the profile picture or choose Edit Pet Profile.</span><button type="button">Upload photo</button>';
    hint.querySelector('button').onclick=()=>window.openPetProfileEditor();
    const title=view.querySelector('.view-title,.sectionhead');
    (title||card).after(hint);
  }
}

const original=window.renderProfile;
if(typeof original==='function'&&!original.__quickPhoto){
  window.renderProfile=function(){const r=original.apply(this,arguments);setTimeout(installQuickPhoto,60);return r};
  window.renderProfile.__quickPhoto=true;
}
setTimeout(installQuickPhoto,700);
setInterval(()=>{if(document.getElementById('profileView')?.classList.contains('active')) installQuickPhoto();},1800);
})();