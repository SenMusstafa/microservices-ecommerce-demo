// Thin fetch wrapper. Throws an Error carrying the HTTP status and the
// server's message, so the UI can show exactly what went wrong.
// The last request's trace id (from the X-Trace-Id header) so the UI can link to Zipkin.
export let lastTraceId = null;
const traceListeners = new Set();
export function onTraceId(fn) {
  traceListeners.add(fn);
  return () => traceListeners.delete(fn);
}

export function zipkinTraceUrl(traceId) {
  const { protocol, hostname } = window.location;
  // Codespaces forwards each port on its own host name: <name>-3000.app.github.dev
  const host = hostname.endsWith(".app.github.dev")
    ? hostname.replace(/-\d+\.app\.github\.dev$/, "-9411.app.github.dev")
    : `${hostname}:9411`;
  return `${protocol}//${host}/zipkin/traces/${traceId}`;
}

async function request(path, options = {}) {
  let res;
  try {
    res = await fetch(path, {
      headers: { "Content-Type": "application/json" },
      ...options,
    });
  } catch (e) {
    throw new Error(`Network error: ${e.message}`);
  }
  const traceId = res.headers.get("x-trace-id");
  if (traceId) {
    lastTraceId = traceId;
    traceListeners.forEach((fn) => fn(traceId));
  }
  const text = await res.text();
  let body = null;
  try {
    body = text ? JSON.parse(text) : null;
  } catch {
    body = text;
  }
  if (!res.ok) {
    const detail = (body && (body.error || body.message)) || text || res.statusText;
    throw new Error(`${res.status} ${detail}`);
  }
  return body;
}

export const listProducts = () => request("/api/v1/inventory/products");
export const createProduct = (p) =>
  request("/api/v1/inventory/products", { method: "POST", body: JSON.stringify(p) });
export const updateProduct = (id, p) =>
  request(`/api/v1/inventory/products/${encodeURIComponent(id)}`, {
    method: "PUT",
    body: JSON.stringify(p),
  });
export const deleteProduct = (id) =>
  request(`/api/v1/inventory/products/${encodeURIComponent(id)}`, { method: "DELETE" });

export const listOrders = () => request("/api/v1/orders");
export const createOrder = (productId, quantity) =>
  request("/api/v1/orders", { method: "POST", body: JSON.stringify({ productId, quantity }) });

export const getWarehouseStock = (id) =>
  request(`/api/v1/inventory/products/${encodeURIComponent(id)}/warehouse`);
