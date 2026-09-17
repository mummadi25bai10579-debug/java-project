package com.library;

import com.library.exception.BookNotAvailableException;
import com.library.exception.InvalidInputException;
import com.library.exception.LibraryException;
import com.library.exception.MemberLimitExceededException;
import com.library.model.Book;
import com.library.model.Faculty;
import com.library.model.Member;
import com.library.model.Student;
import com.library.model.Transaction;
import com.library.service.BookService;
import com.library.service.MemberService;
import com.library.service.TransactionService;
import com.library.util.DBConnection;
import com.library.util.DateUtil;

import java.io.File;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Automated test suite executing and verifying all 15 required business test scenarios.
 * Uses an isolated test database to ensure non-destructive execution.
 */
public class TestRunner {

    private static int totalPassed = 0;
    private static int totalFailed = 0;

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("   SMART LIBRARY MANAGEMENT SYSTEM - TEST SUITE   ");
        System.out.println("==================================================");

        String testDb = "data/test_library.db";
        File testFile = new File(testDb);
        if (testFile.exists()) {
            testFile.delete();
        }

        DBConnection.setDatabasePath(testDb);

        try {
            DBConnection.initializeDatabase();
            System.out.println("Test database initialized at: " + testDb);
            System.out.println();

            BookService bookService = new BookService();
            MemberService memberService = new MemberService();
            TransactionService txService = new TransactionService();

            // Test 1: Add valid book
            testCase(1, "Add Valid Book", () -> {
                Book b = bookService.addBook("Effective Java", "Joshua Bloch", "978-0134685991", "Programming", 3);
                assertCondition(b.getBookId() > 0, "Book ID was not generated");
                assertCondition(b.getAvailableCopies() == 3, "Available copies mismatch");
            });

            // Test 2: Add duplicate ISBN
            testCase(2, "Prevent Duplicate ISBN", () -> {
                try {
                    bookService.addBook("Fake Java", "Anonymous", "978-0134685991", "Tech", 1);
                    fail("Expected InvalidInputException for duplicate ISBN");
                } catch (InvalidInputException e) {
                    assertCondition(e.getMessage().contains("already exists"), "Unexpected error message: " + e.getMessage());
                }
            });

            // Test 3: Add invalid book data (empty title & negative copies)
            testCase(3, "Reject Invalid Book Data", () -> {
                try {
                    bookService.addBook("", "Author", "978-0000000001", "Genre", 5);
                    fail("Expected InvalidInputException for empty title");
                } catch (InvalidInputException expected) {}

                try {
                    bookService.addBook("Valid Title", "Author", "978-0000000002", "Genre", -2);
                    fail("Expected InvalidInputException for negative copies");
                } catch (InvalidInputException expected) {}
            });

            // Test 4: Register Student (Limits & Loan Period)
            testCase(4, "Register Student Member", () -> {
                Member s = memberService.registerMember("Aarav Sharma", "aarav@vit.ac.in", "STUDENT");
                assertCondition(s instanceof Student, "Member should be an instance of Student");
                assertCondition(s.getMaxBooksAllowed() == 3, "Student max books should be 3");
                assertCondition(s.getLoanPeriodDays() == 14, "Student loan period should be 14 days");
            });

            // Test 5: Register Faculty (Limits & Loan Period)
            testCase(5, "Register Faculty Member", () -> {
                Member f = memberService.registerMember("Dr. Priya Rao", "priya@vit.ac.in", "FACULTY");
                assertCondition(f instanceof Faculty, "Member should be an instance of Faculty");
                assertCondition(f.getMaxBooksAllowed() == 5, "Faculty max books should be 5");
                assertCondition(f.getLoanPeriodDays() == 30, "Faculty loan period should be 30 days");
            });

            // Test 6: Issue available book
            testCase(6, "Issue Available Book", () -> {
                // Member 1 (Aarav), Book 1 (Effective Java)
                Transaction tx = txService.issueBook(1, 1);
                assertCondition(tx.getTransactionId() > 0, "Transaction ID not generated");
                assertCondition(tx.getDueDate().isEqual(LocalDate.now().plusDays(14)), "Due date should be 14 days from today");

                Book b = bookService.getBookById(1);
                assertCondition(b.getAvailableCopies() == 2, "Available copies should have decreased to 2");
            });

            // Test 7: Issue unavailable book
            testCase(7, "Reject Issue on Unavailable Book", () -> {
                // Add a book with 1 copy
                Book rare = bookService.addBook("Rare Manuscript", "Ancient", "978-1111111111", "History", 1);
                // Issue the 1 copy to Faculty (Member 2)
                txService.issueBook(2, rare.getBookId());

                // Attempt to issue again when available = 0
                try {
                    txService.issueBook(1, rare.getBookId());
                    fail("Expected BookNotAvailableException");
                } catch (BookNotAvailableException e) {
                    assertCondition(e.getMessage().contains("no available copies"), "Unexpected message: " + e.getMessage());
                }
            });

            // Test 8: Exceed Student Borrowing Limit (3 books max)
            testCase(8, "Enforce Student Borrowing Limit (Max 3)", () -> {
                // Aarav already has 1 book issued (Book 1)
                // Add 3 new books
                Book b2 = bookService.addBook("Clean Code", "Robert Martin", "978-0132350884", "Programming", 2);
                Book b3 = bookService.addBook("Design Patterns", "GoF", "978-0201633610", "Architecture", 2);
                Book b4 = bookService.addBook("Refactoring", "Martin Fowler", "978-0201485677", "Programming", 2);

                // Aarav issues 2nd book
                txService.issueBook(1, b2.getBookId());
                // Aarav issues 3rd book (reaches limit of 3)
                txService.issueBook(1, b3.getBookId());

                // 4th issue attempt must fail
                try {
                    txService.issueBook(1, b4.getBookId());
                    fail("Expected MemberLimitExceededException for 4th book");
                } catch (MemberLimitExceededException e) {
                    assertCondition(e.getMessage().contains("Borrowing limit exceeded"), "Unexpected message: " + e.getMessage());
                }
            });

            // Test 9: Return book on time (Zero fine)
            testCase(9, "Return Book On-Time (Zero Fine)", () -> {
                // Return Transaction 1 on-time
                Transaction returned = txService.returnBook(1, LocalDate.now().plusDays(5));
                assertCondition(returned.getFineAmount() == 0.0, "Fine should be ₹0.00 for on-time return");
                assertCondition(Transaction.STATUS_RETURNED.equals(returned.getStatus()), "Status should be RETURNED");

                Book b = bookService.getBookById(1);
                assertCondition(b.getAvailableCopies() == 3, "Available copies should be restored to 3");
            });

            // Test 10: Return overdue book (Fine calculation)
            testCase(10, "Return Overdue Book with Fine", () -> {
                // Faculty (Member 2) borrowed Book 'Rare Manuscript' (Tx 2). Due in 30 days.
                // Return 37 days from issue date (7 days overdue)
                LocalDate returnDate = LocalDate.now().plusDays(37);
                Transaction returned = txService.returnBook(2, returnDate);
                assertCondition(returned.getFineAmount() == 35.0, "Fine should be 7 days * ₹5 = ₹35.00. Got: " + returned.getFineAmount());
            });

            // Test 11: Calculate fine calculation logic directly
            testCase(11, "Fine Calculation Formula", () -> {
                LocalDate due = LocalDate.of(2026, 9, 10);
                LocalDate ret = LocalDate.of(2026, 9, 17);
                long late = DateUtil.calculateOverdueDays(due, ret);
                assertCondition(late == 7, "Overdue days should be 7");
                double fine = late * TransactionService.FINE_RATE_PER_DAY;
                assertCondition(fine == 35.0, "Fine calculation should equal 35.0");

                long notLate = DateUtil.calculateOverdueDays(due, LocalDate.of(2026, 9, 5));
                assertCondition(notLate == 0, "Non-overdue days should be 0");
            });

            // Test 12: Search Book
            testCase(12, "Search Book by Title/Author/Genre/ISBN", () -> {
                List<Book> searchTitle = bookService.searchBooks("effective");
                assertCondition(!searchTitle.isEmpty(), "Search by title failed");

                List<Book> searchAuthor = bookService.searchBooks("fowler");
                assertCondition(!searchAuthor.isEmpty(), "Search by author failed");

                List<Book> searchGenre = bookService.searchBooks("Architecture");
                assertCondition(!searchGenre.isEmpty(), "Search by genre failed");
            });

            // Test 13: Delete Book Validation
            testCase(13, "Delete Book Rules", () -> {
                // Book 3 has an active loan to Aarav
                try {
                    bookService.deleteBook(3);
                    fail("Expected LibraryException when deleting book with active loan");
                } catch (LibraryException e) {
                    assertCondition(e.getMessage().contains("copies are currently issued"), "Unexpected message: " + e.getMessage());
                }

                // Add a standalone book and delete it
                Book temp = bookService.addBook("Temporary Book", "Temp Author", "978-9999999999", "Misc", 1);
                boolean deleted = bookService.deleteBook(temp.getBookId());
                assertCondition(deleted, "Book deletion failed");
            });

            // Test 14: Update Member & Reject Duplicate Email
            testCase(14, "Update Member & Reject Duplicate Email", () -> {
                // Update Aarav's name
                boolean updated = memberService.updateMember(1, "Aarav S. Sharma", "aarav.sharma@vit.ac.in");
                assertCondition(updated, "Member update failed");

                Member check = memberService.getMemberById(1);
                assertCondition("Aarav S. Sharma".equals(check.getName()), "Name was not updated");

                // Try assigning Priya's email to Aarav
                try {
                    memberService.updateMember(1, "Aarav S. Sharma", "priya@vit.ac.in");
                    fail("Expected InvalidInputException for duplicate email");
                } catch (InvalidInputException expected) {}
            });

            // Test 15: Library Statistics & Analytics
            testCase(15, "Library Statistics & Reporting", () -> {
                Map<String, Object> stats = txService.getLibraryStatistics();
                assertCondition((int) stats.get("Total Books (Titles)") >= 4, "Total titles mismatch");
                assertCondition((int) stats.get("Total Members") == 2, "Total members mismatch");
                assertCondition((double) stats.get("Total Fines Collected") == 35.0, "Total fines mismatch");
            });

        } catch (Exception e) {
            System.err.println("Fatal test exception: " + e.getMessage());
            e.printStackTrace();
            totalFailed++;
        } finally {
            // Clean up test db
            if (testFile.exists()) {
                testFile.delete();
            }
            // Restore default path
            DBConnection.setDatabasePath("data/library.db");
        }

        System.out.println();
        System.out.println("==================================================");
        System.out.printf("TEST SUMMARY: %d PASSED, %d FAILED%n", totalPassed, totalFailed);
        System.out.println("==================================================");

        if (totalFailed > 0) {
            System.exit(1);
        }
    }

    private static void testCase(int id, String name, TestCaseRunnable test) {
        try {
            test.run();
            System.out.printf("[PASS] Test %2d: %s%n", id, name);
            totalPassed++;
        } catch (Throwable t) {
            System.out.printf("[FAIL] Test %2d: %s -> %s%n", id, name, t.getMessage());
            totalFailed++;
        }
    }

    private static void assertCondition(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void fail(String message) {
        throw new AssertionError(message);
    }

    @FunctionalInterface
    interface TestCaseRunnable {
        void run() throws Exception;
    }
}
