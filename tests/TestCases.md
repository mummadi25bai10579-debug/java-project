# Test Cases & Verification Matrix

This document provides the formal test cases and evaluation matrix for the **Smart Library Management System**, matching the academic requirements of the Programming in Java course.

---

## Test Execution Summary

* **Automated Test Suite:** `com.library.TestRunner`
* **Test Database:** `data/test_library.db` (isolated, non-destructive)
* **Status:** 15/15 Passed (100% Success Rate)

---

## Detailed Test Cases

| Test ID | Test Scenario | Input Data / Preconditions | Expected Outcome | Execution Type | Status |
|---|---|---|---|---|---|
| **TC-01** | Add Valid Book | Title: "Effective Java"<br>Author: "Joshua Bloch"<br>ISBN: "978-0134685991"<br>Genre: "Programming"<br>Copies: 3 | Book saved to SQLite; generated book ID > 0; available copies = 3. | Automated (`TestRunner`) | **PASS** |
| **TC-02** | Prevent Duplicate ISBN | Title: "Fake Java"<br>ISBN: "978-0134685991" (already registered) | Throws `InvalidInputException` with message stating ISBN already exists; duplicate rejected. | Automated (`TestRunner`) | **PASS** |
| **TC-03** | Reject Invalid Book Data | Title: "" (empty)<br>Copies: -2 (negative) | Throws `InvalidInputException` on empty title and on negative copy count. | Automated (`TestRunner`) | **PASS** |
| **TC-04** | Register Student Member | Name: "Aarav Sharma"<br>Email: "aarav@vit.ac.in"<br>Type: STUDENT | Registered as `Student` subclass; max books = 3; loan period = 14 days. | Automated (`TestRunner`) | **PASS** |
| **TC-05** | Register Faculty Member | Name: "Dr. Priya Rao"<br>Email: "priya@vit.ac.in"<br>Type: FACULTY | Registered as `Faculty` subclass; max books = 5; loan period = 30 days. | Automated (`TestRunner`) | **PASS** |
| **TC-06** | Issue Available Book | Member ID: 1, Book ID: 1 (Available: 3) | Transaction created with status `ISSUED`; due date = today + 14 days; available copies decreases to 2. | Automated (`TestRunner`) | **PASS** |
| **TC-07** | Reject Issue on Unavailable Book | Book with 1 copy issued to Member 2; Member 1 tries to issue it | Throws `BookNotAvailableException` ("no available copies"); transaction rejected. | Automated (`TestRunner`) | **PASS** |
| **TC-08** | Enforce Student Borrowing Limit | Student with 3 active book loans attempts to issue a 4th book | Throws `MemberLimitExceededException` ("Borrowing limit exceeded: STUDENT members can borrow at most 3 book(s)"); 4th issue blocked. | Automated (`TestRunner`) | **PASS** |
| **TC-09** | Return Book On-Time | Transaction 1 returned before or on due date | Days late = 0; Fine = Rs. 0.00; Status updated to `RETURNED`; book available copies restored. | Automated (`TestRunner`) | **PASS** |
| **TC-10** | Return Overdue Book with Fine | Faculty loan returned 7 days after due date | Days late = 7; Fine calculated = 7 * Rs. 5.00 = Rs. 35.00; Transaction recorded with fine amount. | Automated (`TestRunner`) | **PASS** |
| **TC-11** | Fine Calculation Formula | Due Date: 2026-09-10<br>Return Date: 2026-09-17 | `daysBetween` = 7; `fine` = 7 * 5.0 = Rs. 35.00. Non-overdue returns yield Rs. 0.00. | Automated (`TestRunner`) | **PASS** |
| **TC-12** | Search Book Catalog | Keyword queries: "effective", "fowler", "Architecture" | Search results returned matching title, author, genre, or ISBN case-insensitively. | Automated (`TestRunner`) | **PASS** |
| **TC-13** | Delete Book Rules | Delete Book with active loan vs standalone book | Active loan deletion fails with `LibraryException`; standalone book deletion succeeds. | Automated (`TestRunner`) | **PASS** |
| **TC-14** | Update Member & Duplicate Email | Update Member 1 name; Attempt to assign existing email of Member 2 | Member name updated successfully; duplicate email assignment throws `InvalidInputException`. | Automated (`TestRunner`) | **PASS** |
| **TC-15** | Library Statistics & Analytics | Queries across books, members, transactions | Correct aggregate metrics: total titles, copies, active transactions, returned transactions, total fines. | Automated (`TestRunner`) | **PASS** |
| **TC-16** | Invalid Menu & String Input Handling | User enters non-numeric text "abc" at integer menu prompt | System catches `NumberFormatException`, displays "Invalid choice. Please enter a number between 1 and 6", does not crash, and re-prompts. | Manual / Scripted CLI | **PASS** |

---

## How to Run the Automated Test Suite

### On Windows:
```cmd
test.bat
```
Or directly:
```cmd
java -cp "bin;lib/sqlite-jdbc.jar" com.library.TestRunner
```

### On Linux / macOS:
```bash
./test.sh
```
Or directly:
```bash
java -cp "bin:lib/sqlite-jdbc.jar" com.library.TestRunner
```
