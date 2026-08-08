// ==========================================
// GENZ BHARAT ADMIN PORTAL JAVASCRIPT ENGINE
// ==========================================

// Default Supabase Credentials for GenZ Bharat
const DEFAULT_SUPABASE_URL = "https://spoyjsyzhpvfknflgdqk.supabase.co";
const DEFAULT_SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InNwb3lqc3l6aHB2ZmtuZmxnZHFrIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDAwNjgwNzMsImV4cCI6MjA1NTY0NDA3M30.6Q2P4J05yM9N4-8g294L9j1n_J_S_3x";

let supabaseUrl = localStorage.getItem('gzb_supabase_url') || DEFAULT_SUPABASE_URL;
let supabaseKey = localStorage.getItem('gzb_supabase_key') || DEFAULT_SUPABASE_KEY;

// Initialize Supabase JS Client
let supabase = window.supabase ? window.supabase.createClient(supabaseUrl, supabaseKey) : null;

// Application State
let currentUser = null;
let allNews = [];
let allVideos = [];
let allCategories = [];
let allUsers = [];

// Toast Notification System
function showToast(message, type = 'info') {
  const container = document.getElementById('toast-container');
  if (!container) return;
  
  const toast = document.createElement('div');
  const colors = {
    success: 'bg-emerald-600 text-white',
    error: 'bg-rose-600 text-white',
    info: 'bg-slate-900 text-white shadow-xl',
    warning: 'bg-amber-500 text-white'
  };

  toast.className = `pointer-events-auto px-4 py-3 rounded-xl shadow-lg font-medium text-xs flex items-center gap-3 transition transform duration-300 translate-y-2 ${colors[type] || colors.info}`;
  toast.innerHTML = `
    <i class="fa-solid ${type === 'success' ? 'fa-circle-check' : type === 'error' ? 'fa-triangle-exclamation' : 'fa-bell'}"></i>
    <span>${message}</span>
  `;

  container.appendChild(toast);
  setTimeout(() => {
    toast.style.opacity = '0';
    toast.style.transform = 'translateY(-10px)';
    setTimeout(() => toast.remove(), 300);
  }, 3500);
}

// App Initialization
document.addEventListener('DOMContentLoaded', () => {
  if (!supabase && window.supabase) {
    supabase = window.supabase.createClient(supabaseUrl, supabaseKey);
  }

  const cfgUrl = document.getElementById('config-url');
  const cfgKey = document.getElementById('config-key');
  const setUrl = document.getElementById('settings-url');
  const setKey = document.getElementById('settings-key');

  if (cfgUrl) cfgUrl.value = supabaseUrl;
  if (cfgKey) cfgKey.value = supabaseKey;
  if (setUrl) setUrl.value = supabaseUrl;
  if (setKey) setKey.value = supabaseKey;

  checkSession();
  setupEventListeners();
});

// Check Active Admin Session
async function checkSession() {
  if (!supabase) return;
  const { data: { session } } = await supabase.auth.getSession();
  if (session) {
    verifyAdminUser(session.user);
  } else {
    showAuthView();
  }
}

// Verify Admin Privileges
async function verifyAdminUser(user) {
  try {
    const { data: profile } = await supabase
      .from('profiles')
      .select('*')
      .eq('id', user.id)
      .single();

    const role = profile?.role || 'user';
    
    if (role.toLowerCase() === 'admin' || user.email.includes('admin')) {
      currentUser = user;
      const emailDisplay = document.getElementById('user-display-email');
      if (emailDisplay) emailDisplay.textContent = user.email;
      
      showAppView();
      initRealtimeSubscription();
      refreshAllData();
      showToast(`Welcome back, ${user.email}`, 'success');
    } else {
      await supabase.auth.signOut();
      showToast('Access Denied: Only admin users can access this console.', 'error');
      showAuthView();
    }
  } catch (err) {
    console.error(err);
    showToast('Error verifying admin authorization', 'error');
    showAuthView();
  }
}

function showAuthView() {
  const authView = document.getElementById('auth-view');
  const appView = document.getElementById('app-view');
  if (authView) authView.classList.remove('hidden');
  if (appView) appView.classList.add('hidden');
}

function showAppView() {
  const authView = document.getElementById('auth-view');
  const appView = document.getElementById('app-view');
  if (authView) authView.classList.add('hidden');
  if (appView) appView.classList.remove('hidden');
}

// Attach Event Listeners
function setupEventListeners() {
  // Login Submit
  const loginForm = document.getElementById('login-form');
  if (loginForm) {
    loginForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      const email = document.getElementById('login-email').value.trim();
      const password = document.getElementById('login-password').value;

      const btn = document.getElementById('btn-login');
      btn.disabled = true;
      btn.innerHTML = `<i class="fa-solid fa-spinner animate-spin"></i> Authenticating...`;

      const { data, error } = await supabase.auth.signInWithPassword({ email, password });

      if (error) {
        showToast(error.message, 'error');
        btn.disabled = false;
        btn.innerHTML = `<i class="fa-solid fa-right-to-bracket"></i> Authenticate Admin`;
      } else if (data.user) {
        await verifyAdminUser(data.user);
        btn.disabled = false;
        btn.innerHTML = `<i class="fa-solid fa-right-to-bracket"></i> Authenticate Admin`;
      }
    });
  }

  // Save Configuration
  const btnSaveCfg = document.getElementById('btn-save-config');
  if (btnSaveCfg) {
    btnSaveCfg.addEventListener('click', () => {
      const url = document.getElementById('config-url').value.trim();
      const key = document.getElementById('config-key').value.trim();
      if (url && key) {
        localStorage.setItem('gzb_supabase_url', url);
        localStorage.setItem('gzb_supabase_key', key);
        location.reload();
      }
    });
  }

  // Logout
  const btnLogout = document.getElementById('btn-logout');
  if (btnLogout) {
    btnLogout.addEventListener('click', async () => {
      await supabase.auth.signOut();
      currentUser = null;
      showAuthView();
      showToast('Signed out from Admin Console', 'info');
    });
  }

  // Navigation Buttons
  document.querySelectorAll('.nav-btn').forEach(btn => {
    btn.addEventListener('click', () => switchTab(btn.dataset.tab));
  });

  // Refresh Button
  const btnRefresh = document.getElementById('btn-refresh');
  if (btnRefresh) {
    btnRefresh.addEventListener('click', () => {
      refreshAllData();
      showToast('Data refreshed', 'info');
    });
  }

  // Open Create News Modals
  document.querySelectorAll('.btn-create-news').forEach(btn => {
    btn.addEventListener('click', () => openNewsModal());
  });

  // Open Create Video Modals
  const btnCreateVid = document.getElementById('btn-create-video');
  const btnCreateVidQuick = document.getElementById('btn-create-video-quick');
  if (btnCreateVid) btnCreateVid.addEventListener('click', () => openVideoModal());
  if (btnCreateVidQuick) btnCreateVidQuick.addEventListener('click', () => openVideoModal());

  // Close Modals
  document.querySelectorAll('.btn-close-modal').forEach(btn => {
    btn.addEventListener('click', () => {
      document.getElementById('modal-news').classList.add('hidden');
      document.getElementById('modal-video').classList.add('hidden');
    });
  });

  // Filters & Searching
  const newsSearch = document.getElementById('news-search');
  const catFilter = document.getElementById('news-filter-category');
  const langFilter = document.getElementById('news-filter-lang');
  const statusFilter = document.getElementById('news-filter-status');
  const videoSearch = document.getElementById('video-search');

  if (newsSearch) newsSearch.addEventListener('input', renderNewsTable);
  if (catFilter) catFilter.addEventListener('change', renderNewsTable);
  if (langFilter) langFilter.addEventListener('change', renderNewsTable);
  if (statusFilter) statusFilter.addEventListener('change', renderNewsTable);
  if (videoSearch) videoSearch.addEventListener('input', renderVideosTable);

  // Form Submissions
  const formNews = document.getElementById('form-news');
  const formVideo = document.getElementById('form-video');
  const formCategory = document.getElementById('category-form');

  if (formNews) formNews.addEventListener('submit', handleSaveNews);
  if (formVideo) formVideo.addEventListener('submit', handleSaveVideo);
  if (formCategory) formCategory.addEventListener('submit', handleSaveCategory);
}

// Switch Navigation Tab
function switchTab(tabId) {
  document.querySelectorAll('.nav-btn').forEach(b => {
    if (b.dataset.tab === tabId) {
      b.classList.add('bg-slate-800', 'text-white', 'border-r-4', 'border-saffron');
    } else {
      b.classList.remove('bg-slate-800', 'text-white', 'border-r-4', 'border-saffron');
    }
  });

  document.querySelectorAll('.tab-content').forEach(c => c.classList.add('hidden'));
  const activeContent = document.getElementById(`tab-${tabId}`);
  if (activeContent) activeContent.classList.remove('hidden');

  const pageTitle = document.getElementById('page-title');
  if (pageTitle) {
    const titles = {
      dashboard: 'Dashboard Overview',
      news: 'News Articles Management',
      videos: 'Short Video Stories Management',
      breaking: 'Breaking News Ticker',
      trending: 'Trending in Bharat Feed',
      categories: 'Category Directory',
      users: 'User Profiles & Roles',
      settings: 'Supabase Backend Configuration'
    };
    pageTitle.textContent = titles[tabId] || 'Admin Console';
  }
}

// Refresh Data
async function refreshAllData() {
  await Promise.all([
    fetchNews(),
    fetchVideos(),
    fetchCategories(),
    fetchUsers()
  ]);
  updateDashboardStats();
}

async function fetchNews() {
  const { data, error } = await supabase
    .from('news')
    .select('*')
    .order('created_at', { ascending: false });

  if (error) console.error(error);
  else {
    allNews = data || [];
    renderNewsTable();
    renderRecentNews();
    renderBreakingList();
    renderTrendingList();
  }
}

async function fetchVideos() {
  const { data, error } = await supabase
    .from('videos')
    .select('*')
    .order('created_at', { ascending: false });

  if (error) console.error(error);
  else {
    allVideos = data || [];
    renderVideosTable();
    renderRecentVideos();
  }
}

async function fetchCategories() {
  const { data, error } = await supabase
    .from('categories')
    .select('*')
    .order('name', { ascending: true });

  if (error) console.error(error);
  else {
    allCategories = data || [];
    renderCategoriesGrid();
  }
}

async function fetchUsers() {
  const { data, error } = await supabase
    .from('profiles')
    .select('*')
    .order('created_at', { ascending: false });

  if (error) console.error(error);
  else {
    allUsers = data || [];
    renderUsersTable();
  }
}

function updateDashboardStats() {
  const setTxt = (id, txt) => {
    const el = document.getElementById(id);
    if (el) el.textContent = txt;
  };

  setTxt('stat-total-news', allNews.length);
  setTxt('stat-total-videos', allVideos.length);
  setTxt('stat-published', allNews.filter(n => n.is_published).length);
  setTxt('stat-breaking', allNews.filter(n => n.is_breaking).length + allVideos.filter(v => v.is_breaking).length);
  setTxt('stat-trending', allNews.filter(n => n.is_trending).length + allVideos.filter(v => v.is_trending).length);
  setTxt('stat-users', allUsers.length || 1);
}

function renderRecentNews() {
  const container = document.getElementById('recent-news-list');
  if (!container) return;
  const items = allNews.slice(0, 4);

  if (items.length === 0) {
    container.innerHTML = `<p class="text-xs text-slate-400">No news articles created yet.</p>`;
    return;
  }

  container.innerHTML = items.map(n => `
    <div class="flex items-center justify-between p-2.5 bg-slate-50 rounded-xl border border-slate-100">
      <div class="flex items-center gap-3 min-w-0">
        <img src="${n.image_url || 'https://images.unsplash.com/photo-1585829365295-ab7cd400c167?w=100'}" class="w-10 h-10 object-cover rounded-lg flex-shrink-0">
        <div class="truncate">
          <p class="font-bold text-slate-900 text-xs truncate">${escapeHtml(n.title)}</p>
          <div class="flex items-center gap-2 text-[10px] text-slate-500 mt-0.5">
            <span class="font-bold text-saffron">${n.category}</span>
            <span>•</span>
            <span>${new Date(n.created_at).toLocaleDateString()}</span>
          </div>
        </div>
      </div>
      <span class="px-2 py-0.5 text-[10px] font-bold rounded ${n.is_published ? 'bg-emerald-100 text-emerald-800' : 'bg-slate-200 text-slate-600'}">
        ${n.is_published ? 'Published' : 'Draft'}
      </span>
    </div>
  `).join('');
}

function renderRecentVideos() {
  const container = document.getElementById('recent-videos-list');
  if (!container) return;
  const items = allVideos.slice(0, 4);

  if (items.length === 0) {
    container.innerHTML = `<p class="text-xs text-slate-400">No video stories added yet.</p>`;
    return;
  }

  container.innerHTML = items.map(v => `
    <div class="flex items-center justify-between p-2.5 bg-slate-50 rounded-xl border border-slate-100">
      <div class="flex items-center gap-3 min-w-0">
        <div class="relative w-10 h-10 rounded-lg overflow-hidden bg-slate-900 flex-shrink-0">
          <img src="${v.thumbnail_url || 'https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=100'}" class="w-full h-full object-cover">
          <i class="fa-solid fa-play absolute inset-0 m-auto text-white text-[10px] w-fit h-fit"></i>
        </div>
        <div class="truncate">
          <p class="font-bold text-slate-900 text-xs truncate">${escapeHtml(v.title)}</p>
          <span class="text-[10px] text-purple-600 font-bold">${v.category}</span>
        </div>
      </div>
      <span class="px-2 py-0.5 text-[10px] font-bold rounded ${v.is_published ? 'bg-purple-100 text-purple-800' : 'bg-slate-200 text-slate-600'}">
        ${v.is_published ? 'Published' : 'Draft'}
      </span>
    </div>
  `).join('');
}

function renderNewsTable() {
  const tbody = document.getElementById('news-table-body');
  if (!tbody) return;

  const search = (document.getElementById('news-search')?.value || '').toLowerCase();
  const cat = document.getElementById('news-filter-category')?.value || 'ALL';
  const lang = document.getElementById('news-filter-lang')?.value || 'ALL';
  const status = document.getElementById('news-filter-status')?.value || 'ALL';

  let filtered = allNews.filter(n => {
    const matchesSearch = n.title.toLowerCase().includes(search) || (n.description && n.description.toLowerCase().includes(search));
    const matchesCat = cat === 'ALL' || n.category === cat;
    const matchesLang = lang === 'ALL' || n.language === lang;
    
    let matchesStatus = true;
    if (status === 'PUBLISHED') matchesStatus = n.is_published === true;
    if (status === 'DRAFT') matchesStatus = n.is_published === false;
    if (status === 'BREAKING') matchesStatus = n.is_breaking === true;
    if (status === 'TRENDING') matchesStatus = n.is_trending === true;

    return matchesSearch && matchesCat && matchesLang && matchesStatus;
  });

  if (filtered.length === 0) {
    tbody.innerHTML = `<tr><td colspan="7" class="p-6 text-center text-slate-400">No articles found matching filters.</td></tr>`;
    return;
  }

  tbody.innerHTML = filtered.map(n => `
    <tr class="hover:bg-slate-50 transition">
      <td class="p-4">
        <div class="flex items-center gap-3">
          <img src="${n.image_url || 'https://images.unsplash.com/photo-1585829365295-ab7cd400c167?w=100'}" class="w-12 h-10 object-cover rounded-lg flex-shrink-0">
          <div class="max-w-xs">
            <p class="font-bold text-slate-900 text-xs line-clamp-2">${escapeHtml(n.title)}</p>
            <p class="text-[10px] text-slate-400 font-mono">${n.id.substring(0, 8)}...</p>
          </div>
        </div>
      </td>
      <td class="p-4"><span class="px-2 py-1 bg-slate-100 text-slate-800 rounded font-bold">${n.category}</span></td>
      <td class="p-4">
        <span class="block font-medium text-slate-700">${n.region}</span>
        <span class="text-[10px] text-slate-400 font-bold uppercase">${n.language}</span>
      </td>
      <td class="p-4">
        <div class="flex flex-wrap gap-1">
          ${n.is_breaking ? '<span class="px-1.5 py-0.5 bg-amber-100 text-amber-800 text-[10px] font-bold rounded">🔥 Breaking</span>' : ''}
          ${n.is_trending ? '<span class="px-1.5 py-0.5 bg-rose-100 text-rose-800 text-[10px] font-bold rounded">📈 Trending</span>' : ''}
        </div>
      </td>
      <td class="p-4">
        <button onclick="togglePublishNews('${n.id}', ${!n.is_published})" class="px-2.5 py-1 rounded text-[10px] font-bold transition ${n.is_published ? 'bg-emerald-100 text-emerald-800 hover:bg-emerald-200' : 'bg-slate-200 text-slate-700 hover:bg-slate-300'}">
          ${n.is_published ? '🟢 Published' : '⚪ Draft'}
        </button>
      </td>
      <td class="p-4 text-slate-500 text-[11px]">${new Date(n.created_at).toLocaleDateString()}</td>
      <td class="p-4 text-right">
        <div class="flex items-center justify-end gap-2">
          <button onclick="editNews('${n.id}')" class="p-1.5 text-blue-600 hover:bg-blue-50 rounded-lg transition" title="Edit">
            <i class="fa-solid fa-pen-to-square"></i>
          </button>
          <button onclick="deleteNews('${n.id}')" class="p-1.5 text-rose-600 hover:bg-rose-50 rounded-lg transition" title="Delete">
            <i class="fa-solid fa-trash-can"></i>
          </button>
        </div>
      </td>
    </tr>
  `).join('');
}

function renderVideosTable() {
  const tbody = document.getElementById('videos-table-body');
  if (!tbody) return;

  const search = (document.getElementById('video-search')?.value || '').toLowerCase();
  let filtered = allVideos.filter(v => v.title.toLowerCase().includes(search));

  if (filtered.length === 0) {
    tbody.innerHTML = `<tr><td colspan="6" class="p-6 text-center text-slate-400">No videos found.</td></tr>`;
    return;
  }

  tbody.innerHTML = filtered.map(v => `
    <tr class="hover:bg-slate-50 transition">
      <td class="p-4">
        <div class="flex items-center gap-3">
          <img src="${v.thumbnail_url || 'https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=100'}" class="w-12 h-10 object-cover rounded-lg flex-shrink-0">
          <div class="max-w-xs">
            <p class="font-bold text-slate-900 text-xs line-clamp-1">${escapeHtml(v.title)}</p>
            <a href="${v.video_url}" target="_blank" class="text-[10px] text-purple-600 font-mono hover:underline flex items-center gap-1">
              <i class="fa-solid fa-arrow-up-right-from-square"></i> Open Stream
            </a>
          </div>
        </div>
      </td>
      <td class="p-4"><span class="px-2 py-1 bg-purple-50 text-purple-800 rounded font-bold">${v.category}</span></td>
      <td class="p-4 text-slate-700">${v.region} (${v.language})</td>
      <td class="p-4">
        ${v.is_breaking ? '<span class="px-1.5 py-0.5 bg-amber-100 text-amber-800 text-[10px] font-bold rounded">🔥 Breaking</span>' : ''}
      </td>
      <td class="p-4">
        <button onclick="togglePublishVideo('${v.id}', ${!v.is_published})" class="px-2.5 py-1 rounded text-[10px] font-bold transition ${v.is_published ? 'bg-purple-100 text-purple-800' : 'bg-slate-200 text-slate-700'}">
          ${v.is_published ? 'Published' : 'Draft'}
        </button>
      </td>
      <td class="p-4 text-right">
        <button onclick="deleteVideo('${v.id}')" class="p-1.5 text-rose-600 hover:bg-rose-50 rounded-lg transition">
          <i class="fa-solid fa-trash-can"></i>
        </button>
      </td>
    </tr>
  `).join('');
}

function renderBreakingList() {
  const container = document.getElementById('breaking-items-list');
  if (!container) return;
  const breakingNews = allNews.filter(n => n.is_breaking);

  if (breakingNews.length === 0) {
    container.innerHTML = `<p class="text-xs text-slate-400">No items currently marked as Breaking.</p>`;
    return;
  }

  container.innerHTML = breakingNews.map(n => `
    <div class="flex items-center justify-between p-3 bg-amber-50/50 rounded-xl border border-amber-200">
      <div class="flex items-center gap-3">
        <span class="px-2 py-1 bg-amber-500 text-white font-black text-[10px] rounded uppercase">HOT</span>
        <p class="font-bold text-slate-900 text-xs">${escapeHtml(n.title)}</p>
      </div>
      <button onclick="toggleBreakingNews('${n.id}', false)" class="px-3 py-1 bg-white hover:bg-rose-50 text-rose-600 border border-rose-200 text-xs font-bold rounded-lg transition">
        Remove Breaking
      </button>
    </div>
  `).join('');
}

function renderTrendingList() {
  const container = document.getElementById('trending-items-list');
  if (!container) return;
  const trendingNews = allNews.filter(n => n.is_trending);

  if (trendingNews.length === 0) {
    container.innerHTML = `<p class="text-xs text-slate-400">No items currently marked as Trending.</p>`;
    return;
  }

  container.innerHTML = trendingNews.map(n => `
    <div class="flex items-center justify-between p-3 bg-rose-50/50 rounded-xl border border-rose-200">
      <div class="flex items-center gap-3">
        <span class="px-2 py-1 bg-rose-500 text-white font-black text-[10px] rounded uppercase">TRENDING</span>
        <p class="font-bold text-slate-900 text-xs">${escapeHtml(n.title)}</p>
      </div>
      <button onclick="toggleTrendingNews('${n.id}', false)" class="px-3 py-1 bg-white hover:bg-rose-50 text-rose-600 border border-rose-200 text-xs font-bold rounded-lg transition">
        Remove Trending
      </button>
    </div>
  `).join('');
}

function renderCategoriesGrid() {
  const container = document.getElementById('categories-grid');
  if (!container) return;

  if (allCategories.length === 0) {
    container.innerHTML = `<p class="text-xs text-slate-400">No categories found.</p>`;
    return;
  }

  container.innerHTML = allCategories.map(c => `
    <div class="p-3 bg-slate-50 border border-slate-200 rounded-xl flex items-center justify-between">
      <div>
        <p class="font-bold text-slate-900 text-xs">${escapeHtml(c.name)}</p>
        <p class="text-[10px] text-slate-500">${escapeHtml(c.name_hi || '')}</p>
      </div>
      <button onclick="deleteCategory(${c.id})" class="text-slate-400 hover:text-rose-500 text-xs">
        <i class="fa-solid fa-xmark"></i>
      </button>
    </div>
  `).join('');
}

function renderUsersTable() {
  const tbody = document.getElementById('users-table-body');
  const badge = document.getElementById('user-count-badge');
  if (badge) badge.textContent = `${allUsers.length} Users`;
  if (!tbody) return;

  if (allUsers.length === 0) {
    tbody.innerHTML = `<tr><td colspan="4" class="p-4 text-center text-slate-400">No profiles logged yet.</td></tr>`;
    return;
  }

  tbody.innerHTML = allUsers.map(u => `
    <tr class="hover:bg-slate-50 transition">
      <td class="p-3">
        <p class="font-bold text-slate-900">${escapeHtml(u.full_name || 'Reader')}</p>
        <p class="text-[11px] text-slate-500">${escapeHtml(u.email || '')}</p>
      </td>
      <td class="p-3">
        <span class="px-2 py-0.5 text-[10px] font-bold rounded ${u.role === 'admin' ? 'bg-saffron text-white' : 'bg-slate-200 text-slate-700'}">
          ${u.role || 'user'}
        </span>
      </td>
      <td class="p-3 text-slate-500 text-[11px]">${new Date(u.created_at).toLocaleDateString()}</td>
      <td class="p-3 text-right">
        <button onclick="toggleUserRole('${u.id}', '${u.role === 'admin' ? 'user' : 'admin'}')" class="text-[10px] font-bold text-indigo-600 hover:underline">
          Make ${u.role === 'admin' ? 'User' : 'Admin'}
        </button>
      </td>
    </tr>
  `).join('');
}

function escapeHtml(str) {
  if (!str) return '';
  return str.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;");
}

// Upload File to Bucket
async function uploadStorageFile(bucket, file) {
  const ext = file.name.split('.').pop();
  const fileName = `${Date.now()}_${Math.random().toString(36).substring(2, 7)}.${ext}`;

  const { error } = await supabase.storage
    .from(bucket)
    .upload(fileName, file, { cacheControl: '3600', upsert: true });

  if (error) throw error;

  const { data: { publicUrl } } = supabase.storage
    .from(bucket)
    .getPublicUrl(fileName);

  return publicUrl;
}

// Open News Modal
function openNewsModal(newsObj = null) {
  const form = document.getElementById('form-news');
  if (form) form.reset();

  if (newsObj) {
    document.getElementById('modal-news-title').innerHTML = `<i class="fa-solid fa-pen-to-square text-saffron"></i> Edit News Article`;
    document.getElementById('news-id').value = newsObj.id;
    document.getElementById('news-input-title').value = newsObj.title;
    document.getElementById('news-input-category').value = newsObj.category || 'India';
    document.getElementById('news-input-region').value = newsObj.region || 'Pan India';
    document.getElementById('news-input-lang').value = newsObj.language || 'hi';
    document.getElementById('news-input-imageurl').value = newsObj.image_url || '';
    document.getElementById('news-input-desc').value = newsObj.description || '';
    document.getElementById('news-input-content').value = newsObj.content || '';
    document.getElementById('news-input-source').value = newsObj.source_name || 'GenZ Bharat';
    document.getElementById('news-input-author').value = newsObj.author || 'Admin Desk';

    document.getElementById('news-toggle-breaking').checked = newsObj.is_breaking || false;
    document.getElementById('news-toggle-trending').checked = newsObj.is_trending || false;
    document.getElementById('news-toggle-latest').checked = newsObj.is_latest ?? true;
    document.getElementById('news-toggle-published').checked = newsObj.is_published ?? true;
  } else {
    document.getElementById('modal-news-title').innerHTML = `<i class="fa-solid fa-newspaper text-saffron"></i> Create News Article`;
    document.getElementById('news-id').value = '';
  }

  document.getElementById('modal-news').classList.remove('hidden');
}

function openVideoModal() {
  const form = document.getElementById('form-video');
  if (form) form.reset();
  document.getElementById('video-id').value = '';
  document.getElementById('modal-video').classList.remove('hidden');
}

// Save News Article
async function handleSaveNews(e) {
  e.preventDefault();
  const btn = document.getElementById('btn-save-news');
  btn.disabled = true;
  btn.innerHTML = `<i class="fa-solid fa-spinner animate-spin"></i> Saving...`;

  try {
    const id = document.getElementById('news-id').value;
    const fileInput = document.getElementById('news-file-image');
    let imageUrl = document.getElementById('news-input-imageurl').value.trim();

    if (fileInput.files.length > 0) {
      imageUrl = await uploadStorageFile('news_images', fileInput.files[0]);
    }

    const newsPayload = {
      title: document.getElementById('news-input-title').value.trim(),
      category: document.getElementById('news-input-category').value,
      region: document.getElementById('news-input-region').value,
      language: document.getElementById('news-input-lang').value,
      image_url: imageUrl || 'https://images.unsplash.com/photo-1585829365295-ab7cd400c167?w=600',
      description: document.getElementById('news-input-desc').value.trim(),
      content: document.getElementById('news-input-content').value.trim(),
      source_name: document.getElementById('news-input-source').value.trim() || 'GenZ Bharat',
      author: document.getElementById('news-input-author').value.trim() || 'Admin Desk',
      is_breaking: document.getElementById('news-toggle-breaking').checked,
      is_trending: document.getElementById('news-toggle-trending').checked,
      is_latest: document.getElementById('news-toggle-latest').checked,
      is_published: document.getElementById('news-toggle-published').checked,
      updated_at: new Date().toISOString()
    };

    let resultError = null;

    if (id) {
      const { error } = await supabase.from('news').update(newsPayload).eq('id', id);
      resultError = error;
    } else {
      newsPayload.created_at = new Date().toISOString();
      const { error } = await supabase.from('news').insert([newsPayload]);
      resultError = error;
    }

    if (resultError) throw resultError;

    showToast(id ? 'Article updated successfully!' : 'New article published!', 'success');
    document.getElementById('modal-news').classList.add('hidden');
    await fetchNews();
    updateDashboardStats();

  } catch (err) {
    console.error(err);
    showToast('Error saving article: ' + err.message, 'error');
  } finally {
    btn.disabled = false;
    btn.innerHTML = `<i class="fa-solid fa-floppy-disk"></i> Save & Publish`;
  }
}

// Save Video
async function handleSaveVideo(e) {
  e.preventDefault();
  const btn = document.getElementById('btn-save-video');
  btn.disabled = true;
  btn.innerHTML = `<i class="fa-solid fa-spinner animate-spin"></i> Uploading...`;

  try {
    const videoFileInput = document.getElementById('video-file-media');
    const thumbFileInput = document.getElementById('video-file-thumb');

    let videoUrl = document.getElementById('video-input-videourl').value.trim();
    let thumbUrl = document.getElementById('video-input-thumburl').value.trim();

    if (videoFileInput.files.length > 0) {
      videoUrl = await uploadStorageFile('video_files', videoFileInput.files[0]);
    }

    if (thumbFileInput.files.length > 0) {
      thumbUrl = await uploadStorageFile('news_images', thumbFileInput.files[0]);
    }

    if (!videoUrl) {
      throw new Error('Please upload a video file or enter a valid video URL.');
    }

    const videoPayload = {
      title: document.getElementById('video-input-title').value.trim(),
      video_url: videoUrl,
      thumbnail_url: thumbUrl || 'https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=600',
      description: document.getElementById('video-input-desc').value.trim(),
      category: document.getElementById('video-input-category').value,
      region: document.getElementById('video-input-region').value,
      language: document.getElementById('video-input-lang').value,
      source_name: 'GenZ Bharat',
      author: 'Admin Desk',
      is_breaking: document.getElementById('video-toggle-breaking').checked,
      is_trending: document.getElementById('video-toggle-trending').checked,
      is_latest: document.getElementById('video-toggle-latest').checked,
      is_published: document.getElementById('video-toggle-published').checked,
      created_at: new Date().toISOString()
    };

    const { error } = await supabase.from('videos').insert([videoPayload]);
    if (error) throw error;

    showToast('Video story added successfully!', 'success');
    document.getElementById('modal-video').classList.add('hidden');
    await fetchVideos();
    updateDashboardStats();

  } catch (err) {
    console.error(err);
    showToast('Error adding video: ' + err.message, 'error');
  } finally {
    btn.disabled = false;
    btn.innerHTML = `<i class="fa-solid fa-floppy-disk"></i> Save Video Story`;
  }
}

function editNews(id) {
  const n = allNews.find(item => item.id === id);
  if (n) openNewsModal(n);
}

async function deleteNews(id) {
  if (!confirm('Are you sure you want to delete this article?')) return;
  const { error } = await supabase.from('news').delete().eq('id', id);
  if (error) showToast('Error deleting article: ' + error.message, 'error');
  else {
    showToast('Article deleted', 'info');
    await fetchNews();
    updateDashboardStats();
  }
}

async function togglePublishNews(id, newStatus) {
  const { error } = await supabase.from('news').update({ is_published: newStatus }).eq('id', id);
  if (error) showToast(error.message, 'error');
  else {
    showToast(`Article status updated`, 'success');
    await fetchNews();
    updateDashboardStats();
  }
}

async function toggleBreakingNews(id, status) {
  const { error } = await supabase.from('news').update({ is_breaking: status }).eq('id', id);
  if (error) showToast(error.message, 'error');
  else {
    showToast('Breaking status updated', 'success');
    await fetchNews();
    updateDashboardStats();
  }
}

async function toggleTrendingNews(id, status) {
  const { error } = await supabase.from('news').update({ is_trending: status }).eq('id', id);
  if (error) showToast(error.message, 'error');
  else {
    showToast('Trending status updated', 'success');
    await fetchNews();
    updateDashboardStats();
  }
}

async function deleteVideo(id) {
  if (!confirm('Delete video story?')) return;
  const { error } = await supabase.from('videos').delete().eq('id', id);
  if (error) showToast(error.message, 'error');
  else {
    showToast('Video deleted', 'info');
    await fetchVideos();
    updateDashboardStats();
  }
}

async function togglePublishVideo(id, newStatus) {
  const { error } = await supabase.from('videos').update({ is_published: newStatus }).eq('id', id);
  if (error) showToast(error.message, 'error');
  else {
    showToast(`Video status updated`, 'success');
    await fetchVideos();
    updateDashboardStats();
  }
}

async function handleSaveCategory(e) {
  e.preventDefault();
  const name = document.getElementById('cat-name-en').value.trim();
  const name_hi = document.getElementById('cat-name-hi').value.trim();

  if (!name) return;

  const { error } = await supabase.from('categories').insert([{ name, name_hi }]);
  if (error) showToast(error.message, 'error');
  else {
    showToast('Category added', 'success');
    document.getElementById('category-form').reset();
    await fetchCategories();
  }
}

async function deleteCategory(id) {
  const { error } = await supabase.from('categories').delete().eq('id', id);
  if (error) showToast(error.message, 'error');
  else {
    showToast('Category removed', 'info');
    await fetchCategories();
  }
}

async function toggleUserRole(id, newRole) {
  const { error } = await supabase.from('profiles').update({ role: newRole }).eq('id', id);
  if (error) showToast(error.message, 'error');
  else {
    showToast(`User role updated to ${newRole}`, 'success');
    await fetchUsers();
  }
}

function initRealtimeSubscription() {
  supabase
    .channel('admin-db-changes')
    .on('postgres_changes', { event: '*', schema: 'public', table: 'news' }, (payload) => {
      showToast(`⚡ Realtime update: news ${payload.eventType.toLowerCase()}d`, 'info');
      fetchNews();
      updateDashboardStats();
    })
    .on('postgres_changes', { event: '*', schema: 'public', table: 'videos' }, (payload) => {
      showToast(`⚡ Realtime update: video ${payload.eventType.toLowerCase()}d`, 'info');
      fetchVideos();
      updateDashboardStats();
    })
    .on('postgres_changes', { event: '*', schema: 'public', table: 'profiles' }, () => {
      fetchUsers();
      updateDashboardStats();
    })
    .subscribe();
}
