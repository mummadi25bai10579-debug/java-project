# Operational Workflows

This document describes the end-to-end operational workflows of the core modules in the **Smart Library Management System**.

---

## 1. Book Issue Workflow

When a member requests to borrow a book:

```
[User] -> Selects Option 3 (Issue Book)
   |
   +--> Prompt Member ID
   |      |
   |      +--> Verify Member exists in DB?
   |             |-- No  --> Show Error: Member ID does not exist
   |             +-- Yes --> Fetch active transaction count for Member
   |                           |
   |                           +--> activeLoans >= member.getMaxBooksAllowed()?
   |                                  |-- Yes --> Throw MemberLimitExceededException
   |                                  +-- No  --> Continue
   |
   +--> Prompt Book ID
          |
          +--> Verify Book exists in DB?
                 |-- No  --> Show Error: Book ID does not exist
                 +-- Yes --> availableCopies > 0?
                               |-- No  --> Throw BookNotAvailableException
                               +-- Yes --> Continue
   |
   +--> Compute loan timeline:
   |      issueDate = LocalDate.now()
   |      dueDate   = issueDate + member.getLoanPeriodDays()
   |
   +--> Open DB Connection (conn.setAutoCommit(false))
   |      |
   |      +--> INSERT INTO transactions (book_id, member_id, issue_date, due_date, status='ISSUED')
   |      +--> UPDATE books SET available_copies = available_copies - 1 WHERE book_id = ?
   |      +--> conn.commit()
   |      (On failure: conn.rollback())
   |
   +--> Display Transaction ID, Due Date, and Confirmation
```

---

## 2. Book Return Workflow

When a member returns an issued book:

```
[User] -> Selects Option 4 (Return Book)
   |
   +--> Prompt Transaction ID
   |      |
   |      +--> Find active transaction with status 'ISSUED'
   |             |-- Not Found / Already Returned --> Show Error
   |             +-- Found --> Retrieve dueDate
   |
   +--> Calculate Overdue Days and Fine:
   |      returnDate  = LocalDate.now()
   |      daysLate    = Math.max(0, ChronoUnit.DAYS.between(dueDate, returnDate))
   |      fineAmount  = daysLate * Rs. 5.00
   |
   +--> Open DB Connection (conn.setAutoCommit(false))
   |      |
   |      +--> UPDATE transactions SET return_date = ?, fine_amount = ?, status = 'RETURNED'
   |      +--> UPDATE books SET available_copies = available_copies + 1 WHERE book_id = ?
   |      +--> conn.commit()
   |      (On failure: conn.rollback())
   |
   +--> Display Summary: Due Date, Return Date, Days Late, Fine (Rs.), and Success Confirmation
```

---

## 3. Member Registration Workflow

```
[User] -> Selects Option 2 (Member Management) -> Option 1 (Register Member)
   |
   +--> Prompt Name, Email, Member Type (1: Student, 2: Faculty)
   +--> InputValidator validates non-empty name, email regex, valid member type
   +--> MemberService checks for existing duplicate email in database
          |-- Duplicate exists --> Throw InvalidInputException
          +-- Unique email    --> Instantiate Student or Faculty object polymorphically
   +--> MemberDAO executes INSERT INTO members
   +--> Display generated Member ID, assigned limits, and loan duration
```
