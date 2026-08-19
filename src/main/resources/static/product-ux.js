(function(){
  function safePet(){try{return profile(me)}catch{return {name:'Your pet'}}}
  function completeness(p){const checks=[p?.avatarUrl,p?.birthday,p?.age!=null,p?.breed,p?.location,p?.bio];return Math.round(checks.filter(Boolean).length/checks.length*100)}
  function nextOpenReminder(){return (reminders||[]).filter(r=>!r.completed).sort((a,b)=>String(a.dueDate).localeCompare(String(b.dueDate)))[0]}
  function nextPlay(){return (playdates||[]).slice().sort((a,b)=>String(a.scheduledAt).localeCompare(String(b.scheduledAt)))[0]}
  function nextMeet(){return (meetups||[]).slice().sort((a,b)=>String(a.scheduledAt).localeCompare(String(b.scheduledAt)))[0]}
  function dateLabel(s){if(!s)return 'Nothing scheduled';try{return fmt(s)}catch{return String(s)}}

  function renderDailyBrief(){
    const home=document.getElementById('view-home'); if(!home)return;
    home.querySelectorAll('.daily-brief,.context-strip').forEach(x=>x.remove());
    const p=safePet(),rem=nextOpenReminder(),play=nextPlay(),meet=nextMeet(),pct=completeness(p);
    const hero=home.querySelector('.hero'); if(!hero)return;
    const context=document.createElement('div');context.className='context-strip';context.innerHTML=`<div class="context-pill">🐾 Active pet: <b>${esc(p.name||'Pet')}</b></div><div class="context-pill">📍 ${esc(p.location||'Location not added')}</div><div class="context-pill">👥 ${p.followers||0} followers</div><div class="context-pill">🎂 ${p.age!=null?p.age+' years old':'Age not added'}</div>`;
    const d=document.createElement('section');d.className='daily-brief';d.innerHTML=`<div class="daily-top"><div><h2>Today for ${esc(p.name||'your pet')} ✨</h2><p>Your care, social and activity priorities in one place.</p></div><div class="daily-score"><b>${pct}%</b><small>Profile complete</small><div class="profile-progress"><span style="width:${pct}%"></span></div></div></div><div class="today-grid"><div class="today-item" onclick="showView('reminders')"><div class="today-icon">💉</div><b>${rem?esc(rem.title):'Care is up to date'}</b><small>${rem?dateLabel(rem.dueDate):'Add vaccines, vet visits or medication reminders.'}</small></div><div class="today-item" onclick="showView('playdates')"><div class="today-icon">🎾</div><b>${play?'Next play date':'Find a playmate'}</b><small>${play?dateLabel(play.scheduledAt)+' · '+esc(play.location||'Location TBD'):'Discover compatible pets and schedule a play date.'}</small></div><div class="today-item" onclick="showView('meetups')"><div class="today-icon">🐾</div><b>${meet?esc(meet.title):'Explore the community'}</b><small>${meet?dateLabel(meet.scheduledAt)+' · '+esc(meet.location||'Venue TBD'):'Join pet-friendly meetups and local events.'}</small></div></div>`;
    hero.insertAdjacentElement('afterend',context);context.insertAdjacentElement('afterend',d);
  }

  window.openHappyNotifications=async function(){
    const p=safePet();let requests=[];try{requests=await req(`/profiles/${me}/friend-requests`)}catch{}
    const open=(reminders||[]).filter(r=>!r.completed).slice(0,4);const plays=(playdates||[]).slice(0,3);
    const items=[];open.forEach(r=>items.push(`<div class="notification-item"><div class="nicon">${r.type==='VACCINATION'?'💉':'🔔'}</div><div><b>${esc(r.title)}</b><div class="handle">Due ${esc(r.dueDate||'soon')}</div></div></div>`));requests.forEach(r=>items.push(`<div class="notification-item"><div class="nicon">👥</div><div><b>${esc(profile(r.fromPetId).name)} sent ${esc(p.name)} a friend request</b><div class="handle">Open Friends & Requests to respond.</div></div></div>`));plays.forEach(x=>items.push(`<div class="notification-item"><div class="nicon">🎾</div><div><b>Upcoming play date</b><div class="handle">${dateLabel(x.scheduledAt)} · ${esc(x.location||'Location TBD')}</div></div></div>`));openModal('Notifications',`<div class="notification-list">${items.join('')||'<div class="pro-empty"><b>All caught up 🐾</b>No new care or social notifications.</div>'}</div>`)
  };

  function enhanceTopbar(){const top=document.querySelector('.top-actions');if(!top)return;const buttons=top.querySelectorAll('.icon-btn');if(buttons.length){buttons[buttons.length-1].onclick=openHappyNotifications;buttons[buttons.length-1].title='Notifications'}}

  function mobileNav(){if(document.querySelector('.mobile-nav'))return;const n=document.createElement('nav');n.className='mobile-nav';n.innerHTML=`<button data-v="home" class="active"><span>⌂</span>Home</button><button data-v="discover"><span>◉</span>Discover</button><button data-v="post"><span>＋</span>Post</button><button data-v="reminders"><span>♡</span>Care</button><button data-v="profile"><span>🐾</span>Profile</button>`;document.body.appendChild(n);n.querySelectorAll('button').forEach(b=>b.onclick=()=>{if(b.dataset.v==='post'){openCreatePost();return}showView(b.dataset.v);n.querySelectorAll('button').forEach(x=>x.classList.toggle('active',x===b))})}

  const oldHome=window.renderHome;window.renderHome=function(){oldHome();renderDailyBrief();enhanceTopbar()};
  const oldSitters=window.renderSitters;window.renderSitters=function(){oldSitters();document.querySelectorAll('#sitterGrid .service-card').forEach(card=>{if(card.querySelector('.trust-row'))return;const h=card.querySelector('h3');if(h)h.insertAdjacentHTML('afterend','<div class="trust-row"><span class="trust-badge">✓ Profile complete</span><span class="trust-badge">✓ Availability shown</span></div>')})};
  const oldMeetups=window.renderMeetups;window.renderMeetups=function(){oldMeetups();document.querySelectorAll('#meetupGrid .event-card').forEach(card=>{if(card.querySelector('.event-meta'))return;const h=card.querySelector('h3');if(h)h.insertAdjacentHTML('afterend','<div class="event-meta"><span>🐾 Community event</span><span>📍 Pet-friendly venue</span></div>')})};
  const oldProfile=window.renderProfile;window.renderProfile=function(){oldProfile();const header=document.querySelector('#view-profile .sectionhead');if(header&&!header.querySelector('.pro-edit')){const btn=document.createElement('button');btn.className='btn ghost pro-edit';btn.textContent='⚙ Manage Pet';btn.onclick=()=>location.href='/login.html?manage=1';const action=header.querySelector('.btn.primary');action?.parentNode?.insertBefore(btn,action)}};

  setTimeout(()=>{mobileNav();enhanceTopbar();renderDailyBrief()},450);
})();