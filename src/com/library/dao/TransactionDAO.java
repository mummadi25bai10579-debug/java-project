package com.library.dao;

import com.library.model.Transaction;
import com.library.util.DBConnection;
import com.library.util.DateUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object for Transaction entities.
 * Supports connection-aware execution for atomic JDBC transactions.
 */
public class TransactionDAO {

    public boolean createTransaction(Transaction tx, Connection conn) throws SQLException {
        String sql = """
            INSERT INTO transactions (book_id, member_id, issue_date, due_date, return_date, fine_amount, status)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, tx.getBookId());
            pstmt.setInt(2, tx.getMemberId());
            pstmt.setString(3, DateUtil.format(tx.getIssueDate()));
            pstmt.setString(4, DateUtil.format(tx.getDueDate()));
            pstmt.setString(5, tx.getReturnDate() != null ? DateUtil.format(tx.getReturnDate()) : null);
            pstmt.setDouble(6, tx.getFineAmount());
            pstmt.setString(7, tx.getStatus());

            int affected = pstmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        tx.setTransactionId(rs.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    public boolean updateTransactionReturn(int transactionId, LocalDate returnDate, double fine, Connection conn) throws SQLException {
        String sql = """
            UPDATE transactions
            SET return_date = ?, fine_amount = ?, status = ?
            WHERE transaction_id = ? AND status = ?
        """;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, DateUtil.format(returnDate));
            pstmt.setDouble(2, fine);
            pstmt.setString(3, Transaction.STATUS_RETURNED);
            pstmt.setInt(4, transactionId);
            pstmt.setString(5, Transaction.STATUS_ISSUED);

            return pstmt.executeUpdate() > 0;
        }
    }

    public Transaction findById(int transactionId) throws SQLException {
        String sql = """
            SELECT t.transaction_id, t.book_id, t.member_id, t.issue_date, t.due_date,
                   t.return_date, t.fine_amount, t.status, b.title AS book_title, m.name AS member_name
            FROM transactions t
            JOIN books b ON t.book_id = b.book_id
            JOIN members m ON t.member_id = m.member_id
            WHERE t.transaction_id = ?
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, transactionId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToTransaction(rs);
                }
            }
        }
        return null;
    }

    public Transaction findActiveTransaction(int transactionId) throws SQLException {
        String sql = """
            SELECT t.transaction_id, t.book_id, t.member_id, t.issue_date, t.due_date,
                   t.return_date, t.fine_amount, t.status, b.title AS book_title, m.name AS member_name
            FROM transactions t
            JOIN books b ON t.book_id = b.book_id
            JOIN members m ON t.member_id = m.member_id
            WHERE t.transaction_id = ? AND t.status = ?
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, transactionId);
            pstmt.setString(2, Transaction.STATUS_ISSUED);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToTransaction(rs);
                }
            }
        }
        return null;
    }

    public int getActiveTransactionCountByMember(int memberId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM transactions WHERE member_id = ? AND status = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, memberId);
            pstmt.setString(2, Transaction.STATUS_ISSUED);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    public List<Transaction> getMemberHistory(int memberId) throws SQLException {
        List<Transaction> list = new ArrayList<>();
        String sql = """
            SELECT t.transaction_id, t.book_id, t.member_id, t.issue_date, t.due_date,
                   t.return_date, t.fine_amount, t.status, b.title AS book_title, m.name AS member_name
            FROM transactions t
            JOIN books b ON t.book_id = b.book_id
            JOIN members m ON t.member_id = m.member_id
            WHERE t.member_id = ?
            ORDER BY t.transaction_id DESC
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, memberId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToTransaction(rs));
                }
            }
        }
        return list;
    }

    public List<Transaction> getAllActiveTransactions() throws SQLException {
        List<Transaction> list = new ArrayList<>();
        String sql = """
            SELECT t.transaction_id, t.book_id, t.member_id, t.issue_date, t.due_date,
                   t.return_date, t.fine_amount, t.status, b.title AS book_title, m.name AS member_name
            FROM transactions t
            JOIN books b ON t.book_id = b.book_id
            JOIN members m ON t.member_id = m.member_id
            WHERE t.status = ?
            ORDER BY t.due_date ASC
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, Transaction.STATUS_ISSUED);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToTransaction(rs));
                }
            }
        }
        return list;
    }

    public List<Transaction> getOverdueTransactions(LocalDate currentDate) throws SQLException {
        List<Transaction> list = new ArrayList<>();
        String sql = """
            SELECT t.transaction_id, t.book_id, t.member_id, t.issue_date, t.due_date,
                   t.return_date, t.fine_amount, t.status, b.title AS book_title, m.name AS member_name
            FROM transactions t
            JOIN books b ON t.book_id = b.book_id
            JOIN members m ON t.member_id = m.member_id
            WHERE t.status = ? AND t.due_date < ?
            ORDER BY t.due_date ASC
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, Transaction.STATUS_ISSUED);
            pstmt.setString(2, DateUtil.format(currentDate));
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToTransaction(rs));
                }
            }
        }
        return list;
    }

    public List<Map<String, Object>> getMostBorrowedBooks(int limit) throws SQLException {
        List<Map<String, Object>> results = new ArrayList<>();
        String sql = """
            SELECT b.book_id, b.title, b.author, b.isbn, COUNT(t.transaction_id) AS borrow_count
            FROM books b
            LEFT JOIN transactions t ON b.book_id = t.book_id
            GROUP BY b.book_id, b.title, b.author, b.isbn
            ORDER BY borrow_count DESC, b.book_id ASC
            LIMIT ?
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("bookId", rs.getInt("book_id"));
                    map.put("title", rs.getString("title"));
                    map.put("author", rs.getString("author"));
                    map.put("isbn", rs.getString("isbn"));
                    map.put("borrowCount", rs.getInt("borrow_count"));
                    results.add(map);
                }
            }
        }
        return results;
    }

    public Map<String, Double> getMemberWiseFines() throws SQLException {
        Map<String, Double> fines = new LinkedHashMap<>();
        String sql = """
            SELECT m.member_id, m.name, SUM(t.fine_amount) AS total_fine
            FROM members m
            JOIN transactions t ON m.member_id = t.member_id
            GROUP BY m.member_id, m.name
            HAVING total_fine > 0
            ORDER BY total_fine DESC
        """;

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                fines.put(String.format("[%d] %s", rs.getInt("member_id"), rs.getString("name")),
                          rs.getDouble("total_fine"));
            }
        }
        return fines;
    }

    public Map<String, Object> getLibraryStatistics() throws SQLException {
        Map<String, Object> stats = new LinkedHashMap<>();
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            // Book stats
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*), COALESCE(SUM(total_copies), 0), COALESCE(SUM(available_copies), 0) FROM books")) {
                if (rs.next()) {
                    int totalTitles = rs.getInt(1);
                    int totalCopies = rs.getInt(2);
                    int availableCopies = rs.getInt(3);
                    stats.put("Total Books (Titles)", totalTitles);
                    stats.put("Total Copies", totalCopies);
                    stats.put("Available Copies", availableCopies);
                    stats.put("Issued Copies", totalCopies - availableCopies);
                }
            }

            // Member stats
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM members")) {
                if (rs.next()) {
                    stats.put("Total Members", rs.getInt(1));
                }
            }

            // Transaction stats
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM transactions WHERE status = 'ISSUED'")) {
                if (rs.next()) {
                    stats.put("Active Transactions", rs.getInt(1));
                }
            }

            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*), COALESCE(SUM(fine_amount), 0.0) FROM transactions WHERE status = 'RETURNED'")) {
                if (rs.next()) {
                    stats.put("Returned Transactions", rs.getInt(1));
                    stats.put("Total Fines Collected", rs.getDouble(2));
                }
            }
        }
        return stats;
    }

    private Transaction mapRowToTransaction(ResultSet rs) throws SQLException {
        Transaction tx = new Transaction(
                rs.getInt("transaction_id"),
                rs.getInt("book_id"),
                rs.getInt("member_id"),
                DateUtil.parse(rs.getString("issue_date")),
                DateUtil.parse(rs.getString("due_date")),
                DateUtil.parse(rs.getString("return_date")),
                rs.getDouble("fine_amount"),
                rs.getString("status")
        );

        // Populate optional labels if present in projection
        try {
            tx.setBookTitle(rs.getString("book_title"));
        } catch (SQLException ignored) {}

        try {
            tx.setMemberName(rs.getString("member_name"));
        } catch (SQLException ignored) {}

        return tx;
    }
}
