# Use-Case Specification

This document details the primary actors, use cases, and functional boundaries of the **Smart Library Management System**.

---

## 1. Actors

- **Librarian / Library Administrator**: Operates the terminal interface to manage books, members, transactions, and review analytics.
- **Member (Student / Faculty)**: Library patrons on whose behalf books are borrowed, returned, and tracked.

---

## 2. Use-Case Summary

| Use-Case ID | Use-Case Name | Actor | Description |
|---|---|---|---|
| **UC-01** | Add Book | Librarian | Registers a new book into the library catalog with total copies. |
| **UC-02** | Update Book | Librarian | Edits existing book details (title, author, genre, total copies). |
| **UC-03** | Delete Book | Librarian | Deletes a book only if there are no currently active book loans. |
| **UC-04** | Search Book | Librarian | Searches books across title, author, genre, ISBN, or ID. |
| **UC-05** | View All Books | Librarian | Displays tabular listing of all catalog books with availability. |
| **UC-06** | Register Member | Librarian | Registers a Student (max 3 books, 14 days) or Faculty (max 5 books, 30 days). |
| **UC-07** | Update Member | Librarian | Modifies existing member name or email address. |
| **UC-08** | Delete Member | Librarian | Removes a member only if they have zero outstanding active loans. |
| **UC-09** | Search Member | Librarian | Looks up members by name, email, or member ID. |
| **UC-10** | Issue Book | Librarian | Checks availability and member quota, decrements copies, and issues loan. |
| **UC-11** | Return Book | Librarian | Computes overdue days, applies fine (Rs. 5/day), restores available copies. |
| **UC-12** | Overdue Books Report | Librarian | Lists all active transactions whose due date has elapsed. |
| **UC-13** | Most Borrowed Books | Librarian | Ranks books by cumulative historical borrow count. |
| **UC-14** | Member History Report | Librarian | Shows complete borrowing and return history for a specific member ID. |
| **UC-15** | Fine Summary Report | Librarian | Aggregates all assessed fines broken down per member. |
| **UC-16** | Library Statistics | Librarian | Computes total titles, total copies, issued copies, and total collected fines. |
