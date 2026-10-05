import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

// In dev, /api goes to the gateway; in Docker, nginx does the same job.
export default defineConfig({
  plugins: [react()],
  server: { port: 3000, proxy: { "/api": "http://localhost:8080" } },
});
