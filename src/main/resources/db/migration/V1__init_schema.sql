-- ============================================================
-- V1: Initial Schema
-- IMPORTANT: Never edit this file after it has run
-- To change schema → create V3__your_change.sql
-- ============================================================

-- ============================================================
-- employees table
-- ============================================================
CREATE TABLE IF NOT EXISTS employees
(
    id           BIGSERIAL       PRIMARY KEY,
    name         VARCHAR(100)    NOT NULL,
    department   VARCHAR(100)    NOT NULL,
    role         VARCHAR(100)    NOT NULL,
    joining_date DATE            NOT NULL,
    created_at   TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Prevent duplicate employee in same department
    CONSTRAINT uq_employee_name_dept UNIQUE (name, department)
    );

-- Speeds up GET /employees?department=X queries
CREATE INDEX IF NOT EXISTS idx_employees_department
    ON employees (department);

-- ============================================================
-- review_cycles table
-- ============================================================
CREATE TABLE IF NOT EXISTS review_cycles
(
    id         BIGSERIAL       PRIMARY KEY,
    name       VARCHAR(100)    NOT NULL,
    start_date DATE            NOT NULL,
    end_date   DATE            NOT NULL,
    created_at TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Only one "Q1 2025" cycle can exist
    CONSTRAINT uq_cycle_name   UNIQUE (name),

    -- End must come after start
    CONSTRAINT chk_cycle_dates CHECK (end_date > start_date)
    );

-- ============================================================
-- performance_reviews table
-- NOTE: Multiple reviews per employee per cycle ARE allowed
-- This supports mid-cycle reviews and multi-manager reviews
-- ============================================================
CREATE TABLE IF NOT EXISTS performance_reviews
(
    id             BIGSERIAL       PRIMARY KEY,
    employee_id    BIGINT          NOT NULL,
    cycle_id       BIGINT          NOT NULL,
    rating         INTEGER         NOT NULL,
    reviewer_notes TEXT,
    reviewer_name  VARCHAR(100)    NOT NULL,
    submitted_at   TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- DB level enforcement of rating range
    CONSTRAINT chk_rating_range
    CHECK (rating BETWEEN 1 AND 5),

    -- Cascade: delete reviews when employee is deleted
    CONSTRAINT fk_review_employee
    FOREIGN KEY (employee_id)
    REFERENCES employees (id)
    ON DELETE CASCADE,

    CONSTRAINT fk_review_cycle
    FOREIGN KEY (cycle_id)
    REFERENCES review_cycles (id)
    ON DELETE CASCADE
    );

-- Speeds up GET /employees/{id}/reviews
CREATE INDEX IF NOT EXISTS idx_reviews_employee_id
    ON performance_reviews (employee_id);

-- Speeds up GET /cycles/{id}/summary
CREATE INDEX IF NOT EXISTS idx_reviews_cycle_id
    ON performance_reviews (cycle_id);

-- Speeds up queries filtering by both employee AND cycle
CREATE INDEX IF NOT EXISTS idx_reviews_employee_cycle
    ON performance_reviews (employee_id, cycle_id);

-- ============================================================
-- goals table
-- ============================================================
CREATE TABLE IF NOT EXISTS goals
(
    id          BIGSERIAL       PRIMARY KEY,
    employee_id BIGINT          NOT NULL,
    cycle_id    BIGINT          NOT NULL,
    title       VARCHAR(255)    NOT NULL,
    status      VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    created_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Only these 3 values are allowed
    CONSTRAINT chk_goal_status
    CHECK (status IN ('PENDING', 'COMPLETED', 'MISSED')),

    CONSTRAINT fk_goal_employee
    FOREIGN KEY (employee_id)
    REFERENCES employees (id)
    ON DELETE CASCADE,

    CONSTRAINT fk_goal_cycle
    FOREIGN KEY (cycle_id)
    REFERENCES review_cycles (id)
    ON DELETE CASCADE
    );

CREATE INDEX IF NOT EXISTS idx_goals_cycle_id
    ON goals (cycle_id);

CREATE INDEX IF NOT EXISTS idx_goals_employee_id
    ON goals (employee_id);

-- Composite index: speeds up goal count by status per cycle
-- Directly used in GET /cycles/{id}/summary
CREATE INDEX IF NOT EXISTS idx_goals_cycle_status
    ON goals (cycle_id, status);