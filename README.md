# Performance Review System

## Table of Contents
- [System Design](#system-design)
- [Assumptions](#assumptions)
- [Getting Started](#getting-started)

---

## System Design

### Q1 — Scaling for 500 Concurrent Managers

Since Spring Boot is stateless, multiple identical instances run behind a
load balancer (AWS ALB/nginx). Five instances handling 500 users means 100
requests per instance, which is manageable.

HikariCP connection pooling (max 20 connections per instance) queues requests
during spikes rather than failing immediately. PostgreSQL read replicas handle
all GET traffic automatically via `@Transactional(readOnly=true)`, since report
endpoints dominate peak load.

For extremely heavy queries, async processing via `@Async` or message queues
(RabbitMQ/SQS) lets managers receive results via email instead of waiting on
slow HTTP responses.

---

### Q2 — Fixing Slow Summary Endpoint at 100k+ Reviews

The existing query already performs DB-level aggregation, not row-by-row Java
processing. A composite index enables PostgreSQL index-only scans:

```sql
CREATE INDEX idx_pr_cycle_rating ON performance_reviews(cycle_id, rating);
```

This reduces query time from ~500ms to ~5ms.

For extreme scale, a materialized `cycle_summary` table updated hourly
via `@Scheduled` reduces the endpoint to an O(1) lookup regardless of
review volume:

```sql
CREATE TABLE cycle_summary (
    cycle_id        BIGINT PRIMARY KEY,
    average_rating  DECIMAL(3,2),
    top_employee_id BIGINT,
    completed_goals BIGINT,
    missed_goals    BIGINT,
    computed_at     TIMESTAMP
);
```

Combined with caching, the endpoint becomes a pure memory lookup.

---

### Q3 — Caching Strategy

Spring Cache abstraction with `@Cacheable` and `@CacheEvict` is used.

| Endpoint | Cache Key | TTL | Invalidation |
|----------|-----------|-----|--------------|
| GET /cycles/{id}/summary | `cycle:summary:{cycleId}` | 1 hour | On review submit |
| GET /employees/{id}/reviews | `employee:reviews:{employeeId}` | 15 min | On review submit |
| GET /employees?department=X&minRating=Y | `employees:dept:{dept}:rating:{rating}` | 5 min | On data change |

> POST endpoints are never cached.

**Local profile** — uses ConcurrentHashMap, zero Redis setup needed:
```properties
spring.cache.type=simple
```

**Production profile** — uses Redis:
```properties
spring.cache.type=redis
spring.data.redis.host=${REDIS_HOST}
spring.data.redis.port=6379
```

Service code remains identical across both profiles.

---

## Assumptions

1. Employees can receive multiple reviews per cycle (manager, peer, skip-level).
   No UNIQUE constraint on `(employee_id, cycle_id)`.

2. PostgreSQL only — no H2. Enables production-realistic features like
   read replicas, composite indexes, and connection pooling.

3. Flyway owns all schema changes. Hibernate uses `ddl-auto=validate`
   and never modifies the schema.

4. Credentials injected via environment variables. Local profile uses
   safe Docker Compose defaults.

5. `reviewerNotes` is optional on review submission.

6. Controllers depend on service interfaces only, never implementations,
   keeping unit testing clean.

---

## Getting Started

### Prerequisites
- Docker and Docker Compose
- Java 17+
- Maven

### Run Locally
```bash
# Start PostgreSQL
docker-compose up -d

# Run the application
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

### Run Tests
```bash
./mvnw test
```