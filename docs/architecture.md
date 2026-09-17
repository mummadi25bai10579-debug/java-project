# System Architecture

The **Smart Library Management System** is architected as a layered, modular Java 17+ console application. It cleanly separates concerns across presentation, business services, data access, domain models, and utilities.

---

## 1. Architectural Layers

```
+-------------------------------------------------------------------+
|                        PRESENTATION LAYER                         |
|                 (Main.java, MenuHandler.java)                     |
|  - Console menus, ASCII tables, defensive input parsing loops    |
+---------------------------------+---------------------------------+
                                  |
                                  v
+-------------------------------------------------------------------+
|                         SERVICE LAYER                             |
|    (BookService.java, MemberService.java, TransactionService.java)|
|  - Domain rules: borrowing limits, due dates, fine calculations   |
|  - Atomic database transactions (setAutoCommit, commit, rollback) |
+---------------------------------+---------------------------------+
                                  |
                                  v
+-------------------------------------------------------------------+
|                     DATA ACCESS LAYER (DAO)                       |
|        (BookDAO.java, MemberDAO.java, TransactionDAO.java)        |
|  - Parameterized PreparedStatement queries                        |
|  - Polymorphic model instantiation                                |
|  - Implements Searchable<T> interface                             |
+---------------------------------+---------------------------------+
                                  |
                                  v
+-------------------------------------------------------------------+
|                     PERSISTENCE LAYER (SQLite)                    |
|                (DBConnection.java, data/library.db)               |
|  - Embedded SQLite file storage, foreign keys, table indexes     |
+-------------------------------------------------------------------+

Cross-Cutting Layers:
- Domain Models: Book, Member (abstract), Student, Faculty, Transaction
- Custom Exceptions: LibraryException, BookNotAvailableException,
                     MemberLimitExceededException, InvalidInputException
- Utilities: DBConnection, DateUtil, InputValidator
```

---

## 2. Key Design Decisions

1. **Layered Separation of Concerns**:
   - The CLI (`MenuHandler`) never executes direct SQL or business calculations.
   - The Service layer orchestrates domain rules and transaction boundaries.
   - The DAO layer encapsulates JDBC details and uses `PreparedStatement` to prevent SQL injection.

2. **Persistent Storage via SQLite & JDBC**:
   - Stores all data in a single local database file (`data/library.db`).
   - Evaluators do not need to install or configure external database servers (like MySQL or PostgreSQL).
   - Foreign key constraints are explicitly enforced using `PRAGMA foreign_keys = ON;`.

3. **Atomic Transaction Management**:
   - Critical operations (such as issuing a book and returning a book) involve multi-table updates (e.g., updating the transaction table and updating book available copy counts).
   - These are wrapped in JDBC transactions using `connection.setAutoCommit(false)`, `commit()`, and `rollback()` in case of any runtime or SQL exception.

4. **Object-Oriented Abstraction & Polymorphism**:
   - `Member` is an abstract class defining common attributes and abstract contracts (`getMaxBooksAllowed()`, `getLoanPeriodDays()`).
   - `Student` and `Faculty` specialize these contracts with distinct quotas (3 vs 5 books) and durations (14 vs 30 days).
   - `MemberDAO` instantiates the appropriate subclass dynamically based on the stored `member_type`.
