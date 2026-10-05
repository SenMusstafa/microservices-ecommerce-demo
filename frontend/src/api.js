// Thin fetch wrapper. Throws an Error carrying the HTTP status and the
// server's message, so the UI can show exactly what went wrong.
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
