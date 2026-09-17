# Sequence Diagrams

This document illustrates the message-passing sequences between objects for key business flows: **Issue Book** and **Return Book with Fine Calculation**.

---

## 1. Issue Book Sequence

```mermaid
sequenceDiagram
    autonumber
    actor User as Librarian
    participant UI as MenuHandler
    participant Service as TransactionService
    participant MDAO as MemberDAO
    participant BDAO as BookDAO
    participant TDAO as TransactionDAO
    participant DB as SQLite DB

    User->>UI: Select "3. Issue Book"
    UI->>User: Prompt Member ID & Book ID
    User->>UI: Enters Member ID 101, Book ID 25
    UI->>Service: issueBook(101, 25)

    Service->>MDAO: findById(101)
    MDAO-->>Service: returns Member (e.g. Student)
    Service->>TDAO: getActiveTransactionCountByMember(101)
    TDAO-->>Service: returns active count (e.g. 1)

    Note over Service: Verify activeCount < member.getMaxBooksAllowed() (1 < 3)

    Service->>BDAO: findById(25)
    BDAO-->>Service: returns Book (availableCopies = 2)

    Note over Service: Verify availableCopies > 0

    Service->>DB: Open Connection (conn.setAutoCommit(false))
    Service->>TDAO: createTransaction(tx, conn)
    TDAO->>DB: INSERT INTO transactions ...
    Service->>BDAO: updateAvailableCopies(25, -1, conn)
    BDAO->>DB: UPDATE books SET available_copies = available_copies - 1 ...
    Service->>DB: conn.commit()
    Service-->>UI: returns Transaction object
    UI-->>User: Displays Book Title, Due Date, and Transaction ID
```

---

## 2. Return Book & Fine Calculation Sequence

```mermaid
sequenceDiagram
    autonumber
    actor User as Librarian
    participant UI as MenuHandler
    participant Service as TransactionService
    participant TDAO as TransactionDAO
    participant BDAO as BookDAO
    participant DB as SQLite DB

    User->>UI: Select "4. Return Book"
    UI->>User: Prompt Transaction ID
    User->>UI: Enters Transaction ID 1005
    UI->>Service: returnBook(1005)

    Service->>TDAO: findActiveTransaction(1005)
    TDAO-->>Service: returns Transaction (dueDate = 2026-09-10)

    Note over Service: returnDate = 2026-09-17<br/>daysLate = 7 days<br/>fine = 7 * Rs. 5.00 = Rs. 35.00

    Service->>DB: Open Connection (conn.setAutoCommit(false))
    Service->>TDAO: updateTransactionReturn(1005, returnDate, fine, conn)
    TDAO->>DB: UPDATE transactions SET status='RETURNED', return_date=..., fine_amount=...
    Service->>BDAO: updateAvailableCopies(bookId, +1, conn)
    BDAO->>DB: UPDATE books SET available_copies = available_copies + 1 ...
    Service->>DB: conn.commit()

    Service-->>UI: returns updated Transaction
    UI-->>User: Displays Days Late: 7, Fine: Rs. 35.00, Success Confirmation
```
