# Solace DMQ Manager

A React + Spring Boot application for Solace 10.25.x brokers.

## Features
- HTTP Basic Auth protected API + login page.
- Lists queues ending in `.dmq` and shows message count.
- Derives origin queue by trimming `.dmq`.
- Browse messages in selected DMQ.
- Single and multi-select messages in list view.
- Message detail panel.
- Replay a DMQ to its origin queue with confirmation and optional delete from DMQ after copy.

## Run backend
```bash
cd backend
mvn spring-boot:run
```

Configure with environment variables:
- `APP_USERNAME`, `APP_PASSWORD`
- `SOLACE_HOST`, `SOLACE_VPN`, `SOLACE_USERNAME`, `SOLACE_PASSWORD`
- `SOLACE_SEMP_URL`, `SOLACE_SEMP_USERNAME`, `SOLACE_SEMP_PASSWORD`

## Run frontend
```bash
cd frontend
npm install
npm run dev
```

Frontend runs on `http://localhost:5173` and proxies `/api` to the backend.
