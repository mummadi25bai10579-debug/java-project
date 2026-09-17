package com.library.model;

import java.time.LocalDate;

/**
 * Represents a book borrowing/return transaction record.
 */
public class Transaction {
    public static final String STATUS_ISSUED = "ISSUED";
    public static final String STATUS_RETURNED = "RETURNED";

    private int transactionId;
    private int bookId;
    private int memberId;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private double fineAmount;
    private String status;

    // Optional metadata fields populated during report joins
    private String bookTitle;
    private String memberName;

    public Transaction() {
        this.status = STATUS_ISSUED;
        this.fineAmount = 0.0;
    }

    public Transaction(int transactionId, int bookId, int memberId, LocalDate issueDate,
                       LocalDate dueDate, LocalDate returnDate, double fineAmount, String status) {
        this.transactionId = transactionId;
        this.bookId = bookId;
        this.memberId = memberId;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.returnDate = returnDate;
        this.fineAmount = fineAmount;
        this.status = status != null ? status : STATUS_ISSUED;
    }

    public Transaction(int bookId, int memberId, LocalDate issueDate, LocalDate dueDate) {
        this(0, bookId, memberId, issueDate, dueDate, null, 0.0, STATUS_ISSUED);
    }

    public int getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }

    public int getBookId() {
        return bookId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    public int getMemberId() {
        return memberId;
    }

    public void setMemberId(int memberId) {
        this.memberId = memberId;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public double getFineAmount() {
        return fineAmount;
    }

    public void setFineAmount(double fineAmount) {
        this.fineAmount = fineAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    @Override
    public String toString() {
        return String.format("Transaction [ID=%d, BookID=%d, MemberID=%d, Issued=%s, Due=%s, Returned=%s, Fine=₹%.2f, Status=%s]",
                transactionId, bookId, memberId, issueDate, dueDate,
                (returnDate != null ? returnDate.toString() : "N/A"), fineAmount, status);
    }
}
