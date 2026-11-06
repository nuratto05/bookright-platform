# BookRights – Book Marketplace 

Status: in-progress.

A Spring Boot-based marketplace API for managing book listings and purchase transactions. Users can register, authenticate, publish books for sale, place purchase orders and manage transactions through a seller approval workflow.

The application uses PostgreSQL for persistent storage and Redis for caching frequently accessed marketplace data. It also implements JWT authentication, transactional processing and pessimistic database locking to help maintain consistency during concurrent operations.

---

## Tech Stack

- Java 17
- Spring Boot
- Spring Web
- Spring Security
- Spring Data JPA / Hibernate
- PostgreSQL
- Redis
- Maven
- JWT

---

## Features

- **Authentication & Authorization:** JWT-based authentication with Spring Security and cookie-based token storage.
- **Book Management:** Create, update, delete, and browse books listed on the marketplace.
- **Order Management:** Buyers can place purchase requests, while sellers can accept or reject incoming orders.
- **Wallet System:** Automatically creates a wallet with an initial balance of 1,000 credits for each registered user.
- **Redis Caching:** Caches paginated marketplace results and individual book lookups to reduce repeated database queries.
- **Cache Invalidation:** Invalidates individual book cache entries when listings are updated or deleted.
- **Duplicate Detection:** Checks for existing books and orders before creating new database records.
- **Concurrency Control:** Uses pessimistic database locking to coordinate simultaneous book and order operations.
- **Transactional Processing:** Uses Spring's `@Transactional` to maintain database consistency during multi-step operations.

---

## Environment variables

Configure the following environment-specific values before running the application:

- `DB_URL` — PostgreSQL JDBC connection URL
- `DB_USERNAME` — PostgreSQL username
- `DB_PASSWORD` — PostgreSQL password
- `REDIS_HOST` — Redis host
- `REDIS_PORT` — Redis port
- `JWT_SECRET` — Secret key used for JWT signing and validation
- `SERVER_PORT` — Application server port

Example `application.properties` configuration:

```properties
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

spring.jpa.hibernate.ddl-auto=update

spring.data.redis.host=${REDIS_HOST}
spring.data.redis.port=${REDIS_PORT}

jwt.secret=${JWT_SECRET}

server.port=${SERVER_PORT}
```

Keep database credentials and JWT secrets outside source control.

---

## Core Modules

### Authentication & Authorization

- User registration with hashed passwords.
- JWT generation and validation.
- Cookie-based token storage.
- Custom `CustomUserDetails` implementation.
- Stateless authentication using Spring Security.
- Automatic wallet creation during registration.

### Book Marketplace

- Create, update, and delete book listings.
- Retrieve individual books and paginated marketplace results.
- Associate listings with their respective sellers.
- Enforce unique ISBN identifiers.
- Check book availability before processing purchase requests.

### Order Management

The order workflow supports a buyer-to-seller purchase process:

1. A buyer selects an available book and submits a purchase request.
2. The application validates book availability and the buyer's wallet balance.
3. An order is created and associated with the buyer and seller.
4. The seller can accept or reject the purchase request.
5. Buyers and sellers can retrieve their respective transaction histories.

### Concurrency & Transaction Management

The application uses database-level concurrency control to help prevent conflicting operations during simultaneous requests.

- **Pessimistic Locking:** Acquires database locks when retrieving records that require coordinated updates.
- **Transactional Boundaries:** Uses `@Transactional` to group related database operations.
- **Duplicate Detection:** Checks for existing records before creating books and orders.
- **Data Consistency:** Coordinates book availability, order processing, and wallet validation within the transaction workflow.

### Redis Caching

Redis is used to reduce repeated database queries for frequently accessed marketplace data.

Two caching strategies are implemented:

- **Paginated Book Cache:** Stores marketplace listing results.
- **Individual Book Cache:** Stores frequently accessed book records.

Cache invalidation is performed when individual book listings are updated or deleted, helping prevent stale data from being returned.

---

## API Endpoints

### Authentication

| Method | Endpoint | Description |
|---|---|---|
| POST | `/register` | Register a new user |
| GET | `/login` | Authenticate and obtain a JWT |

### Book Management

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/createBook` | Create a book listing |
| PUT | `/api/updateBook/{id}` | Update a book listing |
| DELETE | `/api/deleteBook/{id}` | Delete a book listing |
| GET | `/api/books` | Retrieve paginated book listings |
| GET | `/api/book/{id}` | Retrieve an individual book |

### Order Management

| Method | Endpoint | Description |
|---|---|---|
| POST | `/transaction/order/placeOrder` | Place a purchase order |
| PUT | `/transaction/order/{orderId}/accept` | Accept an order |
| PUT | `/transaction/order/{orderId}/reject` | Reject an order |
| GET | `/transaction/order/getMyPurchases` | Retrieve purchase history |
| GET | `/transaction/order/getMySales` | Retrieve sales history |

---

## Security

The application uses Spring Security with stateless JWT authentication.

- Public routes: `/register`, `/login`
- Protected routes: `/api/**`, `/transaction/**`
- JWT validation is performed through a custom security filter.
- Authenticated users are represented through `CustomUserDetails`.

---
