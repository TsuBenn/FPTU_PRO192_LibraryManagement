package models;

import utilities.UIRender;

public class Book {
    private String bookId;
    private String title;
    private String author;
    private String genre;
    private int pubYear;
    private int quantity;

    public Book(String bookId, String title, String author, String genre, int pubYear, int quantity) {
        // Xác thực không cho phép dữ liệu trống
        this.bookId = (bookId == null || bookId.trim().isEmpty()) ? "UNKNOWN_BOK" : bookId.trim();
        this.title = (title == null || title.trim().isEmpty()) ? "Untitled" : title.trim();
        this.author = (author == null || author.trim().isEmpty()) ? "Unknown Author" : author.trim();
        this.genre = (genre == null || genre.trim().isEmpty()) ? "General" : genre.trim();

        // Xác thực năm xuất bản (Không lớn hơn năm hiện tại 2026 và không âm)
        this.pubYear = (pubYear > 2026 || pubYear < 0) ? 2026 : pubYear;

        // Xác thực số lượng không được âm
        this.quantity = (quantity < 0) ? 0 : quantity;
    }

    // Getters
    public String getBookId() { return bookId; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getGenre() { return genre; }
    public int getPubYear() { return pubYear; }
    public int getQuantity() { return quantity; }

    // Setter có chốt chặn kiểm tra giá trị âm
    public void setQuantity(int quantity) {
        if (quantity >= 0) {
            this.quantity = quantity;
        } else {
            UIRender.renderError("Quantity cannot be negative!");
        }
    }
}