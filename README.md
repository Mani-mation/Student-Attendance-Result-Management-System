# Student Attendance & Result Management System

A desktop application built in Java that lets teachers manage student records, attendance, and exam results, and lets students view their own attendance percentage and grade summary. Built with Swing for the UI, JDBC for data access, and MySQL for storage.

## Features

- Role-based login (Teacher / Student) with SHA-256 hashed passwords
- Teacher dashboard: add/edit/delete/search students, mark attendance per course and date, record exam grades
- Student dashboard: read-only view of per-course and overall attendance percentage, and a grade summary with average score
- Normalized MySQL schema: `students`, `courses`, `enrollments`, `attendance`, `grades`, `users`

## Tech Stack

- Java (Swing for UI)
- JDBC
- MySQL 8.x

## Project Structure

```
StudentAttendanceResultSystem/
├── sql/
│   └── schema.sql          # database tables + seed data
├── src/
│   ├── Main.java           # entry point
│   ├── db/
│   │   ├── DBConnection.java
│   │   └── PasswordUtil.java
│   ├── model/
│   │   ├── Student.java
│   │   ├── Course.java
│   │   ├── Attendance.java
│   │   ├── Grade.java
│   │   └── User.java
│   ├── dao/
│   │   ├── StudentDAO.java
│   │   ├── CourseDAO.java
│   │   ├── AttendanceDAO.java
│   │   ├── GradeDAO.java
│   │   └── UserDAO.java
│   └── ui/
│       ├── LoginFrame.java
│       ├── TeacherDashboard.java
│       └── StudentDashboard.java
└── lib/
    └── (place mysql-connector-j-x.x.x.jar here)
```

## Prerequisites

- JDK 17 or later
- MySQL Server 8.x, running locally
- MySQL Connector/J (JDBC driver) — download from https://dev.mysql.com/downloads/connector/j/ and place the `.jar` in the `lib/` folder

## Setup

1. **Create the database.**
   Open a MySQL client and run:
   ```
   mysql -u root -p < sql/schema.sql
   ```
   This creates the `sarms_db` database with all tables and some seed data.

2. **Set your database credentials.**
   Open `src/db/DBConnection.java` and update:
   ```java
   private static final String USER = "root";
   private static final String PASSWORD = "your_mysql_password";
   ```

3. **Compile.**
   ```
   javac -cp "lib/*" -d bin $(find src -name "*.java")
   ```

4. **Run.**
   ```
   java -cp "bin:lib/*" Main
   ```
   (On Windows, use `bin;lib/*` instead of `bin:lib/*`.)

## Default Login Accounts

All seeded accounts use the password `password123`.

| Username  | Role    |
|-----------|---------|
| teacher1  | Teacher |
| aarav     | Student |
| priya     | Student |
| rohan     | Student |

## Notes

- The teacher account can see and manage all students, courses, attendance, and grades.
- A student account only sees their own attendance and grades, with no edit access.
- Attendance percentage and grade averages are computed on the fly from the database, not stored as separate fields, so they always reflect the latest data.
