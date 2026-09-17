# Class Diagram & Object-Oriented Structure

This document details the class hierarchy, interfaces, attributes, and relationships in the **Smart Library Management System**.

---

## Mermaid Class Diagram

```mermaid
classDiagram
    direction TB

    class Searchable~T~ {
        <<interface>>
        +search(String keyword) List~T~
    }

    class Book {
        -int bookId
        -String title
        -String author
        -String isbn
        -String genre
        -int totalCopies
        -int availableCopies
        +getBookId() int
        +getTitle() String
        +getAuthor() String
        +getIsbn() String
        +getGenre() String
        +getTotalCopies() int
        +getAvailableCopies() int
    }

    class Member {
        <<abstract>>
        -int memberId
        -String name
        -String email
        -MemberType memberType
        -LocalDate registeredOn
        +getMaxBooksAllowed()* int
        +getLoanPeriodDays()* int
        +getMemberId() int
        +getName() String
        +getEmail() String
    }

    class Student {
        +getMaxBooksAllowed() int
        +getLoanPeriodDays() int
    }

    class Faculty {
        +getMaxBooksAllowed() int
        +getLoanPeriodDays() int
    }

    class Transaction {
        -int transactionId
        -int bookId
        -int memberId
        -LocalDate issueDate
        -LocalDate dueDate
        -LocalDate returnDate
        -double fineAmount
        -String status
    }

    class BookDAO {
        +addBook(Book book) boolean
        +updateBook(Book book) boolean
        +deleteBook(int bookId) boolean
        +findById(int bookId) Book
        +search(String keyword) List~Book~
        +updateAvailableCopies(int bookId, int delta, Connection conn) boolean
    }

    class MemberDAO {
        +addMember(Member member) boolean
        +updateMember(Member member) boolean
        +deleteMember(int memberId) boolean
        +findById(int memberId) Member
        +search(String keyword) List~Member~
    }

    class TransactionDAO {
        +createTransaction(Transaction tx, Connection conn) boolean
        +updateTransactionReturn(int txId, LocalDate returnDate, double fine, Connection conn) boolean
        +findActiveTransaction(int txId) Transaction
        +getActiveTransactionCountByMember(int memberId) int
        +getOverdueTransactions(LocalDate currentDate) List~Transaction~
        +getLibraryStatistics() Map
    }

    class BookService {
        -BookDAO bookDAO
        +addBook(...) Book
        +updateBook(...) boolean
        +deleteBook(int bookId) boolean
        +searchBooks(String keyword) List~Book~
    }

    class MemberService {
        -MemberDAO memberDAO
        -TransactionDAO transactionDAO
        +registerMember(...) Member
        +updateMember(...) boolean
        +deleteMember(int memberId) boolean
    }

    class TransactionService {
        -BookDAO bookDAO
        -MemberDAO memberDAO
        -TransactionDAO transactionDAO
        +issueBook(int memberId, int bookId) Transaction
        +returnBook(int transactionId) Transaction
        +getOverdueBooks() List~Transaction~
        +getLibraryStatistics() Map
    }

    class MenuHandler {
        -BookService bookService
        -MemberService memberService
        -TransactionService transactionService
        +start() void
    }

    %% Relationships
    Member <|-- Student : Inheritance
    Member <|-- Faculty : Inheritance

    Searchable <|.. BookDAO : Implements
    Searchable <|.. MemberDAO : Implements

    BookService --> BookDAO : Uses
    MemberService --> MemberDAO : Uses
    MemberService --> TransactionDAO : Uses
    TransactionService --> BookDAO : Uses
    TransactionService --> MemberDAO : Uses
    TransactionService --> TransactionDAO : Uses

    MenuHandler --> BookService : Coordinates
    MenuHandler --> MemberService : Coordinates
    MenuHandler --> TransactionService : Coordinates
```

---

## Key OOP Concepts Visibly Demonstrated

1. **Abstraction**:
   `Member` is an abstract base class that defines the core properties of library patrons while leaving borrowing rules (`getMaxBooksAllowed()`, `getLoanPeriodDays()`) abstract for concrete realization.

2. **Inheritance**:
   `Student` and `Faculty` inherit all member state and methods from `Member`, establishing an *is-a* relationship.

3. **Polymorphism**:
   - `member.getMaxBooksAllowed()` resolves dynamically at runtime to `3` for Students and `5` for Faculty.
   - `member.getLoanPeriodDays()` dynamically resolves to `14` for Students and `30` for Faculty.
   - `MemberDAO` parses database rows and polymorphically constructs either `Student` or `Faculty` based on the database column `member_type`.

4. **Encapsulation**:
   All entity fields across `Book`, `Member`, and `Transaction` are declared `private` and accessed strictly via public getters, setters, and constructors.

5. **Interfaces**:
   The `Searchable<T>` generic interface defines a uniform search contract implemented by catalog and registry DAOs (`BookDAO`, `MemberDAO`).
