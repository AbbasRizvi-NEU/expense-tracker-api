# Expense Tracker REST API

[![Build and Test](https://img.shields.io/badge/build-passing-brightgreen)](https://github.com/AbbasRizvi-NEU/expense-tracker-api/actions) [![Coverage](https://img.shields.io/badge/coverage-93%25-brightgreen)](https://github.com/AbbasRizvi-NEU/expense-tracker-api)

A production-ready RESTful API for personal expense tracking with comprehensive testing and automated CI/CD deployment.

## 🚀 Features

- **Complete CRUD Operations** - Create, read, update, and delete expenses
- **Category-Based Tracking** - Organize expenses by categories (Food, Transport, Entertainment, etc.)
- **Spending Analytics** - Get summaries of expenses by category
- **Input Validation** - Ensures data integrity with comprehensive validation
- **Exception Handling** - Graceful error responses with meaningful messages
- **93% Test Coverage** - Comprehensive unit and integration tests
- **Automated CI/CD** - GitHub Actions pipeline with automated testing
- **RESTful Design** - Follows REST best practices

## 🛠️ Tech Stack

- **Backend**: Java 17, Spring Boot 3.2
- **Database**: H2 (Development), PostgreSQL-ready (Production)
- **Testing**: JUnit 5, MockMvc, Mockito (24 tests)
- **Build Tool**: Maven
- **CI/CD**: GitHub Actions
- **Code Coverage**: JaCoCo (93% coverage)
- **Containerization**: Docker

## 📊 Test Coverage

![Test Coverage](https://img.shields.io/badge/coverage-93%25-brightgreen)

- **Service Layer**: 100% coverage
- **Controller Layer**: 100% coverage
- **Exception Handling**: 100% coverage
- **Model Layer**: 85% coverage

## 📖 API Documentation

### Base URL
```
http://localhost:8080/api
```

### Endpoints

#### Health Check
```http
GET /expenses/health
```
Returns application health status.

#### Create Expense

Clients can send an optional `Idempotency-Key` header to safely retry an expense submission after a timeout. Generate a new key for each intended expense. A key must contain 1 to 128 printable ASCII characters without spaces.

Retrying with the same key and expense fields returns the original `201 Created` response and creates no new expense. Reusing that key with different expense fields returns `409 Conflict`. Requests without a key keep the usual create behavior.

The database stores the original response even if the expense is later edited or deleted. The default development database is in memory, so restarting the application clears both expenses and retry records.

```http
POST /expenses
Content-Type: application/json

{
  "description": "Lunch at restaurant",
  "amount": 25.50,
  "category": "FOOD",
  "date": "2024-12-15"
}
```

#### Get All Expenses
```http
GET /expenses
```

#### Get Expense by ID
```http
GET /expenses/{id}
```

#### Update Expense
```http
PUT /expenses/{id}
Content-Type: application/json

{
  "description": "Updated description",
  "amount": 30.00,
  "category": "FOOD",
  "date": "2024-12-15"
}
```

#### Delete Expense
```http
DELETE /expenses/{id}
```

#### Get Spending Summary
```http
GET /expenses/summary
```
Returns total spending grouped by category.

### Available Categories
- `FOOD`
- `TRANSPORT`
- `ENTERTAINMENT`
- `UTILITIES`
- `HEALTHCARE`
- `OTHER`

### Response Codes
- `200 OK` - Successful GET/PUT request
- `201 Created` - Successful POST request
- `204 No Content` - Successful DELETE request
- `400 Bad Request` - Invalid input data
- `404 Not Found` - Resource not found
- `500 Internal Server Error` - Server error

## 🧪 Testing

### Run All Tests
```bash
mvn test
```

### Generate Coverage Report
```bash
mvn jacoco:report
```
View report at: `target/site/jacoco/index.html`

### Test Structure
- **Unit Tests**: Service layer business logic (9 tests)
- **Integration Tests**: Full API endpoint testing (15 tests)
- **Coverage Target**: 80%+ maintained at 93%

## 🚀 Local Development

### Prerequisites
- Java 17+
- Maven 3.8+
- Git

### Setup
1. Clone the repository:
```bash
git clone https://github.com/AbbasRizvi-NEU/expense-tracker-api.git
cd expense-tracker-api
```

2. Build the project:
```bash
mvn clean install
```

3. Run the application:
```bash
mvn spring-boot:run
```

4. Access the API at `http://localhost:8080`

5. View H2 Console at `http://localhost:8080/h2-console`
    - JDBC URL: `jdbc:h2:mem:testdb`
    - Username: `sa`
    - Password: (leave blank)

### Using Docker
```bash
# Build image
docker build -t expense-tracker-api .

# Run container
docker run -p 8080:8080 expense-tracker-api
```

## 📁 Project Structure
```
expense-tracker/
├── src/
│   ├── main/
│   │   ├── java/com/abbasrizvi/expense_tracker/
│   │   │   ├── controller/     # REST endpoints
│   │   │   ├── service/        # Business logic
│   │   │   ├── repository/     # Data access
│   │   │   ├── model/          # Entity classes
│   │   │   └── exception/      # Error handling
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── java/com/abbasrizvi/expense_tracker/
│           ├── service/        # Unit tests
│           └── controller/     # Integration tests
├── .github/workflows/
│   └── ci-cd.yml              # GitHub Actions pipeline
├── pom.xml                    # Maven configuration
├── Dockerfile                 # Docker configuration
└── README.md
```

## 🔄 CI/CD Pipeline

The project uses GitHub Actions for continuous integration and deployment:

1. **Build & Compile** - Maven clean install
2. **Run Tests** - Execute all 24 tests
3. **Code Coverage** - Generate JaCoCo report
4. **Artifact Creation** - Package JAR file
5. **Quality Gates** - Ensure 80%+ coverage

Pipeline triggers on:
- Push to `main` or `develop` branches
- Pull requests to `main`

## 🏗️ Architecture

**Layered Architecture:**
- **Controller Layer** - Handles HTTP requests/responses
- **Service Layer** - Business logic and validation
- **Repository Layer** - Database operations
- **Model Layer** - Data entities

**Design Principles:**
- Single Responsibility Principle
- Dependency Injection
- Separation of Concerns
- RESTful API Design

## 🔒 Error Handling

Global exception handling provides:
- `ResourceNotFoundException` - 404 responses
- `MethodArgumentNotValidException` - 400 responses with field-level errors
- Generic exception handling for unexpected errors

## 📝 Future Enhancements

- [ ] JWT Authentication & Authorization
- [ ] User Management & Multi-tenancy
- [ ] Expense Reports & Export (PDF/CSV)
- [ ] Budget Tracking & Alerts
- [ ] Recurring Expenses
- [ ] Receipt Image Upload
- [ ] RESTful API Documentation (Swagger/OpenAPI)
- [ ] Pagination & Filtering
- [ ] Caching Layer (Redis)
- [ ] Production Database Migration

## 👤 Author

**Abbas Rizvi**
- LinkedIn: [LinkedIn](https://www.linkedin.com/in/abbas-rizvi-763a0135a/)
- GitHub: [@AbbasRizvi-NEU](https://github.com/AbbasRizvi-NEU)
- Email: rizvi.syedmo@northeastern.edu

## 📄 License

This project is open source and available under the [MIT License](LICENSE).

## 🙏 Acknowledgments

Built as a portfolio project demonstrating:
- RESTful API design and implementation
- Test-driven development (TDD)
- CI/CD pipeline setup and automation
- Professional software engineering practices
- Clean code and documentation standards

---

**⭐ If you find this project useful, please consider giving it a star!**
