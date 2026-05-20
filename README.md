🚀 Courier Microservice

A production-ready Spring Boot microservice designed for managing courier operations with a clean, layered architecture and database versioning support.

📌 Overview

This service provides a RESTful backend for handling courier-related operations such as creation, management, and retrieval. It follows best practices in Spring Boot development including separation of concerns, DTO mapping, and database migration management.


⚙️ Tech Stack
Java 21
Spring Boot 4.x
Spring Web (REST API)
Spring Data JPA (Hibernate)
PostgreSQL
Liquibase (Database migrations)
Gradle

🏗 Architecture

The project follows a layered architecture:

Controller Layer → Handles HTTP requests
Service Layer → Business logic
Repository Layer → Database access
DTO Layer → Data transfer abstraction
Entity Layer → Database models
Exception Handling → Centralized error management


📁 Project Structure
src/main/java/az/delivery/mscourier
├── controller
├── service
├── repository
├── entity
├── dto
├── mapper
└── exception




🗄 Database
PostgreSQL used as primary database
Schema versioned using Liquibase
Automatic migration on application startup

Database:

courier_db


🚀 Getting Started

1. Clone repository
   git clone https://github.com/your-username/ms-courier.git
cd ms-courier



2. Create database
   CREATE DATABASE courier_db;

3. Configure application
   application.yml:

   spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/courier_db
    username: postgres
    password: your_password


   4. Run application
      ./gradlew bootRun

Application will be available at:
http://localhost:8082

📡 API Endpoints
| Method | Endpoint       | Description       |
| ------ | -------------- | ----------------- |
| POST   | /couriers      | Create courier    |
| GET    | /couriers      | Get all couriers  |
| GET    | /couriers/{id} | Get courier by ID |
| PUT    | /couriers/{id} | Update courier    |
| DELETE | /couriers/{id} | Delete courier    |


🧪 Testing

Recommended tools:

Postman
cURL
Swagger (if enabled in future)

🔧 Key Features
Clean layered architecture
DTO-based request/response handling
Database migration with Liquibase
Input validation
Centralized exception handling
Scalable project structure




📌 Future Improvements
JWT Authentication & Authorization
Docker containerization
Kafka / RabbitMQ integration
CI/CD pipeline (GitHub Actions)
Swagger/OpenAPI documentation
Unit & Integration tests



👨‍💻 Author

Farid Karimli


⭐ Project Status

✔ Backend core completed
✔ Database schema stable
✔ Ready for extension (security, messaging, deployment)
