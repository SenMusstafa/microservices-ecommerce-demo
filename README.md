# microservices-ecommerce-demo

Spring Boot microservices (Eureka, Config Server, API Gateway, Inventory, Order, Notification over Kafka)
with a React frontend. Everything runs in Docker locally; **only the PostgreSQL database is hosted
externally** (free Neon tier), so you don't need a local DB.

```
Browser → frontend (nginx :3000) → gateway (:8080) → order-service / inventory-service → Neon Postgres
                                                        └→ Kafka → notification-service
```

## Run it

Requirements: Docker with Compose v2 (about 3 GB free RAM).

1. **Create a free database** at <https://neon.tech> (no card needed). In the project's **Connect**
   dialog copy the host, database, user and password.
2. **Configure credentials**:
   ```bash
   cp .env.example .env
   # edit .env: DB_URL=jdbc:postgresql://<host>/<database>?sslmode=require, DB_USER, DB_PASSWORD
   ```
   Use the plain host (no `channel_binding` parameter — the JDBC driver doesn't support it).
   `.env` is git-ignored; never commit it.
3. **Start everything**:
   ```bash
   docker compose up --build
   ```
   The first build takes a few minutes. Tables are created automatically and three demo
   products are seeded on first start.
4. **Open** <http://localhost:3000> (app) and <http://localhost:8761> (Eureka dashboard).

Stop with `docker compose down`. Data stays in Neon, so it survives restarts.

## Things to try (and break)

- Add / edit / delete products in the UI; check them in the Neon console's Tables view.
- Order `product-3` (stock 0) or more than the stock → `REJECTED`. Order a normal amount → `CREATED`,
  and `docker compose logs -f notification-service` shows the Kafka event arriving.
- Create a product with an existing id → 409; negative quantity → 400; unknown id → 404.
- `docker compose stop inventory-service`, then use the UI: the gateway returns an error the UI shows.
  `docker compose start inventory-service` and it recovers once it re-registers in Eureka.
- `docker compose stop kafka`, place an order and read the order-service logs.
- Neon's free tier suspends an idle database; the first request after a pause can take a few seconds.

Useful: `docker compose ps`, `docker compose logs -f <service>`, `docker compose up -d --build <service>`.

## Development without Docker

```bash
mvn test                      # all unit + Testcontainers tests (needs Docker for the Kafka ones)
cd frontend && npm install && npm run dev   # http://localhost:3000, proxies /api to localhost:8080
```
Without `DB_URL` the services fall back to an in-memory H2 database.

## Troubleshooting

- `required variable DB_URL is missing` → you skipped step 2.
- Services crash-looping with `UnknownHostException` for the Neon host, or `Connect timed out`
  between containers → your Docker network blocks container DNS/TCP (seen in some sandboxes and
  corporate VPN setups). Try a plain Docker host.
