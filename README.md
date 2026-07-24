# Autonomous Contract Negotiation System - Backend

Production-ready Spring Boot 3.2 backend for the Autonomous Contract Negotiation System, built with Java 21, Spring Security 6, JWT authentication, PostgreSQL (Spring Data JPA), and AWS SDK v2 for S3 file storage.

---

## Technical Stack & Architecture

- **Java Version**: Java 21
- **Framework**: Spring Boot 3.2.5
- **Build Tool**: Maven
- **Database**: PostgreSQL
- **Cloud Storage**: AWS S3 (AWS SDK v2)
- **Security**: Spring Security 6 + JJWT 0.12.5 (Stateless REST API)
- **Design Pattern**: Clean Layered Architecture (Controller -> Service -> Repository -> Entity / DTO)
- **Injection Pattern**: 100% Constructor Injection (no `@Autowired` on fields)

---

## Folder Structure

```
contract-negotiation-backend/
├── pom.xml
├── README.md
└── src/
    ├── main/
    │   ├── java/
    │   │   └── com/
    │   │       └── contractnegotiation/
    │   │           └── backend/
    │   │               ├── BackendApplication.java
    │   │               ├── config/
    │   │               │   └── S3Config.java
    │   │               ├── controller/
    │   │               │   ├── AuthController.java
    │   │               │   └── ContractController.java
    │   │               ├── dto/
    │   │               │   ├── ApiResponseDto.java
    │   │               │   ├── ContractDto.java
    │   │               │   ├── FileUploadResponseDto.java
    │   │               │   ├── JwtResponseDto.java
    │   │               │   ├── LoginRequestDto.java
    │   │               │   ├── RegisterRequestDto.java
    │   │               │   └── UserDto.java
    │   │               ├── entity/
    │   │               │   ├── Contract.java
    │   │               │   ├── ContractStatus.java
    │   │               │   ├── Role.java
    │   │               │   └── User.java
    │   │               ├── exception/
    │   │               │   ├── GlobalExceptionHandler.java
    │   │               │   ├── InvalidFileException.java
    │   │               │   ├── ResourceNotFoundException.java
    │   │               │   └── S3UploadException.java
    │   │               ├── repository/
    │   │               │   ├── ContractRepository.java
    │   │               │   └── UserRepository.java
    │   │               ├── security/
    │   │               │   ├── CustomUserDetailsService.java
    │   │               │   ├── JwtAuthenticationEntryPoint.java
    │   │               │   ├── JwtAuthenticationFilter.java
    │   │               │   ├── JwtTokenProvider.java
    │   │               │   └── SecurityConfig.java
    │   │               └── service/
    │   │                   ├── AuthService.java
    │   │                   ├── ContractService.java
    │   │                   ├── S3Service.java
    │   │                   └── impl/
    │   │                       ├── AuthServiceImpl.java
    │   │                       ├── ContractServiceImpl.java
    │   │                       └── S3ServiceImpl.java
    │   └── resources/
    │       └── application.properties
    └── test/
        └── java/
            └── com/
                └── contractnegotiation/
                    └── backend/
                        └── BackendApplicationTests.java
```

---

## Configuration

### 1. Database Configuration (`src/main/resources/application.properties`)

Ensure PostgreSQL is running and a database named `contract_db` exists:

```sql
CREATE DATABASE contract_db;
```

Update parameters or pass environment variables:
```properties
spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/contract_db}
spring.datasource.username=${DB_USERNAME:postgres}
spring.datasource.password=${DB_PASSWORD:postgres}
spring.jpa.hibernate.ddl-auto=update
```

### 2. AWS S3 Configuration

Provide your AWS credentials in `application.properties` or environment variables:
```properties
aws.accessKey=${AWS_ACCESS_KEY:your_access_key}
aws.secretKey=${AWS_SECRET_KEY:your_secret_key}
aws.region=${AWS_REGION:us-east-1}
aws.s3.bucket-name=${AWS_S3_BUCKET_NAME:your_s3_bucket_name}
```

---

## Import into Spring Tool Suite (STS)

1. Open **Spring Tool Suite (STS)**.
2. Click `File` -> `Import...`
3. Select `Maven` -> `Existing Maven Projects` and click `Next`.
4. Click `Browse...` and navigate to the project directory: `contract-negotiation-backend`.
5. Ensure `pom.xml` is selected and click `Finish`.
6. Right-click the project -> `Maven` -> `Update Project...` (or `Alt + F5`).
7. Right-click `BackendApplication.java` -> `Run As` -> `Spring Boot App`.

---

## Building and Running from Terminal

```bash
# Build the project
mvn clean package

# Run the executable jar
java -jar target/contract-negotiation-backend-1.0.0-SNAPSHOT.jar
```

---

## Postman API Testing Documentation

### 1. User Registration

- **HTTP Method**: `POST`
- **URL**: `http://localhost:8080/api/auth/register`
- **Headers**:
  - `Content-Type`: `application/json`
- **Request Body**:
```json
{
  "username": "john_doe",
  "email": "john.doe@example.com",
  "password": "password123",
  "role": "ROLE_USER"
}
```
- **Response JSON** (`201 Created`):
```json
{
  "timestamp": "2026-07-24T19:45:00",
  "status": 201,
  "message": "User registered successfully",
  "data": {
    "id": 1,
    "username": "john_doe",
    "email": "john.doe@example.com",
    "role": "ROLE_USER",
    "createdAt": "2026-07-24T19:45:00"
  }
}
```

---

### 2. User Login (Obtain JWT Token)

- **HTTP Method**: `POST`
- **URL**: `http://localhost:8080/api/auth/login`
- **Headers**:
  - `Content-Type`: `application/json`
- **Request Body**:
```json
{
  "username": "john_doe",
  "password": "password123"
}
```
- **Response JSON** (`200 OK`):
```json
{
  "timestamp": "2026-07-24T19:46:00",
  "status": 200,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huX2RvZSIsImlhdCI6MTYyN...",
    "tokenType": "Bearer",
    "id": 1,
    "username": "john_doe",
    "email": "john.doe@example.com",
    "role": "ROLE_USER"
  }
}
```

---

### 3. Upload Contract PDF

- **HTTP Method**: `POST`
- **URL**: `http://localhost:8080/api/contracts/upload`
- **Headers**:
  - `Authorization`: `Bearer <YOUR_JWT_TOKEN>`
- **Request Body** (`form-data`):
  - `file`: `[Select PDF file]` (File)
  - `title`: `Software Procurement Contract 2026` (Text, optional)
- **Response JSON** (`201 Created`):
```json
{
  "timestamp": "2026-07-24T19:47:00",
  "status": 201,
  "message": "PDF Contract uploaded successfully to S3 and recorded in database",
  "data": {
    "message": "Contract PDF uploaded successfully",
    "contractId": 1,
    "title": "Software Procurement Contract 2026",
    "fileName": "sample_contract.pdf",
    "fileType": "application/pdf",
    "fileSize": 1048576,
    "s3Url": "https://contract-negotiation-bucket.s3.us-east-1.amazonaws.com/contracts/a1b2c3d4_sample_contract.pdf",
    "status": "UPLOADED"
  }
}
```

---

### 4. Get Contract Details by ID

- **HTTP Method**: `GET`
- **URL**: `http://localhost:8080/api/contracts/1`
- **Headers**:
  - `Authorization`: `Bearer <YOUR_JWT_TOKEN>`
- **Response JSON** (`200 OK`):
```json
{
  "timestamp": "2026-07-24T19:48:00",
  "status": 200,
  "message": "Contract retrieved successfully",
  "data": {
    "id": 1,
    "title": "Software Procurement Contract 2026",
    "originalFileName": "sample_contract.pdf",
    "fileType": "application/pdf",
    "fileSize": 1048576,
    "s3Url": "https://contract-negotiation-bucket.s3.us-east-1.amazonaws.com/contracts/a1b2c3d4_sample_contract.pdf",
    "uploadedById": 1,
    "uploadedByUsername": "john_doe",
    "uploadedAt": "2026-07-24T19:47:00",
    "status": "UPLOADED"
  }
}
```

---

### 5. Get All Contracts Uploaded by User

- **HTTP Method**: `GET`
- **URL**: `http://localhost:8080/api/contracts/user/1`
- **Headers**:
  - `Authorization`: `Bearer <YOUR_JWT_TOKEN>`
- **Response JSON** (`200 OK`):
```json
{
  "timestamp": "2026-07-24T19:49:00",
  "status": 200,
  "message": "User contracts retrieved successfully",
  "data": [
    {
      "id": 1,
      "title": "Software Procurement Contract 2026",
      "originalFileName": "sample_contract.pdf",
      "fileType": "application/pdf",
      "fileSize": 1048576,
      "s3Url": "https://contract-negotiation-bucket.s3.us-east-1.amazonaws.com/contracts/a1b2c3d4_sample_contract.pdf",
      "uploadedById": 1,
      "uploadedByUsername": "john_doe",
      "uploadedAt": "2026-07-24T19:47:00",
      "status": "UPLOADED"
    }
  ]
}
```
