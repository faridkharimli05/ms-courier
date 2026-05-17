# 🚚 Courier Service API

Spring Boot REST API for managing couriers and retrieving available couriers.

---

## ⚙️ Tech Stack
- Java 17+
- Spring Boot
- Spring Web
- Spring Validation
- Lombok
- PostgreSQL

---

## 🚀 Features
- Create courier
- Get all couriers
- Get available courier
- Input validation

---

## 📌 Base URL 
http://localhost:8082/api/couriers


---

## 📡 API Endpoints

### ➕ Create Courier
**POST** `/api/couriers`

Request:
```json
{
  "name": "Farid",
  "phone": "+994501112233",
  "vehicleType": "BIKE"
}


📦 Get All Couriers

GET /api/couriers

🟢 Get Available Courier

GET /api/couriers/available

🧪 Error Handling
400 BAD REQUEST → invalid input
validation errors handled via Spring Validation


🚀 Run Project
git clone https://github.com/your-username/ms-courier.git
./mvnw spring-boot:run

App runs on: http://localhost:8082

👨‍💻 Author

Farid 🚀
