(()=>{
function byView(v){try{if(typeof window.showView==='function')window.showView(v)}catch{}sync(v);closeDrawer()}
function closeDrawer(){document.body.classList.remove('mobile-nav-open')}
function openDrawer(){document.body.classList.add('mobile-nav-open')}
function sync(view){document.querySelectorAll('.mobile-bottom-nav button').forEach(b=>b.classList.toggle('active',b.dataset.view===view))}
function install(){
 if(document.getElementById('mobileMenuBtn'))return;
 const top=document.querySelector('.topbar');if(!top)return;
 const menu=document.createElement('button');menu.id='mobileMenuBtn';menu.className='icon-btn mobile-menu-btn';menu.type='button';menu.title='Open navigation';menu.setAttribute('aria-label','Open navigation');menu.textContent='☰';menu.onclick=openDrawer;top.prepend(menu);
 const backdrop=document.createElement('div');backdrop.className='mobile-nav-backdrop';backdrop.onclick=closeDrawer;document.body.appendChild(backdrop);
 const bottom=document.createElement('nav');bottom.className='mobile-bottom-nav';bottom.setAttribute('aria-label','Primary mobile navigation');
 const items=[['home','⌂','Home'],['discover','◉','Discover'],['friends','👥','Friends'],['playdates','🎾','Play'],['profile','🐾','Profile']];
 bottom.innerHTML=items.map(([v,i,t])=>`<button type="button" data-view="${v}" aria-label="${t}"><span>${i}</span><span>${t}</span></button>`).join('');
 bottom.querySelectorAll('button').forEach(b=>b.onclick=()=>byView(b.dataset.view));document.body.appendChild(bottom);
 document.querySelectorAll('.side .nav button[data-view]').forEach(b=>b.addEventListener('click',()=>{sync(b.dataset.view);closeDrawer()}));
 const active=document.querySelector('.side .nav button.active[data-view]')?.dataset.view||'home';sync(active);
 document.addEventListener('keydown',e=>{if(e.key==='Escape')closeDrawer()});
}
const original=window.showView;if(typeof original==='function'&&!original.__mobileNav){window.showView=function(v){const r=original.apply(this,arguments);sync(v);closeDrawer();return r};window.showView.__mobileNav=true}
if(document.readyState==='loading')document.addEventListener('DOMContentLoaded',install);else install();setTimeout(install,800);
})();