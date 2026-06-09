CREATE DATABASE IF NOT EXISTS erp_auth;
USE erp_auth;

CREATE TABLE IF NOT EXISTS users_auth (
                                          user_id INT AUTO_INCREMENT PRIMARY KEY,
                                          username VARCHAR(50) NOT NULL UNIQUE, -- Login identifier
                                          role ENUM('ADMIN', 'INSTRUCTOR', 'STUDENT') NOT NULL, -- User role
                                          password_hash VARCHAR(255) NOT NULL, -- Secure password hash
                                          status ENUM('ACTIVE', 'INACTIVE', 'LOCKED') NOT NULL DEFAULT 'ACTIVE',
                                          last_login DATETIME,
                                          failed_login_attempts INT DEFAULT 0,
                                          lockout_until DATETIME NULL,
                                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                          updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                                          INDEX idx_username (username)
);
CREATE TABLE IF NOT EXISTS password_history (
                                                id INT AUTO_INCREMENT PRIMARY KEY,
                                                user_id INT NOT NULL,
                                                password_hash VARCHAR(255) NOT NULL,
                                                changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                                FOREIGN KEY (user_id) REFERENCES users_auth(user_id) ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS login_attempts (
                                              id INT AUTO_INCREMENT PRIMARY KEY,
                                              user_id INT,
                                              username VARCHAR(50),
                                              attempt_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                              success BOOLEAN DEFAULT FALSE
);