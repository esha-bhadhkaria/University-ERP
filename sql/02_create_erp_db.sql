CREATE DATABASE IF NOT EXISTS erp_main;
USE erp_main;

CREATE TABLE IF NOT EXISTS students (
                                        student_id INT PRIMARY KEY,
                                        roll_no VARCHAR(20) UNIQUE NOT NULL,
                                        program VARCHAR(100) NOT NULL,
                                        year INT NOT NULL,
                                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
CREATE TABLE IF NOT EXISTS instructors (
                                           instructor_id INT PRIMARY KEY,
                                           department VARCHAR(100) NOT NULL,
                                           title VARCHAR(50),
                                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                           updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
CREATE TABLE IF NOT EXISTS courses (
                                       course_id INT AUTO_INCREMENT PRIMARY KEY,
                                       code VARCHAR(10) NOT NULL UNIQUE,
                                       title VARCHAR(200) NOT NULL,
                                       credits INT NOT NULL,
                                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                       INDEX idx_code (code)
);

CREATE TABLE IF NOT EXISTS sections (
                                        section_id INT AUTO_INCREMENT PRIMARY KEY,
                                        course_id INT NOT NULL,
                                        instructor_id INT,
                                        day_of_week INT NOT NULL,
                                        start_time TIME NOT NULL,
                                        end_time TIME NOT NULL,
                                        room VARCHAR(50),
                                        capacity INT NOT NULL CHECK (capacity >= 0),
                                        semester INT NOT NULL,
                                        year INT NOT NULL,
                                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                                        FOREIGN KEY (course_id) REFERENCES courses(course_id),
                                        FOREIGN KEY (instructor_id) REFERENCES instructors(instructor_id)
);

CREATE TABLE IF NOT EXISTS enrollments (
                                           enrollment_id INT AUTO_INCREMENT PRIMARY KEY,
                                           student_id INT NOT NULL,
                                           section_id INT NOT NULL,
                                           status ENUM('REGISTERED', 'DROPPED', 'COMPLETED') NOT NULL DEFAULT 'REGISTERED',
                                           enrolled_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                           dropped_date DATETIME NULL,
                                           final_grade VARCHAR(5) NULL,
                                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                           updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                           FOREIGN KEY (student_id) REFERENCES students(student_id),
                                           FOREIGN KEY (section_id) REFERENCES sections(section_id),
                                           UNIQUE KEY unique_enrollment (student_id, section_id)
);

CREATE TABLE IF NOT EXISTS grades (
                                      grade_id INT AUTO_INCREMENT PRIMARY KEY,
                                      enrollment_id INT NOT NULL,
                                      component VARCHAR(50) NOT NULL,
                                      score DECIMAL(5, 2) NOT NULL,
                                      max_score DECIMAL(5, 2) NOT NULL,
                                      weight DECIMAL(5, 2) NOT NULL,
                                      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                      updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                      FOREIGN KEY (enrollment_id) REFERENCES enrollments(enrollment_id),
                                      UNIQUE KEY unique_component (enrollment_id, component)
);

CREATE TABLE IF NOT EXISTS settings (
                                        setting_key VARCHAR(100) PRIMARY KEY,
                                        setting_value VARCHAR(500) NOT NULL,
                                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS drop_deadlines (
                                              deadline_id INT AUTO_INCREMENT PRIMARY KEY,
                                              semester INT NOT NULL,
                                              year INT NOT NULL,
                                              deadline_date DATE NOT NULL,
                                              UNIQUE KEY unique_deadline (semester, year)
);

CREATE TABLE IF NOT EXISTS timetables (
                                          id INT AUTO_INCREMENT PRIMARY KEY,
                                          filename VARCHAR(255) NOT NULL,
                                          file_data LONGBLOB NOT NULL,
                                          upload_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
USE erp_main;

CREATE TABLE IF NOT EXISTS erp_courses_backup (
                                                  course_id INT PRIMARY KEY,
                                                  code VARCHAR(10) NOT NULL,
                                                  title VARCHAR(200) NOT NULL,
                                                  credits INT NOT NULL,
                                                  snapshot_time TIMESTAMP
);

CREATE TABLE IF NOT EXISTS erp_sections_backup (
                                                   section_id INT PRIMARY KEY,
                                                   course_id INT NOT NULL,
                                                   instructor_id INT,
                                                   day_of_week INT NOT NULL,
                                                   start_time TIME NOT NULL,
                                                   end_time TIME NOT NULL,
                                                   room VARCHAR(50),
                                                   capacity INT NOT NULL,
                                                   semester INT NOT NULL,
                                                   year INT NOT NULL,
                                                   snapshot_time TIMESTAMP
);

CREATE TABLE IF NOT EXISTS notifications (
                                             id INT PRIMARY KEY,
                                             message TEXT NOT NULL,
                                             posted_by_user_id INT NOT NULL,
                                             posted_by_role ENUM('ADMIN', 'INSTRUCTOR') NOT NULL,
                                             posted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

INSERT INTO notifications (id, message, posted_by_user_id, posted_by_role)
VALUES (1, 'Welcome to the University ERP System!', 1, 'ADMIN')
ON DUPLICATE KEY UPDATE message = message;