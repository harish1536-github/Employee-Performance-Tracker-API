# Employee Performance Tracker API

Spring Boot REST API for tracking employee performance reviews,
goals and generating cycle based reports.

---

## Table of Contents
- [Tech Stack](#tech-stack)
- [System Design](#system-design)
- [API Endpoints](#api-endpoints)
- [Assumptions](#assumptions)
- [Getting Started](#getting-started)

---

## Tech Stack

| Layer      | Technology              |
|------------|-------------------------|
| Language   | Java 17                 |
| Framework  | Spring Boot 3.2.5       |
| Database   | PostgreSQL 15 (Docker)  |
| Cache      | Redis 7 (Docker)        |
| Migrations | Flyway                  |
| ORM        | Spring Data JPA         |
| Container  | Docker + Docker Compose |

---

## System Design

### Q1 — Scaling for 500 Concurrent Managers

Since Spring Boot is stateless, multiple instances run behind a load balancer
(AWS ALB/nginx). Five instances handling 500 users means 100 requests per
instance. HikariCP connection pooling (max 20 per instance) queues requests
during spikes. PostgreSQL read replicas handle all GET traffic via
`@Transactional(readOnly=true)`. For heavy queries, `@Async` or message queues
(RabbitMQ/SQS) lets managers receive results via email.

### Q2 — Fixing Slow Summary Endpoint at 100k+ Reviews

A composite index enables PostgreSQL index-only scans:

```sql
CREATE INDEX idx_pr_cycle_rating ON performance_reviews(cycle_id, rating);
```

This reduces query time from ~500ms to ~5ms. A materialized `cycle_summary`
table updated via `@Scheduled` reduces the endpoint to an O(1) lookup.
Combined with Redis caching, the endpoint becomes a pure memory lookup.

### Q3 — Caching Strategy

Spring Cache with `@Cacheable` and `@CacheEvict` is used across endpoints.

| Endpoint | Cache Key | TTL |
|----------|-----------|-----|
| GET /cycles/{id}/summary | `cycle:summary:{cycleId}` | 1 hour |
| GET /employees/{id}/reviews | `employee:reviews:{employeeId}` | 15 min |
| GET /employees?department=X&minRating=Y | `employees:dept:{dept}:rating:{rating}` | 5 min |

**Local** — `spring.cache.type=simple` (ConcurrentHashMap, no Redis needed)

**Production** — `spring.cache.type=redis` with Redis running via Docker

---

## API Endpoints

| Method | Endpoint                            | Description          |
|--------|-------------------------------------|----------------------|
| POST   | /employees                          | Create employee      |
| GET    | /employees/{id}                     | Get employee by ID   |
| GET    | /employees?department=X&minRating=Y | Filter employees     |
| POST   | /reviews                            | Submit review        |
| GET    | /employees/{id}/reviews             | Get employee reviews |
| POST   | /cycles                             | Create cycle         |
| GET    | /cycles/{id}/summary                | Get cycle summary    |

---

## Assumptions

1. Multiple reviews per employee per cycle are allowed
2. Cycle names must be unique
3. Joining date cannot be in the future
4. Deleting an employee removes all their reviews and goals
5. Top performer is employee with highest average rating in cycle
6. PostgreSQL only — enables read replicas, composite indexes and connection pooling
7. Flyway owns all schema changes — Hibernate uses `ddl-auto=validate`
8. `reviewerNotes` is optional on review submission
9. Controllers depend on service interfaces only, never implementations

---

## Getting Started

### Prerequisites
- Java 17+, Maven, Docker Desktop

### Run Locally
```bash
# Start PostgreSQL and Redis via Docker
docker-compose up -d

# Run the application
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```