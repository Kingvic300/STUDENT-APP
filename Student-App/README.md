# Student Assistant Application

> An AI-powered study companion built with Spring Boot, featuring intelligent question generation, document summarization, and voice-based authentication.

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

## 📋 Table of Contents

- [Features](#-features)
- [Technology Stack](#-technology-stack)
- [Prerequisites](#-prerequisites)
- [Installation](#-installation)
- [Configuration](#-configuration)
- [Running the Application](#-running-the-application)
- [API Endpoints](#-api-endpoints)
- [Docker Deployment](#-docker-deployment)
- [Testing](#-testing)
- [Architecture](#-architecture)
- [License](#-license)

## ✨ Features

### 🤖 AI-Powered Study Tools
- **Question Generation**: Generate study questions from uploaded documents (PDF, Excel, etc.)
- **Document Summarization**: AI-powered summarization of study materials
- **Study Material Generation**: Create comprehensive study guides from content
- **Study History**: Track and manage previously generated study materials

### 👤 User Management
- **Traditional Authentication**: Email/password-based registration and login
- **Voice Authentication**: Biometric voice-based signup, login, and verification
- **OTP Verification**: Email-based OTP for account verification and password reset
- **Profile Management**: Update user profiles and manage account settings
- **Session Management**: Token-based authentication with logout capabilities

### 📁 File Processing
- **Multi-format Support**: Extract text from PDF and Excel files
- **Cloud Storage**: Integration with Cloudinary for file uploads
- **Voice Sample Processing**: Handle voice recordings for authentication

### 🔒 Security Features
- JWT-based authentication
- Token blacklisting for logout functionality
- Redis-backed session management
- OAuth2 integration (Google authentication)
- Secure password handling

## 🛠 Technology Stack

### Core Framework
- **Java 21**: Latest LTS version for enhanced performance
- **Spring Boot 3.3.5**: Modern Spring framework
- **MongoDB**: NoSQL database for flexible data storage
- **Redis**: In-memory data store for session management

### AI & Machine Learning
- **Spring AI**: Integration with OpenAI-compatible APIs (OpenAI, Groq)
- Advanced question generation and summarization capabilities

### Security & Authentication
- **Spring Security**: Comprehensive security framework
- **JWT (JSON Web Tokens)**: Stateless authentication
- **OAuth2 Client**: Google authentication support

### File Processing
- **Apache PDFBox**: PDF text extraction
- **Apache POI**: Excel file processing
- **Cloudinary**: Cloud-based file storage and management

### Additional Libraries
- **Lombok**: Reduce boilerplate code
- **Brevo (Sendinblue)**: Email service integration
- **dotenv-java**: Environment variable management

## 📦 Prerequisites

- **Java**: JDK 21 or higher
- **Maven**: 3.6+ for dependency management
- **MongoDB**: 4.4+ (local or cloud instance)
- **Redis**: 6.0+ for session management
- **Docker** (optional): For containerized deployment

## 🚀 Installation

### 1. Clone the Repository

```bash
git clone https://github.com/Kingvic300/STUDENT-APP.git
cd Student-App
```

### 2. Install Dependencies

```bash
mvn clean install
```

## ⚙️ Configuration

### Environment Variables

Create a `.env` file in the project root directory:

```env
# MongoDB Configuration
MONGODB_URI=mongodb://localhost:27017/student_assistant
MONGODB_DATABASE=student_assistant

# Redis Configuration
REDIS_HOST=localhost
REDIS_PORT=6379

# JWT Configuration
JWT_SECRET=your_jwt_secret_key_here
JWT_EXPIRATION=86400000

# Cloudinary Configuration
CLOUDINARY_CLOUD_NAME=your_cloud_name
CLOUDINARY_API_KEY=your_api_key
CLOUDINARY_API_SECRET=your_api_secret

# Email Service (Brevo/Sendinblue)
BREVO_API_KEY=your_brevo_api_key
SENDER_EMAIL=noreply@yourdomain.com
SENDER_NAME=Student Assistant

# AI Service Configuration
OPENAI_API_KEY=your_openai_api_key
# Or for Groq:
# GROQ_API_KEY=your_groq_api_key

# Google OAuth2 (Optional)
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret

# Application Configuration
SERVER_PORT=9191
```

### Application Properties

The application uses `application.properties` (or `application.yml`) for Spring Boot configuration. Key properties are loaded from environment variables via the dotenv system.

## 🏃 Running the Application

### Development Mode

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:9191`

### Production Build

```bash
mvn clean package -DskipTests
java -jar target/StudentAssistanApp-0.0.1-SNAPSHOT.jar
```

## 📡 API Endpoints

### User Endpoints (`/users`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/users/send-verification-otp` | Send OTP for email verification |
| POST | `/users/register` | Register new user account |
| POST | `/users/login` | Login with email/password |
| POST | `/users/voice-signup` | Register using voice authentication |
| POST | `/users/voice-login` | Login using voice authentication |
| POST | `/users/enroll-voice` | Enroll voice sample for authentication |
| POST | `/users/verify-voice` | Verify voice sample |
| POST | `/users/enable-voice-auth` | Enable voice authentication for account |
| POST | `/users/disable-voice-auth` | Disable voice authentication |
| POST | `/users/upload` | Upload files to cloud storage |
| PUT | `/users/update-profile` | Update user profile information |
| POST | `/users/send-reset-otp` | Send OTP for password reset |
| POST | `/users/reset-password` | Reset user password |
| GET | `/users/{id}` | Get user details by ID |
| POST | `/users/logout` | Logout from current device |
| POST | `/users/logout-all-devices` | Logout from all devices |

### Study Endpoints (`/api/study`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/study/generate-questions` | Generate questions from uploaded content |
| POST | `/api/study/summarize` | Summarize uploaded document |
| POST | `/api/study/generate-study-material` | Generate comprehensive study material |
| GET | `/api/study/history` | Retrieve user's study history |
| DELETE | `/api/study/history/{studyMaterialId}` | Delete specific study material |

## 🐳 Docker Deployment

### Build Docker Image

```bash
docker build -t student-assistant-app .
```

### Run Container

```bash
docker run -p 9191:9191 \
  --env-file .env \
  student-assistant-app
```

### Docker Compose (Recommended)

Create `docker-compose.yml`:

```yaml
version: '3.8'

services:
  app:
    build: .
    ports:
      - "9191:9191"
    env_file:
      - .env
    depends_on:
      - mongodb
      - redis
    networks:
      - student-app-network

  mongodb:
    image: mongo:latest
    ports:
      - "27017:27017"
    volumes:
      - mongodb_data:/data/db
    networks:
      - student-app-network

  redis:
    image: redis:alpine
    ports:
      - "6379:6379"
    networks:
      - student-app-network

volumes:
  mongodb_data:

networks:
  student-app-network:
    driver: bridge
```

Run with:

```bash
docker-compose up -d
```

## 🧪 Testing

### Run All Tests

```bash
mvn test
```

### Run Specific Test

```bash
mvn test -Dtest=UserServiceImplTest
```

### Test Coverage

The project includes comprehensive tests for:
- User Service operations
- File text extraction functionality
- Application context loading

## 🏗 Architecture

```
├── src/
│   ├── main/
│   │   ├── java/com/izabi/
│   │   │   ├── configuration/     # Spring configuration classes
│   │   │   ├── controller/        # REST API controllers
│   │   │   ├── data/              # MongoDB entities and repositories
│   │   │   ├── dtos/              # Request/Response DTOs
│   │   │   ├── exception/         # Custom exception handlers
│   │   │   ├── mapper/            # Entity-DTO mappers
│   │   │   ├── service/           # Business logic layer
│   │   │   └── util/              # Utility classes
│   │   └── resources/
│   │       └── application.properties
│   └── test/                      # Unit and integration tests
├── Dockerfile
├── pom.xml
└── README.md
```

### Key Components

- **Controllers**: Handle HTTP requests and responses
- **Services**: Implement business logic and orchestrate operations
- **Repositories**: Interface with MongoDB for data persistence
- **DTOs**: Data Transfer Objects for API communication
- **Mappers**: Convert between entities and DTOs
- **Configuration**: Security, MongoDB, Redis, and AI service setup

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

**Copyright © 2025 Victor Oladimeji**

---

## 🤝 Contributing

Contributions, issues, and feature requests are welcome!

1. Fork the project
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📧 Contact

For questions or support, please open an issue in the repository.

---

**Built with ❤️ using Spring Boot and AI**
