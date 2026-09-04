-- ============================================================
-- Student Attendance & Result Management System
-- Database schema (MySQL 8.x)
-- ============================================================

DROP DATABASE IF EXISTS sarms_db;
CREATE DATABASE sarms_db;
USE sarms_db;

-- ------------------------------------------------------------
-- USERS: login accounts for teachers and students (role-based)
-- ------------------------------------------------------------
CREATE TABLE users (
    user_id      INT AUTO_INCREMENT PRIMARY KEY,
    username     VARCHAR(50)  NOT NULL UNIQUE,
    password     VARCHAR(255) NOT NULL,          -- SHA-256 hash, see PasswordUtil
    role         ENUM('TEACHER', 'STUDENT') NOT NULL,
    linked_student_id INT NULL,                  -- filled in only for STUDENT accounts
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ------------------------------------------------------------
-- STUDENTS: student master data
-- ------------------------------------------------------------
CREATE TABLE students (
    student_id   INT AUTO_INCREMENT PRIMARY KEY,
    roll_no      VARCHAR(20)  NOT NULL UNIQUE,
    full_name    VARCHAR(100) NOT NULL,
    email        VARCHAR(100),
    phone        VARCHAR(20),
    year_level   INT NOT NULL DEFAULT 1,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE users
    ADD CONSTRAINT fk_users_student
    FOREIGN KEY (linked_student_id) REFERENCES students(student_id)
    ON DELETE CASCADE;

-- ------------------------------------------------------------
-- COURSES: subjects/courses offered
-- ------------------------------------------------------------
CREATE TABLE courses (
    course_id    INT AUTO_INCREMENT PRIMARY KEY,
    course_code  VARCHAR(20)  NOT NULL UNIQUE,
    course_name  VARCHAR(100) NOT NULL,
    credits      INT NOT NULL DEFAULT 3
);

-- ------------------------------------------------------------
-- ENROLLMENTS: which student is enrolled in which course
-- (many-to-many bridge table between students and courses)
-- ------------------------------------------------------------
CREATE TABLE enrollments (
    enrollment_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id    INT NOT NULL,
    course_id     INT NOT NULL,
    UNIQUE KEY uq_enrollment (student_id, course_id),
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    FOREIGN KEY (course_id)  REFERENCES courses(course_id)  ON DELETE CASCADE
);

-- ------------------------------------------------------------
-- ATTENDANCE: one row per student, per course, per date
-- ------------------------------------------------------------
CREATE TABLE attendance (
    attendance_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id    INT NOT NULL,
    course_id     INT NOT NULL,
    class_date    DATE NOT NULL,
    status        ENUM('PRESENT', 'ABSENT') NOT NULL,
    UNIQUE KEY uq_attendance (student_id, course_id, class_date),
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    FOREIGN KEY (course_id)  REFERENCES courses(course_id)  ON DELETE CASCADE
);

-- ------------------------------------------------------------
-- GRADES: exam results per student, per course
-- ------------------------------------------------------------
CREATE TABLE grades (
    grade_id      INT AUTO_INCREMENT PRIMARY KEY,
    student_id    INT NOT NULL,
    course_id     INT NOT NULL,
    exam_type     ENUM('MIDTERM', 'FINAL', 'QUIZ', 'ASSIGNMENT') NOT NULL,
    marks_obtained DECIMAL(5,2) NOT NULL,
    max_marks      DECIMAL(5,2) NOT NULL DEFAULT 100.00,
    exam_date      DATE NOT NULL,
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    FOREIGN KEY (course_id)  REFERENCES courses(course_id)  ON DELETE CASCADE
);

-- ============================================================
-- SAMPLE DATA
-- ============================================================

INSERT INTO students (roll_no, full_name, email, phone, year_level) VALUES
('CS101', 'Aarav Sharma',  'aarav.sharma@example.com',  '9800000001', 2),
('CS102', 'Priya Verma',   'priya.verma@example.com',   '9800000002', 2),
('CS103', 'Rohan Mehta',   'rohan.mehta@example.com',   '9800000003', 2);

INSERT INTO courses (course_code, course_name, credits) VALUES
('CSE201', 'Data Structures',      4),
('CSE202', 'Database Systems',     4),
('CSE203', 'Operating Systems',    3);

INSERT INTO enrollments (student_id, course_id) VALUES
(1,1),(1,2),(1,3),
(2,1),(2,2),
(3,2),(3,3);

INSERT INTO attendance (student_id, course_id, class_date, status) VALUES
(1,1,'2026-01-05','PRESENT'), (1,1,'2026-01-06','ABSENT'), (1,1,'2026-01-07','PRESENT'),
(1,2,'2026-01-05','PRESENT'), (1,2,'2026-01-06','PRESENT'),
(2,1,'2026-01-05','PRESENT'), (2,1,'2026-01-06','PRESENT'),
(3,2,'2026-01-05','ABSENT'),  (3,2,'2026-01-06','PRESENT');

INSERT INTO grades (student_id, course_id, exam_type, marks_obtained, max_marks, exam_date) VALUES
(1,1,'MIDTERM', 78, 100, '2026-02-10'),
(1,1,'FINAL',   85, 100, '2026-04-15'),
(1,2,'MIDTERM', 66, 100, '2026-02-11'),
(2,1,'MIDTERM', 90, 100, '2026-02-10'),
(3,2,'FINAL',   72, 100, '2026-04-16');

-- ------------------------------------------------------------
-- LOGIN ACCOUNTS
-- Default password for every account below is: "password123"
-- (stored as a SHA-256 hash; the app hashes input the same way)
-- ------------------------------------------------------------
INSERT INTO users (username, password, role, linked_student_id) VALUES
('teacher1', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'TEACHER', NULL),
('aarav',    'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'STUDENT', 1),
('priya',    'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'STUDENT', 2),
('rohan',    'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'STUDENT', 3);
