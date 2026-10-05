# System Architecture Documentation

The **Cloud File Storage System** is organized as a production-grade monorepo cleanly separating the client presentation tier from the backend API services and cloud storage infrastructure.

---

## 🏛 High-Level Architecture Diagram

```
User (Browser)
       │
       │ HTTPS / CDN
       ▼
┌───────────────────────────────┐
│     Vercel Deployment         │
│     Frontend (Vite / SPA)     │
│  - Plus Jakarta UI System     │
│  - Supabase Auth Client       │
└──────────────┬────────────────┘
               │
               │ HTTPS REST API Requests (Bearer JWT Token)
               ▼
┌───────────────────────────────┐
│      Render Deployment        │
│   Backend (Spring Boot 3.5)   │
│  - Supabase JWT Security      │
│  - File Controller & Services │
│  - Delegating Storage Engine  │
└───────┬──────────────┬────────┘
        │              │
        │ SQL (JDBC)   │ HTTPS Azure SDK (v12.28)
        ▼              ▼
┌──────────────┐ ┌──────────────────────────┐
│  PostgreSQL  │ │   Azure Blob Storage     │
│  - RLS       │ │  - Enterprise Container  │
│  - Flyway    │ │  - Blob Metadata         │
│  - Metadata  │ │  - Secure Streaming      │
└──────────────┘ └──────────────────────────┘
```

---

## 🧩 Architectural Layers & Responsibilities

### 1. Presentation Tier (Frontend — Vercel)
- **Framework**: Vite 5 Single Page Application (HTML5, Vanilla CSS3, Modern ES Modules).
- **Hosting**: Vercel Edge Network.
- **Client Auth**: Supabase Auth JavaScript SDK handles token acquisition, refresh, and session persistence in browser secure storage.
- **REST Communication**: Injects `Authorization: Bearer <Supabase_JWT>` into HTTP request headers against the configurable `VITE_API_BASE_URL`.
- **CORS Handling**: Dispatches cross-origin requests to Render backend respecting credentials and preflight policies.

### 2. Application Tier (Backend — Render)
- **Framework**: Java 17 + Spring Boot 3.5.13 with Spring Data JPA and Spring Security.
- **Hosting**: Render Dockerized Web Service.
- **Port Dynamic Binding**: Listens on port dynamically provided by Render via `${PORT:8080}`.
- **Security & Authorization**: `SupabaseJwtFilter` intercepts requests, extracts the JWT, verifies cryptographic claims using `SUPABASE_JWT_SECRET`, and populates `SecurityContextHolder`.
- **Multi-Tenant Data Isolation**: User ID is extracted from token claims and injected into DB transactions and file isolation queries.

### 3. Storage Layer (`DelegatingStorageService`)
- The backend encapsulates file persistence behind a generic `StorageService` interface.
- **`AzureBlobStorageService`** (Default Production Engine):
  - Uses `com.azure:azure-storage-blob` SDK (v12.28.1).
  - Uses connection strings and container names configured via environment variables.
  - Automatically verifies and initializes the blob container on startup.
  - Handles streaming upload, secure content-type detection, download streaming, and atomic blob deletion.
- **`SupabaseStorageService`**:
  - Direct integration with Supabase Storage buckets via authenticated REST multipart requests.
- **`LocalStorageService`**:
  - Local disk filesystem fallback for local development or container-based offline testing.
- **Pluggable Selection**:
  - Active storage engine is controlled by `STORAGE_TYPE` (`azure`, `supabase`, `local`).

### 4. Persistence Tier (PostgreSQL Database)
- **Database**: PostgreSQL with schema versioning powered by Flyway (`db/migration`).
- **Connection Pooling**: HikariCP with connection validation and keep-alive configuration.
- **Row Level Security (RLS)**: Enforces multi-user isolation directly at the database layer.

---

## 🔒 Security & CORS Architecture

- **Token Validation**: Stateless JWT validation; no server-side session cookies required.
- **Configurable CORS**:
  - `SecurityConfig` and `CorsConfig` bind to `FRONTEND_URL` and `CORS_ALLOWED_ORIGINS`.
  - Production deployments allow the Vercel domain (e.g. `https://your-app.vercel.app`) without wildcard `*` restrictions when credentials are included.
- **Least-Privilege Secret Management**:
  - Frontend only bundles public anon keys.
  - Sensitive DB credentials, Azure Connection Strings, and JWT Secrets are injected exclusively on Render.
