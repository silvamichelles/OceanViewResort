/**
 * OceanView Resort  API Fetch Wrapper
 * Thin wrapper around the Fetch API for clean, centralized HTTP calls.
 */
const API = (function () {

  // Detect the WAR context path from the current URL
  // e.g. /OceanViewResort-1.0-SNAPSHOT/  base is that prefix
  const base = (function () {
    const path = window.location.pathname; // "/OceanViewResort-1.0-SNAPSHOT/dashboard.html"
    const idx  = path.lastIndexOf('/');
    return path.substring(0, idx);         // "/OceanViewResort-1.0-SNAPSHOT"
  })();

  const DEFAULT_HEADERS = {
    'X-Requested-With': 'XMLHttpRequest'
  };

  // Build full URL: API.get('api/dashboard')  /ContextPath/api/dashboard
  function url(path) {
    const p = path.startsWith('/') ? path : '/' + path;
    return base + p;
  }

  async function handleResponse(res) {
    if (res.status === 401) {
      window.location.href = base + '/index.html?error=session';
      return null;
    }
    const contentType = res.headers.get('content-type') || '';
    const data = contentType.includes('application/json') ? await res.json() : await res.text();
    return { ok: res.ok, status: res.status, data };
  }

  async function get(path, params) {
    let fullUrl = url(path);
    if (params) {
      const qs = new URLSearchParams(params).toString();
      fullUrl += '?' + qs;
    }
    const res = await fetch(fullUrl, { headers: DEFAULT_HEADERS });
    return handleResponse(res);
  }

  async function post(path, body) {
    const isFormData = body instanceof FormData || body instanceof URLSearchParams;
    const headers = { ...DEFAULT_HEADERS };
    let bodyStr;
    if (isFormData || body instanceof URLSearchParams) {
      bodyStr = body;
    } else if (typeof body === 'object') {
      headers['Content-Type'] = 'application/x-www-form-urlencoded';
      bodyStr = new URLSearchParams(body).toString();
    } else {
      bodyStr = body;
    }
    const res = await fetch(url(path), { method: 'POST', headers, body: bodyStr });
    return handleResponse(res);
  }

  async function put(path, body) {
    const headers = { ...DEFAULT_HEADERS, 'Content-Type': 'application/x-www-form-urlencoded' };
    const bodyStr = (typeof body === 'object' && !(body instanceof URLSearchParams))
      ? new URLSearchParams(body).toString()
      : body;
    const res = await fetch(url(path), { method: 'PUT', headers, body: bodyStr });
    return handleResponse(res);
  }

  async function del(path) {
    const res = await fetch(url(path), { method: 'DELETE', headers: DEFAULT_HEADERS });
    return handleResponse(res);
  }

  return { get, post, put, delete: del, url };
})();
