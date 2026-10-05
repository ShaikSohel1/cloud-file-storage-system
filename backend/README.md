# CloudStorage Backend (Java Spring Boot)

Enterprise RESTful API backend for the Cloud File Storage System built with Java 17 and Spring Boot. Handles multi-user data isolation, role-based access control, file metadata management, storage layer abstraction (Azure Blob Storage, Supabase Storage, Local Storage), and real-time storage quotas.

---

## 🛠 Tech Stack

- **Runtime**: Java 17 (OpenJDK / Eclipse Temurin)
- **Framework**: Spring Boot 3.5.13
- **Data Access**: Spring Data JPA & Hibernate
- **Database**: PostgreSQL (with Row Level Security & connection pooling)
- **Migrations**: Flyway
- **Cloud Storage**: Azure Blob Storage SDK (`com.azure:azure-storage-blob:12.28.1`)
- **Authentication**: Supabase JWT verification with Spring Security
- **Containerization**: Multi-stage Docker build optimized for Render

---

## 📁 Directory Structure

```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/sohel/cloudstorage/
│   │   │   ├── config/          # SecurityConfig, CorsConfig, AppProperties
│   │   │   ├── controller/      # FileController, AuthController, etc.
│   │   │   ├── dto/             # Request & Response DTOs
│   │   │   ├── entity/          # FileMetadata, User, etc.
│   │   │   ├── repository/      # Spring Data Repositories
│   │   │   ├── security/        # SupabaseJwtFilter, CurrentUserContext
│   │   │   ├── service/         # FileService, StatsService, etc.
│   │   │   └── storage/         # StorageService, AzureBlobStorageService, DelegatingStorageService
│   │   └── resources/
│   │       ├── db/migration/    # Flyway SQL migrations (V1, V2...)
│   │       └── application.yml  # Dynamic environment-driven configuration
│   └── test/java/               # Unit and Integration test suite
├── .env.example                 # Environment variable templates
├── Dockerfile                   # Multi-stage production container
├── pom.xml                      # Maven project dependencies
└── README.md                    # Backend documentation
```

---

## ⚙️ Environment Variables

The backend dynamically reads runtime configurations from environment variables or falls back to sensible defaults in `application.yml`. See `.env.example` for reference:

| Variable | Description | Default / Example |
| :--- | :--- | :--- |
| `PORT` | Listening HTTP port assigned by Render | `8080` |
| `SPRING_DATASOURCE_URL` | PostgreSQL JDBC connection URL | `jdbc:postgresql://localhost:5432/cloudstorage` |
| `SPRING_DATASOURCE_USERNAME` | PostgreSQL database username | `postgres` |
| `SPRING_DATASOURCE_PASSWORD` | PostgreSQL database password | *(secret)* |
| `STORAGE_TYPE` | Primary storage engine (`azure`, `supabase`, `local`) | `azure` |
| `AZURE_STORAGE_CONNECTION_STRING` | Azure Blob Storage Connection String | *(from Azure Portal)* |
| `AZURE_STORAGE_CONTAINER_NAME` | Azure Blob Storage container name | `cloudstorage-files` |
| `FRONTEND_URL` | Allowed frontend origin for CORS | `http://localhost:5173` |
| `CORS_ALLOWED_ORIGINS` | Comma-separated CORS origins (Render + Vercel) | `http://localhost:5173,https://your-frontend.vercel.app` |
| `SUPABASE_JWT_SECRET` | Supabase JWT Secret for token verification | *(secret)* |

---

## 🚀 Local Development

### 1. Prerequisites
- Java JDK 17 or higher
- Maven 3.9+ (or use the included `./mvnw` wrapper)
- Running PostgreSQL instance

### 2. Run the Application
```bash
# From the backend directory:
./mvnw spring-boot:run
```

The server starts at `http://localhost:8080`.

### 3. Run Tests
```bash
./mvnw clean test
```

### 4. Build Production JAR
```bash
./mvnw clean package -DskipTests
```
The compiled executable JAR is output to `target/cloudstorage-0.0.1-SNAPSHOT.jar`.

---

## 🐳 Docker

### Build the Docker Image
```bash
docker build -t cloudstorage-backend .
```

### Run the Container Locally
```bash
docker run -p 8080:8080 \
  -e SPRING_DATASOURCE_URL="jdbc:postgresql://..." \
  -e SPRING_DATASOURCE_USERNAME="postgres" \
  -e SPRING_DATASOURCE_PASSWORD="..." \
  -e STORAGE_TYPE="azure" \
  -e AZURE_STORAGE_CONNECTION_STRING="..." \
  -e AZURE_STORAGE_CONTAINER_NAME="cloudstorage-files" \
  cloudstorage-backend
```

---

## ☁️ Deployment on Render

1. Create a **New Web Service** on Render.
2. Connect your Git repository.
3. Set the following settings:
   - **Root Directory**: `backend`
   - **Environment**: `Docker`
   - **Region**: Closest to your database & Azure Blob Storage region
4. Under **Environment Variables**, add:
   - `SPRING_DATASOURCE_URL`
   - `SPRING_DATASOURCE_USERNAME`
   - `SPRING_DATASOURCE_PASSWORD`
   - `STORAGE_TYPE=azure`
   - `AZURE_STORAGE_CONNECTION_STRING`
   - `AZURE_STORAGE_CONTAINER_NAME`
   - `CORS_ALLOWED_ORIGINS` (set to your Vercel URL, e.g. `https://your-app.vercel.app`)
5. Render will automatically build the multi-stage Docker container and deploy the service.
