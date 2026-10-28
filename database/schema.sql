-- ===================================================================
-- Customer Relationship Manager (CRM) Database Initialization Script
-- Target Database: MySQL 8.x
-- ===================================================================

-- 1. Create the database if it doesn't already exist
CREATE DATABASE IF NOT EXISTS crm_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE crm_db;

-- 2. Optional: Create a dedicated application user
-- Replace 'your_password' with a secure credential in non-local environments
-- CREATE USER IF NOT EXISTS 'crm_user'@'localhost' IDENTIFIED BY 'your_password';
-- GRANT ALL PRIVILEGES ON crm_db.* TO 'crm_user'@'localhost';
-- FLUSH PRIVILEGES;

-- 3. Create the 'customers' table
CREATE TABLE IF NOT EXISTS customers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    CONSTRAINT uq_customers_email UNIQUE (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Insert optional sample records for demonstration / testing
INSERT INTO customers (first_name, last_name, email) VALUES
('John', 'Doe', 'john.doe@example.com'),
('Jane', 'Smith', 'jane.smith@example.com'),
('Robert', 'Johnson', 'robert.j@example.com'),
('Emily', 'Davis', 'emily.davis@example.com')
ON DUPLICATE KEY UPDATE id=id;
