-- ==========================================
-- 1. AUTHOR TABLE
-- ==========================================
INSERT INTO author (name, email, birth_date, country)
VALUES
    ('Alice Nguyen', 'alice@example.com', '1988-03-12', 'Vietnam'),
    ('Bob Tran', 'bob.tran@example.com', '1975-11-02', 'USA'),
    ('Charlie Le', 'charlie.le@example.com', '1990-07-25', 'France'),
    ('David Pham', 'david.pham@example.com', '1985-04-10', 'UK'),
    ('Emily Vo', 'emily.vo@example.com', '1992-12-01', 'Singapore'),
    ('Frank Huynh', 'frank.huynh@example.com', '1980-09-14', 'Germany');

-- ==========================================
-- 2. BOOK TABLE
-- ==========================================
INSERT INTO book (title, isbn, published_year, price)
VALUES
    ('Modern Java in Practice', '978-1-1111-1111-1', 2022, 45.00),
    ('Spring Boot Deep Dive', '978-1-2222-2222-2', 2023, 55.00),
    ('Reactive Programming with Project Reactor', '978-1-3333-3333-3', 2024, 60.00),
    ('Microservices with jOOQ', '978-1-4444-4444-4', 2023, 48.50),
    ('Clean Architecture in Java', '978-1-5555-5555-5', 2021, 39.99),
    ('Advanced SQL for Developers', '978-1-6666-6666-6', 2024, 70.00),
    ('Database Performance Tuning', '978-1-7777-7777-7', 2020, 35.50),
    ('Domain-Driven Design Patterns', '978-1-8888-8888-8', 2022, 59.00),
    ('Effective Testing Strategies', '978-1-9999-9999-9', 2024, 44.00),
    ('Cloud Native Java', '978-1-0000-0000-0', 2023, 52.50);

-- ==========================================
-- 3. BOOK_AUTHOR TABLE
-- ==========================================
-- Mapping between books and authors with contribution description
INSERT INTO book_author (book_id, author_id, contribution)
VALUES
-- Modern Java in Practice
(1, 1, 'Main Author'),
(1, 2, 'Co-Author'),

-- Spring Boot Deep Dive
(2, 2, 'Main Author'),
(2, 3, 'Reviewer'),

-- Reactive Programming with Project Reactor
(3, 1, 'Main Author'),
(3, 4, 'Technical Reviewer'),
(3, 5, 'Editor'),

-- Microservices with jOOQ
(4, 1, 'Main Author'),
(4, 6, 'Contributor'),

-- Clean Architecture in Java
(5, 3, 'Main Author'),
(5, 4, 'Reviewer'),

-- Advanced SQL for Developers
(6, 2, 'Main Author'),
(6, 5, 'Editor'),

-- Database Performance Tuning
(7, 6, 'Main Author'),

-- Domain-Driven Design Patterns
(8, 1, 'Co-Author'),
(8, 3, 'Main Author'),

-- Effective Testing Strategies
(9, 4, 'Main Author'),
(9, 5, 'Contributor'),

-- Cloud Native Java
(10, 1, 'Reviewer'),
(10, 2, 'Main Author'),
(10, 6, 'Contributor');
