# REST API Reference Documentation

All endpoints (except public health checks) require a valid Supabase JWT bearer token passed in the HTTP Authorization header:

```http
Authorization: Bearer <SUPABASE_JWT_ACCESS_TOKEN>
```

Responses adhere to the standardized `ApiResponse<T>` envelope:

```json
{
  "status": "success",
  "message": "Operation completed successfully",
  "data": { ... },
  "timestamp": "2026-10-04T14:30:00Z"
}
```

---

## 📂 File Management API (`/files`)

### 1. Upload File
- **Method**: `POST /files/upload`
- **Content-Type**: `multipart/form-data`
- **Form Parameters**:
  - `file` (File, Required): The file to upload.
  - `folderId` (UUID, Optional): Target folder UUID.
- **Backend Flow**:
  1. Authenticates current user from JWT claim.
  2. Passes data stream to `DelegatingStorageService` -> `AzureBlobStorageService` (or Supabase/Local).
  3. Writes metadata record into PostgreSQL `files` table.
- **Response**: `200 OK` with `FileResponse` object.

### 2. List User Files
- **Method**: `GET /files`
- **Query Parameters**:
  - `folderId` (UUID, Optional): Filter by parent folder. Root folder if omitted.
- **Response**: `200 OK` with `List<FileResponse>`.

### 3. Download File
- **Method**: `GET /files/download/{id}`
- **Path Parameters**:
  - `id` (UUID): File UUID.
- **Response**: `200 OK` with binary octet stream and `Content-Disposition: attachment; filename="..."`.

### 4. Inline File View / Preview
- **Method**: `GET /files/view/{id}`
- **Path Parameters**:
  - `id` (UUID): File UUID.
- **Response**: `200 OK` with binary stream suitable for browser tab rendering.

### 5. Toggle Favorite Status
- **Method**: `PUT /files/{id}/favorite`
- **Path Parameters**:
  - `id` (UUID): File UUID.
- **Response**: `200 OK` with updated `FileResponse`.

### 6. Rename File
- **Method**: `PUT /files/{id}/rename?name={newName}`
- **Path Parameters**:
  - `id` (UUID): File UUID.
- **Query Parameters**:
  - `name` (String, Required): New file display name.
- **Response**: `200 OK` with updated `FileResponse`.

### 7. Soft Delete File (Move to Trash)
- **Method**: `DELETE /files/{id}`
- **Path Parameters**:
  - `id` (UUID): File UUID.
- **Response**: `200 OK` with confirmation message.

### 8. List Favorites
- **Method**: `GET /files/favorites`
- **Response**: `200 OK` with `List<FileResponse>` marked as favorite.

### 9. Move File
- **Method**: `POST /files/{id}/move`
- **Request Body**:
  ```json
  {
    "targetFolderId": "uuid-here"
  }
  ```
- **Response**: `200 OK` with updated `FileResponse`.

---

## 📊 Analytics & Storage Quotas (`/stats`)

### Get Storage Metrics
- **Method**: `GET /stats/storage`
- **Response**: `200 OK`
```json
{
  "status": "success",
  "message": "Storage analytics retrieved",
  "data": {
    "totalUsedBytes": 104857600,
    "storageQuotaBytes": 5368709120,
    "fileCount": 42,
    "folderCount": 6,
    "categories": {
      "Documents": 31457280,
      "Images": 52428800,
      "Videos": 0,
      "Audio": 0,
      "Code": 10485760,
      "Archives": 10485760,
      "Other": 0
    }
  }
}
```

---

## 📁 Folders API (`/folders`)

- `POST /folders`: Create new folder (`name`, `parentId`).
- `GET /folders`: List root or nested folders.
- `PUT /folders/{id}`: Rename folder.
- `DELETE /folders/{id}`: Soft delete folder and contents.

---

## 🗑 Trash & Recovery API (`/trash`)

- `GET /trash`: List items in trash.
- `POST /trash/{id}/restore`: Restore item from trash to original location.
- `DELETE /trash/{id}`: Permanently delete file from storage and database.
- `DELETE /trash/empty`: Permanently purge all items in trash.

---

## 🔍 Search API (`/search`)

- `GET /search?query={q}`: Full-text search matching filename, extensions, and tags.

---

## ⚙️ Error Codes & Handling

| HTTP Status | Reason |
| :--- | :--- |
| `400 Bad Request` | Missing required parameters or payload validation failure |
| `401 Unauthorized` | Missing or invalid Supabase JWT bearer token |
| `403 Forbidden` | Accessing another user's private file |
| `404 Not Found` | Requested file or folder does not exist |
| `413 Payload Too Large` | Upload exceeds maximum configured file size |
| `500 Internal Error` | Cloud storage or database connectivity error |
