
USE erp_auth;

SET @PASS_HASH = '$2a$10$nk7zUmVywmGb0JNJLBJxGe3fH9AqhobqXYaPws2McAp0hJPKZBe5q';

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE login_attempts;
TRUNCATE TABLE password_history;
TRUNCATE TABLE users_auth;
SET FOREIGN_KEY_CHECKS = 1;

-- 1. Insert Users
INSERT INTO users_auth (user_id, username, role, password_hash, status) VALUES
                                                                            (1, 'admin1', 'ADMIN', @PASS_HASH, 'ACTIVE'),
                                                                            (2, 'inst1', 'INSTRUCTOR', @PASS_HASH, 'ACTIVE'),
                                                                            (3, 'stu1', 'STUDENT', @PASS_HASH, 'ACTIVE'),
                                                                            (4, 'stu2', 'STUDENT', @PASS_HASH, 'ACTIVE')
    AS new_data ON DUPLICATE KEY UPDATE password_hash = @PASS_HASH;


USE erp_main;

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE grades;
TRUNCATE TABLE enrollments;
TRUNCATE TABLE sections;
TRUNCATE TABLE courses;
TRUNCATE TABLE instructors;
TRUNCATE TABLE students;
TRUNCATE TABLE settings;
TRUNCATE TABLE drop_deadlines;
TRUNCATE TABLE timetables;
SET FOREIGN_KEY_CHECKS = 1;


INSERT INTO students (student_id, roll_no, program, year) VALUES
                                                              (3, '2024246', 'B.Tech CSB', 2),
                                                              (4, '2024207', 'B.Tech CSE', 2) AS new_data ON DUPLICATE KEY UPDATE program = new_data.program;

INSERT INTO instructors (instructor_id, department, title) VALUES
    (2, 'Computer Science', 'Assistant Professor') AS new_data ON DUPLICATE KEY UPDATE title = new_data.title;

INSERT INTO courses (course_id, code, title, credits) VALUES
(101, 'CSE101', 'Introduction to Programming', 4),
(102, 'ECE111', 'Digital Circuits', 4),
(103, 'MTH100', 'Maths I (Linear Algebra)', 4),
(104, 'DES101', 'Introduction to HCI', 4),
(105, 'COM101', 'Communication Skills', 2),
(201, 'CSE201', 'Data Structures and Algorithms', 4),
(202, 'ECE201', 'Basic Electronics', 4),
(203, 'MTH201', 'Maths II (Probability and Statistics)', 4),
(204, 'CSE111', 'Computer Organization', 4),
(205, 'BIO101', 'Foundations of Biology I', 4),
(301, 'CSE231', 'Operating Systems', 4),
(302, 'CSE202', 'Advanced Programming', 4),
(303, 'MTH210', 'Discrete Mathematics/Structure', 4),
(304, 'ECE303', 'Circuit theory and Devices', 4),
(305, 'BIO301', 'Introduction to Quantitative Biology', 4),
(306, 'ECE301', 'Signals and Systems', 4),
(401, 'CSE222', 'Algorithm Design and Analysis', 4),
(402, 'CSE203', 'Fundamentals of Database Management Systems', 4),
(403, 'MTH310', 'Graph Theory', 4),
(404, 'BIO401', 'Biophysics', 4),
(405, 'BIO403', 'Foundations of Biomedical Informatics', 4),
(406, 'ECE401', 'Fields & Waves', 4),
(407, 'ECE403', 'Integrated Electronics', 4),
(408, 'ECE405', 'Principles of Communication Systems', 4),
(501, 'CSE232', 'Computer Networks', 4),
(502, 'ECE501', 'Digital Communication Systems (Core Elect)', 4),
(503, 'COM501', 'Technical Communication + Env Studies', 2),
(901, 'MTH211', 'Number Theory (Special Elective)', 4),
(902, 'MTH240', 'Real Analysis II (Special Elective)', 4),
(903, 'PHY101', 'Physics (Special Elective)', 4),
(904, 'ECE301', 'Signals and Systems (Special Elective)', 4)
    AS new_data ON DUPLICATE KEY UPDATE title = new_data.title;


-- 4. Insert Sections (Making them available)
INSERT INTO sections (section_id, course_id, instructor_id, day_of_week, start_time, end_time, room, capacity, semester, year) VALUES
(1, 101, 2, 1, '09:00:00', '10:30:00', 'C01', 60, 1, 2024),
(2, 102, 2, 2, '11:00:00', '12:30:00', 'C02', 60, 1, 2024),
(3, 103, 2, 3, '14:00:00', '15:30:00', 'C03', 60, 1, 2024),
(4, 104, 2, 4, '09:00:00', '10:30:00', 'C04', 60, 1, 2024),
(5, 105, 2, 5, '11:00:00', '12:30:00', 'C05', 60, 1, 2024),
(6, 201, 2, 1, '14:00:00', '15:30:00', 'L11', 60, 1, 2024),
(7, 203, 2, 3, '10:00:00', '11:30:00', 'L12', 60, 1, 2024),
(8, 301, 2, 5, '09:00:00', '10:30:00', 'L21', 60, 1, 2024),
(9, 302, 2, 2, '14:00:00', '15:30:00', 'L22', 60, 1, 2024),
(10, 901, 2, 3, '16:00:00', '17:30:00', 'E01', 40, 1, 2024),
(11, 401, 2, 4, '11:00:00', '12:30:00', 'E02', 40, 1, 2024)
    AS new_data ON DUPLICATE KEY UPDATE room = new_data.room;

INSERT INTO enrollments (student_id, section_id, status) VALUES
(3, 1, 'REGISTERED'),
(3, 3, 'REGISTERED'),
(3, 5, 'REGISTERED'),
(4, 7, 'REGISTERED'),
(4, 9, 'REGISTERED')
    AS new_data ON DUPLICATE KEY UPDATE status = new_data.status;


INSERT INTO settings (setting_key, setting_value) VALUES ('maintenance_on', 'false')
    AS new_data ON DUPLICATE KEY UPDATE setting_value = 'false';

INSERT INTO drop_deadlines (semester, year, deadline_date) VALUES (1, 2024, '2027-06-01')
    AS new_data ON DUPLICATE KEY UPDATE deadline_date = new_data.deadline_date;