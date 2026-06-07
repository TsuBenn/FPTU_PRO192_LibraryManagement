package models;

import utilities.IDManager;

import java.time.LocalDate;

public class BorrowTransaction {

    private final String transactionId;
    private String memberId;
    private String bookId;
    private LocalDate borrowDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private double finePaid;

    public BorrowTransaction(String memberId, String bookId, LocalDate borrowDate) {
        transactionId = IDManager.transactionIDGenerator.newID();
        this.memberId = memberId;
        this.bookId = bookId;
        this.borrowDate = borrowDate;
        this.dueDate = borrowDate.plusDays(14); // Default 14-day loan period
        this.returnDate = null; // null means it's currently outstanding
        this.finePaid = 0.0;
    }

    // Getters and Setters
    public String getTransactionId() { return transactionId; }
    public String getMemberId() { return memberId; }
    public String getBookId() { return bookId; }
    public LocalDate getBorrowDate() { return borrowDate; }
    public LocalDate getDueDate() { return dueDate; }
    public LocalDate getReturnDate() { return returnDate; }
    public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }
    public double getFinePaid() { return finePaid; }
    public void setFinePaid(double finePaid) { this.finePaid = finePaid; }
    public void setDueDate(LocalDate dueDate) {
        if (dueDate.isBefore(borrowDate))
            return;
        this.dueDate = dueDate;
    }

}
