package models;

import java.time.LocalDate;
import java.util.UUID;

public class BorrowTransaction {
    private final String id;
    private final String bookId;    // Lưu reference bằng ID dạng String
    private final String memberId;  // Lưu reference bằng ID dạng String
    private LocalDate borrowDate;
    private LocalDate dueDate;
    private LocalDate returnDate;   // Mặc định null
    private TransactionStatus status; // Lưu trạng thái dạng Enum

    public BorrowTransaction(String tId, String bookId, String memberId, LocalDate borrowDate, LocalDate dueDate) {
        this.id = tId;
        this.bookId = bookId;
        this.memberId = memberId;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.returnDate = null;
        this.status = TransactionStatus.ACTIVE; // Mặc định khi tạo mới là ACTIVE
    }

    // Getters and Setters
    public String getId() { return id; }
    public String getBookId() { return bookId; }
    public String getMemberId() { return memberId; }
    public LocalDate getBorrowDate() { return borrowDate; }
    public void setBorrowDate(LocalDate borrowDate) { this.borrowDate = borrowDate; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public LocalDate getReturnDate() { return returnDate; }
    public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }
    public TransactionStatus getStatus() { return status; }
    public void setStatus(TransactionStatus status) { this.status = status; }
}