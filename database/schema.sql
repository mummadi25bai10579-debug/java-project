-- ==========================================================
-- Smart Library Management System Database Schema
-- Database: SQLite 3
-- ==========================================================

PRAGMA foreign_keys = ON;

-- Table: books
CREATE TABLE IF NOT EXISTS books (
    book_id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    author TEXT NOT NULL,
    isbn TEXT NOT NULL UNIQUE,
    genre TEXT NOT NULL,
    total_copies INTEGER NOT NULL CHECK(total_copies >= 0),
    available_copies INTEGER NOT NULL CHECK(available_copies >= 0 AND available_copies <= total_copies)
);

-- Table: members
CREATE TABLE IF NOT EXISTS members (
    member_id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    email TEXT NOT NULL UNIQUE,
    member_type TEXT NOT NULL CHECK(member_type IN ('STUDENT', 'FACULTY')),
    max_books INTEGER NOT NULL CHECK(max_books > 0),
    registered_on TEXT NOT NULL
);

-- Table: transactions
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

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_books_isbn ON books(isbn);
CREATE INDEX IF NOT EXISTS idx_books_title ON books(title);
CREATE INDEX IF NOT EXISTS idx_members_email ON members(email);
CREATE INDEX IF NOT EXISTS idx_transactions_member_status ON transactions(member_id, status);
CREATE INDEX IF NOT EXISTS idx_transactions_book_status ON transactions(book_id, status);
