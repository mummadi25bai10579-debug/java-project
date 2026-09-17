# Smart Library Management System

A modular **Java 17+ command-line Library Management System** developed as an academic project for the **Programming in Java** course.

The system manages books, library members, book issuing and returning, overdue fines, and library reports through a terminal-based interface. Data is stored persistently using **SQLite with JDBC**.

---

## 1. Project Overview

The Smart Library Management System is a terminal-based application designed to simplify common library operations.

The system provides:

- Book catalog management
- Student and Faculty member management
- Book issuing and returning
- Automatic due-date calculation
- Overdue fine calculation
- Borrowing history
- Library reports and statistics
- Persistent storage using SQLite

The application does not require a graphical user interface. It can be compiled and executed directly from a terminal using the Java Development Kit.

---

## 2. Problem Statement

Traditional library management can involve manual record keeping, making it difficult to track book availability, member borrowing limits, due dates, and overdue fines.

This project provides a Java-based solution that manages these operations through a structured command-line application with persistent database storage.

---

## 3. Objectives

The main objectives of the project are:

1. Maintain a searchable catalog of library books.
2. Track total and available copies of each book.
3. Register and manage Student and Faculty members.
4. Apply different borrowing limits based on member type.
5. Issue books only when copies are available and borrowing limits are not exceeded.
6. Process book returns and calculate overdue fines automatically.
7. Maintain borrowing and transaction history.
8. Generate useful library reports and statistics.
9. Store application data persistently using SQLite.
10. Demonstrate important Programming in Java concepts through a practical application.

---

## 4. Features & Functional Modules

### Module 1: Book Management

The Book Management module provides:

- Add new books
- Update book details
- Delete books
- Search books
- View all books
- Search by title, author, ISBN, or genre
- Track total copies
- Track available copies
- Validate duplicate ISBNs
- Prevent deletion when the book has active loans

---

### Module 2: Member Management

The Member Management module provides:

- Register members
- Update member details
- Delete members
- Search members
- View all members
- View member borrowing history
- Support Student and Faculty member types

#### Student
- Maximum active books: 3
- Loan period: 14 days

#### Faculty
- Maximum active books: 5
- Loan period: 30 days

---

### Module 3: Issue, Return & Fine Management

#### Issue Book
The system:
1. Verifies that the member exists.
2. Verifies that the book exists.
3. Checks book availability.
4. Checks the member's active borrowing limit.
5. Calculates the due date based on member type.
6. Creates a transaction record.
7. Decreases the available book count.

#### Return Book
The system:
1. Finds the active transaction.
2. Records the return date.
3. Calculates overdue days.
4. Calculates the applicable fine.
5. Updates the transaction status.
6. Increases the available book count.

The fine is calculated using:

```text
Fine = Overdue Days × Rs. 5.00
```

Critical issue and return operations use database transactions (`connection.setAutoCommit(false)`, `commit()`, `rollback()`) to maintain database consistency.

---

### Module 4: Reports & Analytics

The Reports and Analytics module provides:

- **Overdue Books:** Lists all active book loans where the due date has passed.
- **Currently Issued Books:** Displays all currently checked-out books along with borrower details.
- **Most Borrowed Books:** Identifies top-borrowed books across the library history.
- **Member-wise Borrowing History:** Shows the full loan and return history for any member ID.
- **Fine Summary:** Aggregates pending and collected fines per member.
- **Library Statistics:** Overall dashboard showing total titles, total copies, available copies, issued copies, total members, active transactions, returned transactions, and total fines collected.

---

## 5. Technologies Used

- **Programming Language:** Java 17+ (tested and verified on Java 21 / 26)
- **Database:** SQLite 3 (`data/library.db`)
- **Database Driver:** SQLite JDBC Driver (`lib/sqlite-jdbc.jar`)
- **Interface:** Standard Command-Line / Terminal
- **Architecture:** Layered Model-DAO-Service-UI design

---

## 6. Project Architecture & OOP Concepts

### Layered Architecture
```text
src/com/library/
├── Main.java               --> Application entry point & SQLite initialization
├── TestRunner.java         --> Standalone 15-case automated test suite
├── model/                  --> Domain entities (Book, Member, Student, Faculty, Transaction)
├── interfaces/             --> Generic search contract (Searchable<T>)
├── dao/                    --> JDBC Data Access Objects with PreparedStatements (BookDAO, MemberDAO, TransactionDAO)
├── service/                --> Business logic, rules, & atomic transactions (BookService, MemberService, TransactionService)
├── exception/              --> Custom domain exceptions (LibraryException, BookNotAvailableException, etc.)
├── util/                   --> DBConnection, DateUtil (java.time), InputValidator
└── ui/                     --> MenuHandler (console menus, ASCII tables, defensive input parsing)
```

### Java OOP Principles Demonstrated

1. **Abstraction:**
   - Abstract base class [`Member`](file:///c:/Users/Nages/OneDrive/Desktop/java/src/com/library/model/Member.java) defines core patron properties and abstract methods `getMaxBooksAllowed()` and `getLoanPeriodDays()`.

2. **Inheritance:**
   - [`Student`](file:///c:/Users/Nages/OneDrive/Desktop/java/src/com/library/model/Student.java) and [`Faculty`](file:///c:/Users/Nages/OneDrive/Desktop/java/src/com/library/model/Faculty.java) extend [`Member`](file:///c:/Users/Nages/OneDrive/Desktop/java/src/com/library/model/Member.java).

3. **Polymorphism:**
   - Runtime resolution of limits (Student: 3, Faculty: 5) and loan durations (Student: 14 days, Faculty: 30 days).
   - Dynamic subclass instantiation from database records in [`MemberDAO`](file:///c:/Users/Nages/OneDrive/Desktop/java/src/com/library/dao/MemberDAO.java).

4. **Encapsulation:**
   - Private attributes accessed strictly through public constructors, getters, and setters with validation.

5. **Interfaces:**
   - Generic [`Searchable<T>`](file:///c:/Users/Nages/OneDrive/Desktop/java/src/com/library/interfaces/Searchable.java) interface implemented by [`BookDAO`](file:///c:/Users/Nages/OneDrive/Desktop/java/src/com/library/dao/BookDAO.java) and [`MemberDAO`](file:///c:/Users/Nages/OneDrive/Desktop/java/src/com/library/dao/MemberDAO.java).

6. **Custom Exceptions:**
   - Meaningful domain exceptions ([`LibraryException`](file:///c:/Users/Nages/OneDrive/Desktop/java/src/com/library/exception/LibraryException.java), [`BookNotAvailableException`](file:///c:/Users/Nages/OneDrive/Desktop/java/src/com/library/exception/BookNotAvailableException.java), [`MemberLimitExceededException`](file:///c:/Users/Nages/OneDrive/Desktop/java/src/com/library/exception/MemberLimitExceededException.java), [`InvalidInputException`](file:///c:/Users/Nages/OneDrive/Desktop/java/src/com/library/exception/InvalidInputException.java)) prevent unexpected program crashes.

7. **Database Transaction Consistency:**
   - Critical issue and return operations wrap multi-table modifications in JDBC transactions (`conn.setAutoCommit(false)`, `conn.commit()`, `conn.rollback()`).

---

## 7. Folder Structure

```text
smart-library-management-system/
├── src/
│   └── com/
│       └── library/
│           ├── Main.java
│           ├── TestRunner.java
│           ├── model/
│           │   ├── Book.java
│           │   ├── Member.java
│           │   ├── Student.java
│           │   ├── Faculty.java
│           │   └── Transaction.java
│           ├── dao/
│           │   ├── BookDAO.java
│           │   ├── MemberDAO.java
│           │   └── TransactionDAO.java
│           ├── service/
│           │   ├── BookService.java
│           │   ├── MemberService.java
│           │   └── TransactionService.java
│           ├── exception/
│           │   ├── LibraryException.java
│           │   ├── BookNotAvailableException.java
│           │   ├── MemberLimitExceededException.java
│           │   └── InvalidInputException.java
│           ├── interfaces/
│           │   └── Searchable.java
│           ├── util/
│           │   ├── DBConnection.java
│           │   ├── DateUtil.java
│           │   └── InputValidator.java
│           └── ui/
│               └── MenuHandler.java
├── lib/
│   └── sqlite-jdbc.jar
├── database/
│   └── schema.sql
├── data/
│   ├── library.db (auto-generated on first launch)
│   └── README.md
├── docs/
│   ├── architecture.md
│   ├── workflow.md
│   ├── use-case.md
│   ├── class-diagram.md
│   └── sequence-diagram.md
├── tests/
│   └── TestCases.md
├── compile.bat / compile.sh
├── run.bat / run.sh
├── test.bat / test.sh
├── statement.md
├── .gitignore
├── LICENSE
└── README.md
```

---

## 8. Prerequisites

- **Java Development Kit (JDK):** Version 17 or higher (`javac -version` and `java -version`).
- **Operating System:** Windows, Linux, or macOS.
- **No external database server installation required.**

---

## 9. Installation & Execution

### Prerequisites

- Java Development Kit (JDK) 17 or higher
- Windows Command Prompt or PowerShell
- No external database server is required

Check Java installation:

```powershell
java -version
javac -version

## 10. Sample Usage Walkthrough

### 1. Main Menu
```text
==================================================
        SMART LIBRARY MANAGEMENT SYSTEM           
==================================================
1. Book Management
2. Member Management
3. Issue Book
4. Return Book
5. Reports & Analytics
6. Exit
Enter your choice: 1
```

### 2. Adding a Book
```text
========== BOOK MANAGEMENT ==========
1. Add Book
2. Update Book
3. Delete Book
4. Search Book
5. View All Books
6. Back
Enter your choice: 1

--- Add New Book ---
Enter Title: Effective Java
Enter Author: Joshua Bloch
Enter ISBN (10 or 13 digits): 978-0134685991
Enter Genre: Programming
Enter Total Copies: 3
SUCCESS: Book added successfully! Assigned Book ID: 1
```

### 3. Registering a Member
```text
========== MEMBER MANAGEMENT ==========
1. Register Member
...
Enter your choice: 1

--- Register New Member ---
Enter Member Name: Aarav Sharma
Enter Email Address: aarav@vit.ac.in
Select Member Type:
  1. STUDENT (Limit: 3 books, 14 days)
  2. FACULTY (Limit: 5 books, 30 days)
Choice (1 or 2): 1
SUCCESS: Member registered successfully!
Assigned Member ID : 1
Member Type        : STUDENT
Max Books Allowed  : 3
Standard Loan Days : 14
```

### 4. Issuing a Book
```text
========== ISSUE BOOK ==========
Enter Member ID: 1
Enter Book ID: 1

Book Available : YES
Book Title     : Effective Java
Issued To      : Aarav Sharma
Issue Date     : 2026-09-17
Due Date       : 2026-10-01

SUCCESS: Book issued successfully.
Transaction ID : 1
```

### 5. Returning a Book
```text
========== RETURN BOOK ==========
Enter Transaction ID: 1

Transaction ID : 1
Due Date       : 2026-10-01
Return Date    : 2026-09-17
Days Late      : 0
Fine           : Rs. 0.00

SUCCESS: Book returned successfully.
```

---

## 11. Testing & Verification

The project includes an automated test runner [`com.library.TestRunner`](file:///c:/Users/Nages/OneDrive/Desktop/java/src/com/library/TestRunner.java) verifying all 15 core business rules:

```powershell
.\test.bat
```

Output:
```text
==================================================
   SMART LIBRARY MANAGEMENT SYSTEM - TEST SUITE   
==================================================
Test database initialized at: data/test_library.db

[PASS] Test  1: Add Valid Book
[PASS] Test  2: Prevent Duplicate ISBN
[PASS] Test  3: Reject Invalid Book Data
[PASS] Test  4: Register Student Member
[PASS] Test  5: Register Faculty Member
[PASS] Test  6: Issue Available Book
[PASS] Test  7: Reject Issue on Unavailable Book
[PASS] Test  8: Enforce Student Borrowing Limit (Max 3)
[PASS] Test  9: Return Book On-Time (Zero Fine)
[PASS] Test 10: Return Overdue Book with Fine
[PASS] Test 11: Fine Calculation Formula
[PASS] Test 12: Search Book by Title/Author/Genre/ISBN
[PASS] Test 13: Delete Book Rules
[PASS] Test 14: Update Member & Reject Duplicate Email
[PASS] Test 15: Library Statistics & Reporting

==================================================
TEST SUMMARY: 15 PASSED, 0 FAILED
==================================================
```

Detailed test scenarios and evaluation matrices are documented in [tests/TestCases.md](tests/TestCases.md).

---

## 12. Future Enhancements

- Book reservation and waiting queue for books with zero available copies.
- Automated email reminder simulator for approaching due dates.
- Exporting library reports to CSV or spreadsheet format.
- Support for barcode/QR-code scanning simulation for physical book tracking.
"# java-project" 
