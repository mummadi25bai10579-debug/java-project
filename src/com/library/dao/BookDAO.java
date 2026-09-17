package com.library.dao;

import com.library.interfaces.Searchable;
import com.library.model.Book;
import com.library.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Book entities.
 * Implements Searchable interface for polymorphic catalog queries.
 */
public class BookDAO implements Searchable<Book> {

    public boolean addBook(Book book) throws SQLException {
        String sql = "INSERT INTO books (title, author, isbn, genre, total_copies, available_copies) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, book.getTitle());
            pstmt.setString(2, book.getAuthor());
            pstmt.setString(3, book.getIsbn());
            pstmt.setString(4, book.getGenre());
            pstmt.setInt(5, book.getTotalCopies());
            pstmt.setInt(6, book.getAvailableCopies());

            int affected = pstmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        book.setBookId(rs.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    public boolean updateBook(Book book) throws SQLException {
        String sql = "UPDATE books SET title = ?, author = ?, isbn = ?, genre = ?, total_copies = ?, available_copies = ? WHERE book_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, book.getTitle());
            pstmt.setString(2, book.getAuthor());
            pstmt.setString(3, book.getIsbn());
            pstmt.setString(4, book.getGenre());
            pstmt.setInt(5, book.getTotalCopies());
            pstmt.setInt(6, book.getAvailableCopies());
            pstmt.setInt(7, book.getBookId());

            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean deleteBook(int bookId) throws SQLException {
        String sql = "DELETE FROM books WHERE book_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, bookId);
            return pstmt.executeUpdate() > 0;
        }
    }

    public Book findById(int bookId) throws SQLException {
        String sql = "SELECT book_id, title, author, isbn, genre, total_copies, available_copies FROM books WHERE book_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, bookId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToBook(rs);
                }
            }
        }
        return null;
    }

    public Book findByIsbn(String isbn) throws SQLException {
        String sql = "SELECT book_id, title, author, isbn, genre, total_copies, available_copies FROM books WHERE isbn = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, isbn.trim());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToBook(rs);
                }
            }
        }
        return null;
    }

    public List<Book> getAllBooks() throws SQLException {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT book_id, title, author, isbn, genre, total_copies, available_copies FROM books ORDER BY book_id ASC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                books.add(mapRowToBook(rs));
            }
        }
        return books;
    }

    @Override
    public List<Book> search(String keyword) {
        List<Book> results = new ArrayList<>();
        String sql = """
            SELECT book_id, title, author, isbn, genre, total_copies, available_copies
            FROM books
            WHERE LOWER(title) LIKE ?
               OR LOWER(author) LIKE ?
               OR LOWER(isbn) LIKE ?
               OR LOWER(genre) LIKE ?
               OR CAST(book_id AS TEXT) = ?
            ORDER BY book_id ASC
        """;

        String pattern = "%" + keyword.toLowerCase().trim() + "%";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, pattern);
            pstmt.setString(2, pattern);
            pstmt.setString(3, pattern);
            pstmt.setString(4, pattern);
            pstmt.setString(5, keyword.trim());

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRowToBook(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error during book search: " + e.getMessage());
        }
        return results;
    }

    /**
     * Atomically adjusts available copies by delta (+1 or -1) using a supplied active connection.
     */
    public boolean updateAvailableCopies(int bookId, int delta, Connection conn) throws SQLException {
        String sql = "UPDATE books SET available_copies = available_copies + ? WHERE book_id = ? AND (available_copies + ?) >= 0 AND (available_copies + ?) <= total_copies";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, delta);
            pstmt.setInt(2, bookId);
            pstmt.setInt(3, delta);
            pstmt.setInt(4, delta);
            return pstmt.executeUpdate() > 0;
        }
    }

    private Book mapRowToBook(ResultSet rs) throws SQLException {
        return new Book(
                rs.getInt("book_id"),
                rs.getString("title"),
                rs.getString("author"),
                rs.getString("isbn"),
                rs.getString("genre"),
                rs.getInt("total_copies"),
                rs.getInt("available_copies")
        );
    }
}
