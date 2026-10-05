# Production Deployment Guide: Vercel + Render + Azure + PostgreSQL

This guide provides the complete, step-by-step procedure for deploying the **Cloud File Storage System** to production using **Render** for the Java Spring Boot backend, **Vercel** for the Vite frontend, **Azure Blob Storage** for high-durability file persistence, and **PostgreSQL** for relational metadata and Row Level Security.

---

## 📋 Architecture Overview

- **Frontend**: Single Page Application hosted on **Vercel Edge Network**.
- **Backend**: Containerized Spring Boot 3.5 service hosted on **Render**.
- **Storage Layer**: **Azure Blob Storage** container (`cloudstorage-files`).
- **Database Layer**: **PostgreSQL** with Flyway migrations & Row Level Security.
- **Authentication**: **Supabase Auth** with stateless JWT verification.

---

## 🚀 Step-by-Step Deployment Sequence

### Step 1: Configure PostgreSQL Database
1. Provision a PostgreSQL instance (e.g., Supabase, Neon, AWS RDS, or Render Managed PostgreSQL).
2. Obtain your JDBC connection string, username, and password:
   ```text
   jdbc:postgresql://<host>:5432/<dbname>?sslmode=require
   ```
3. Flyway migrations (`backend/src/main/resources/db/migration/`) will execute automatically on application startup to create and migrate tables.
4. Verify database network connectivity (ensure cloud IP access is allowed).

---

### Step 2: Configure Azure Blob Storage
1. Log in to the [Azure Portal](https://portal.azure.com/).
2. Create or select a **Storage Account** (Standard General-purpose v2).
3. Under **Security + networking** -> **Access keys**, copy the **Connection string**:
   ```text
   DefaultEndpointsProtocol=https;AccountName=<storage_account_name>;AccountKey=<secret_key>;EndpointSuffix=core.windows.net
   ```
4. Under **Data storage** -> **Containers**, create a new container:
   - Name: `cloudstorage-files` (or custom name configured in `AZURE_STORAGE_CONTAINER_NAME`)
   - Public access level: **Private (no anonymous access)**
5. Note: The backend `AzureBlobStorageService` will also automatically verify container existence on startup and create it if missing.

---

### Step 3: Deploy Backend to Render
1. Push this repository to GitHub / GitLab.
2. Log in to the [Render Dashboard](https://dashboard.render.com/).
3. Click **New +** -> **Web Service**.
4. Connect your Git repository.
5. In the configuration page, specify:
   - **Name**: `cloudstorage-backend`
   - **Root Directory**: `backend`
   - **Environment / Runtime**: `Docker`
   - **Region**: Choose the region closest to your PostgreSQL database and Azure Blob Storage
   - **Branch**: `main`
   - **Plan**: Free or Starter

---

### Step 4: Add Backend Environment Variables on Render
Under **Environment Variables** in your Render Web Service dashboard, add:

| Key | Example Value | Description |
| :--- | :--- | :--- |
| `PORT` | `8080` | Dynamic port Render binds to |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://<host>:5432/<db>?sslmode=require` | PostgreSQL JDBC connection URL |
| `SPRING_DATASOURCE_USERNAME` | `postgres` | Database username |
| `SPRING_DATASOURCE_PASSWORD` | `<secure_password>` | Database password |
| `STORAGE_TYPE` | `azure` | Primary storage engine |
| `AZURE_STORAGE_CONNECTION_STRING` | `DefaultEndpointsProtocol=https;...` | Azure Blob Storage connection string |
| `AZURE_STORAGE_CONTAINER_NAME` | `cloudstorage-files` | Azure container name |
| `SUPABASE_JWT_SECRET` | `<supabase_jwt_secret>` | Supabase JWT Secret for token verification |
| `FRONTEND_URL` | `http://localhost:5173` | Initial allowed CORS origin |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173` | Updated after Step 7 |

Click **Save Changes**. Render will automatically build the multi-stage Docker container and launch the service.

---

### Step 5: Get Render Backend URL
1. Once the deployment status turns to **"Live"**, copy the service URL at the top of your Render dashboard.
2. Example format:
   ```text
   https://cloudstorage-backend.onrender.com
   ```
3. Test the service responds to ping / health requests:
   ```bash
   curl -I https://cloudstorage-backend.onrender.com/files
   # Should return 401 Unauthorized (confirming Spring Security is active)
   ```

---

### Step 6: Configure Frontend Environment Variables for Vercel
In your local environment or directly in Vercel:
1. Navigate to `frontend/.env.example` to review required values.
2. Prepare the values:
   - `VITE_API_BASE_URL`: `https://cloudstorage-backend.onrender.com`
   - `VITE_SUPABASE_URL`: `https://<your-project>.supabase.co`
   - `VITE_SUPABASE_ANON_KEY`: `<your-public-anon-key>`

---

### Step 7: Deploy Frontend to Vercel
1. Log in to [Vercel](https://vercel.com/) and click **Add New...** -> **Project**.
2. Select your repository.
3. Configure project settings:
   - **Root Directory**: `frontend`
   - **Framework Preset**: `Vite` (auto-detected)
   - **Build Command**: `npm run build`
   - **Output Directory**: `dist`
4. Expand **Environment Variables** and enter:
   - `VITE_API_BASE_URL` = `https://cloudstorage-backend.onrender.com`
   - `VITE_SUPABASE_URL` = `https://<your-project>.supabase.co`
   - `VITE_SUPABASE_ANON_KEY` = `<your-public-anon-key>`
5. Click **Deploy**.
6. When deployment finishes, copy your live Vercel domain:
   ```text
   https://cloud-file-storage-system.vercel.app
   ```

---

### Step 8: Configure Backend CORS with Vercel URL
1. Return to the Render dashboard for `cloudstorage-backend`.
2. Under **Environment Variables**, update `CORS_ALLOWED_ORIGINS` and `FRONTEND_URL`:
   - `FRONTEND_URL` = `https://cloud-file-storage-system.vercel.app`
   - `CORS_ALLOWED_ORIGINS` = `https://cloud-file-storage-system.vercel.app,http://localhost:5173`
3. Click **Save Changes**. Render will automatically perform a zero-downtime rolling restart.

---

### Step 9: Test Authentication
1. Open your Vercel URL in your browser: `https://cloud-file-storage-system.vercel.app`.
2. Sign up with a test email account or log in with an existing account.
3. Confirm that the JWT access token is stored and the security session check screen transitions into the main file manager view.

---

### Step 10: Test File Upload
1. Click **Upload File** or drag-and-drop a test document (e.g., `test.pdf` or `photo.png`).
2. Watch progress bar until complete.
3. Verify in the Azure Portal:
   - Under `cloudstorage-files` container, verify the newly created blob is present.
4. Verify in the PostgreSQL database:
   - Verify a new record was inserted in the `files` table with the user's ID.

---

### Step 11: Test File Listing & Metadata
1. Refresh the Vercel web page.
2. Confirm the uploaded file appears in the table with its name, size, category icon, and upload date.
3. Search for the file in the search box; confirm real-time filtering works.
4. Switch to the **Storage Analytics** tab; confirm used space reflects the uploaded bytes.

---

### Step 12: Test Download & Inline Preview
1. Click the **View** icon for an image or text file; verify inline preview displays correctly.
2. Click the **Download** icon; confirm the file downloads with the proper original filename and byte size.

---

### Step 13: Test File Operations & Delete
1. Click the **Star** icon to mark the file as favorite. Check the **Favorites** tab.
2. Click the **Rename** icon and change the filename.
3. Click the **Delete** icon to move the file to trash.
4. Check the **Trash** tab; restore or permanently delete the file.
5. In Azure Portal, verify the blob is deleted if purged permanently.
