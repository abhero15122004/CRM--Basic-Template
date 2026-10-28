# Customer Relationship Manager (CRM) Web Application

A full-stack, enterprise-grade Customer Relationship Manager (CRM) web application built using **Java 17**, **Spring Boot 3.5.6**, **Spring MVC**, **Spring Data JPA**, **Hibernate ORM**, **MySQL 8.x**, and **Thymeleaf**.

The application demonstrates clean layered architecture, robust database persistence, server-side Jakarta Bean Validation, resilient error handling, responsive UI design, and automated test coverage without external test dependencies.

---

## 1. Project Overview

The Customer Relationship Manager (CRM) enables sales and operations managers to maintain, track, and manage customer records seamlessly. It implements full Create, Read, Update, and Delete (CRUD) operations backed by a relational MySQL database.

Key architectural features include:
- Strict separation of concerns (Entity, Repository, Service, Controller, Exception layers).
- Server-side and database-level duplicate email prevention.
- Post/Redirect/Get (PRG) pattern for idempotency on form submissions.
- Self-contained automated testing using an isolated in-memory H2 database alongside comprehensive unit and MockMvc integration tests.

---

## 2. Features

- **Customer Directory**: View all registered customers in a clean, responsive data table with formatted columns and customer counts.
- **Empty State UX**: Informative, user-friendly empty state with an invitation to add the first customer when no records exist.
- **Add Customer**: Validated form capturing First Name, Last Name, and Email Address.
- **Edit Customer**: Pre-populated edit form allowing customer updates while safely permitting a customer to retain their existing email without false duplicate conflicts.
- **Delete Customer**: Destructive action executed strictly via HTTP POST with a client-side JavaScript confirmation prompt.
- **Server-Side Validation**: Jakarta Bean Validation (`@NotBlank`, `@Email`, `@Size`) with field-level inline error banners.
- **Email Uniqueness**: Enforced both in application business logic and via database table unique constraints (`uq_customers_email`).
- **Resilient Error Handling**: Centralized `@ControllerAdvice` providing clean error views without leaking SQL syntax, credentials, or internal stack traces.

---

## 3. Architecture & Project Structure

The project strictly follows a layered enterprise architecture:

```
com.example.crm
├── CrmApplication.java               # Spring Boot Application Entrypoint
├── entity
│   └── Customer.java                 # JPA Entity with Validation Annotations
├── repository
│   └── CustomerRepository.java       # Spring Data JPA Repository
├── service
│   ├── CustomerService.java          # Business Logic Interface
│   └── CustomerServiceImpl.java      # Transactional Service Implementation
├── controller
│   └── CustomerController.java       # Spring MVC Web Controller
└── exception
    ├── CustomerNotFoundException.java # Exception for missing entities
    ├── DuplicateEmailException.java   # Exception for duplicate email conflicts
    └── GlobalExceptionHandler.java    # Centralized controller advice

Resources:
src/main/resources
├── application.properties             # Environment-overridable MySQL config
├── templates
│   ├── customers.html                 # Customer list and empty state view
│   ├── customer-form.html             # Add / Edit customer form view
│   └── error.html                     # User-friendly error view
└── static
    └── css
        └── style.css                  # Responsive modern stylesheet

Test Resources:
src/test/resources
└── application.properties             # In-memory H2 test database profile

Database Scripts:
database
└── schema.sql                         # MySQL 8.x DDL and seed data script
```

---

## 4. Technology Stack

- **Java**: Version 17+ (LTS / release 17 target)
- **Framework**: Spring Boot 3.5.6
- **Web Layer**: Spring MVC, Embedded Apache Tomcat
- **Template Engine**: Thymeleaf 3.x
- **Persistence**: Spring Data JPA, Hibernate ORM 6.6
- **Database**: MySQL 8.x (production/local run), H2 Database (automated test isolation)
- **Validation**: Jakarta Bean Validation (Hibernate Validator)
- **Testing**: JUnit 5 (Jupiter), AssertJ, Mockito, Spring Boot Test, Spring MVC Test (MockMvc)
- **Build Tool**: Apache Maven 3.9+ with Maven Wrapper (`mvnw` / `mvnw.cmd`)

---

## 5. Prerequisites

Before running the application, ensure the following are installed on your machine:

1. **Java Development Kit (JDK)**: JDK 17 or higher.
2. **Apache Maven**: Version 3.8+ (or use the included Maven Wrapper `mvnw` / `mvnw.cmd`).
3. **MySQL Server**: MySQL 8.0 or 8.4 running locally on port 3306 (or reachable remotely).
4. **Web Browser**: Chrome, Edge, Firefox, or Safari.

---

## 6. Java & Maven Version Verification

Verify that your installed Java runtime is version 17 or higher:

### Windows (PowerShell or Command Prompt)
```powershell
java -version
mvn -version
```

### macOS / Linux (Terminal)
```bash
java -version
mvn -version
```

Expected output:
```text
java version "17.x.x" (or 21.x.x, 24.x.x)
Apache Maven 3.9.x
```

---

## 7. MySQL Database & User Setup

The repository includes a dedicated SQL script in `database/schema.sql`.

Execute the following commands in MySQL Workbench or via the MySQL CLI client:

```sql
-- 1. Create the database
CREATE DATABASE IF NOT EXISTS crm_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE crm_db;

-- 2. Optional: Create a dedicated user with grants
CREATE USER IF NOT EXISTS 'crm_user'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON crm_db.* TO 'crm_user'@'localhost';
FLUSH PRIVILEGES;

-- 3. Create the customers table
CREATE TABLE IF NOT EXISTS customers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    CONSTRAINT uq_customers_email UNIQUE (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Insert sample data (optional)
INSERT INTO customers (first_name, last_name, email) VALUES
('John', 'Doe', 'john.doe@example.com'),
('Jane', 'Smith', 'jane.smith@example.com'),
('Robert', 'Johnson', 'robert.j@example.com')
ON DUPLICATE KEY UPDATE id=id;
```

---

## 8. Database Configuration Instructions

The application uses standard Spring Boot externalized configuration via `src/main/resources/application.properties`.

Default configuration:
```properties
spring.datasource.url=${SPRING_DATASOURCE_URL:jdbc:mysql://localhost:3306/crm_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&createDatabaseIfNotExist=true}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME:crm_user}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD:your_password}
```

### Overriding via Environment Variables

To provide your local database credentials without modifying source files, set environment variables:

#### Windows (PowerShell)
```powershell
$env:SPRING_DATASOURCE_URL="jdbc:mysql://localhost:3306/crm_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
$env:SPRING_DATASOURCE_USERNAME="root"
$env:SPRING_DATASOURCE_PASSWORD="YourRootPasswordHere"
```

#### Windows (Command Prompt)
```cmd
set SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/crm_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
set SPRING_DATASOURCE_USERNAME=root
set SPRING_DATASOURCE_PASSWORD=YourRootPasswordHere
```

#### macOS / Linux (Bash / Zsh)
```bash
export SPRING_DATASOURCE_URL="jdbc:mysql://localhost:3306/crm_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
export SPRING_DATASOURCE_USERNAME="root"
export SPRING_DATASOURCE_PASSWORD="YourRootPasswordHere"
```

A sample `.env.example` file is provided at the root of the workspace for documentation.

---

## 9. IntelliJ IDEA Import Instructions

1. Open **IntelliJ IDEA**.
2. Select **File > Open...** and navigate to this workspace folder (`c:\Users\reddy\Downloads\CRM`).
3. Select `pom.xml` and choose **Open as Project**.
4. Allow IntelliJ to import Maven dependencies (ensure Project SDK is configured to JDK 17 or higher under **File > Project Structure > Project**).
5. Locate `com.example.crm.CrmApplication` under `src/main/java/com/example/crm/`.
6. Click the green **Run** triangle icon next to `main()` to launch the application.

---

## 10. How to Run the Application

### Option A: Using Maven CLI

From the workspace root directory:

#### Windows (PowerShell / CMD)
```powershell
mvn spring-boot:run
```
Or using the Maven wrapper:
```powershell
.\mvnw spring-boot:run
```

#### macOS / Linux
```bash
./mvnw spring-boot:run
```

### Option B: Running the Packaged Executable JAR

1. Build the application archive:
   ```bash
   mvn clean package -DskipTests
   ```
2. Run the executable jar:
   ```bash
   java -jar target/crm-0.0.1-SNAPSHOT.jar
   ```

Once started, open your web browser and navigate to:
**[http://localhost:8080/customers](http://localhost:8080/customers)** (or [http://localhost:8080](http://localhost:8080)).

---

## 11. How to Run Automated Tests

The automated test suite runs 36 tests spanning unit tests, repository data persistence tests, and web layer MockMvc tests. Tests execute against an isolated in-memory H2 database, requiring no running MySQL instance.

### Run All Tests
```bash
mvn clean test
```

### Test Coverage Highlights
- **Service Layer (`CustomerServiceTest`)**: 14 tests verifying CRUD logic, unique email verification, updating a customer's details while retaining their own email, rejecting duplicate emails, and handling nonexistent customer IDs.
- **Repository Layer (`CustomerRepositoryTest`)**: 7 tests verifying Spring Data JPA query derivations (`findByEmail`, `existsByEmail`, `existsByEmailAndIdNot`) and database-level constraint enforcement.
- **Controller Layer (`CustomerControllerTest`)**: 14 tests using `MockMvc` verifying HTTP routing, model bindings, validation error presentation, empty table state, and Post/Redirect/Get workflows.
- **Application Context (`CrmApplicationTests`)**: 1 test validating full Spring Boot application context startup.

---

## 12. Application Route Reference

| HTTP Method | Route | Description | View / Response |
| :--- | :--- | :--- | :--- |
| `GET` | `/` | Root index route | Redirects to `/customers` |
| `GET` | `/customers` | List all customer records | `customers.html` |
| `GET` | `/customers/new` | Display new customer registration form | `customer-form.html` |
| `POST` | `/customers/save` | Handle form submission (add or update) | Redirect to `/customers` or return `customer-form.html` on validation failure |
| `GET` | `/customers/edit/{id}` | Load existing customer data into edit form | `customer-form.html` (or redirect on missing ID) |
| `POST` | `/customers/delete/{id}` | Delete customer by primary key ID | Redirect to `/customers` with flash status message |

---

## 13. Example CRUD Workflow

### Step 1: Create a Customer
1. Navigate to `http://localhost:8080/customers`.
2. Click **+ Add Customer**.
3. Fill in the form:
   - First Name: `Sarah`
   - Last Name: `Connor`
   - Email: `sarah.c@example.com`
4. Click **Save Customer**.
5. You are redirected to `/customers` with a green alert: *"Customer 'Sarah Connor' was successfully added."*

### Step 2: Read Customers
- The customer table displays ID, First Name (`Sarah`), Last Name (`Connor`), Email (`sarah.c@example.com`), and Action buttons.
- The total customer badge updates dynamically.

### Step 3: Update Customer
1. Click the blue **Edit** button next to Sarah Connor.
2. The form loads pre-populated with her details.
3. Update Last Name to `Connor-Reese`.
4. Leave the email unchanged (`sarah.c@example.com`).
5. Click **Save Customer**.
6. The application validates that retaining her own email is permitted and redirects with: *"Customer 'Sarah Connor-Reese' was successfully updated."*

### Step 4: Duplicate Email Rejection
1. Click **+ Add Customer**.
2. Enter First Name: `John`, Last Name: `Doe`, Email: `sarah.c@example.com`.
3. Click **Save Customer**.
4. The form remains open and highlights the email field with: *"This email address is already in use by another customer."*

### Step 5: Delete Customer
1. Next to the customer record, click the red **Delete** button.
2. A browser dialog appears: *"Are you sure you want to delete customer 'Sarah Connor-Reese'?"*
3. Click **OK**.
4. An HTTP POST request is submitted to `/customers/delete/{id}`.
5. The record is removed from MySQL, and you are redirected with: *"Customer was successfully deleted."*
6. If all customers are deleted, the informative empty state panel is displayed.

---

## 14. Troubleshooting Common Errors

### 1. `CommunicationsException: Communications link failure`
- **Cause**: MySQL server is not running, or is listening on a different port.
- **Resolution**:
  - Check that the MySQL service is active (Windows: `Get-Service MySQL80`, Linux: `systemctl status mysql`).
  - Verify `SPRING_DATASOURCE_URL` points to the correct host and port (default: `localhost:3306`).

### 2. `Access denied for user 'crm_user'@'localhost'`
- **Cause**: Invalid database credentials or missing user permissions.
- **Resolution**:
  - Run the user creation and privilege grant commands from `database/schema.sql` using the MySQL root account.
  - Or override credentials with your root password: `$env:SPRING_DATASOURCE_PASSWORD="your_password"`.

### 3. `Port 8080 is already in use`
- **Cause**: Another service or previous application instance is occupying port 8080.
- **Resolution**:
  - Run with an alternative port: `mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8081"`.
  - Or set environment variable: `$env:SERVER_PORT="8081"`.

---

## 15. Security & Production Limitations

This project is structured as a robust learning and demonstration CRM application. Before promoting this code to an internet-facing production environment, the following enterprise security controls should be implemented:

1. **Authentication & Authorization**: Integrate Spring Security (OAuth2 / OIDC / LDAP / form-based login with BCrypt password hashing and role-based access control e.g., `ROLE_ADMIN`, `ROLE_MANAGER`).
2. **CSRF Protection**: When Spring Security is active, CSRF tokens should be enabled on all mutation requests (`POST`, `PUT`, `DELETE`). Thymeleaf's `th:action` automatically binds CSRF tokens when Spring Security is on the classpath.
3. **Database Schema Management**: Disable `spring.jpa.hibernate.ddl-auto=update` in production. Adopt controlled, version-controlled schema migrations using **Flyway** or **Liquibase**.
4. **Secret Management**: Store sensitive database credentials in a secure secrets vault (AWS Secrets Manager, HashiCorp Vault, Azure Key Vault) rather than environment variables or property files.
5. **HTTPS / TLS**: Terminate TLS at a reverse proxy (e.g., NGINX, Cloudflare) or configure TLS directly inside Spring Boot's embedded Tomcat using an SSL certificate.
6. **Rate Limiting & Audit Logging**: Implement request rate limiting and append immutable audit logs for customer data modifications to adhere to compliance frameworks (e.g., GDPR, CCPA).
