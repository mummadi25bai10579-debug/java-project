package com.library.service;

import com.library.dao.BookDAO;
import com.library.dao.MemberDAO;
import com.library.dao.TransactionDAO;
import com.library.exception.BookNotAvailableException;
import com.library.exception.InvalidInputException;
import com.library.exception.LibraryException;
import com.library.exception.MemberLimitExceededException;
import com.library.model.Book;
import com.library.model.Member;
import com.library.model.Transaction;
import com.library.util.DBConnection;
import com.library.util.DateUtil;
import com.library.util.InputValidator;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Core business service managing book issues, returns, overdue fine calculation,
 * atomic JDBC transactions, and library reports.
 */
public class TransactionService {

    public static final double FINE_RATE_PER_DAY = 5.0; // ₹5 per day late

    private final BookDAO bookDAO;
    private final MemberDAO memberDAO;
    private final TransactionDAO transactionDAO;

    public TransactionService() {
        this.bookDAO = new BookDAO();
        this.memberDAO = new MemberDAO();
        this.transactionDAO = new TransactionDAO();
    }

    public TransactionService(BookDAO bookDAO, MemberDAO memberDAO, TransactionDAO transactionDAO) {
        this.bookDAO = bookDAO;
        this.memberDAO = memberDAO;
        this.transactionDAO = transactionDAO;
    }

    /**
     * Issues a book to a member.
     * Verifies member existence, active borrowing limit by member type,
     * book existence, and book availability.
     * Executes transaction creation and copy decrement within an atomic database transaction.
     */
    public Transaction issueBook(int memberId, int bookId) throws LibraryException {
        InputValidator.validatePositiveInt(memberId, "Member ID");
        InputValidator.validatePositiveInt(bookId, "Book ID");

        try {
            // 1. Verify Member
            Member member = memberDAO.findById(memberId);
            if (member == null) {
                throw new LibraryException("Member ID " + memberId + " does not exist.");
            }

            // 2. Check active loans limit
            int activeLoans = transactionDAO.getActiveTransactionCountByMember(memberId);
            if (activeLoans >= member.getMaxBooksAllowed()) {
                throw new MemberLimitExceededException(String.format(
                        "Borrowing limit exceeded: %s members can borrow at most %d book(s). Currently holding: %d.",
                        member.getMemberType(), member.getMaxBooksAllowed(), activeLoans));
            }

            // 3. Verify Book
            Book book = bookDAO.findById(bookId);
            if (book == null) {
                throw new LibraryException("Book ID " + bookId + " does not exist.");
            }

            // 4. Check Availability
            if (book.getAvailableCopies() <= 0) {
                throw new BookNotAvailableException("This book currently has no available copies (0/" + book.getTotalCopies() + ").");
            }

            // 5. Calculate loan timeline
            LocalDate issueDate = LocalDate.now();
            LocalDate dueDate = issueDate.plusDays(member.getLoanPeriodDays());

            Transaction tx = new Transaction(bookId, memberId, issueDate, dueDate);

            // 6. Execute atomic database transaction
            try (Connection conn = DBConnection.getConnection()) {
                conn.setAutoCommit(false);
                try {
                    boolean created = transactionDAO.createTransaction(tx, conn);
                    if (!created) {
                        throw new SQLException("Failed to record issue transaction.");
                    }

                    boolean updated = bookDAO.updateAvailableCopies(bookId, -1, conn);
                    if (!updated) {
                        throw new SQLException("Failed to decrement book copies.");
                    }

                    conn.commit();
                } catch (SQLException e) {
                    conn.rollback();
                    throw new LibraryException("Transaction rollback: " + e.getMessage(), e);
                } finally {
                    conn.setAutoCommit(true);
                }
            }

            tx.setBookTitle(book.getTitle());
            tx.setMemberName(member.getName());
            return tx;

        } catch (SQLException e) {
            throw new LibraryException("Database error during book issue: " + e.getMessage(), e);
        }
    }

    /**
     * Returns a book using the current system date.
     */
    public Transaction returnBook(int transactionId) throws LibraryException {
        return returnBook(transactionId, LocalDate.now());
    }

    /**
     * Returns a book for a given return date.
     * Calculates overdue days and applies ₹5/day fine if overdue.
     * Executes transaction status update and copy increment within an atomic database transaction.
     */
    public Transaction returnBook(int transactionId, LocalDate returnDate) throws LibraryException {
        InputValidator.validatePositiveInt(transactionId, "Transaction ID");
        if (returnDate == null) {
            returnDate = LocalDate.now();
        }

        try {
            Transaction tx = transactionDAO.findActiveTransaction(transactionId);
            if (tx == null) {
                Transaction anyTx = transactionDAO.findById(transactionId);
                if (anyTx != null && Transaction.STATUS_RETURNED.equalsIgnoreCase(anyTx.getStatus())) {
                    throw new LibraryException("Transaction ID " + transactionId + " was already returned on " + anyTx.getReturnDate() + ".");
                }
                throw new LibraryException("Active transaction ID " + transactionId + " not found.");
            }

            long daysLate = DateUtil.calculateOverdueDays(tx.getDueDate(), returnDate);
            double fine = daysLate * FINE_RATE_PER_DAY;

            // Execute atomic database transaction
            try (Connection conn = DBConnection.getConnection()) {
                conn.setAutoCommit(false);
                try {
                    boolean updatedTx = transactionDAO.updateTransactionReturn(transactionId, returnDate, fine, conn);
                    if (!updatedTx) {
                        throw new SQLException("Failed to update transaction return status.");
                    }

                    boolean updatedBook = bookDAO.updateAvailableCopies(tx.getBookId(), 1, conn);
                    if (!updatedBook) {
                        throw new SQLException("Failed to increment available book copies.");
                    }

                    conn.commit();
                } catch (SQLException e) {
                    conn.rollback();
                    throw new LibraryException("Transaction rollback: " + e.getMessage(), e);
                } finally {
                    conn.setAutoCommit(true);
                }
            }

            tx.setReturnDate(returnDate);
            tx.setFineAmount(fine);
            tx.setStatus(Transaction.STATUS_RETURNED);
            return tx;

        } catch (SQLException e) {
            throw new LibraryException("Database error during book return: " + e.getMessage(), e);
        }
    }

    public List<Transaction> getOverdueBooks() throws LibraryException {
        try {
            return transactionDAO.getOverdueTransactions(LocalDate.now());
        } catch (SQLException e) {
            throw new LibraryException("Database error fetching overdue books: " + e.getMessage(), e);
        }
    }

    public List<Transaction> getCurrentlyIssuedBooks() throws LibraryException {
        try {
            return transactionDAO.getAllActiveTransactions();
        } catch (SQLException e) {
            throw new LibraryException("Database error fetching issued books: " + e.getMessage(), e);
        }
    }

    public List<Map<String, Object>> getMostBorrowedBooks(int limit) throws LibraryException {
        try {
            return transactionDAO.getMostBorrowedBooks(limit <= 0 ? 5 : limit);
        } catch (SQLException e) {
            throw new LibraryException("Database error fetching most borrowed books: " + e.getMessage(), e);
        }
    }

    public List<Transaction> getMemberHistory(int memberId) throws LibraryException {
        InputValidator.validatePositiveInt(memberId, "Member ID");
        try {
            Member member = memberDAO.findById(memberId);
            if (member == null) {
                throw new LibraryException("Member ID " + memberId + " does not exist.");
            }
            return transactionDAO.getMemberHistory(memberId);
        } catch (SQLException e) {
            throw new LibraryException("Database error fetching member history: " + e.getMessage(), e);
        }
    }

    public Map<String, Double> getMemberWiseFines() throws LibraryException {
        try {
            return transactionDAO.getMemberWiseFines();
        } catch (SQLException e) {
            throw new LibraryException("Database error fetching fine summary: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> getLibraryStatistics() throws LibraryException {
        try {
            return transactionDAO.getLibraryStatistics();
        } catch (SQLException e) {
            throw new LibraryException("Database error fetching library statistics: " + e.getMessage(), e);
        }
    }
}
