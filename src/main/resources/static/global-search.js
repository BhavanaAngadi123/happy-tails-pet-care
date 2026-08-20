(()=>{
const esc=s=>String(s??'').replace(/[&<>"']/g,m=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[m]));
const norm=s=>String(s||'').trim().toLowerCase();
const sections=[
 {view:'home',icon:'⌂',title:'Home',terms:'dashboard feed posts'},
 {view:'profile',icon:'🐾',title:'Pet Profile',terms:'profile photo birthday bio privacy'},
 {view:'discover',icon:'◉',title:'Discover Pets',terms:'discover search pet breed species friends'},
 {view:'friends',icon:'👥',title:'Friends & Requests',terms:'friends requests followers following'},
 {view:'playdates',icon:'🎾',title:'Play Dates',terms:'play date schedule pet friends'},
 {view:'meetups',icon:'💗',title:'Meetups',terms:'meetup events community nearby'},
 {view:'sitters',icon:'🧑',title:'Pet Sitters',terms:'sitter care booking travel'},
 {view:'reminders',icon:'🔔',title:'Health Reminders',terms:'vaccination vaccine health care reminder vet'},
 {view:'memories',icon:'⭐',title:'Memories',terms:'memories milestones birthday gotcha'},
 {view:'shop',icon:'🛍',title:'Pet Shop',terms:'shop toys snacks treats gifts dresses accessories food'}
];
let box,panel;
function profileRows(q){const meId=Number(window.me);return (window.profiles||[]).filter(p=>Number(p.id)!==meId).filter(p=>[p.name,p.handle,p.species,p.breed,p.location,p.personality,p.favoriteActivities].some(v=>norm(v).includes(q))).slice(0,6)}
function sectionRows(q){return sections.filter(x=>norm(x.title+' '+x.terms).includes(q)).slice(0,5)}
function open(){if(panel)panel.classList.add('open')}
function close(){panel?.classList.remove('open')}
function render(){if(!box||!panel)return;const q=norm(box.value);if(!q){panel.innerHTML='<div class="gs-empty"><b>Search Happy Tails</b><span>Find pets, breeds, species or jump to a feature.</span></div>';open();return}const pets=profileRows(q),nav=sectionRows(q);panel.innerHTML=`${pets.length?`<div class="gs-group"><div class="gs-label">Pets</div>${pets.map(p=>`<button class="gs-row" data-pet="${p.id}"><span class="gs-avatar">${p.avatarUrl?`<img src="${p.avatarUrl}" alt="">`:typeof petEmoji==='function'?petEmoji(p.species):'🐾'}</span><span><b>${esc(p.name)}</b><small>${esc(p.handle||'')} · ${esc(p.breed||p.species||'Pet')}</small></span></button>`).join('')}</div>`:''}${nav.length?`<div class="gs-group"><div class="gs-label">Happy Tails</div>${nav.map(x=>`<button class="gs-row" data-view="${x.view}"><span class="gs-feature">${x.icon}</span><span><b>${esc(x.title)}</b><small>Open ${esc(x.title)}</small></span></button>`).join('')}</div>`:''}${!pets.length&&!nav.length?'<div class="gs-empty"><b>No results found</b><span>Try a pet name, breed, species, “vaccination”, “sitter” or “shop”.</span></div>':''}`;open();panel.querySelectorAll('[data-view]').forEach(el=>el.onclick=()=>{close();box.value='';if(typeof window.showView==='function')window.showView(el.dataset.view)});panel.querySelectorAll('[data-pet]').forEach(el=>el.onclick=()=>{const id=Number(el.dataset.pet);close();box.value='';if(typeof window.showView==='function')window.showView('discover');setTimeout(()=>{const card=[...document.querySelectorAll('#discoverGrid .pet-card')].find(c=>c.innerText.includes((window.profiles||[]).find(p=>Number(p.id)===id)?.name||'__none__'));card?.scrollIntoView({behavior:'smooth',block:'center'});card?.classList.add('gs-highlight');setTimeout(()=>card?.classList.remove('gs-highlight'),1600)},150)})}
function init(){box=document.getElementById('globalSearch');if(!box||document.getElementById('globalSearchPanel'))return;box.setAttribute('autocomplete','off');box.setAttribute('aria-label','Search Happy Tails');panel=document.createElement('div');panel.id='globalSearchPanel';panel.className='global-search-panel';box.parentElement.style.position='relative';box.insertAdjacentElement('afterend',panel);box.addEventListener('focus',render);box.addEventListener('input',render);box.addEventListener('keydown',e=>{if(e.key==='Escape'){close();box.blur()}if(e.key==='Enter'){const first=panel?.querySelector('.gs-row');if(first){e.preventDefault();first.click()}}});document.addEventListener('click',e=>{if(e.target!==box&&!panel.contains(e.target))close()});document.addEventListener('keydown',e=>{if((e.ctrlKey||e.metaKey)&&e.key.toLowerCase()==='k'){e.preventDefault();box.focus();box.select();render()}})}
document.readyState==='loading'?document.addEventListener('DOMContentLoaded',init):init();setTimeout(init,900);
})();