package models;

import java.time.LocalDate;

public class BorrowTransaction {
    private String transactionId;
    private String memberId;
    private String bookId;
    private LocalDate borrowDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private double finePaid;

    public BorrowTransaction(String transactionId, String memberId, String bookId, LocalDate borrowDate) {
        this.transactionId = transactionId;
        this.memberId = memberId;
        this.bookId = bookId;
        this.borrowDate = borrowDate;
        this.dueDate = borrowDate.plusDays(14); // Standard default loan span
        this.returnDate = null;
        this.finePaid = 0.0;
    }

    // Getters and Setters
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public String getMemberId() { return memberId; }
    public void setMemberId(String memberId) { this.memberId = memberId; }

    public String getBookId() { return bookId; }
    public void setBookId(String bookId) { this.bookId = bookId; }

    public LocalDate getBorrowDate() { return borrowDate; }
    public void setBorrowDate(LocalDate borrowDate) { this.borrowDate = borrowDate; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public LocalDate getReturnDate() { return returnDate; }
    public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }

    public double getFinePaid() { return finePaid; }
    public void setFinePaid(double finePaid) { this.finePaid = finePaid; }
}
