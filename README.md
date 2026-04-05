# ☁️ Cloud File Storage System

A Spring Boot backend project to upload, download, view, and manage files.

## 🚀 Features
- Upload files
- Download files
- View files
- Delete files
- List all files

## 🛠 Tech Stack
- Java
- Spring Boot
- Spring Data JPA
- H2 Database

## 📌 API Endpoints

| Method | Endpoint |
|--------|---------|
| POST | /files/upload |
| GET | /files/download/{fileName} |
| GET | /files/view/{fileName} |
| GET | /files |
| DELETE | /files/{fileName} |

## ▶️ Run Project

```bash
./mvnw spring-boot:run
