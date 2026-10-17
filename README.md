# ABC Engineering Services Invoice Application

## Project Overview

A Java Spring Boot application developed to manage supplier, invoice, and payment information for a fictional engineering services company.

The application provides a REST API for managing supplier records, recording invoices, and handling payments. It implements business rules governing supplier deactivation, invoice cancellation and payment handling to maintain consistent business data.

The project demonstrates the design and development of a layered backend application, incorporating REST API development, relational database persistence, and automated testing.

## Project Status and Scope

The application implements core supplier, invoice and payment management functionality, including business rules governing supplier deactivation, invoice cancellation, and payment handling.

The project focuses on backend development, exposing functionality through a REST API backed by a relational database. It does not currently include a user interface or authentication and authorisation functionality.

Further improvements are being considered and prioritised as part of the ongoing development process.

## Technology Stack

- **Language:** Java 21
- **Framework:** Spring Boot 4.1.1
- **Build tool:** Apache Maven 3.9.16
- **Database:** PostgreSQL 18
- **Persistence:** Spring Data JPA and Hibernate ORM 7.4.5
- **Testing:** JUnit 6, Mockito, and Spring Boot testing support
- **Version control:** Git and GitHub

## Key Functionality

- **Supplier Management:** Create, view, update, deactivate, and reactivate supplier records.
- **Invoice Management:** Create, view, update, and cancel invoices associated with suppliers.
- **Payment Management:** Record payments against invoices and cancel payments when necessary.

The application enforces business rules governing supplier deactivation, invoice cancellation, and payment handling to maintain data consistency.

## Architecture and Design

The application follows a layered architecture, separating REST controllers, business logic, data access, and domain models.

- **Controller layer:** Exposes REST endpoints and handles HTTP requests.
- **Service layer:** Implements business logic and enforces business rules.
- **Repository layer:** Uses Spring Data JPA to access the PostgreSQL database.
- **Model and DTO layers:** Represent domain entities and structure data exchanged through the API.

The application uses Hibernate ORM for persistence and relational database mapping.

Further details are available in the [Design Document](docs/ABC_Engineering_Services_Application_Design.pdf).

## Testing

- **Unit Testing:** Automated unit tests using JUnit and Mockito.
- **End-to-End Testing:** Manual testing of REST API endpoints using Postman, a REST API testing tool, against a running application connected to a PostgreSQL database.

Testing covers successful operations, business rule enforcement, and error handling across supplier, invoice, and payment functionality.

## Running the Application

### Prerequisites

- Java 21 or later.
- PostgreSQL 18.

### Database Configuration

The application uses PostgreSQL. Before starting the application:

1. Create a PostgreSQL database named `abc_engineering`.
2. Check the database connection settings in `application.properties` and update the database URL and username if required for your environment.
3. Set the `DB_PASSWORD` environment variable to the password for the configured database user.

### Starting the Application

From the project root directory, run:

### Starting the Application

From the project root directory, run:

```powershell
.\mvnw.cmd spring-boot:run
```

Press `Ctrl + C` in the terminal to stop the application.

## Further Development

Potential future enhancements include:

- Containerisation using Docker.
- Authentication and authorisation using Spring Security.
- Cloud deployment.
- Database schema management using Flyway migrations.
- A React frontend for user interaction.
- An accounts dashboard providing an overview of invoices and outstanding payments.
