# Project Statement: Smart Library Management System

**Course:** Programming in Java  
**Project Title:** Smart Library Management System  
**Application Type:** Command-Line Interface (Terminal Application)  
**Target Environment:** Java 17+ with SQLite 3 (via JDBC)  

---

## 1. Problem Statement

Libraries routinely handle large catalogs of books, diverse patron groups (students and faculty), lending transactions, due dates, and overdue penalties. When performed manually or with fragmented tools, librarians encounter:
- Inaccurate tracking of physical book inventory and available copies.
- Ambiguity regarding member borrowing allowances and distinct loan periods.
- Inconsistent and error-prone overdue fine calculations.
- Lack of consolidated records for active loans, borrowing history, and library utilization metrics.

The **Smart Library Management System** addresses these issues through a consolidated, terminal-based software system that automates catalog management, membership tracking, policy enforcement, transactional lending, fine calculation, and analytical reporting.

---

## 2. Project Scope

The project encompasses a complete standalone command-line application built without heavyweight web frameworks or graphical user interfaces. It delivers:
- Zero-configuration data persistence using an embedded SQLite database (`library.db`).
- Strict Object-Oriented Programming (OOP) design principles using core Java APIs.
- Domain policy rules differentiating **Student** members from **Faculty** members.
- Transaction-safe database operations for book issue and return workflows.
- Rich terminal interaction with formatted tabular outputs and defensive user input handling.

---

## 3. Target Users

1. **Library Administrators / Librarians**: Primary operators who manage inventory, register library patrons, issue and return books, and generate statistical reports.
2. **Academic Evaluators**: Instructors or examiners evaluating code quality, OOP principles, modular separation, and application behavior.

---

## 4. High-Level Features

- **Catalog Management**: Full CRUD operations for books with total and available copy counts. Duplicate ISBNs are strictly rejected.
- **Member Registry**: Distinct registration for Student and Faculty members with automatic borrowing limits and loan period assignments.
- **Book Issue & Limit Verification**: Validates book copy availability and member active quotas before lending.
- **Book Return & Automated Fines**: Calculates overdue days based on system dates and charges a configurable rate of **Rs. 5.00 per overdue day**.
- **Reports & Analytics**: Generates overdue lists, currently issued books, top borrowed books, member transaction history, fine totals, and comprehensive library statistics.

---

## 5. Functional Modules

### Module 1: Book Management
- Add Book (validates title, author, genre, ISBN, copies > 0).
- Update Book (prevents reducing total copies below active loans).
- Delete Book (protected against deletion if active loans exist).
- Search Books (case-insensitive search across title, author, ISBN, genre).
- View All Books (formatted ASCII table showing available/total copies).

### Module 2: Member Management
- Register Member (Student: max 3 books, 14-day loan; Faculty: max 5 books, 30-day loan).
- Update Member (name, email format validation, duplicate email prevention).
- Delete Member (protected against deletion if active loans exist).
- Search Member (by name, email, or member ID).
- View Member Borrowing History.

### Module 3: Issue, Return & Fine Management
- Issue Book (enforces borrowing quotas, decrements available inventory atomically).
- Return Book (computes days late, calculates overdue fines, increments available inventory atomically).
- Transaction Safety (JDBC transactions with rollback on failure).

### Module 4: Reports & Analytics
- Overdue Books List
- Currently Issued Books List
- Most Borrowed Books (Top 5)
- Member-wise History
- Member-wise Fine Summary
- Overall Library Statistics Dashboard

---

## 6. Expected Outcome

A robust, self-contained terminal application that compiles and runs from the command line using standard Java tools. The evaluator can clone the repository, run the compilation script, and immediately execute the interactive program or automated test suite without configuring an external database or IDE.
