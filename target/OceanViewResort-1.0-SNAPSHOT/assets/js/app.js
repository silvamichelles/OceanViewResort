/**
 * app.js  –  OceanView Resort Dashboard Application
 * Handles navigation, data rendering, CRUD operations, modals and toasts.
 */
'use strict';

/* ══════════════════════════════════════════════════════════
   UTILITIES
══════════════════════════════════════════════════════════ */

/** Toast notification system */
const Toast = (() => {
  const container = document.getElementById('toast-container');
  const icons = { success: '✅', error: '❌', info: 'ℹ️', warning: '⚠️' };

  function show(message, type = 'info', duration = 3500) {
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.innerHTML = `
      <span class="toast-icon">${icons[type] ?? 'ℹ️'}</span>
      <span class="toast-text">${message}</span>
      <button class="toast-close" onclick="this.closest('.toast').remove()">✕</button>`;
    container.appendChild(toast);
    const remove = () => {
      toast.classList.add('removing');
      toast.addEventListener('animationend', () => toast.remove(), { once: true });
    };
    const t = setTimeout(remove, duration);
    toast.querySelector('.toast-close').addEventListener('click', () => { clearTimeout(t); });
  }
  return { show, success: m => show(m, 'success'), error: m => show(m, 'error'), info: m => show(m, 'info'), warning: m => show(m, 'warning') };
})();

/** Modal management */
const Modal = (() => {
  function open(id) {
    const el = document.getElementById(id);
    if (!el) return;
    el.classList.add('open');
    document.body.style.overflow = 'hidden';
  }
  function close(id) {
    const el = document.getElementById(id);
    if (!el) return;
    el.classList.remove('open');
    document.body.style.overflow = '';
  }
  function closeAllByClick(e) {
    if (e.target.classList.contains('modal-overlay')) close(e.target.id);
  }
  document.querySelectorAll('.modal-overlay').forEach(m => {
    m.addEventListener('click', closeAllByClick);
  });
  document.querySelectorAll('[data-close-modal]').forEach(btn => {
    btn.addEventListener('click', () => close(btn.closest('.modal-overlay').id));
  });
  return { open, close };
})();

/** Simple confirm dialog */
function confirmDialog(message, title = 'Confirm Action') {
  return new Promise(resolve => {
    document.getElementById('confirm-title').textContent = title;
    document.getElementById('confirm-message').textContent = message;
    Modal.open('modal-confirm');
    const yes = document.getElementById('confirm-yes');
    const no  = document.getElementById('confirm-no');
    const cleanup = val => {
      Modal.close('modal-confirm');
      yes.replaceWith(yes.cloneNode(true)); // remove old listeners
      no.replaceWith(no.cloneNode(true));
      resolve(val);
    };
    document.getElementById('confirm-yes').addEventListener('click', () => cleanup(true),  { once: true });
    document.getElementById('confirm-no').addEventListener('click',  () => cleanup(false), { once: true });
  });
}

/** Format currency (LKR) */
const fmt = n => 'LKR ' + Number(n).toLocaleString('en-LK', { minimumFractionDigits: 2 });

/** Format date string */
const fmtDate = s => s ? new Date(s).toLocaleDateString('en-GB', { day:'2-digit', month:'short', year:'numeric' }) : '—';

/** Status badge HTML */
function badge(status) {
  const map = {
    'Available': 'badge-success', 'Booked': 'badge-danger',
    'Maintenance': 'badge-warning', 'Confirmed': 'badge-info',
    'Checked In': 'badge-success', 'Cancelled': 'badge-gray',
  };
  const cls = map[status] ?? 'badge-gray';
  return `<span class="badge ${cls}">${status ?? '—'}</span>`;
}

/* ══════════════════════════════════════════════════════════
   NAVIGATION
══════════════════════════════════════════════════════════ */
const navItems = document.querySelectorAll('.nav-item[data-view]');
const views    = document.querySelectorAll('.view');
const titleEl  = document.querySelector('.page-title h2');
const subEl    = document.querySelector('.page-title p');

const viewMeta = {
  dashboard:    { title: 'Dashboard',    sub: 'Resort overview & key metrics' },
  reservations: { title: 'Reservations', sub: 'Manage all room bookings' },
  guests:       { title: 'Guests',       sub: 'Guest directory & management' },
  rooms:        { title: 'Rooms',        sub: 'Room inventory & status' },
  billing:      { title: 'Billing',      sub: 'Bills, invoices & revenue' },
};

function navigate(viewId) {
  navItems.forEach(n => n.classList.toggle('active', n.dataset.view === viewId));
  views.forEach(v => v.classList.toggle('active', v.id === 'view-' + viewId));
  const meta = viewMeta[viewId] ?? {};
  titleEl.textContent = meta.title ?? '';
  subEl.textContent   = meta.sub   ?? '';
  // Lazy-load data on first visit
  loaders[viewId]?.();
}

navItems.forEach(n => n.addEventListener('click', () => navigate(n.dataset.view)));

// Hamburger for mobile
document.getElementById('hamburger')?.addEventListener('click', () => {
  document.querySelector('.sidebar').classList.toggle('open');
  document.getElementById('sidebar-backdrop').classList.toggle('open');
});
document.getElementById('sidebar-backdrop')?.addEventListener('click', () => {
  document.querySelector('.sidebar').classList.remove('open');
  document.getElementById('sidebar-backdrop').classList.remove('open');
});

/* ══════════════════════════════════════════════════════════
   DATA LOADERS  (each is called once, then replaced by noop)
══════════════════════════════════════════════════════════ */
const loaders = {
  dashboard:    () => { loadDashboard();    loaders.dashboard    = null; },
  reservations: () => { loadReservations(); loaders.reservations = null; },
  guests:       () => { loadGuests();       loaders.guests       = null; },
  rooms:        () => { loadRooms();        loaders.rooms        = null; },
  billing:      () => { loadBilling();      loaders.billing      = null; },
};

/* ══════════════════════════════════════════════════════════
   ①  DASHBOARD
══════════════════════════════════════════════════════════ */
async function loadDashboard() {
  try {
    const data = await API.get('/api/dashboard');
    // Update user greeting
    const name = data.username ?? 'Staff';
    document.getElementById('user-name').textContent = name.charAt(0).toUpperCase() + name.slice(1);
    document.getElementById('avatar-initials').textContent = name.charAt(0).toUpperCase();
    document.getElementById('greeting-name').textContent = name.charAt(0).toUpperCase() + name.slice(1);

    // Stat cards
    animateCount('stat-total-res',   parseInt(data.total    ?? 0));
    animateCount('stat-available',   parseInt(data.available ?? 0));
    animateCount('stat-booked',      parseInt(data.booked   ?? 0));
    document.getElementById('stat-revenue').textContent = data.revenue ?? 'LKR 0K';
  } catch (e) {
    Toast.error('Failed to load dashboard stats.');
  }

  // Load recent reservations for the mini-table
  try {
    const list = await API.get('/api/reservations');
    const tbody = document.getElementById('recent-res-tbody');
    const recent = list.slice(0, 8);
    tbody.innerHTML = recent.length ? recent.map(r => `
      <tr>
        <td class="td-mono">${r.reservationNumber}</td>
        <td class="td-name">${r.guestName}</td>
        <td>${r.roomType}</td>
        <td>${fmtDate(r.checkIn)}</td>
        <td>${fmtDate(r.checkOut)}</td>
        <td>${badge(r.status)}</td>
        <td class="td-amount">${fmt(r.amount)}</td>
      </tr>`).join('') : emptyRow(7, 'No reservations yet');
  } catch (e) { /* silent */ }
}

function animateCount(id, target) {
  const el = document.getElementById(id);
  if (!el) return;
  let current = 0;
  const step = Math.ceil(target / 30);
  const t = setInterval(() => {
    current = Math.min(current + step, target);
    el.textContent = current;
    if (current >= target) clearInterval(t);
  }, 30);
}

/* ══════════════════════════════════════════════════════════
   ②  RESERVATIONS
══════════════════════════════════════════════════════════ */
let reservationsCache = [];

async function loadReservations(forceRefresh = false) {
  if (loaders.reservations === null || forceRefresh) {
    if (forceRefresh) setTableLoading('res-tbody', 8);
    try {
      reservationsCache = await API.get('/api/reservations');
      renderReservationsTable(reservationsCache);
    } catch (e) {
      Toast.error('Failed to load reservations.');
    }
  }
}

function renderReservationsTable(data) {
  const tbody = document.getElementById('res-tbody');
  tbody.innerHTML = data.length ? data.map(r => `
    <tr data-resno="${esc(r.reservationNumber)}">
      <td class="td-mono">${r.reservationNumber}</td>
      <td><div class="td-name">${r.guestName}</div><div class="td-muted">${r.contact}</div></td>
      <td>${r.roomType}</td>
      <td>${fmtDate(r.checkIn)}</td>
      <td>${fmtDate(r.checkOut)}</td>
      <td>${r.nights} night${r.nights !== 1 ? 's' : ''}</td>
      <td class="td-amount">${fmt(r.amount)}</td>
      <td>${badge(r.status)}</td>
      <td><div class="td-actions">
        <button class="btn btn-sm btn-danger btn-icon" title="Cancel" onclick="deleteReservation('${esc(r.reservationNumber)}')">🗑️</button>
      </div></td>
    </tr>`).join('') : emptyRow(9, '📅 No reservations found', 'Create a new reservation using the button above');
}

async function deleteReservation(resNo) {
  const ok = await confirmDialog(`Cancel reservation ${resNo}? This will also delete the associated bill and free the room.`, 'Cancel Reservation');
  if (!ok) return;
  try {
    await API.delete('/api/reservations/' + encodeURIComponent(resNo));
    Toast.success(`Reservation ${resNo} cancelled.`);
    loaders.reservations = () => { loadReservations(true); };
    loaders.dashboard    = () => { loadDashboard(); };
    await loadReservations(true);
  } catch (e) {
    Toast.error(e.message ?? 'Failed to delete reservation.');
  }
}

// New Reservation modal
document.getElementById('btn-new-reservation')?.addEventListener('click', async () => {
  // Populate room dropdown
  const sel = document.getElementById('res-room-select');
  sel.innerHTML = '<option value="">Loading rooms…</option>';
  Modal.open('modal-new-reservation');
  try {
    const rooms = await API.get('/api/reservations/rooms');
    sel.innerHTML = rooms.length
      ? rooms.map(r => `<option value="${r}">Room ${r}</option>`).join('')
      : '<option value="">No available rooms</option>';
  } catch { sel.innerHTML = '<option value="">Error loading rooms</option>'; }
});

document.getElementById('form-new-reservation')?.addEventListener('submit', async e => {
  e.preventDefault();
  const fd  = new FormData(e.target);
  const btn = e.target.querySelector('[type=submit]');
  btn.disabled = true; btn.innerHTML = '<span class="spinner"></span> Saving…';
  try {
    await API.post('/api/reservations', fd);
    Toast.success('Reservation created successfully!');
    Modal.close('modal-new-reservation');
    e.target.reset();
    await loadReservations(true);
    // Refresh dashboard stats
    loaders.dashboard = () => loadDashboard();
  } catch (err) {
    Toast.error(err.message ?? 'Failed to create reservation.');
  } finally {
    btn.disabled = false; btn.innerHTML = '💾 Save Reservation';
  }
});

// Search reservations
document.getElementById('res-search')?.addEventListener('input', e => {
  const q = e.target.value.toLowerCase();
  const filtered = reservationsCache.filter(r =>
    r.reservationNumber?.toLowerCase().includes(q) ||
    r.guestName?.toLowerCase().includes(q) ||
    r.roomType?.toLowerCase().includes(q)
  );
  renderReservationsTable(filtered);
});

/* ══════════════════════════════════════════════════════════
   ③  GUESTS
══════════════════════════════════════════════════════════ */
let guestsCache = [];

async function loadGuests(forceRefresh = false) {
  if (loaders.guests === null || forceRefresh) {
    if (forceRefresh) setTableLoading('guests-tbody', 5);
    try {
      guestsCache = await API.get('/api/guests');
      renderGuestsTable(guestsCache);
    } catch (e) {
      Toast.error('Failed to load guests.');
    }
  }
}

function renderGuestsTable(data) {
  const tbody = document.getElementById('guests-tbody');
  tbody.innerHTML = data.length ? data.map(g => `
    <tr data-guestid="${g.guestId}">
      <td class="td-mono">#${g.guestId}</td>
      <td><div class="td-name">${g.fullName}</div></td>
      <td>${g.contact}</td>
      <td class="td-muted">${g.address ?? '—'}</td>
      <td><div class="td-actions">
        <button class="btn btn-sm btn-outline btn-icon" title="Edit" onclick='openEditGuest(${JSON.stringify(g)})'>✏️</button>
        <button class="btn btn-sm btn-danger btn-icon" title="Delete" onclick="deleteGuest(${g.guestId}, '${esc(g.fullName)}')">🗑️</button>
      </div></td>
    </tr>`).join('') : emptyRow(5, '👥 No guests found', 'Add a new guest using the button above');
}

document.getElementById('btn-add-guest')?.addEventListener('click', () => {
  document.getElementById('form-add-guest').reset();
  document.getElementById('guest-modal-title').textContent = 'Add New Guest';
  document.getElementById('edit-guest-id').value = '';
  Modal.open('modal-add-guest');
});

function openEditGuest(g) {
  const f = document.getElementById('form-add-guest');
  f.elements['firstName'].value = g.firstName ?? '';
  f.elements['lastName'].value  = g.lastName  ?? '';
  f.elements['contact'].value   = g.contact   ?? '';
  f.elements['address'].value   = g.address   ?? '';
  document.getElementById('edit-guest-id').value = g.guestId;
  document.getElementById('guest-modal-title').textContent = 'Edit Guest';
  Modal.open('modal-add-guest');
}

document.getElementById('form-add-guest')?.addEventListener('submit', async e => {
  e.preventDefault();
  const fd  = new FormData(e.target);
  const id  = document.getElementById('edit-guest-id').value;
  const btn = e.target.querySelector('[type=submit]');
  btn.disabled = true; btn.innerHTML = '<span class="spinner"></span> Saving…';
  try {
    if (id) {
      await API.put('/api/guests/' + id, Object.fromEntries(fd));
      Toast.success('Guest updated successfully.');
    } else {
      await API.post('/api/guests', fd);
      Toast.success('Guest added successfully.');
    }
    Modal.close('modal-add-guest');
    e.target.reset();
    await loadGuests(true);
  } catch (err) {
    Toast.error(err.message ?? 'Operation failed.');
  } finally {
    btn.disabled = false; btn.innerHTML = '💾 Save Guest';
  }
});

async function deleteGuest(id, name) {
  const ok = await confirmDialog(`Delete guest "${name}"? This will also remove all their reservations and billing records.`, 'Delete Guest');
  if (!ok) return;
  try {
    await API.delete('/api/guests/' + id);
    Toast.success(`Guest "${name}" deleted.`);
    await loadGuests(true);
  } catch (e) {
    Toast.error(e.message ?? 'Failed to delete guest.');
  }
}

// Search guests
document.getElementById('guests-search')?.addEventListener('input', e => {
  const q = e.target.value.toLowerCase();
  renderGuestsTable(guestsCache.filter(g =>
    g.fullName?.toLowerCase().includes(q) ||
    g.contact?.toLowerCase().includes(q) ||
    g.address?.toLowerCase().includes(q)
  ));
});

/* ══════════════════════════════════════════════════════════
   ④  ROOMS
══════════════════════════════════════════════════════════ */
let roomsCache = [];

async function loadRooms(forceRefresh = false) {
  if (loaders.rooms === null || forceRefresh) {
    try {
      roomsCache = await API.get('/api/rooms');
      renderRoomsGrid(roomsCache);
    } catch { Toast.error('Failed to load rooms.'); }
  }
}

function renderRoomsGrid(data) {
  const grid = document.getElementById('rooms-grid');
  if (!data.length) { grid.innerHTML = '<p style="padding:2rem;color:var(--gray-400);">No rooms found. Add one using the button above.</p>'; return; }
  grid.innerHTML = data.map(r => `
    <div class="room-card" data-roomid="${r.roomId}">
      <div class="room-card-header">
        <div>
          <div class="room-number">#${r.roomId}</div>
          <div class="room-type">${r.type}</div>
        </div>
        ${badge(r.status)}
      </div>
      <div class="room-rate">${fmt(r.price)} <span>/ night</span></div>
      <div class="room-amenities">🛁 ${r.amenities ?? 'AC, WiFi, TV'} &nbsp;·&nbsp; 🏢 ${r.floor ?? '1st'} Floor</div>
      <div class="room-actions">
        <select class="form-group" style="flex:1;padding:.35rem .6rem;font-size:.78rem;border:1.5px solid var(--gray-300);border-radius:8px;" onchange="changeRoomStatus('${r.roomId}', this.value)">
          <option value="">Change Status</option>
          <option value="Available"${r.status==='Available'?' disabled':''}>✅ Available</option>
          <option value="Booked"${r.status==='Booked'?' disabled':''}>🔴 Booked</option>
          <option value="Maintenance"${r.status==='Maintenance'?' disabled':''}>🔧 Maintenance</option>
        </select>
        <button class="btn btn-sm btn-danger btn-icon" title="Delete Room" onclick="deleteRoom('${r.roomId}')">🗑️</button>
      </div>
    </div>`).join('');
}

async function changeRoomStatus(roomId, status) {
  if (!status) return;
  try {
    await API.put('/api/rooms/' + roomId + '/status', { status });
    Toast.success(`Room ${roomId} status changed to ${status}.`);
    await loadRooms(true);
  } catch (e) {
    Toast.error(e.message ?? 'Failed to update status.');
  }
}

async function deleteRoom(roomId) {
  const ok = await confirmDialog(`Delete Room #${roomId}? This cannot be undone.`, 'Delete Room');
  if (!ok) return;
  try {
    await API.delete('/api/rooms/' + roomId);
    Toast.success(`Room #${roomId} deleted.`);
    await loadRooms(true);
    loaders.reservations = () => loadReservations(true);
  } catch (e) {
    Toast.error(e.message ?? 'Cannot delete – room may have active reservations.');
  }
}

document.getElementById('btn-add-room')?.addEventListener('click', () => {
  document.getElementById('form-add-room').reset();
  Modal.open('modal-add-room');
});

document.getElementById('form-add-room')?.addEventListener('submit', async e => {
  e.preventDefault();
  const fd  = new FormData(e.target);
  const btn = e.target.querySelector('[type=submit]');
  btn.disabled = true; btn.innerHTML = '<span class="spinner"></span> Adding…';
  try {
    await API.post('/api/rooms', fd);
    Toast.success('Room added successfully!');
    Modal.close('modal-add-room');
    e.target.reset();
    await loadRooms(true);
  } catch (err) {
    Toast.error(err.message ?? 'Failed to add room.');
  } finally {
    btn.disabled = false; btn.innerHTML = '💾 Add Room';
  }
});

// Search rooms
document.getElementById('rooms-search')?.addEventListener('input', e => {
  const q = e.target.value.toLowerCase();
  renderRoomsGrid(roomsCache.filter(r =>
    String(r.roomId).includes(q) ||
    r.type?.toLowerCase().includes(q) ||
    r.status?.toLowerCase().includes(q)
  ));
});

/* ══════════════════════════════════════════════════════════
   ⑤  BILLING
══════════════════════════════════════════════════════════ */
async function loadBilling(forceRefresh = false) {
  if (loaders.billing === null || forceRefresh) {
    if (forceRefresh) setTableLoading('billing-tbody', 6);
    try {
      const bills = await API.get('/api/billing');
      renderBillingTable(bills);
    } catch (e) {
      Toast.error('Failed to load billing records.');
    }
  }
}

function renderBillingTable(data) {
  const tbody = document.getElementById('billing-tbody');
  tbody.innerHTML = data.length ? data.map(b => `
    <tr>
      <td class="td-mono">#${b.billId}</td>
      <td class="td-mono">${b.reservationNumber}</td>
      <td class="td-name">${b.guestName}</td>
      <td>${b.roomType}</td>
      <td>${b.totalNights} nights</td>
      <td class="td-amount">${fmt(b.totalAmount)}</td>
      <td class="td-muted">${fmtDate(b.billingDate)}</td>
      <td><div class="td-actions">
        <button class="btn btn-sm btn-primary btn-icon" title="View Invoice" onclick="viewInvoice(${b.billId})">🧾</button>
      </div></td>
    </tr>`).join('') : emptyRow(8, '💰 No billing records found');
}

async function viewInvoice(billId) {
  Modal.open('modal-invoice');
  document.getElementById('invoice-body').innerHTML = '<div style="text-align:center;padding:2rem"><div class="spinner dark"></div></div>';
  try {
    const b = await API.get('/api/billing/' + billId);

    document.getElementById('inv-bill-id').textContent      = '#' + b.billId;
    document.getElementById('inv-res-no').textContent        = b.reservationNumber;
    document.getElementById('inv-guest').textContent         = b.guestName;
    document.getElementById('inv-contact').textContent       = b.contact;
    document.getElementById('inv-room').textContent          = b.roomType;
    document.getElementById('inv-checkin').textContent       = fmtDate(b.checkIn);
    document.getElementById('inv-checkout').textContent      = fmtDate(b.checkOut);
    document.getElementById('inv-nights').textContent        = b.totalNights + ' nights';
    document.getElementById('inv-rate').textContent          = fmt(b.ratePerNight);
    document.getElementById('inv-subtotal').textContent      = fmt(b.subtotal);
    document.getElementById('inv-tax').textContent           = fmt(b.tax);
    document.getElementById('inv-service').textContent       = fmt(b.serviceCharge);
    document.getElementById('inv-total').textContent         = fmt(b.grandTotal);
    document.getElementById('inv-date').textContent          = fmtDate(b.billingDate);

    document.getElementById('invoice-body').innerHTML = document.getElementById('invoice-template').innerHTML;
    renderInvoiceContent(b);
  } catch {
    document.getElementById('invoice-body').innerHTML = '<p style="color:var(--red);padding:1rem">Failed to load invoice.</p>';
  }
}

function renderInvoiceContent(b) {
  const body = document.getElementById('invoice-body');
  body.innerHTML = `
    <div class="invoice-header">
      <div class="invoice-logo">
        <strong>🌊 OceanView Resort</strong>
        <span>Premium Waterfront Experience</span>
        <div style="font-size:.72rem;color:var(--gray-400);margin-top:.25rem">Issued: ${fmtDate(b.billingDate)}</div>
      </div>
      <div>
        <div style="font-size:.7rem;color:var(--gray-400);text-transform:uppercase;letter-spacing:.5px">Invoice</div>
        <div style="font-size:1.3rem;font-weight:800;color:var(--oc-800)">#BILL-${b.billId}</div>
      </div>
    </div>
    <div style="display:grid;grid-template-columns:1fr 1fr;gap:.75rem;background:var(--oc-50);border-radius:var(--radius);padding:1rem;margin-bottom:1rem">
      <div><dt style="font-size:.7rem;text-transform:uppercase;color:var(--gray-400);letter-spacing:.4px">Guest</dt><dd style="font-weight:600;font-size:.9rem">${b.guestName}</dd></div>
      <div><dt style="font-size:.7rem;text-transform:uppercase;color:var(--gray-400);letter-spacing:.4px">Contact</dt><dd style="font-weight:600;font-size:.9rem">${b.contact}</dd></div>
      <div><dt style="font-size:.7rem;text-transform:uppercase;color:var(--gray-400);letter-spacing:.4px">Reservation</dt><dd style="font-weight:600;font-size:.9rem">${b.reservationNumber}</dd></div>
      <div><dt style="font-size:.7rem;text-transform:uppercase;color:var(--gray-400);letter-spacing:.4px">Room Type</dt><dd style="font-weight:600;font-size:.9rem">${b.roomType}</dd></div>
      <div><dt style="font-size:.7rem;text-transform:uppercase;color:var(--gray-400);letter-spacing:.4px">Check-In</dt><dd style="font-weight:600;font-size:.9rem">${fmtDate(b.checkIn)}</dd></div>
      <div><dt style="font-size:.7rem;text-transform:uppercase;color:var(--gray-400);letter-spacing:.4px">Check-Out</dt><dd style="font-weight:600;font-size:.9rem">${fmtDate(b.checkOut)}</dd></div>
    </div>
    <div style="border:1px solid var(--gray-200);border-radius:var(--radius);overflow:hidden;margin-bottom:.75rem">
      <table style="width:100%;border-collapse:collapse;font-size:.85rem">
        <thead style="background:var(--oc-900);color:white">
          <tr><th style="padding:.6rem 1rem;text-align:left">Description</th><th style="padding:.6rem 1rem;text-align:right">Amount</th></tr>
        </thead>
        <tbody>
          <tr style="border-bottom:1px solid var(--gray-100)"><td style="padding:.6rem 1rem">Room Rate × ${b.totalNights} nights @ ${fmt(b.ratePerNight)}</td><td style="padding:.6rem 1rem;text-align:right;font-weight:600">${fmt(b.subtotal)}</td></tr>
          <tr style="border-bottom:1px solid var(--gray-100)"><td style="padding:.6rem 1rem;color:var(--gray-500)">Government Tax (8%)</td><td style="padding:.6rem 1rem;text-align:right;color:var(--gray-600)">${fmt(b.tax)}</td></tr>
          <tr style="border-bottom:1px solid var(--gray-100)"><td style="padding:.6rem 1rem;color:var(--gray-500)">Service Charge (10%)</td><td style="padding:.6rem 1rem;text-align:right;color:var(--gray-600)">${fmt(b.serviceCharge)}</td></tr>
          <tr style="background:var(--oc-50)"><td style="padding:.75rem 1rem;font-weight:700;font-size:.95rem;color:var(--oc-800)">GRAND TOTAL</td><td style="padding:.75rem 1rem;text-align:right;font-weight:800;font-size:1.05rem;color:var(--oc-700)">${fmt(b.grandTotal)}</td></tr>
        </tbody>
      </table>
    </div>
    <p style="font-size:.72rem;color:var(--gray-400);text-align:center">Thank you for choosing OceanView Resort. We hope to see you again!</p>`;
}

document.getElementById('btn-print-invoice')?.addEventListener('click', () => {
  window.print();
});

/* ══════════════════════════════════════════════════════════
   HELPERS
══════════════════════════════════════════════════════════ */
function emptyRow(cols, icon, sub = '') {
  return `<tr class="empty-row"><td colspan="${cols}">
    <span class="empty-icon">${icon.startsWith('<') ? '' : icon.split(' ')[0]}</span>
    ${icon} <div style="font-size:.78rem;color:var(--gray-400);margin-top:.25rem">${sub}</div>
  </td></tr>`;
}

function setTableLoading(tbodyId, cols) {
  const tbody = document.getElementById(tbodyId);
  if (!tbody) return;
  tbody.innerHTML = Array.from({ length: 4 }, () =>
    `<tr class="skeleton-row">${Array.from({ length: cols }, () => '<td>&nbsp;</td>').join('')}</tr>`
  ).join('');
}

function esc(str) {
  return String(str ?? '').replace(/'/g, "\\'").replace(/"/g, '&quot;');
}

/* ══════════════════════════════════════════════════════════
   HEADER SEARCH (global)
══════════════════════════════════════════════════════════ */
document.getElementById('global-search')?.addEventListener('input', e => {
  const q = e.target.value.toLowerCase().trim();
  if (!q) return;
  // Jump to reservations and filter
  navigate('reservations');
  document.getElementById('res-search').value = q;
  document.getElementById('res-search').dispatchEvent(new Event('input'));
});

/* ══════════════════════════════════════════════════════════
   INITIALISE
══════════════════════════════════════════════════════════ */
navigate('dashboard');
