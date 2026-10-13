# ABC Engineering Services --- Implementation Backlog

## Purpose

This document records the implementation tasks, decisions, engineering
improvements and future enhancements for the ABC Engineering Services
application. It is a working backlog rather than a replacement for the
requirements and design document, which remains the source of truth for
the agreed business requirements and solution design.

## Status Legend

  -----------------------------------------------------------------------
  Status                              Meaning
  ----------------------------------- -----------------------------------
  **DONE**                            Completed

  **IN PROGRESS**                     Currently being worked on

  **TO DO**                           Agreed work that has not yet been
                                      completed

  **DECISION NEEDED**                 A decision is required before
                                      proceeding

  **DECISION**                        A decision has been made and
                                      recorded

  **PARKED**                          Deliberately deferred; not part of
                                      the current implementation scope
  -----------------------------------------------------------------------

## 1. Development Approach

**Status: DONE**

The application uses the following technologies:

-   Java 21
-   Spring Boot 4.1.1
-   Spring Framework 7.0.9
-   Maven
-   Spring Web
-   Spring Data JPA and Hibernate
-   PostgreSQL
-   Jakarta Bean Validation
-   Lombok
-   JUnit 6 and Spring Boot Test
-   Git

The implementation prioritises a straightforward, maintainable solution
that satisfies the agreed requirements. Features and technical
improvements are kept within scope; useful ideas that are not required
for the current phase are recorded for later consideration.

## 2. Application Structure

**Status: DONE**

The base package is `com.abcengineering.invoiceapp`.

The application is organised into the following packages:

| Package | Responsibility |
|---|---|
| `controller` | REST API boundary; receives HTTP requests and returns responses |
| `service` | Application operations and business-rule enforcement |
| `repository` | Persistence access through Spring Data JPA |
| `model` | JPA entity classes |
| `dto` | Data transfer objects, including request DTOs |
| `exception` | Application-specific exceptions and centralised REST exception handling |

Dependencies are injected through constructors.

## 3. Validation Approach

**Status: DONE**

Validation is applied at appropriate layers:

-   **Request DTOs:** Jakarta Bean Validation annotations such as
    `@NotNull`, `@NotBlank` and `@Size` validate incoming data.
-   **Service layer:** checks entity existence, current state and
    business rules that depend on application data.
-   **Persistence layer:** JPA mappings and database constraints provide
    the final integrity boundary.

Basic request validation is not duplicated unnecessarily in the service
layer. Business rules that depend on persisted state remain enforced in
the service layer.

## 4. Supplier Management

**Status: DONE**

### 4.1 Entity and Data Model

The `Supplier` entity is in `com.abcengineering.invoiceapp.model`.

The entity includes:

-   A database-generated identifier.
-   Company name and business address.
-   Optional contact name, email address and telephone number.
-   An `active` flag.

Supplier names are not required to be unique. A supplier has one
business address in the current model. Suppliers are deactivated rather
than physically deleted, preserving historical references from invoices.
The application does not currently record a deactivation reason or
history.

### 4.2 JPA Mapping and Constructors

The entity uses `@Entity`, `@Table`, `@Id` and
`@GeneratedValue(strategy = GenerationType.IDENTITY)`. Database column
names use the agreed naming convention.

The company name and address are required. The company name also uses
`@NotBlank` validation. The entity provides a no-argument constructor
for JPA and explicit constructors for application use. The `active` flag
defaults to active for newly created suppliers.

Lombok `@Getter` and `@Setter` are used rather than `@Data`. The latter
also generates methods such as `equals()`, `hashCode()` and
`toString()`, which can have unintended consequences with JPA entities
and relationships.

### 4.3 Repository

`SupplierRepository` extends `JpaRepository<Supplier, Integer>`. The
inherited repository operations are sufficient for the current
requirements; no custom queries are needed.

### 4.4 Service and API Operations

The service supports:

-   Creating a supplier.
-   Retrieving a supplier by ID.
-   Retrieving all suppliers.
-   Updating supplier details.
-   Deactivating a supplier.
-   Reactivating a supplier.

The REST API is exposed under `/api/suppliers`. Request DTOs validate
incoming data.

### 4.5 Testing

Tests cover supplier creation and retrieval, listing, updating,
deactivation and reactivation, not-found cases, and prevention of
deactivation when the supplier has outstanding invoices.

## 5. Invoice Management

**Status: DONE**

The `Invoice` entity is associated with a supplier and can have zero or
more payments.

### 5.1 Outstanding Amount

The outstanding amount is calculated as the invoice amount minus the
total amount of payments made that count towards the invoice balance.

### 5.2 Payment Status

Invoice payment status is derived from the invoice balance:

-   **Outstanding:** no payments have been made.
-   **Partially Paid:** payments have been made and a positive balance
    remains.
-   **Paid in Full:** the outstanding balance is zero.

The status is derived rather than stored as an independent database
value.

### 5.3 Overdue State

Whether an invoice is overdue is derived from its due date and payment
state rather than stored as a separate full status. Proactive overdue
monitoring and reporting are not part of the current implementation
scope.

### 5.4 Repository, Service, API and Tests

Invoice repository, service and controller operations are implemented,
along with request DTOs and tests. The implementation covers invoice
creation, retrieval, listing, updating and cancellation, including
relevant not-found and business-rule cases.

Overpayment handling remains deferred.

## 6. Payment Management

**Status: DONE**

The `Payment` entity is associated with an invoice; supplier information
is available through that invoice relationship. A payment records its
amount, date and method.

Payment repository, service and controller operations are implemented,
together with request DTOs and tests. The API supports creating and
retrieving payments and cancelling a payment. Tests cover relevant
not-found and already-cancelled cases.

Cancelled payments are excluded from the appropriate outstanding-balance
calculations.

## 7. Integration and End-to-End Testing

**Status: DONE**

End-to-end tests exercise the Supplier, Invoice and Payment features
through REST API requests against the running application. PostgreSQL
state is checked during testing using pgAdmin.

The test scenarios cover normal operations and relevant error and
business-rule cases across all three feature areas.

## 8. Javadoc

**Status: DONE**

Javadoc comments have been added to relevant controller and service
methods and custom repository methods, where applicable.

## 9. Database and Configuration

**Status: DONE**

-   The development database is PostgreSQL, named `abc_engineering`.
-   The database password is supplied through the `DB_PASSWORD`
    environment variable rather than hard-coded or committed to source
    control.
-   `spring.jpa.hibernate.ddl-auto=update` is used during development.
-   `spring.jpa.show-sql=true` is enabled to assist debugging.

A database migration tool may be appropriate as the application matures.

## 10. Phase 1B Engineering Improvements

This section records engineering improvements made or considered after
the initial Phase 1 implementation.

### 10.1 Service Method Naming

**Status: DONE**

Service method names were reviewed for consistency and clarity.

### 10.2 Supplier Reactivation Return Type

**Status: PARKED**

Consider changing `reactivateSupplier()` from `boolean` to `void`,
because successful completion does not require a return value and
failure is represented by `SupplierNotFoundException`. The existing
return type is retained for now for consistency with
`deactivateSupplier()`.

### 10.3 Reusable IntelliJ Test Run Configuration

**Status: DONE**

A reusable IntelliJ JUnit run-configuration template was created with
the environment configuration required to run tests.

### 10.4 Application Logging

**Status: DONE**

Application logging uses SLF4J with Logback, the logging implementation
provided by the Spring Boot setup. Logging statements and levels are
used where appropriate to record relevant application activity and
problems.

### 10.5 Standardised REST API Error Responses

**Status: DONE**

REST API error responses were standardised using Spring's
`ProblemDetail` support to return structured JSON error information.

### 10.6 Entity Responses, Lazy Loading and Open EntityManager in View

**Status: PARKED --- DEFERRED IMPROVEMENT**

The API currently returns entity-based responses in places. Serialising
entity relationships can expose unwanted Hibernate properties or trigger
lazy-loading concerns.

As an interim measure, `@JsonIgnore` is used on relevant relationships
to avoid serialising nested relationship data. The default Open
EntityManager in View behaviour remains enabled because disabling it in
the current implementation can lead to `LazyInitializationException`
errors.

A more complete future solution is to introduce explicit response DTOs
for Supplier, Invoice and Payment, map entities to those DTOs within
service-layer transactions, update the controllers and tests, disable
Open EntityManager in View, and retest the API. This was deferred to
keep the current implementation within scope.

## 11. Planned Future Enhancements and Parking Lot

The items below are outside the current implementation scope. Recording
an item here does not constitute a commitment to implement it.

### 11.1 Planned Enhancements

-   Supplier search and filtering.
-   Invoice search and filtering.
-   An invoice overview that includes associated payments, outstanding
    amounts and derived payment status.
-   Authentication and authorisation if the application becomes
    multi-user, potentially using Spring Security and an appropriate
    token-based approach.
-   Docker support.
-   Cloud deployment.
-   Database schema migrations using a tool such as Flyway.

### 11.2 Aspirational Features

-   Review URI construction and the use of constants where helpful.
-   Proactive overdue-invoice reporting, including identifying and
    highlighting overdue invoices for the Accounts User, scheduled
    reports and possible email notifications.
-   An Accounts dashboard showing:
    -   The number and list of outstanding invoices.
    -   The number and list of overdue invoices.
    -   Total outstanding and overdue amounts.
    -   Invoices approaching their due dates.
    -   Active supplier information where useful.
-   Define and implement appropriate overpayment handling.
-   A React frontend.

## 12. Implementation Principles

The implementation follows these principles:

1.  Business requirements take precedence over technical possibilities.
2.  Keep the application useful to the Accounts User.
3.  Choose the simplest design that satisfies the requirements.
4.  Do not invent business rules that have not been agreed.
5.  Avoid unnecessary duplication of validation across layers.
6.  Keep related information retrieval separate when the complete object
    graph is not required.
7.  Record interesting ideas in the backlog rather than allowing them to
    derail the current implementation.
