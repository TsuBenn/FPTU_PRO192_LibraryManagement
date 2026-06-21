package models;

import utilities.IDManager;
import java.time.LocalDate;

public class BorrowTransaction {
    private final String transactionId;
    private final String memberId;
    private final String memberName;
    private final String bookId;
    private final String bookTitle;
    private TransactionStatus transactionStatus;
    private final LocalDate borrowDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private double finePaid;

    public BorrowTransaction(String memberId, String memberName,
                             String bookId, String bookTitle,
                             LocalDate borrowDate) {
        this.transactionId = IDManager.transactionIDGenerator.newID();
        this.memberId = memberId;
        this.memberName = memberName;
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.borrowDate = borrowDate;
        this.dueDate = borrowDate.plusDays(14);
        this.returnDate = null;
    }

    public String getTransactionId() { return transactionId; }
    public String getMemberId() { return memberId; }
    public String getMemberName() { return memberName; }
    public String getBookId() { return bookId; }
    public String getBookTitle() { return bookTitle; }
    public LocalDate getBorrowDate() { return borrowDate; }
    public LocalDate getDueDate() { return dueDate; }
    public LocalDate getReturnDate() { return returnDate; }
    public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }

    public TransactionStatus getTransactionStatus() {
        return transactionStatus;
    }

    public void setTransactionStatus(TransactionStatus transactionStatus) {
        this.transactionStatus = transactionStatus;
    }

    public void setDueDate(LocalDate dueDate) {
        if (dueDate.isBefore(borrowDate)) return;
        this.dueDate = dueDate;
    }

    @Override
    public String toString() {
        String returnDateStr = (returnDate == null) ? "OUT_ON_LOAN" : returnDate.toString();
        return transactionId + "|" + memberId + "|" + bookId + "|" + borrowDate + "|" + dueDate
                + "|" + returnDateStr + "|" + String.format("%.0f", finePaid );
    }

    public void setFinePaid(double finePaid) {
        if (returnDate != null)
            this.finePaid = finePaid;
    }

    public double getFinePaid() {
        return finePaid;
    }
}
