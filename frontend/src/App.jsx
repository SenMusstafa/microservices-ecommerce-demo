import { useCallback, useEffect, useState } from "react";
import * as api from "./api.js";

const emptyForm = { id: "", name: "", quantity: 0 };

export default function App() {
  const [tab, setTab] = useState("products");
  const [products, setProducts] = useState([]);
  const [orders, setOrders] = useState([]);
  const [error, setError] = useState(null);
  const [notice, setNotice] = useState(null);
  const [traceId, setTraceId] = useState(null);

  useEffect(() => api.onTraceId(setTraceId), []);

  const run = useCallback(async (fn, okMessage) => {
    setError(null);
    setNotice(null);
    try {
      const result = await fn();
      if (okMessage) setNotice(okMessage);
      return result;
    } catch (e) {
      setError(e.message);
    }
  }, []);

  const refreshProducts = useCallback(
    () => run(async () => setProducts(await api.listProducts())),
    [run]
  );
  const refreshOrders = useCallback(
    () => run(async () => setOrders(await api.listOrders())),
    [run]
  );

  useEffect(() => {
    refreshProducts();
    refreshOrders();
  }, [refreshProducts, refreshOrders]);

  return (
    <main>
      <h1>Microservices Shop</h1>
      <nav>
        <button className={tab === "products" ? "active" : ""} onClick={() => setTab("products")}>
          Products
        </button>
        <button className={tab === "orders" ? "active" : ""} onClick={() => setTab("orders")}>
          Orders
        </button>
      </nav>

      {error && <div className="banner error">⚠ {error}</div>}
      {traceId && (
        <div className="trace">
          Last request trace:{" "}
          <a href={api.zipkinTraceUrl(traceId)} target="_blank" rel="noreferrer">
            {traceId}
          </a>
        </div>
      )}
      {notice && <div className="banner ok">{notice}</div>}

      {tab === "products" ? (
        <Products products={products} run={run} refresh={refreshProducts} />
      ) : (
        <Orders
          products={products}
          orders={orders}
          run={run}
          refreshOrders={refreshOrders}
          refreshProducts={refreshProducts}
        />
      )}
    </main>
  );
}

function Products({ products, run, refresh }) {
  const [form, setForm] = useState(emptyForm);
  const [editing, setEditing] = useState(false);
  const [warehouse, setWarehouse] = useState({});

  const checkWarehouse = async (id) => {
    const result = await run(() => api.getWarehouseStock(id));
    if (result) setWarehouse((w) => ({ ...w, [id]: result }));
  };

  const submit = async (e) => {
    e.preventDefault();
    const payload = { id: form.id, name: form.name, quantity: Number(form.quantity) };
    const result = await run(
      () => (editing ? api.updateProduct(form.id, payload) : api.createProduct(payload)),
      editing ? `Updated ${form.id}` : `Created ${form.id}`
    );
    if (result) {
      setForm(emptyForm);
      setEditing(false);
      refresh();
    }
  };

  const remove = async (id) => {
    await run(() => api.deleteProduct(id), `Deleted ${id}`);
    refresh();
  };

  return (
    <section>
      <form onSubmit={submit} className="row">
        <input
          placeholder="id"
          value={form.id}
          disabled={editing}
          onChange={(e) => setForm({ ...form, id: e.target.value })}
        />
        <input
          placeholder="name"
          value={form.name}
          onChange={(e) => setForm({ ...form, name: e.target.value })}
        />
        <input
          type="number"
          placeholder="quantity"
          value={form.quantity}
          onChange={(e) => setForm({ ...form, quantity: e.target.value })}
        />
        <button type="submit">{editing ? "Update" : "Add"}</button>
        {editing && (
          <button
            type="button"
            className="secondary"
            onClick={() => {
              setForm(emptyForm);
              setEditing(false);
            }}
          >
            Cancel
          </button>
        )}
      </form>

      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>Name</th>
            <th>Stock</th>
            <th title="Fetched from the legacy SOAP system through the adapter">Legacy warehouse</th>
            <th />
          </tr>
        </thead>
        <tbody>
          {products.map((p) => (
            <tr key={p.id}>
              <td>{p.id}</td>
              <td>{p.name}</td>
              <td>{p.quantity}</td>
              <td>
                {warehouse[p.id] ? (
                  `${warehouse[p.id].quantity} @ ${warehouse[p.id].location}`
                ) : (
                  <button className="secondary" onClick={() => checkWarehouse(p.id)}>
                    Check
                  </button>
                )}
              </td>
              <td className="actions">
                <button
                  className="secondary"
                  onClick={() => {
                    setForm({ id: p.id, name: p.name, quantity: p.quantity });
                    setEditing(true);
                  }}
                >
                  Edit
                </button>
                <button className="danger" onClick={() => remove(p.id)}>
                  Delete
                </button>
              </td>
            </tr>
          ))}
          {products.length === 0 && (
            <tr>
              <td colSpan="5" className="empty">No products</td>
            </tr>
          )}
        </tbody>
      </table>
      <button className="secondary" onClick={refresh}>Refresh</button>
    </section>
  );
}

function Orders({ products, orders, run, refreshOrders, refreshProducts }) {
  const [productId, setProductId] = useState("");
  const [quantity, setQuantity] = useState(1);
  const [last, setLast] = useState(null);

  const submit = async (e) => {
    e.preventDefault();
    const result = await run(() => api.createOrder(productId, Number(quantity)));
    // Grab the order's own trace id before the list refreshes below overwrite "last request".
    if (result) setLast({ ...result, traceId: api.lastTraceId });
    refreshOrders();
    refreshProducts();
  };

  return (
    <section>
      <form onSubmit={submit} className="row">
        <select value={productId} onChange={(e) => setProductId(e.target.value)} required>
          <option value="">Select product…</option>
          {products.map((p) => (
            <option key={p.id} value={p.id}>
              {p.name} ({p.quantity} in stock)
            </option>
          ))}
        </select>
        <input
          type="number"
          min="1"
          value={quantity}
          onChange={(e) => setQuantity(e.target.value)}
        />
        <button type="submit">Place order</button>
      </form>

      {last && (
        <div className={`banner ${last.status === "CREATED" ? "ok" : "error"}`}>
          {last.status}: {last.message}
          {last.traceId && (
            <>
              {" "}
              —{" "}
              <a href={api.zipkinTraceUrl(last.traceId)} target="_blank" rel="noreferrer">
                view this order's trace
              </a>
            </>
          )}
        </div>
      )}

      <table>
        <thead>
          <tr>
            <th>Order</th>
            <th>Product</th>
            <th>Qty</th>
            <th>Status</th>
            <th>When</th>
          </tr>
        </thead>
        <tbody>
          {orders.map((o) => (
            <tr key={o.id}>
              <td title={o.id}>{o.id.slice(0, 8)}</td>
              <td>{o.productId}</td>
              <td>{o.quantity}</td>
              <td>
                <span className={`tag ${o.status}`}>{o.status}</span>
              </td>
              <td>{new Date(o.createdAt).toLocaleString()}</td>
            </tr>
          ))}
          {orders.length === 0 && (
            <tr>
              <td colSpan="5" className="empty">No orders yet</td>
            </tr>
          )}
        </tbody>
      </table>
      <button className="secondary" onClick={refreshOrders}>Refresh</button>
    </section>
  );
}
