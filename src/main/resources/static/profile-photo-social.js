(()=>{
  function current(){
    try{return typeof profile==='function'?profile(me):null}catch{return null}
  }
  function waitForEditor(){
    if(typeof window.savePetProfile!=='function'){setTimeout(waitForEditor,300);return}
    if(window.savePetProfile.__photoSocial)return;
    const original=window.savePetProfile;
    window.savePetProfile=async function(event){
      const before=current()?.avatarUrl||'';
      await original.call(this,event);
      const after=current()?.avatarUrl||'';
      if(after&&after!==before){
        const pet=current();
        try{
          await fetch('/api/social/posts',{
            method:'POST',
            headers:{'Content-Type':'application/json'},
            body:JSON.stringify({caption:`${pet?.name||'My pet'} updated their profile picture 📸`})
          });
          if(typeof window.renderAll==='function')window.renderAll();
          if(typeof window.toast==='function')toast('Profile photo saved and shared 📸');
        }catch(e){
          if(typeof window.toast==='function')toast('Photo saved, but the activity post could not be created.');
        }
      }
    };
    window.savePetProfile.__photoSocial=true;
  }
  waitForEditor();
})();