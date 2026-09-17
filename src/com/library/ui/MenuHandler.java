package com.library.ui;

import com.library.exception.LibraryException;
import com.library.model.Book;
import com.library.model.Faculty;
import com.library.model.Member;
import com.library.model.Student;
import com.library.model.Transaction;
import com.library.service.BookService;
import com.library.service.MemberService;
import com.library.service.TransactionService;
import com.library.util.DateUtil;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Terminal UI controller handling menus, user input parsing, and presentation tables.
 */
public class MenuHandler {

    private final Scanner scanner;
    private final BookService bookService;
    private final MemberService memberService;
    private final TransactionService transactionService;

    public MenuHandler() {
        this.scanner = new Scanner(System.in);
        this.bookService = new BookService();
        this.memberService = new MemberService();
        this.transactionService = new TransactionService();
    }

    public void start() {
        boolean running = true;
        while (running) {
            printMainMenu();
            int choice = readIntChoice("Enter your choice: ", 1, 6);
            System.out.println();

            switch (choice) {
                case 1 -> handleBookMenu();
                case 2 -> handleMemberMenu();
                case 3 -> handleIssueBook();
                case 4 -> handleReturnBook();
                case 5 -> handleReportMenu();
                case 6 -> {
                    System.out.println("Thank you for using Smart Library Management System. Goodbye!");
                    running = false;
                }
                default -> System.out.println("Invalid choice. Please enter a number between 1 and 6.");
            }
            System.out.println();
        }
    }

    private void printMainMenu() {
        System.out.println("==================================================");
        System.out.println("        SMART LIBRARY MANAGEMENT SYSTEM           ");
        System.out.println("==================================================");
        System.out.println("1. Book Management");
        System.out.println("2. Member Management");
        System.out.println("3. Issue Book");
        System.out.println("4. Return Book");
        System.out.println("5. Reports & Analytics");
        System.out.println("6. Exit");
    }

    // ==========================================================
    // MODULE 1: BOOK MANAGEMENT
    // ==========================================================
    private void handleBookMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("========== BOOK MANAGEMENT ==========");
            System.out.println("1. Add Book");
            System.out.println("2. Update Book");
            System.out.println("3. Delete Book");
            System.out.println("4. Search Book");
            System.out.println("5. View All Books");
            System.out.println("6. Back");

            int choice = readIntChoice("Enter your choice: ", 1, 6);
            System.out.println();

            switch (choice) {
                case 1 -> addBook();
                case 2 -> updateBook();
                case 3 -> deleteBook();
                case 4 -> searchBooks();
                case 5 -> viewAllBooks();
                case 6 -> back = true;
            }
            System.out.println();
        }
    }

    private void addBook() {
        System.out.println("--- Add New Book ---");
        String title = readLine("Enter Title: ");
        String author = readLine("Enter Author: ");
        String isbn = readLine("Enter ISBN (10 or 13 digits): ");
        String genre = readLine("Enter Genre: ");
        int totalCopies = readInt("Enter Total Copies: ", 1, 10000);

        try {
            Book book = bookService.addBook(title, author, isbn, genre, totalCopies);
            System.out.println("SUCCESS: Book added successfully! Assigned Book ID: " + book.getBookId());
        } catch (LibraryException e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }

    private void updateBook() {
        System.out.println("--- Update Existing Book ---");
        int bookId = readInt("Enter Book ID to update: ", 1, Integer.MAX_VALUE);
        try {
            Book existing = bookService.getBookById(bookId);
            System.out.println("Current details: " + existing);

            String title = readLine("Enter New Title [" + existing.getTitle() + "]: ");
            if (title.isEmpty()) title = existing.getTitle();

            String author = readLine("Enter New Author [" + existing.getAuthor() + "]: ");
            if (author.isEmpty()) author = existing.getAuthor();

            String isbn = readLine("Enter New ISBN [" + existing.getIsbn() + "]: ");
            if (isbn.isEmpty()) isbn = existing.getIsbn();

            String genre = readLine("Enter New Genre [" + existing.getGenre() + "]: ");
            if (genre.isEmpty()) genre = existing.getGenre();

            int totalCopies = readInt("Enter New Total Copies [" + existing.getTotalCopies() + "]: ", 1, 10000);

            boolean success = bookService.updateBook(bookId, title, author, isbn, genre, totalCopies);
            if (success) {
                System.out.println("SUCCESS: Book ID " + bookId + " updated successfully.");
            } else {
                System.out.println("ERROR: Book update failed.");
            }
        } catch (LibraryException e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }

    private void deleteBook() {
        System.out.println("--- Delete Book ---");
        int bookId = readInt("Enter Book ID to delete: ", 1, Integer.MAX_VALUE);
        try {
            boolean success = bookService.deleteBook(bookId);
            if (success) {
                System.out.println("SUCCESS: Book ID " + bookId + " deleted successfully.");
            } else {
                System.out.println("ERROR: Failed to delete book.");
            }
        } catch (LibraryException e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }

    private void searchBooks() {
        System.out.println("--- Search Books ---");
        String keyword = readLine("Enter search term (Title, Author, ISBN, Genre, or ID): ");
        try {
            List<Book> books = bookService.searchBooks(keyword);
            if (books.isEmpty()) {
                System.out.println("No books matched the search keyword: '" + keyword + "'");
            } else {
                renderBookTable(books);
            }
        } catch (LibraryException e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }

    private void viewAllBooks() {
        try {
            List<Book> books = bookService.getAllBooks();
            if (books.isEmpty()) {
                System.out.println("No books found in the library catalog.");
            } else {
                renderBookTable(books);
            }
        } catch (LibraryException e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }

    // ==========================================================
    // MODULE 2: MEMBER MANAGEMENT
    // ==========================================================
    private void handleMemberMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("========== MEMBER MANAGEMENT ==========");
            System.out.println("1. Register Member");
            System.out.println("2. Update Member");
            System.out.println("3. Delete Member");
            System.out.println("4. Search Member");
            System.out.println("5. View All Members");
            System.out.println("6. View Member History");
            System.out.println("7. Back");

            int choice = readIntChoice("Enter your choice: ", 1, 7);
            System.out.println();

            switch (choice) {
                case 1 -> registerMember();
                case 2 -> updateMember();
                case 3 -> deleteMember();
                case 4 -> searchMembers();
                case 5 -> viewAllMembers();
                case 6 -> viewMemberHistory();
                case 7 -> back = true;
            }
            System.out.println();
        }
    }

    private void registerMember() {
        System.out.println("--- Register New Member ---");
        String name = readLine("Enter Member Name: ");
        String email = readLine("Enter Email Address: ");
        System.out.println("Select Member Type:");
        System.out.println("  1. STUDENT (Limit: " + Student.STUDENT_MAX_BOOKS + " books, " + Student.STUDENT_LOAN_PERIOD_DAYS + " days)");
        System.out.println("  2. FACULTY (Limit: " + Faculty.FACULTY_MAX_BOOKS + " books, " + Faculty.FACULTY_LOAN_PERIOD_DAYS + " days)");
        int typeChoice = readIntChoice("Choice (1 or 2): ", 1, 2);
        String typeStr = (typeChoice == 1) ? "STUDENT" : "FACULTY";

        try {
            Member member = memberService.registerMember(name, email, typeStr);
            System.out.println("SUCCESS: Member registered successfully!");
            System.out.println("Assigned Member ID : " + member.getMemberId());
            System.out.println("Member Type        : " + member.getMemberType());
            System.out.println("Max Books Allowed  : " + member.getMaxBooksAllowed());
            System.out.println("Standard Loan Days : " + member.getLoanPeriodDays());
        } catch (LibraryException e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }

    private void updateMember() {
        System.out.println("--- Update Member Details ---");
        int memberId = readInt("Enter Member ID to update: ", 1, Integer.MAX_VALUE);
        try {
            Member existing = memberService.getMemberById(memberId);
            System.out.println("Current details: " + existing);

            String name = readLine("Enter New Name [" + existing.getName() + "]: ");
            if (name.isEmpty()) name = existing.getName();

            String email = readLine("Enter New Email [" + existing.getEmail() + "]: ");
            if (email.isEmpty()) email = existing.getEmail();

            boolean success = memberService.updateMember(memberId, name, email);
            if (success) {
                System.out.println("SUCCESS: Member ID " + memberId + " updated successfully.");
            } else {
                System.out.println("ERROR: Member update failed.");
            }
        } catch (LibraryException e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }

    private void deleteMember() {
        System.out.println("--- Delete Member ---");
        int memberId = readInt("Enter Member ID to delete: ", 1, Integer.MAX_VALUE);
        try {
            boolean success = memberService.deleteMember(memberId);
            if (success) {
                System.out.println("SUCCESS: Member ID " + memberId + " deleted successfully.");
            } else {
                System.out.println("ERROR: Failed to delete member.");
            }
        } catch (LibraryException e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }

    private void searchMembers() {
        System.out.println("--- Search Members ---");
        String keyword = readLine("Enter search term (Name, Email, or Member ID): ");
        try {
            List<Member> members = memberService.searchMembers(keyword);
            if (members.isEmpty()) {
                System.out.println("No members matched keyword: '" + keyword + "'");
            } else {
                renderMemberTable(members);
            }
        } catch (LibraryException e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }

    private void viewAllMembers() {
        try {
            List<Member> members = memberService.getAllMembers();
            if (members.isEmpty()) {
                System.out.println("No members registered in the system.");
            } else {
                renderMemberTable(members);
            }
        } catch (LibraryException e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }

    // ==========================================================
    // MODULE 3: ISSUE & RETURN
    // ==========================================================
    private void handleIssueBook() {
        System.out.println("========== ISSUE BOOK ==========");
        int memberId = readInt("Enter Member ID: ", 1, Integer.MAX_VALUE);
        int bookId = readInt("Enter Book ID: ", 1, Integer.MAX_VALUE);

        try {
            Transaction tx = transactionService.issueBook(memberId, bookId);
            System.out.println();
            System.out.println("Book Available : YES");
            System.out.println("Book Title     : " + tx.getBookTitle());
            System.out.println("Issued To      : " + tx.getMemberName());
            System.out.println("Issue Date     : " + DateUtil.format(tx.getIssueDate()));
            System.out.println("Due Date       : " + DateUtil.format(tx.getDueDate()));
            System.out.println();
            System.out.println("SUCCESS: Book issued successfully.");
            System.out.println("Transaction ID : " + tx.getTransactionId());
        } catch (LibraryException e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }

    private void handleReturnBook() {
        System.out.println("========== RETURN BOOK ==========");
        int transactionId = readInt("Enter Transaction ID: ", 1, Integer.MAX_VALUE);

        try {
            Transaction tx = transactionService.returnBook(transactionId);
            long daysLate = DateUtil.calculateOverdueDays(tx.getDueDate(), tx.getReturnDate());

            System.out.println();
            System.out.println("Transaction ID : " + tx.getTransactionId());
            System.out.println("Due Date       : " + DateUtil.format(tx.getDueDate()));
            System.out.println("Return Date    : " + DateUtil.format(tx.getReturnDate()));
            System.out.println("Days Late      : " + daysLate);
            System.out.printf("Fine           : Rs. %.2f%n", tx.getFineAmount());
            System.out.println();
            System.out.println("SUCCESS: Book returned successfully.");
        } catch (LibraryException e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }

    // ==========================================================
    // MODULE 4: REPORTS & ANALYTICS
    // ==========================================================
    private void handleReportMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("========== LIBRARY REPORTS ==========");
            System.out.println("1. Overdue Books");
            System.out.println("2. Currently Issued Books");
            System.out.println("3. Most Borrowed Books");
            System.out.println("4. Member History");
            System.out.println("5. Fine Summary");
            System.out.println("6. Library Statistics");
            System.out.println("7. Back");

            int choice = readIntChoice("Enter your choice: ", 1, 7);
            System.out.println();

            switch (choice) {
                case 1 -> viewOverdueBooks();
                case 2 -> viewIssuedBooks();
                case 3 -> viewMostBorrowedBooks();
                case 4 -> viewMemberHistory();
                case 5 -> viewFineSummary();
                case 6 -> viewLibraryStatistics();
                case 7 -> back = true;
            }
            System.out.println();
        }
    }

    private void viewOverdueBooks() {
        System.out.println("--- Overdue Books ---");
        try {
            List<Transaction> list = transactionService.getOverdueBooks();
            if (list.isEmpty()) {
                System.out.println("Great! There are currently no overdue books.");
            } else {
                renderTransactionTable(list);
            }
        } catch (LibraryException e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }

    private void viewIssuedBooks() {
        System.out.println("--- Currently Issued Books ---");
        try {
            List<Transaction> list = transactionService.getCurrentlyIssuedBooks();
            if (list.isEmpty()) {
                System.out.println("No books are currently issued.");
            } else {
                renderTransactionTable(list);
            }
        } catch (LibraryException e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }

    private void viewMostBorrowedBooks() {
        System.out.println("--- Most Borrowed Books (Top 5) ---");
        try {
            List<Map<String, Object>> topBooks = transactionService.getMostBorrowedBooks(5);
            if (topBooks.isEmpty()) {
                System.out.println("No book borrow records found.");
            } else {
                System.out.printf("%-8s | %-32s | %-22s | %-15s | %-12s%n", "Book ID", "Title", "Author", "ISBN", "Borrow Count");
                System.out.println("-".repeat(100));
                for (Map<String, Object> map : topBooks) {
                    System.out.printf("%-8s | %-32s | %-22s | %-15s | %-12s%n",
                            map.get("bookId"),
                            truncate((String) map.get("title"), 32),
                            truncate((String) map.get("author"), 22),
                            map.get("isbn"),
                            map.get("borrowCount"));
                }
            }
        } catch (LibraryException e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }

    private void viewMemberHistory() {
        System.out.println("--- Member-wise Borrowing History ---");
        int memberId = readInt("Enter Member ID: ", 1, Integer.MAX_VALUE);
        try {
            List<Transaction> history = transactionService.getMemberHistory(memberId);
            if (history.isEmpty()) {
                System.out.println("No transaction history found for Member ID " + memberId);
            } else {
                renderTransactionTable(history);
            }
        } catch (LibraryException e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }

    private void viewFineSummary() {
        System.out.println("--- Member Fine Summary ---");
        try {
            Map<String, Double> fineMap = transactionService.getMemberWiseFines();
            if (fineMap.isEmpty()) {
                System.out.println("Zero pending or collected fines across all members.");
            } else {
                System.out.printf("%-35s | %-15s%n", "Member [ID] Name", "Total Fines");
                System.out.println("-".repeat(55));
                double grandTotal = 0.0;
                for (Map.Entry<String, Double> entry : fineMap.entrySet()) {
                    System.out.printf("%-35s | Rs. %-10.2f%n", entry.getKey(), entry.getValue());
                    grandTotal += entry.getValue();
                }
                System.out.println("-".repeat(55));
                System.out.printf("%-35s | Rs. %-10.2f%n", "GRAND TOTAL", grandTotal);
            }
        } catch (LibraryException e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }

    private void viewLibraryStatistics() {
        System.out.println("--- Overall Library Statistics ---");
        try {
            Map<String, Object> stats = transactionService.getLibraryStatistics();
            System.out.println("==================================================");
            for (Map.Entry<String, Object> entry : stats.entrySet()) {
                if (entry.getValue() instanceof Double) {
                    System.out.printf("%-26s : Rs. %.2f%n", entry.getKey(), entry.getValue());
                } else {
                    System.out.printf("%-26s : %s%n", entry.getKey(), entry.getValue());
                }
            }
            System.out.println("==================================================");
        } catch (LibraryException e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }

    // ==========================================================
    // PRESENTATION TABLES
    // ==========================================================
    private void renderBookTable(List<Book> books) {
        System.out.printf("%-6s | %-30s | %-20s | %-15s | %-15s | %-8s%n",
                "ID", "Title", "Author", "ISBN", "Genre", "Copies");
        System.out.println("-".repeat(105));
        for (Book b : books) {
            System.out.printf("%-6d | %-30s | %-20s | %-15s | %-15s | %d/%d%n",
                    b.getBookId(),
                    truncate(b.getTitle(), 30),
                    truncate(b.getAuthor(), 20),
                    b.getIsbn(),
                    truncate(b.getGenre(), 15),
                    b.getAvailableCopies(),
                    b.getTotalCopies());
        }
    }

    private void renderMemberTable(List<Member> members) {
        System.out.printf("%-6s | %-24s | %-28s | %-10s | %-9s | %-12s%n",
                "ID", "Name", "Email", "Type", "Max Books", "Registered");
        System.out.println("-".repeat(100));
        for (Member m : members) {
            System.out.printf("%-6d | %-24s | %-28s | %-10s | %-9d | %-12s%n",
                    m.getMemberId(),
                    truncate(m.getName(), 24),
                    truncate(m.getEmail(), 28),
                    m.getMemberType(),
                    m.getMaxBooksAllowed(),
                    DateUtil.format(m.getRegisteredOn()));
        }
    }

    private void renderTransactionTable(List<Transaction> transactions) {
        System.out.printf("%-6s | %-24s | %-18s | %-10s | %-10s | %-10s | %-9s | %-8s%n",
                "TX ID", "Book Title", "Member", "Issue Date", "Due Date", "Returned", "Status", "Fine");
        System.out.println("-".repeat(110));
        for (Transaction tx : transactions) {
            System.out.printf("%-6d | %-24s | %-18s | %-10s | %-10s | %-10s | %-9s | Rs. %-7.2f%n",
                    tx.getTransactionId(),
                    truncate(tx.getBookTitle() != null ? tx.getBookTitle() : ("Book #" + tx.getBookId()), 24),
                    truncate(tx.getMemberName() != null ? tx.getMemberName() : ("Member #" + tx.getMemberId()), 18),
                    DateUtil.format(tx.getIssueDate()),
                    DateUtil.format(tx.getDueDate()),
                    tx.getReturnDate() != null ? DateUtil.format(tx.getReturnDate()) : "N/A",
                    tx.getStatus(),
                    tx.getFineAmount());
        }
    }

    private String truncate(String val, int maxLen) {
        if (val == null) return "";
        if (val.length() <= maxLen) return val;
        return val.substring(0, maxLen - 3) + "...";
    }

    // ==========================================================
    // SAFE INPUT HELPERS
    // ==========================================================
    private String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private int readInt(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                int val = Integer.parseInt(input);
                if (val >= min && val <= max) {
                    return val;
                }
                System.out.printf("Please enter a value between %d and %d.%n", min, max);
            } catch (NumberFormatException e) {
                System.out.println("Invalid numeric input. Please enter a valid integer.");
            }
        }
    }

    private int readIntChoice(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                int val = Integer.parseInt(input);
                if (val >= min && val <= max) {
                    return val;
                }
                System.out.printf("Invalid choice. Please enter a number between %d and %d.%n", min, max);
            } catch (NumberFormatException e) {
                System.out.printf("Invalid choice. Please enter a number between %d and %d.%n", min, max);
            }
        }
    }
}
