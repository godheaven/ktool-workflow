# ktool-workflow

Dynamic Business Workflow Platform built with Spring Boot 3, Thymeleaf, and H2.

## Features (MVP)
- **Dynamic Workflow Engine**: State-machine based execution with SpEL rule evaluation.
- **Dynamic Forms**: Metadata-driven form rendering.
- **Premium UI**: Modern sidebar layout with TailwindCSS.
- **Embedded Database**: H2 with file-based persistence for easy development.
- **Docker Support**: Ready for containerization.
- **Monitoring**: Actuator + Prometheus metrics.

## Tech Stack
- Java 21
- Spring Boot 3.4.5
- Spring Security
- JPA/Hibernate
- Flyway
- Thymeleaf
- TailwindCSS
- H2 Database

## Getting Started

### Prerequisites
- JDK 21+
- Maven 3.9+
- Docker (optional)

### Build and Run
```bash
mvn clean install
mvn spring-boot:run
```

The application will be available at `http://localhost:8080`.

### Default Credentials
- **Admin**: `admin` / `admin`
- **User**: `user` / `user`

### H2 Console
Available at `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:file:./data/ktool-workflow-db`
- User: `sa`
- Password: (empty)

## Project Structure
- `cl.kanopus.workflow.config`: App & Security configuration.
- `cl.kanopus.workflow.data`: JPA Entities and Repositories.
- `cl.kanopus.workflow.engine`: Core workflow engine and rule evaluation.
- `cl.kanopus.workflow.service`: Business logic layer.
- `cl.kanopus.workflow.web`: Spring MVC controllers and UI logic.
