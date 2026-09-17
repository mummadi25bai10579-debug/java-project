package com.library.service;

import com.library.dao.BookDAO;
import com.library.exception.InvalidInputException;
import com.library.exception.LibraryException;
import com.library.model.Book;
import com.library.util.InputValidator;

import java.sql.SQLException;
import java.util.List;

/**
 * Service handling book catalog business logic and validation.
 */
public class BookService {

    private final BookDAO bookDAO;

    public BookService() {
        this.bookDAO = new BookDAO();
    }

    public BookService(BookDAO bookDAO) {
        this.bookDAO = bookDAO;
    }

    public Book addBook(String title, String author, String isbn, String genre, int totalCopies) throws LibraryException {
        InputValidator.validateNonEmpty(title, "Title");
        InputValidator.validateNonEmpty(author, "Author");
        InputValidator.validateIsbn(isbn);
        InputValidator.validateNonEmpty(genre, "Genre");
        InputValidator.validatePositiveInt(totalCopies, "Total Copies");

        try {
            Book existing = bookDAO.findByIsbn(isbn);
            if (existing != null) {
                throw new InvalidInputException("A book with ISBN '" + isbn + "' already exists (ID: " + existing.getBookId() + ").");
            }

            Book book = new Book(title.trim(), author.trim(), isbn.trim(), genre.trim(), totalCopies);
            boolean created = bookDAO.addBook(book);
            if (!created) {
                throw new LibraryException("Failed to save book to the database.");
            }
            return book;
        } catch (SQLException e) {
            throw new LibraryException("Database error adding book: " + e.getMessage(), e);
        }
    }

    public boolean updateBook(int bookId, String title, String author, String isbn, String genre, int totalCopies) throws LibraryException {
        InputValidator.validatePositiveInt(bookId, "Book ID");
        InputValidator.validateNonEmpty(title, "Title");
        InputValidator.validateNonEmpty(author, "Author");
        InputValidator.validateIsbn(isbn);
        InputValidator.validateNonEmpty(genre, "Genre");
        InputValidator.validatePositiveInt(totalCopies, "Total Copies");

        try {
            Book existing = bookDAO.findById(bookId);
            if (existing == null) {
                throw new LibraryException("Book ID " + bookId + " does not exist.");
            }

            // Check duplicate ISBN on different book
            Book isbnOwner = bookDAO.findByIsbn(isbn);
            if (isbnOwner != null && isbnOwner.getBookId() != bookId) {
                throw new InvalidInputException("ISBN '" + isbn + "' is already assigned to book ID " + isbnOwner.getBookId());
            }

            int currentlyIssued = existing.getTotalCopies() - existing.getAvailableCopies();
            if (totalCopies < currentlyIssued) {
                throw new InvalidInputException("Cannot reduce total copies to " + totalCopies +
                        " because " + currentlyIssued + " copies are currently issued.");
            }

            int newAvailable = totalCopies - currentlyIssued;
            existing.setTitle(title.trim());
            existing.setAuthor(author.trim());
            existing.setIsbn(isbn.trim());
            existing.setGenre(genre.trim());
            existing.setTotalCopies(totalCopies);
            existing.setAvailableCopies(newAvailable);

            return bookDAO.updateBook(existing);
        } catch (SQLException e) {
            throw new LibraryException("Database error updating book: " + e.getMessage(), e);
        }
    }

    public boolean deleteBook(int bookId) throws LibraryException {
        InputValidator.validatePositiveInt(bookId, "Book ID");
        try {
            Book existing = bookDAO.findById(bookId);
            if (existing == null) {
                throw new LibraryException("Book ID " + bookId + " does not exist.");
            }

            int currentlyIssued = existing.getTotalCopies() - existing.getAvailableCopies();
            if (currentlyIssued > 0) {
                throw new LibraryException("Cannot delete book: " + currentlyIssued + " copies are currently issued to members.");
            }

            return bookDAO.deleteBook(bookId);
        } catch (SQLException e) {
            throw new LibraryException("Database error deleting book: " + e.getMessage(), e);
        }
    }

    public Book getBookById(int bookId) throws LibraryException {
        InputValidator.validatePositiveInt(bookId, "Book ID");
        try {
            Book book = bookDAO.findById(bookId);
            if (book == null) {
                throw new LibraryException("Book ID " + bookId + " does not exist.");
            }
            return book;
        } catch (SQLException e) {
            throw new LibraryException("Database error retrieving book: " + e.getMessage(), e);
        }
    }

    public List<Book> getAllBooks() throws LibraryException {
        try {
            return bookDAO.getAllBooks();
        } catch (SQLException e) {
            throw new LibraryException("Database error fetching books: " + e.getMessage(), e);
        }
    }

    public List<Book> searchBooks(String keyword) throws LibraryException {
        InputValidator.validateNonEmpty(keyword, "Search keyword");
        return bookDAO.search(keyword);
    }
}
