# Cloud File Storage System

[![Java 17](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.org/projects/jdk/17/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.13-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Frontend](https://img.shields.io/badge/Frontend-Vite%20SPA-646CFF.svg)](https://vitejs.dev/)
[![Deployment: Vercel](https://img.shields.io/badge/Deploy-Vercel-black.svg)](https://vercel.com/)
[![Deployment: Render](https://img.shields.io/badge/Deploy-Render-46E3B7.svg)](https://render.com/)
[![Storage: Azure](https://img.shields.io/badge/Storage-Azure%20Blob%20Storage-0078D4.svg)](https://azure.microsoft.com/)
[![Database: PostgreSQL](https://img.shields.io/badge/Database-PostgreSQL%20with%20RLS-4169E1.svg)](https://www.postgresql.org/)

An enterprise-ready, multi-user cloud file storage and document management system cleanly structured as a production-grade monorepo. It features a modern, responsive Single Page Application frontend ready for zero-downtime deployment to **Vercel**, paired with a containerized **Java Spring Boot 3.5** backend deployed on **Render**, utilizing **Azure Blob Storage** for high-durability file persistence and **PostgreSQL** with Row Level Security (RLS) for relational metadata and tenant isolation.

---

## 🏛 System Architecture

```text
User (Browser)
      |
      v
┌───────────────────────────────┐
│            Vercel             │
│      Vite SPA Frontend        │
│    (HTML5 / CSS3 / ES6+)      │
│     Supabase Auth Client      │
└──────────────┬────────────────┘
               |
               | HTTPS REST API (Bearer JWT)
               v
┌───────────────────────────────┐
│            Render             │
│     Spring Boot Backend       │
│     (Java 17 / Docker)        │
│   Supabase JWT Verification   │
│   Delegating Storage Engine   │
└───────┬──────────────┬────────┘
        |              |
        | JDBC         | Azure SDK v12
        v              v
┌──────────────┐ ┌──────────────────────────┐
│  PostgreSQL  │ │   Azure Blob Storage     │
│  - RLS       │ │  - Enterprise Container  │
│  - Flyway    │ │  - Blob Streaming        │
│  - Metadata  │ │  - Content-Type Registry │
└──────────────┘ └──────────────────────────┘
```

---

## 🛠 Technology Stack

### Frontend
- **Framework & Bundler**: Vite 5 Single Page Application (HTML5, modern ES modules).
- **Design System**: Vanilla CSS3 (custom CSS variables, responsive grid, glassmorphism accents, Plus Jakarta Sans typography).
- **Authentication**: Official Supabase Auth Client (`@supabase/supabase-js`).
- **Production Host**: [Vercel](https://vercel.com/) with native SPA routing rewrites.

### Backend
- **Framework**: Spring Boot 3.5.13 on Java 17.
- **ORM & Data**: Spring Data JPA, Hibernate, HikariCP connection pooling.
- **Security**: Spring Security with `SupabaseJwtFilter` and configurable CORS origin mapping.
- **Database Migrations**: Flyway.
- **Storage Layer**: Official Azure Blob Storage SDK (`com.azure:azure-storage-blob:12.28.1`), pluggable with Supabase Storage and Local Storage via `DelegatingStorageService`.
- **Production Host**: [Render](https://render.com/) via multi-stage Docker container (`render.yaml`).

### Storage & Database
- **Primary Storage**: Azure Blob Storage (Container: `cloudstorage-files`).
- **Database**: PostgreSQL 15+ / 17 with Row Level Security (RLS).

---

## 📁 Repository Structure

```
cloud-file-storage/
│
├── frontend/                     # Independent frontend application (Vercel)
│   ├── public/                   # Static assets & icons
│   ├── src/
│   │   ├── styles/
│   │   │   └── style.css         # Design system & responsive styles
│   │   └── main.js               # Application logic, Supabase Auth, REST client
│   ├── index.html                # Entry point
│   ├── package.json              # Vite & npm dependencies
│   ├── vite.config.js            # Vite build configuration
│   ├── vercel.json               # Vercel SPA routing rewrites & security headers
│   ├── .env.example              # Frontend environment variables template
│   ├── .gitignore                # Frontend gitignore
│   └── README.md                 # Frontend documentation
│
├── backend/                      # Independent Spring Boot application (Render)
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/             # Controllers, Services, Storage, Security, Entities
│   │   │   └── resources/        # application.yml, Flyway migrations (db/migration)
│   │   └── test/                 # Test suite
│   ├── Dockerfile                # Multi-stage production container
│   ├── pom.xml                   # Maven dependencies & plugins
│   ├── mvnw / mvnw.cmd           # Maven wrapper
│   ├── .env.example              # Backend environment variables template
│   ├── .gitignore                # Backend gitignore
│   └── README.md                 # Backend documentation
│
├── docs/                         # System documentation
│   ├── architecture.md           # In-depth architectural specification
│   ├── api.md                    # REST API endpoint reference
│   └── deployment.md             # Complete step-by-step deployment guide
│
├── .gitignore                    # Monorepo root gitignore
├── docker-compose.yml            # Local container orchestration
├── render.yaml                   # Render Blueprint (Infrastructure-as-Code)
└── README.md                     # Monorepo root documentation
```

---

## 💻 Local Development

### 1. Backend (Spring Boot)

```bash
cd backend

# Ensure local PostgreSQL is running, then launch:
./mvnw spring-boot:run
```
The backend starts on `http://localhost:8080`.

### 2. Frontend (Vite)

```bash
cd frontend

# Install dependencies
npm install

# Start development server
npm run dev
```
The frontend starts on `http://localhost:5173`. When running locally, it communicates with `http://localhost:8080`.

### 3. Full-Stack with Docker Compose

To test the containerized backend alongside a local PostgreSQL instance:
```bash
docker-compose up --build
```

---

## ☁️ Deployment Guides

### Frontend Deployment (Vercel)

1. Connect your repository to **Vercel**.
2. Set **Root Directory** to `frontend`.
3. Set **Framework Preset** to `Vite`.
4. Configure Environment Variables:
   - `VITE_API_BASE_URL`: `https://your-backend.onrender.com`
   - `VITE_SUPABASE_URL`: Your Supabase Project URL
   - `VITE_SUPABASE_ANON_KEY`: Your Supabase Public Anon Key
5. Click **Deploy**. Vercel will build with `npm run build` and output `dist/`.

Detailed guide: [docs/deployment.md](docs/deployment.md).

---

### Backend Deployment (Render)

1. Connect your repository to **Render**.
2. Create a new **Web Service**:
   - **Root Directory**: `backend`
   - **Runtime**: `Docker`
3. Configure Environment Variables in Render Dashboard:
   - `PORT`: `8080` (automatically provided by Render)
   - `SPRING_DATASOURCE_URL`: PostgreSQL JDBC connection URL
   - `SPRING_DATASOURCE_USERNAME`: Database username
   - `SPRING_DATASOURCE_PASSWORD`: Database password
   - `STORAGE_TYPE`: `azure`
   - `AZURE_STORAGE_CONNECTION_STRING`: Azure Blob Storage connection string
   - `AZURE_STORAGE_CONTAINER_NAME`: `cloudstorage-files`
   - `SUPABASE_JWT_SECRET`: Supabase JWT secret
   - `CORS_ALLOWED_ORIGINS`: Your Vercel frontend URL (e.g. `https://your-app.vercel.app,http://localhost:5173`)
4. Deploy the service.

Detailed guide: [docs/deployment.md](docs/deployment.md).

---

## ⚙️ Environment Variables Summary

### Backend (`backend/.env.example`)
| Variable | Description |
| :--- | :--- |
| `PORT` | Listening port assigned dynamically by Render (default: `8080`) |
| `SPRING_DATASOURCE_URL` | PostgreSQL JDBC connection URL |
| `SPRING_DATASOURCE_USERNAME` | Database username |
| `SPRING_DATASOURCE_PASSWORD` | Database password |
| `STORAGE_TYPE` | Primary storage engine (`azure`, `supabase`, `local`) |
| `AZURE_STORAGE_CONNECTION_STRING` | Azure Blob Storage connection string |
| `AZURE_STORAGE_CONTAINER_NAME` | Azure Blob Storage container name |
| `FRONTEND_URL` | Primary frontend URL |
| `CORS_ALLOWED_ORIGINS` | Comma-delimited list of allowed CORS origins |
| `SUPABASE_JWT_SECRET` | Supabase JWT Secret |

### Frontend (`frontend/.env.example`)
| Variable | Description |
| :--- | :--- |
| `VITE_API_BASE_URL` | Base URL of the Spring Boot backend |
| `VITE_SUPABASE_URL` | Supabase Project URL |
| `VITE_SUPABASE_ANON_KEY` | Public anonymous key for Supabase Auth |

---

## 🔒 Security Notes

- **Never Commit Secrets**: Real credentials, Azure connection strings, database passwords, and JWT secrets must be set via environment variables only. All `.env` files containing secrets are ignored by `.gitignore`.
- **Stateless Authentication**: Requests require a verified JWT bearer token; no session state is maintained on the server.
- **Configurable CORS**: CORS headers strictly allow the designated Vercel domain and local development origins, rejecting wildcard origins on authenticated endpoints.
- **Row Level Security (RLS)**: Enforced directly inside PostgreSQL to ensure complete multi-tenant user data isolation.
- **Storage Durability**: Render's ephemeral filesystem is not used for file storage; files are securely persisted in Azure Blob Storage.

---

## 📚 Documentation Links

- [System Architecture](docs/architecture.md)
- [REST API Reference](docs/api.md)
- [Production Deployment Guide](docs/deployment.md)
- [Backend Documentation](backend/README.md)
- [Frontend Documentation](frontend/README.md)