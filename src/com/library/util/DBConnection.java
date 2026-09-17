package com.library.util;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Manages SQLite JDBC database connections and automated schema initialization.
 */
public final class DBConnection {

    private static final String DEFAULT_DB_PATH = "data/library.db";
    private static String currentDbPath = DEFAULT_DB_PATH;

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("FATAL: SQLite JDBC Driver not found in classpath. " + e.getMessage());
        }
    }

    private DBConnection() {
        // Prevent instantiation
    }

    public static synchronized void setDatabasePath(String dbPath) {
        currentDbPath = dbPath;
    }

    public static String getDatabasePath() {
        return currentDbPath;
    }

    /**
     * Obtains a new database connection with foreign keys enabled.
     */
    public static Connection getConnection() throws SQLException {
        File dbFile = new File(currentDbPath);
        File parentDir = dbFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        String url = "jdbc:sqlite:" + currentDbPath;
        Connection conn = DriverManager.getConnection(url);

        // Always enforce foreign key constraints in SQLite
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
        }
        return conn;
    }

    /**
     * Automatically initializes database tables and indexes if they do not already exist.
     */
    public static void initializeDatabase() throws SQLException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // Table: books
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS books (
                    book_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    title TEXT NOT NULL,
                    author TEXT NOT NULL,
                    isbn TEXT NOT NULL UNIQUE,
                    genre TEXT NOT NULL,
                    total_copies INTEGER NOT NULL CHECK(total_copies >= 0),
                    available_copies INTEGER NOT NULL CHECK(available_copies >= 0 AND available_copies <= total_copies)
                );
            """);

            // Table: members
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS members (
                    member_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    email TEXT NOT NULL UNIQUE,
                    member_type TEXT NOT NULL CHECK(member_type IN ('STUDENT', 'FACULTY')),
                    max_books INTEGER NOT NULL CHECK(max_books > 0),
                    registered_on TEXT NOT NULL
                );
            """);

            // Table: transactions
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS transactions (
                    transaction_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    book_id INTEGER NOT NULL,
                    member_id INTEGER NOT NULL,
                    issue_date TEXT NOT NULL,
                    due_date TEXT NOT NULL,
                    return_date TEXT,
                    fine_amount REAL DEFAULT 0.0,
                    status TEXT NOT NULL CHECK(status IN ('ISSUED', 'RETURNED')),
                    FOREIGN KEY (book_id) REFERENCES books(book_id) ON DELETE RESTRICT,
                    FOREIGN KEY (member_id) REFERENCES members(member_id) ON DELETE RESTRICT
                );
            """);

            // Indexes
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_books_isbn ON books(isbn);");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_books_title ON books(title);");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_members_email ON members(email);");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_transactions_member_status ON transactions(member_id, status);");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_transactions_book_status ON transactions(book_id, status);");
        }
    }
}
