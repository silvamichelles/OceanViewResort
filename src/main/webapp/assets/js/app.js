/**
 * OceanView Resort  Main SPA Application (app.js)
 * Handles all UI rendering, API calls, modals, toasts, and navigation.
 */
const App = (function () {
  'use strict';

  //  State 
  let currentView = 'dashboard';
  let resData = [], guestData = [], roomData = [], billingData = [], userData = [];
  let confirmResolve = null;

  const PAGE_SIZE = 10;
  let resPage = 1, billingPage = 1;

  //  Toast notifications 
  const Toast = {
    show(msg, type = 'info', duration = 3500) {
      const icons = { success: 'fa-check-circle', error: 'fa-times-circle',
                      warning: 'fa-exclamation-triangle', info: 'fa-info-circle' };
      const tc = document.getElementById('toast-container');
      const t  = document.createElement('div');
      t.className = `toast ${type}`;
      t.innerHTML = `<i class="fas ${icons[type] || icons.info} toast-icon ${type}"></i>
                     <span class="toast-text">${msg}</span>
                     <button class="toast-close" onclick="this.parentElement.remove()"><i class="fas fa-times"></i></button>`;
      tc.appendChild(t);
      setTimeout(() => { t.classList.add('removing'); setTimeout(() => t.remove(), 250); }, duration);
    },
    success : (m) => Toast.show(m, 'success'),
    error   : (m) => Toast.show(m, 'error', 5000),
    warning : (m) => Toast.show(m, 'warning'),
    info    : (m) => Toast.show(m, 'info')
  };

  //  Modal management 
  function openModal(id) {
    const el = document.getElementById(id);
    if (el) { el.style.display = 'flex'; document.body.style.overflow = 'hidden'; }
  }
  function closeModal(id) {
    const el = document.getElementById(id);
    if (el) { el.style.display = 'none'; document.body.style.overflow = ''; }
  }
  // Close modals on backdrop click
  document.addEventListener('click', (e) => {
    if (e.target.classList.contains('modal-overlay')) {
      closeModal(e.target.id);
    }
  });

  //  Confirm dialog 
  function confirmDialog(msg, title = 'Confirm Action') {
    return new Promise((resolve) => {
      document.getElementById('confirm-title').textContent = title;
      document.getElementById('confirm-msg').textContent   = msg;
      confirmResolve = resolve;
      openModal('modal-confirm');
    });
  }
  document.getElementById('confirm-yes').addEventListener('click', () => {
    closeModal('modal-confirm');
    if (confirmResolve) { confirmResolve(true); confirmResolve = null; }
  });
  document.getElementById('confirm-no').addEventListener('click', () => {
    closeModal('modal-confirm');
    if (confirmResolve) { confirmResolve(false); confirmResolve = null; }
  });

  //  Navigation 
  function navigate(viewId) {
    if (currentView === viewId) return;
    currentView = viewId;

    // Deactivate all views
    document.querySelectorAll('.view').forEach(v => v.classList.remove('active'));
    document.querySelectorAll('.nav-item').forEach(n => n.classList.remove('active'));

    // Activate selected view
    const view = document.getElementById('view-' + viewId);
    if (view) view.classList.add('active');

    const navItem = document.querySelector(`.nav-item[data-view="${viewId}"]`);
    if (navItem) navItem.classList.add('active');

    // Update page title
    const titles = { dashboard: 'Dashboard', reservations: 'Reservations',
                     guests: 'Guests', rooms: 'Rooms',
                     billing: 'Billing', users: 'User Management' };
    document.getElementById('page-title').textContent = titles[viewId] || viewId;

    // Close mobile sidebar
    document.getElementById('sidebar').classList.remove('open');
    document.getElementById('sidebar-backdrop').classList.remove('open');

    // Lazy-load view data
    const loaders = {
      dashboard    : loadDashboard,
      reservations : loadReservations,
      guests       : loadGuests,
      rooms        : loadRooms,
      billing      : loadBilling,
      users        : loadUsers
    };
    if (loaders[viewId]) loaders[viewId]();
  }

  //  Navigation Wire-up 
  document.querySelectorAll('.nav-item[data-view]').forEach(item => {
    item.addEventListener('click', () => navigate(item.dataset.view));
  });

  // Hamburger menu
  document.getElementById('hamburger').addEventListener('click', () => {
    document.getElementById('sidebar').classList.toggle('open');
    document.getElementById('sidebar-backdrop').classList.toggle('open');
  });
  document.getElementById('sidebar-backdrop').addEventListener('click', () => {
    document.getElementById('sidebar').classList.remove('open');
    document.getElementById('sidebar-backdrop').classList.remove('open');
  });

  // Global search  navigate to reservations and filter
  document.getElementById('global-search').addEventListener('input', function () {
    if (currentView !== 'reservations') navigate('reservations');
    document.getElementById('res-search').value = this.value;
    filterTable('res-tbody', 'res-search');
  });

  // 
  // DASHBOARD
  // 
  async function loadDashboard() {
    const r = await API.get('api/dashboard');
    if (!r || !r.ok) return;
    const d = r.data;

    // Greeting
    document.getElementById('greeting-name').textContent = d.username || 'User';
    document.getElementById('user-name').textContent     = d.username || 'User';
    const initials = (d.username || 'U').substring(0,2).toUpperCase();
    document.getElementById('user-avatar').textContent   = initials;

    // Date
    const today = new Date();
    document.getElementById('greeting-date').textContent =
      today.toLocaleDateString('en-US', { weekday:'long', year:'numeric', month:'long', day:'numeric' });

    // Stats
    animateCount('stat-total-res', d.totalReservations);
    animateCount('stat-available', d.availableRooms);
    animateCount('stat-booked',    d.occupiedRooms);
    animateRevenue('stat-revenue', d.totalRevenue);

    // Recent reservations
    const res2 = await API.get('api/reservations');
    if (res2 && res2.ok) {
      const recent = res2.data.slice(0, 7);
      const tbody  = document.getElementById('recent-res-tbody');
      tbody.innerHTML = recent.length
        ? recent.map(r2 => `<tr>
            <td><strong>${r2.reservationNumber || ''}</strong></td>
            <td>${r2.guestName || ''}</td>
            <td>${r2.roomType || ''}</td>
            <td>${r2.checkIn || ''}</td>
            <td>${r2.checkOut || ''}</td>
            <td>${statusBadge(r2.status)}</td>
          </tr>`).join('')
        : '<tr><td colspan="6" class="text-center" style="padding:1.5rem;color:#8A96A3;">No recent reservations.</td></tr>';
    }
  }

  function animateCount(id, value) {
    const el = document.getElementById(id);
    if (!el) return;
    const target = parseInt(value) || 0;
    let current  = 0;
    const step   = Math.ceil(target / 30);
    const timer  = setInterval(() => {
      current = Math.min(current + step, target);
      el.textContent = current.toLocaleString();
      if (current >= target) clearInterval(timer);
    }, 30);
  }

  function animateRevenue(id, value) {
    const el = document.getElementById(id);
    if (!el) return;
    const target = parseFloat(value) || 0;
    let current  = 0;
    const step   = target / 40;
    const timer  = setInterval(() => {
      current = Math.min(current + step, target);
      el.textContent = 'Rs. ' + current.toLocaleString('en-US', { minimumFractionDigits:0, maximumFractionDigits:0 });
      if (current >= target) clearInterval(timer);
    }, 25);
  }

  // 
  // RESERVATIONS
  // 
  async function loadReservations() {
    const r = await API.get('api/reservations');
    if (!r || !r.ok) { Toast.error('Failed to load reservations.'); return; }
    resData = r.data;
    resPage = 1;
    renderResTable();
  }

  function renderResTable() {
    const search = (document.getElementById('res-search').value || '').toLowerCase();
    const filtered = resData.filter(r =>
      (r.guestName || '').toLowerCase().includes(search) ||
      (r.reservationNumber || '').toLowerCase().includes(search) ||
      (r.roomType || '').toLowerCase().includes(search)
    );

    const startIdx  = (resPage - 1) * PAGE_SIZE;
    const page      = filtered.slice(startIdx, startIdx + PAGE_SIZE);
    const tbody     = document.getElementById('res-tbody');

    tbody.innerHTML = page.length
      ? page.map(r => `<tr>
          <td><strong>${r.reservationNumber || ''}</strong></td>
          <td>${r.guestName || ''}</td>
          <td>${r.contact || ''}</td>
          <td>${r.roomType || ''}</td>
          <td>${r.checkIn || ''}</td>
          <td>${r.checkOut || ''}</td>
          <td>${r.nights || ''}</td>
          <td>${r.amount ? 'Rs. ' + Number(r.amount).toLocaleString() : ''}</td>
          <td>${statusBadge(r.status)}</td>
          <td class="td-actions">
            <button class="btn btn-danger btn-icon" onclick="App.deleteReservation('${r.reservationNumber}')" title="Cancel">
              <i class="fas fa-trash-alt"></i>
            </button>
          </td>
        </tr>`).join('')
      : `<tr><td colspan="10" style="text-align:center;padding:2rem;color:#8A96A3;">No reservations found.</td></tr>`;

    renderPagination('res-pagination', filtered.length, resPage, (p) => { resPage = p; renderResTable(); });
  }

  async function deleteReservation(resNo) {
    const ok = await confirmDialog(`Cancel reservation ${resNo}? This action cannot be undone.`, 'Cancel Reservation');
    if (!ok) return;
    const r = await API.delete('api/reservations/' + resNo);
    if (r && r.ok) {
      Toast.success('Reservation ' + resNo + ' cancelled.');
      loadReservations();
    } else {
      Toast.error((r && r.data && r.data.message) || 'Could not cancel reservation.');
    }
  }

  // Validate & submit new reservation
  async function submitReservation() {
    const guestId  = document.getElementById('r-guestId').value;
    const roomId   = document.getElementById('r-roomId').value;
    const checkIn  = document.getElementById('r-checkIn').value;
    const checkOut = document.getElementById('r-checkOut').value;
    const roomType = document.getElementById('r-roomType').value;
    const resNo    = document.getElementById('r-resNo').value.trim();
    const errEl    = document.getElementById('res-form-error');

    errEl.classList.add('hidden');

    // Validation
    if (!guestId) { showFormErr(errEl, 'Please select a guest.'); return; }
    if (!roomId)  { showFormErr(errEl, 'Please select a room.'); return; }
    if (!checkIn || !checkOut) { showFormErr(errEl, 'Check-in and check-out dates are required.'); return; }
    if (!roomType) { showFormErr(errEl, 'Please select a room type.'); return; }

    const ci = new Date(checkIn);
    const co = new Date(checkOut);
    const today = new Date(); today.setHours(0,0,0,0);

    if (ci < today) { showFormErr(errEl, 'Check-in date cannot be in the past.'); return; }
    if (co <= ci)   { showFormErr(errEl, 'Check-out must be after check-in date.'); return; }

    const body = { guestId, roomId, checkIn, checkOut, roomType };
    if (resNo) body.resNo = resNo;

    const r = await API.post('api/reservations', body);
    if (r && r.ok) {
      Toast.success('Reservation created successfully!');
      closeModal('modal-new-res');
      document.getElementById('form-new-res').reset();
      loadReservations();
      loadDashboard();
    } else {
      showFormErr(errEl, (r && r.data && r.data.message) || 'Failed to create reservation.');
    }
  }

  // Populate guest & room dropdowns in reservation modal
  async function populateResDropdowns() {
    const [gRes, rRes] = await Promise.all([
      API.get('api/guests'),
      API.get('api/reservations/rooms')
    ]);

    const gSel = document.getElementById('r-guestId');
    gSel.innerHTML = '<option value=""> Select guest </option>';
    if (gRes && gRes.ok) {
      gRes.data.forEach(g => {
        const opt = document.createElement('option');
        opt.value = g.guestId;
        opt.textContent = g.fullName + (g.contact ? ' (' + g.contact + ')' : '');
        gSel.appendChild(opt);
      });
    }

    const rSel = document.getElementById('r-roomId');
    rSel.innerHTML = '<option value=""> Select room </option>';
    if (rRes && rRes.ok) {
      rRes.data.forEach(id => {
        const opt = document.createElement('option'); opt.value = id; opt.textContent = 'Room ' + id;
        rSel.appendChild(opt);
      });
    }

    // Set today as minimum check-in date
    const todayStr = new Date().toISOString().split('T')[0];
    document.getElementById('r-checkIn').min  = todayStr;
    document.getElementById('r-checkOut').min = todayStr;
  }

  // Auto-calc amount when dates change
  ['r-checkIn','r-checkOut','r-roomType'].forEach(id => {
    document.getElementById(id).addEventListener('change', function () {
      const ci = new Date(document.getElementById('r-checkIn').value);
      const co = new Date(document.getElementById('r-checkOut').value);
      if (ci && co && co > ci) {
        const nights = Math.round((co - ci) / 86400000);
        const rates  = { Single: 5000, Double: 8000, Suite: 15000, Deluxe: 12000, Family: 10000 };
        const type   = document.getElementById('r-roomType').value;
        const rate   = rates[type] || 0;
        document.getElementById('r-amount').value = (nights * rate).toFixed(2);
      }
    });
  });

  // Export CSV
  function exportCSV(type) {
    if (type === 'reservations') {
      if (!resData.length) { Toast.warning('No reservation data to export.'); return; }
      const headers = ['Ref #','Guest','Contact','Room Type','Check-In','Check-Out','Nights','Amount','Status'];
      const rows = resData.map(r => [
        r.reservationNumber, r.guestName, r.contact, r.roomType,
        r.checkIn, r.checkOut, r.nights, r.amount, r.status
      ]);
      downloadCSV('reservations.csv', headers, rows);
    } else if (type === 'guests') {
      if (!guestData.length) { Toast.warning('No guest data to export.'); return; }
      const headers = ['ID','Full Name','Contact','Email','NIC','Nationality','Address'];
      const rows = guestData.map(g => [
        g.guestId, g.fullName, g.contact, g.email || '', g.nic || '', g.nationality || '', g.address || ''
      ]);
      downloadCSV('guests.csv', headers, rows);
    }
    Toast.success('CSV exported successfully!');
  }

  function downloadCSV(filename, headers, rows) {
    const csv = [headers, ...rows].map(r => r.map(v => `"${String(v||'').replace(/"/g,'""')}"`).join(',')).join('\n');
    const blob = new Blob([csv], { type: 'text/csv' });
    const a    = document.createElement('a');
    a.href     = URL.createObjectURL(blob);
    a.download = filename;
    a.click();
    URL.revokeObjectURL(a.href);
  }

  // Generic inline table filter
  function filterTable(tbodyId, searchId) {
    if (tbodyId === 'res-tbody') { resPage = 1; renderResTable(); return; }
    const search = (document.getElementById(searchId).value || '').toLowerCase();
    document.querySelectorAll('#' + tbodyId + ' tr').forEach(tr => {
      tr.style.display = tr.textContent.toLowerCase().includes(search) ? '' : 'none';
    });
  }

  // 
  // GUESTS
  // 
  async function loadGuests() {
    const r = await API.get('api/guests');
    if (!r || !r.ok) { Toast.error('Failed to load guests.'); return; }
    guestData = r.data;
    renderGuestsTable(guestData);

    // Analytics
    document.getElementById('g-stat-total').textContent     = guestData.length;
    document.getElementById('g-stat-staying').textContent   = '';
    document.getElementById('g-stat-returning').textContent = '';

    // Populate nationality filter
    const sel = document.getElementById('guests-nationality-filter');
    const nations = [...new Set(guestData.map(g => g.nationality).filter(Boolean))].sort();
    sel.innerHTML = '<option value="">All Nationalities</option>';
    nations.forEach(n => { const o = document.createElement('option'); o.value = n; o.textContent = n; sel.appendChild(o); });
  }

  function renderGuestsTable(data) {
    const tbody = document.getElementById('guests-tbody');
    tbody.innerHTML = data.length
      ? data.map(g => `<tr>
          <td>${g.guestId}</td>
          <td><strong>${g.fullName || ''}</strong></td>
          <td>${g.contact || ''}</td>
          <td>${g.email || ''}</td>
          <td>${g.nic || ''}</td>
          <td>${g.nationality || ''}</td>
          <td>${g.address || ''}</td>
          <td class="td-actions">
            <button class="btn btn-outline btn-icon" onclick="App.openEditGuest(${g.guestId})" title="Edit">
              <i class="fas fa-pen"></i>
            </button>
            <button class="btn btn-danger btn-icon" onclick="App.deleteGuest(${g.guestId},'${escHtml(g.fullName)}')" title="Delete">
              <i class="fas fa-trash-alt"></i>
            </button>
          </td>
        </tr>`).join('')
      : `<tr><td colspan="8" style="text-align:center;padding:2rem;color:#8A96A3;">No guests found.</td></tr>`;
  }

  function filterGuests() {
    const search  = (document.getElementById('guests-search').value || '').toLowerCase();
    const nation  = document.getElementById('guests-nationality-filter').value;
    const filtered = guestData.filter(g =>
      (!search  || (g.fullName||'').toLowerCase().includes(search) || (g.contact||'').toLowerCase().includes(search)) &&
      (!nation  || g.nationality === nation)
    );
    renderGuestsTable(filtered);
  }

  function openAddGuest() {
    document.getElementById('g-id').value       = '';
    document.getElementById('g-first').value    = '';
    document.getElementById('g-last').value     = '';
    document.getElementById('g-contact').value  = '';
    document.getElementById('g-email').value    = '';
    document.getElementById('g-nic').value      = '';
    document.getElementById('g-nationality').value = '';
    document.getElementById('g-address').value  = '';
    document.getElementById('guest-modal-title').innerHTML = '<i class="fas fa-user-plus"></i> Add Guest';
    document.getElementById('guest-form-error').classList.add('hidden');
    openModal('modal-guest');
  }

  async function openEditGuest(id) {
    const r = await API.get('api/guests/' + id);
    if (!r || !r.ok) { Toast.error('Failed to load guest details.'); return; }
    const g = r.data;
    document.getElementById('g-id').value          = g.guestId;
    document.getElementById('g-first').value       = g.firstName  || '';
    document.getElementById('g-last').value        = g.lastName   || '';
    document.getElementById('g-contact').value     = g.contact    || '';
    document.getElementById('g-email').value       = g.email      || '';
    document.getElementById('g-nic').value         = g.nic        || '';
    document.getElementById('g-nationality').value = g.nationality|| '';
    document.getElementById('g-address').value     = g.address    || '';
    document.getElementById('guest-modal-title').innerHTML = '<i class="fas fa-user-edit"></i> Edit Guest';
    document.getElementById('guest-form-error').classList.add('hidden');
    openModal('modal-guest');
  }

  async function submitGuest() {
    const id      = document.getElementById('g-id').value;
    const first   = document.getElementById('g-first').value.trim();
    const contact = document.getElementById('g-contact').value.trim();
    const errEl   = document.getElementById('guest-form-error');
    errEl.classList.add('hidden');

    if (!first)   { showFormErr(errEl, 'First name is required.'); return; }
    if (!contact) { showFormErr(errEl, 'Contact number is required.'); return; }

    const body = {
      firstName  : first,
      lastName   : document.getElementById('g-last').value.trim(),
      contact    : contact,
      email      : document.getElementById('g-email').value.trim(),
      nic        : document.getElementById('g-nic').value.trim(),
      nationality: document.getElementById('g-nationality').value.trim(),
      address    : document.getElementById('g-address').value.trim()
    };

    const r = id
      ? await API.put ('api/guests/' + id, body)
      : await API.post('api/guests',       body);

    if (r && r.ok) {
      Toast.success(id ? 'Guest updated successfully.' : 'Guest added successfully.');
      closeModal('modal-guest');
      loadGuests();
    } else {
      showFormErr(errEl, (r && r.data && r.data.message) || 'Operation failed.');
    }
  }

  async function deleteGuest(id, name) {
    const ok = await confirmDialog(`Delete guest "${name}"? This will also remove their reservations.`, 'Delete Guest');
    if (!ok) return;
    const r = await API.delete('api/guests/' + id);
    if (r && r.ok) {
      Toast.success('Guest deleted.');
      loadGuests();
    } else {
      Toast.error((r && r.data && r.data.message) || 'Failed to delete guest.');
    }
  }

  // 
  // ROOMS
  // 
  async function loadRooms() {
    const r = await API.get('api/rooms');
    if (!r || !r.ok) { Toast.error('Failed to load rooms.'); return; }
    roomData = r.data;
    renderRoomsGrid(roomData);
  }

  function renderRoomsGrid(data) {
    const grid = document.getElementById('rooms-grid');
    if (!data.length) {
      grid.innerHTML = '<p style="text-align:center;color:#8A96A3;padding:2rem;">No rooms found.</p>';
      return;
    }
    grid.innerHTML = data.map(r => `
      <div class="room-card">
        <div class="room-card-header">
          <span class="room-number"><i class="fas fa-door-closed" style="color:var(--navy);margin-right:.35rem;"></i>Room ${r.roomId}</span>
          ${roomBadge(r.status)}
        </div>
        <div class="room-card-body">
          <p><span>Type</span><strong>${r.type || ''}</strong></p>
          <p><span>Floor</span><strong>${r.floor || ''}</strong></p>
          <p><span>Rate/Night</span><strong>Rs. ${Number(r.price||0).toLocaleString()}</strong></p>
          <p><span>Amenities</span><strong style="font-size:.75rem;">${r.amenities || 'Standard'}</strong></p>
        </div>
        <div class="room-card-footer">
          <select class="filter-select" style="flex:1;font-size:.76rem;" onchange="App.changeRoomStatus('${r.roomId}',this.value)">
            <option ${r.status==='Available'?'selected':''}>Available</option>
            <option ${r.status==='Booked'?'selected':''}>Booked</option>
            <option ${r.status==='Maintenance'?'selected':''}>Maintenance</option>
          </select>
          <button class="btn btn-danger btn-icon" onclick="App.deleteRoom('${r.roomId}')" title="Delete">
            <i class="fas fa-trash-alt"></i>
          </button>
        </div>
      </div>`).join('');
  }

  function filterRooms() {
    const search = (document.getElementById('rooms-search').value || '').toLowerCase();
    const status = document.getElementById('rooms-status-filter').value;
    const filtered = roomData.filter(r =>
      (!search || String(r.roomId).includes(search) || (r.type||'').toLowerCase().includes(search)) &&
      (!status || r.status === status)
    );
    renderRoomsGrid(filtered);
  }

  async function submitRoom() {
    const errEl  = document.getElementById('room-form-error');
    const number = document.getElementById('rm-number').value.trim();
    const type   = document.getElementById('rm-type').value;
    const price  = document.getElementById('rm-price').value;
    errEl.classList.add('hidden');

    if (!number) { showFormErr(errEl, 'Room number is required.');  return; }
    if (!type)   { showFormErr(errEl, 'Room type is required.');    return; }
    if (!price || parseFloat(price) <= 0) { showFormErr(errEl, 'Price must be greater than 0.'); return; }

    const body = { roomNumber: number, type, price,
                   floor: document.getElementById('rm-floor').value || '1',
                   amenities: document.getElementById('rm-amenities').value.trim() };

    const r = await API.post('api/rooms', body);
    if (r && r.ok) {
      Toast.success('Room added successfully!');
      closeModal('modal-add-room');
      document.getElementById('form-add-room').reset();
      loadRooms();
    } else {
      showFormErr(errEl, (r && r.data && r.data.message) || 'Failed to add room.');
    }
  }

  async function changeRoomStatus(roomId, newStatus) {
    const r = await API.put('api/rooms/' + roomId + '/status', { status: newStatus });
    if (r && r.ok) {
      Toast.success('Room ' + roomId + ' status  ' + newStatus);
      loadRooms();
    } else {
      Toast.error('Could not update room status.');
      loadRooms(); // Reload to reset the dropdown
    }
  }

  async function deleteRoom(roomId) {
    const ok = await confirmDialog('Delete room ' + roomId + '? This cannot be undone.', 'Delete Room');
    if (!ok) return;
    const r = await API.delete('api/rooms/' + roomId);
    if (r && r.ok) {
      Toast.success('Room ' + roomId + ' deleted.');
      loadRooms();
    } else {
      Toast.error((r && r.data && r.data.message) || 'Cannot delete  room may have active reservations.');
    }
  }

  // 
  // BILLING
  // 
  async function loadBilling() {
    const r = await API.get('api/billing');
    if (!r || !r.ok) { Toast.error('Failed to load billing records.'); return; }
    billingData = r.data;
    renderBillingTable(billingData);
  }

  function renderBillingTable(data) {
    const tbody = document.getElementById('billing-tbody');
    tbody.innerHTML = data.length
      ? data.map(b => `<tr>
          <td><strong>#${b.billId}</strong></td>
          <td>${b.reservationNumber || ''}</td>
          <td>${b.guestName || ''}</td>
          <td>${b.roomType || ''}</td>
          <td>${b.totalNights || ''}</td>
          <td>Rs. ${Number(b.ratePerNight||0).toLocaleString()}</td>
          <td><strong>Rs. ${Number(b.totalAmount||0).toLocaleString()}</strong></td>
          <td>${b.billingDate ? b.billingDate.split('T')[0] : ''}</td>
          <td class="td-actions">
            <button class="btn btn-outline btn-icon" onclick="App.viewInvoice(${b.billId})" title="Invoice">
              <i class="fas fa-receipt"></i>
            </button>
          </td>
        </tr>`).join('')
      : `<tr><td colspan="9" style="text-align:center;padding:2rem;color:#8A96A3;">No billing records found.</td></tr>`;
  }

  async function viewInvoice(billId) {
    const r = await API.get('api/billing/' + billId);
    if (!r || !r.ok) { Toast.error('Failed to load invoice.'); return; }
    const b = r.data;
    const calc = b.calc || {};

    document.getElementById('invoice-body').innerHTML = `
      <div class="invoice-wrap">
        <div class="invoice-header">
          <div class="invoice-brand">
            <h2><i class="fas fa-anchor"></i> OceanView Resort</h2>
            <p>Staff Management System</p>
            <p>Tel: +94 11 234 5678 | oceanview@resort.lk</p>
          </div>
          <div class="invoice-meta">
            <p><strong>INVOICE</strong></p>
            <p>Bill ID: <strong>#${b.billId}</strong></p>
            <p>Reservation: <strong>${b.reservationNumber||''}</strong></p>
            <p>Date: ${b.billingDate ? b.billingDate.split('T')[0] : ''}</p>
          </div>
        </div>
        <table class="invoice-table">
          <thead><tr><th>Description</th><th>Details</th></tr></thead>
          <tbody>
            <tr><td>Guest Name</td><td>${b.guestName||''}</td></tr>
            <tr><td>Contact</td><td>${b.contact||''}</td></tr>
            <tr><td>Room Type</td><td>${b.roomType||''}</td></tr>
            <tr><td>Check-In</td><td>${b.checkIn||''}</td></tr>
            <tr><td>Check-Out</td><td>${b.checkOut||''}</td></tr>
            <tr><td>Nights Stayed</td><td>${b.totalNights||''}</td></tr>
            <tr><td>Rate Per Night</td><td>Rs. ${Number(b.ratePerNight||0).toLocaleString()}</td></tr>
          </tbody>
        </table>
        <div class="invoice-total">
          <table>
            <tr><td>Room Charges</td><td>Rs. ${Number(b.totalAmount||0).toLocaleString()}</td></tr>
            ${calc.tax ? `<tr><td>Tax (${((calc.tax/b.totalAmount)*100).toFixed(0)}%)</td><td>Rs. ${Number(calc.tax||0).toLocaleString()}</td></tr>` : ''}
            ${calc.serviceCharge ? `<tr><td>Service Charge</td><td>Rs. ${Number(calc.serviceCharge||0).toLocaleString()}</td></tr>` : ''}
            <tr class="grand"><td><strong>TOTAL DUE</strong></td><td><strong>Rs. ${Number(calc.total||b.totalAmount||0).toLocaleString()}</strong></td></tr>
          </table>
        </div>
        <p style="text-align:center;margin-top:1.5rem;font-size:.75rem;color:#8A96A3;">Thank you for staying at OceanView Resort. We hope to see you again!</p>
      </div>`;
    openModal('modal-invoice');
  }

  async function calculateBill() {
    const rate   = parseFloat(document.getElementById('calc-rate').value)   || 0;
    const nights = parseInt(document.getElementById('calc-nights').value)   || 0;
    const result = document.getElementById('calc-result');

    if (rate <= 0 || nights <= 0) { Toast.warning('Enter valid rate and nights.'); return; }

    const roomTotal = rate * nights;
    const r = await API.post('api/billing/calc', { roomTotal });
    if (r && r.ok) {
      const d = r.data;
      result.classList.remove('hidden');
      result.innerHTML = `
        <div class="calc-row"><span>Room Charges (${nights} nights)</span><span>Rs. ${roomTotal.toLocaleString()}</span></div>
        ${d.tax ? `<div class="calc-row"><span>Tax</span><span>Rs. ${Number(d.tax).toLocaleString()}</span></div>` : ''}
        ${d.serviceCharge ? `<div class="calc-row"><span>Service Charge</span><span>Rs. ${Number(d.serviceCharge).toLocaleString()}</span></div>` : ''}
        <div class="calc-row total"><span>TOTAL</span><span>Rs. ${Number(d.total||roomTotal).toLocaleString()}</span></div>`;
    }
  }

  // 
  // USERS
  // 
  async function loadUsers() {
    const r = await API.get('api/users');
    if (!r || !r.ok) { Toast.error('Failed to load users.'); return; }
    userData = r.data;
    renderUsersTable();
  }

  function renderUsersTable() {
    const tbody = document.getElementById('users-tbody');
    tbody.innerHTML = userData.length
      ? userData.map((u, i) => `<tr>
          <td>${i + 1}</td>
          <td><i class="fas fa-user-circle" style="color:var(--teal);margin-right:.4rem;"></i>${u.username}</td>
          <td><span class="badge badge-${(u.role||'').toLowerCase()}">${u.role||'Staff'}</span></td>
          <td class="td-actions">
            <button class="btn btn-danger btn-icon" onclick="App.deleteUser(${u.userId},'${escHtml(u.username)}')" title="Delete">
              <i class="fas fa-trash-alt"></i>
            </button>
          </td>
        </tr>`).join('')
      : `<tr><td colspan="4" style="text-align:center;padding:2rem;color:#8A96A3;">No users found.</td></tr>`;
  }

  async function submitUser() {
    const username = document.getElementById('u-username').value.trim();
    const password = document.getElementById('u-password').value;
    const role     = document.getElementById('u-role').value;
    const errEl    = document.getElementById('user-form-error');
    errEl.classList.add('hidden');

    if (!username) { showFormErr(errEl, 'Username is required.'); return; }
    if (!password) { showFormErr(errEl, 'Password is required.'); return; }

    const r = await API.post('api/users', { username, password, role });
    if (r && r.ok) {
      Toast.success('User "' + username + '" created!');
      closeModal('modal-add-user');
      document.getElementById('form-add-user').reset();
      loadUsers();
    } else {
      showFormErr(errEl, (r && r.data && r.data.message) || 'Could not create user.');
    }
  }

  async function deleteUser(id, name) {
    const ok = await confirmDialog(`Delete user "${name}"? This cannot be undone.`, 'Delete User');
    if (!ok) return;
    const r = await API.delete('api/users/' + id);
    if (r && r.ok) {
      Toast.success('User deleted.');
      loadUsers();
    } else {
      Toast.error((r && r.data && r.data.message) || 'Failed to delete user.');
    }
  }

  // 
  // HELPERS
  // 
  function statusBadge(status) {
    const map = { Confirmed: 'blue', Available: 'green', Booked: 'red',
                  Cancelled: 'red', Pending: 'yellow', Checked_In: 'green' };
    const cls = map[status] || 'blue';
    return `<span class="badge badge-${cls}">${status || 'Active'}</span>`;
  }

  function roomBadge(status) {
    const map = { Available: 'available', Booked: 'booked', Maintenance: 'maintenance' };
    const cls = map[status] || 'available';
    return `<span class="badge badge-${cls}">${status || 'Available'}</span>`;
  }

  function showFormErr(el, msg) { el.textContent = msg; el.classList.remove('hidden'); }
  function escHtml(s) { return (s||'').replace(/'/g, "\\'"); }

  function renderPagination(containerId, total, current, onPage) {
    const totalPages = Math.ceil(total / PAGE_SIZE);
    const container  = document.getElementById(containerId);
    if (!container || totalPages <= 1) { if (container) container.innerHTML = ''; return; }
    let html = '';
    for (let i = 1; i <= totalPages; i++) {
      html += `<button class="page-btn${i === current ? ' active' : ''}" onclick="(${onPage.toString()})(${i})">${i}</button>`;
    }
    container.innerHTML = `<span style="font-size:.8rem;color:#8A96A3;">Page ${current} of ${totalPages}</span>${html}`;
  }

  // 
  // INITIALIZE
  // 
  (function init() {
    navigate('dashboard');

    // Wire up "New Reservation" modal open  refresh dropdowns
    document.getElementById('btn-new-res').addEventListener('click', populateResDropdowns);
    document.querySelectorAll('[onclick*="modal-new-res"]').forEach(el => {
      el.addEventListener('click', populateResDropdowns);
    });

    // Search listeners
    document.getElementById('res-search').addEventListener('input', () => { resPage = 1; renderResTable(); });
    document.getElementById('guests-search').addEventListener('input', filterGuests);
  })();

  // Public API
  return {
    navigate, openModal, closeModal, exportCSV, filterTable, filterGuests,
    filterRooms, submitReservation, deleteReservation, submitGuest, openAddGuest,
    openEditGuest, deleteGuest, submitRoom, changeRoomStatus, deleteRoom,
    viewInvoice, calculateBill, submitUser, deleteUser
  };
})();
