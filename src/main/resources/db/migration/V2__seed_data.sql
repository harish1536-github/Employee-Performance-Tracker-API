-- ============================================================
-- V2: Seed Data for local development
-- ============================================================

INSERT INTO review_cycles (name, start_date, end_date)
VALUES ('Q1 2025', '2025-01-01', '2025-03-31'),
       ('Q2 2025', '2025-04-01', '2025-06-30'),
       ('Q3 2025', '2025-07-01', '2025-09-30');

INSERT INTO employees (name, department, role, joining_date)
VALUES ('Alice Johnson',  'Engineering', 'Senior Developer',   '2022-03-15'),
       ('Bob Smith',      'Engineering', 'Junior Developer',   '2023-06-01'),
       ('Carol Davis',    'Engineering', 'Tech Lead',          '2021-01-10'),
       ('David Wilson',   'Marketing',   'Marketing Manager',  '2022-08-20'),
       ('Emma Brown',     'Marketing',   'Content Specialist', '2023-02-14'),
       ('Frank Miller',   'HR',          'HR Manager',         '2020-05-30'),
       ('Grace Lee',      'Engineering', 'DevOps Engineer',    '2023-09-01');

INSERT INTO performance_reviews
(employee_id, cycle_id, rating, reviewer_notes, reviewer_name)
VALUES (1, 1, 5, 'Exceptional. Led microservices migration.',  'Manager Mike'),
       (2, 1, 3, 'Good progress. Needs stronger code review.', 'Manager Mike'),
       (3, 1, 5, 'Outstanding leadership. Mentored 3 devs.',   'Director Sara'),
       (4, 1, 4, 'Great Q1 campaign. Hit 95% of targets.',     'Director James'),
       (5, 1, 3, 'Solid content output. Room to grow.',        'Manager David'),
       (6, 1, 4, 'Smooth hiring. Filled 8 positions on time.', 'Director Sara'),
       (7, 1, 4, 'Improved pipeline. Reduced downtime 40%.',   'Manager Mike'),
       (1, 2, 5, 'Continued excellence. Ahead of schedule.',   'Manager Mike'),
       (2, 2, 4, 'Big improvement. Taking ownership.',         'Manager Mike');

INSERT INTO goals (employee_id, cycle_id, title, status)
VALUES (1, 1, 'Complete microservices migration',     'COMPLETED'),
       (1, 1, 'Mentor 2 junior developers',           'COMPLETED'),
       (1, 1, 'Get AWS certification',                'MISSED'),
       (2, 1, 'Complete 5 code reviews per week',     'COMPLETED'),
       (2, 1, 'Deliver feature X independently',      'PENDING'),
       (3, 1, 'Create team onboarding documentation', 'COMPLETED'),
       (3, 1, 'Lead architecture review sessions',    'COMPLETED'),
       (4, 1, 'Achieve Q1 revenue target',            'COMPLETED'),
       (5, 1, 'Publish 20 blog posts',                'MISSED'),
       (6, 1, 'Fill all open positions by March',     'COMPLETED'),
       (7, 1, 'Reduce deployment time by 30%',        'COMPLETED');