(()=>{
  function current(){try{return typeof profile==='function'?profile(me):window.happyTailsActivePet||null}catch{return window.happyTailsActivePet||null}}
  function waitForEditor(){
    if(typeof window.savePetProfile!=='function'){setTimeout(waitForEditor,250);return}
    if(window.savePetProfile.__photoSocial)return;
    const original=window.savePetProfile;
    window.savePetProfile=async function(event){
      const before=current()?.avatarUrl||'';
      const saved=await original.call(this,event);
      const after=saved?.avatarUrl||current()?.avatarUrl||'';
      if(after&&after!==before){
        const pet=saved||current();
        try{
          const r=await fetch('/api/social/posts',{method:'POST',headers:{'Content-Type':'application/json'},credentials:'same-origin',body:JSON.stringify({caption:`${pet?.name||'My pet'} updated their profile picture 📸`,mediaUrl:after})});
          const d=await r.json().catch(()=>null);
          if(!r.ok)throw new Error(d?.error||'activity post failed');
          if(typeof window.renderEnhancedFeed==='function')await window.renderEnhancedFeed('for-you').catch(()=>{});
          if(typeof window.renderProfile==='function')window.renderProfile();
          document.dispatchEvent(new CustomEvent('happy-tails:profile-photo-posted',{detail:{pet,post:d}}));
          if(typeof window.toast==='function')toast('Profile photo saved and shared 📸');
        }catch(e){if(typeof window.toast==='function')toast('Photo saved, but the activity post could not be created.');}
      }
      return saved;
    };
    window.savePetProfile.__photoSocial=true;
  }
  waitForEditor();
})();