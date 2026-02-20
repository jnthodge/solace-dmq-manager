# Solace DMQ Manager

A React + Spring Boot application for Solace 10.25.x brokers.

## Features
- HTTP Basic Auth login screen backed by authenticated API calls.
- Lists queues ending in `.dmq` and shows message count.
- Derives origin queue by trimming `.dmq`.
- Browse messages in selected DMQ.
- Single and multi-select messages in list view.
- Message detail panel.
- Replay a DMQ to its origin queue with confirmation and optional delete from DMQ after copy.

## One-port runtime (Spring Boot serves React)
The React app is bundled into Spring Boot static resources, so the UI and API are served from the same port.

```bash
cd backend
mvn spring-boot:run
```

Then open `http://localhost:8080`.

## Configuration
Set environment variables as needed:
- `APP_USERNAME`, `APP_PASSWORD`
- `SOLACE_HOST`, `SOLACE_VPN`, `SOLACE_USERNAME`, `SOLACE_PASSWORD`
- `SOLACE_SEMP_URL`, `SOLACE_SEMP_USERNAME`, `SOLACE_SEMP_PASSWORD`

## Optional frontend-only development
```bash
cd frontend
npm install
npm run dev
```

Dev frontend runs on `http://localhost:5173` and proxies `/api` to `http://localhost:8080`.
