/**
 * api.js  –  Thin Fetch API wrapper for OceanView Resort
 * All requests include X-Requested-With for servlet-side XHR detection.
 */
const API = (() => {
  const BASE = document.querySelector('base')?.href?.replace(/\/$/, '') ?? '';

  const headers = (extra = {}) => ({
    'X-Requested-With': 'XMLHttpRequest',
    ...extra,
  });

  /** Handle 401 → redirect to login */
  async function handle(res) {
    if (res.status === 401) {
      window.location.href = BASE + '/index.html?error=session';
      throw new Error('Session expired');
    }
    const text = await res.text();
    let json;
    try { json = JSON.parse(text); } catch { json = { raw: text }; }
    if (!res.ok) throw Object.assign(new Error(json.message ?? 'Server error'), { status: res.status, data: json });
    return json;
  }

  async function get(path) {
    const res = await fetch(BASE + path, { headers: headers() });
    return handle(res);
  }

  async function post(path, data) {
    const isForm = data instanceof FormData || data instanceof URLSearchParams;
    const body = isForm ? new URLSearchParams(data) : JSON.stringify(data);
    const hdrs = headers(isForm ? { 'Content-Type': 'application/x-www-form-urlencoded' }
                                : { 'Content-Type': 'application/json' });
    const res = await fetch(BASE + path, { method: 'POST', headers: hdrs, body });
    return handle(res);
  }

  async function put(path, data) {
    const body = new URLSearchParams(data);
    const res = await fetch(BASE + path, {
      method: 'PUT',
      headers: headers({ 'Content-Type': 'application/x-www-form-urlencoded' }),
      body,
    });
    return handle(res);
  }

  async function del(path) {
    const res = await fetch(BASE + path, { method: 'DELETE', headers: headers() });
    return handle(res);
  }

  return { get, post, put, delete: del };
})();
