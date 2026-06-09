University ERP System (Java Swing + MySQL)

This project implements a desktop Enterprise Resource Planning (ERP) application designed for a university setting, facilitating course management, student enrollment, and grade recording.
The application is built using Java Swing for the UI, FlatLaf for modern aesthetics, and relies on MySQL for persistent data storage managed via HikariCP connection pooling.

#Quick Setup and Installation
-Prerequisites
JDK 11+ (The application is compiled for Java 11/14, as per the pom.xml and ErpApplication.java).
MySQL 8.0+
Maven (Required for building and running).

#Database Configuration
1.Create both required databases:
    CREATE DATABASE erp_auth
    CREATE DATABASE erp_main
2.Execute schema and seed data scripts:
-Create tables
    mysql -u root -p < sql/01_create_auth_db.sql
    mysql -u root -p < sql/02_create_erp_db.sql
-Insert test users and course data
    mysql -u root -p < sql/03_seed_data.sql
3.Configure connection settings:
    Verify and update the credentials in src/main/resources/db.properties
    to match your local MySQL environment:

    # Auth DB Configuration (Security Data)
    auth.db.host=localhost
    auth.db.port=3306
    auth.db.name=erp_auth
    auth.db.user=root
    auth.db.password=rootpass

    # ERP DB Configuration (Academic Data)
    erp.db.host=localhost
    erp.db.port=3306
    erp.db.name=erp_main
    erp.db.user=root
    erp.db.password=rootpass

#Build & Run the Application

The application is built using Maven.
The final executable JAR will be created in the target directory (assuming standard Maven build configuration).

    # 1. Clean and package the project (creates the executable JAR in target/)
    mvn clean install
    # 2. Run the application (replace target/ with the actual path and filename, e.g., university-erp-1.0.0.jar)
    java -jar target/university-erp-1.0.0.jar

##Default Test Credentials
All users created in 03_seed_data.sql use the password: password123

-admin1  ADMIN       Full system control, maintenance toggle, backups.
-inst1   INSTRUCTOR  Manage assigned sections, grade entry, class stats.
-stu1    STUDENT     course registration, timetable.
-stu2    STUDENT     course registration, timetable

##Key Features Implemented
This project meets all mandatory requirements and incorporates multiple bonus features for enhanced security and usability.

->Core Architecture & Security
1.Dual Database System: Strict separation of security data (erp_auth) and academic data (erp_main).
2.Secure Hashing: All passwords stored exclusively in erp_auth as BCrypt hashes (10 rounds).
3.Role-Based Access Control : Enforced at the Service Layer (AccessRuleChecker.java) before every write operation.
4.Data Ownership: Students/Instructors can only access/modify their own records.
5.Maintenance Mode: Admin can globally toggle the system to view-only status for Students and Instructors.

->Security and Authentication (Bonus Features)
1.Login Lockout: Accounts are temporarily locked for 15 minutes after 5 consecutive failed login attempts.
2.Change Password: A common dialog allows users to securely change their password (checks history for reuse).

##EXTERNAL LIBRARIES AND DEPENDENCIES
    FlatLaf (flatlaf)   com.formdev   - Modern Look and Feel (UI/UX) for Swing applications, replacing the default Java themes.
    FlatLaf Extras      com.formdev   - Provides additional UI components and utilities for enhancing the visual experience.
    MigLayout           com.miglayout - A highly flexible and powerful layout manager used for complex and responsive Swing form design.
    HikariCP            com.zaxxer    - High-performance JDBC Connection Pool used by DbConnectionManager to efficiently manage database connections to both erp_auth and erp_main.
    jBCrypt             org.mindrot   - Security library used for strong, one-way password hashing and verification, ensuring plaintext passwords are never stored.
    MySQL Connector/J   com.mysql     - The JDBC Driver required to connect the Java application to the MySQL database instances.
    SLF4J API & Logback org.slf4j & ch.qos.logback  - Standard logging facade and implementation used for runtime logging, debugging, and error handling.
    OpenCSV             com.opencsv   - Utility library used for CSV file handling (reading during Instructor grade import and writing during transcript/grade export).


##TESTING AND VALIDATION

Sample test data includes:
- 1 admin user
- 1 instructor with assigned sections
- 2 students with existing enrollments
- courses with multiple sections
- Sample grades for enrolled students

Test scenarios:
Role                Test Scenario                                       Expected Outcome
1.Common            Login with wrong password                           Login is rejected; clear "Incorrect username or password" message shown.
2.Student           Attempt to register for an already full section.    Registration is blocked with "Section is full." message.
3.Student           Attempt to register for the same section twice      Registration is blocked with "Already registered." message.
4.Student           Attempt to drop a section after the deadline.       Drop is blocked with "Deadline passed." message.
5.Admin             Toggle Maintenance Mode ON.                         System status banner changes immediately; writes are blocked for Student/Instructor roles.
6.Admin             Toggle Maintenance Mode OFF.                        Normal behavior returns; writes are enabled.

##Troubleshooting

1. "Database initialization failed"
    -The MySQL service is not running, or credentials in db.properties are incorrect.
        ->Ensure MySQL is running.
        ->Verify auth.db.user and auth.db.password match your setup.

2."java.sql.SQLException: Access denied"
    -Incorrect username or password in db.properties for the database user.
        ->Check db.properties credentials and ensure the user has access to both erp_auth and erp_main.

3."java.lang.ClassNotFoundException"
    -Maven dependencies were not compiled, or the executable JAR path is wrong.
        ->Run mvn clean install to ensure all libraries are correctly packaged, and verify the path to the JAR.

4.Login rejected (after 5 tries)
    -Account is temporarily locked due to the security feature.
        ->Wait 15 minutes for the lockout timer to expire

5.Application hangs or crashes
    -Connection pool exhaustion or a fatal database error.
        ->Check the console logs for detailed SQL errors (look for logger.error output from the DAO classes).

##Project Structure
src/main/java/edu/univ/erp/
                            ErpApplication.java      # Main entry point, sets global theme (FlatLightLaf)
                            auth/                    # BCrypt hashing, PasswordUtil, SessionManager
                            access/                  # AccessRuleChecker (Role and Maintenance Mode enforcement)
                            domain/                  # Data Models (User, Student, Section, Notification, etc.)
                            data/                    # Data Access Objects (DAOs) for Auth, Student, Course, Section, Notification, etc.
                            service/                 # Business Logic (StudentService, InstructorService, AdminService)

sql/
   01_create_auth_db.sql    # Auth DB schema (users, hash, history)
   02_create_erp_db.sql     # ERP DB schema (academic data, settings, notifications)
   03_seed_data.sql         # Test data