package models;

import java.time.LocalDate;

public class BorrowingTransaction {
    private String transactionId;
    private Member member;
    private Book book;
    private LocalDate borrowDate;
    private LocalDate returnDate; // Thêm thuộc tính ngày trả sách

    public BorrowingTransaction(String transactionId, Member member, Book book, LocalDate borrowDate) {
        this.transactionId = transactionId;
        this.member = member;
        this.book = book;
        this.borrowDate = borrowDate;
        this.returnDate = null; // Khi mới mượn, ngày trả mặc định là null
    }

    public String getTransactionId() { return transactionId; }
    public Member getMember() { return member; }
    public Book getBook() { return book; }
    public LocalDate getBorrowDate() { return borrowDate; }

    public LocalDate getReturnDate() { return returnDate; }
    public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }

    // Hàm tiện ích kiểm tra xem giao dịch này đã hoàn thành (trả sách) chưa
    public boolean isReturned() {
        return returnDate != null;
    }
}