☁️ Cloud File Storage System

A file management backend built using Spring Boot and MySQL that supports uploading, viewing, downloading, renaming, and deleting files through REST APIs.

The project stores file metadata such as file name, path, and file type in MySQL, while the actual files are stored locally in the uploads folder.

Features

* Upload files
* Download files
* View files
* List all uploaded files
* Rename files
* Delete files

Tech Stack

* Java
* Spring Boot
* Spring Data JPA
* MySQL
* Maven
* Postman

Project Structure

* Controller Layer → Handles API requests
* Service Layer → Contains business logic
* Repository Layer → Communicates with the database using JPA
* MySQL → Stores file metadata
* Uploads Folder → Stores actual files

API Endpoints

Method	Endpoint	Description
POST	/files/upload	Upload a file
GET	/files	List all files
GET	/files/view/{fileName}	View a file
GET	/files/download/{fileName}	Download a file
PUT	/files/rename	Rename a file
DELETE	/files/{fileName}	Delete a file

Running the Project

Clone the repository and run:

./mvnw spring-boot:run

The application will start on:

http://localhost:8080

Future Improvements

* JWT Authentication
* User-specific file management
* Azure Blob Storage integration
* File sharing using generated links