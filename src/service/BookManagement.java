package service;

import models.Book;
import utilities.IDGenerator;
import utilities.InputController;
import utilities.Validator;
import utilities.UIRender;

import java.util.ArrayList;
import java.util.List;

public class BookManagement {
    private final List<Book> books;
    private final IDGenerator idGenerator;

    public BookManagement() {
        this.books = new ArrayList<>();
        this.idGenerator = new IDGenerator("BOK", 4);
    }

    /**
     * Lấy toàn bộ danh sách sách (Chỉ dùng nội bộ hệ thống hoặc tầng Service tra cứu)
     */
    public List<Book> getAllBooks() {
        return this.books;
    }

    /**
     * Hỗ trợ nhập liệu từ bàn phím
     */
    public Book inputBook() {
        String title = InputController.getString("Enter book title: ");
        String author = InputController.getString("Enter book author: ");
        String genre = InputController.getString("Enter book genre: ");
        int maximumQuantity = InputController.getInt("Enter maximum quantity (total copies): ");
        int publishYear = InputController.getInt("Enter publish year: ");
        double price = InputController.getDouble("Enter purchase price: ");

        if (price <= 0 || publishYear <= 0 || maximumQuantity <= 0) {
            UIRender.renderError("Invalid numeric bounds. Values must be greater than 0.");
            return null;
        }
        return new Book(idGenerator.newID(), title, author, genre, publishYear, maximumQuantity, price);
    }

    private boolean isInvalidBook(Book book) {
        return book == null || !Validator.isValidString(book.getTitle())
                || !Validator.isValidString(book.getAuthor());
    }

    public boolean add(Book book) {
        if (isInvalidBook(book)) {
            UIRender.renderError("Failed to add book. Provided data is invalid or empty.");
            return false;
        }
        books.add(book);
        return true;
    }

    /**
     * R - READ: Tìm kiếm sách theo ID chính xác
     */
    public Book findById(String bookId) {
        if (bookId == null || bookId.trim().isEmpty()) return null;
        for (Book book : books) {
            if (book.getId().equalsIgnoreCase(bookId.trim())) {
                return book;
            }
        }
        return null;
    }

    /**
     * R - READ: Các hàm tìm kiếm nâng cao (Search Queries)
     */
    public Book findByTitle(String title) {
        if (title == null || title.trim().isEmpty()) return null;
        for (Book book : books) {
            if (book.getTitle().equalsIgnoreCase(title.trim())) return book;
        }
        return null;
    }

    public List<Book> findByAuthor(String author) {
        List<Book> result = new ArrayList<>();
        if (author == null || author.trim().isEmpty()) return result;
        for (Book book : books) {
            if (book.getAuthor().toLowerCase().contains(author.toLowerCase().trim())) {
                result.add(book);
            }
        }
        return result;
    }

    public List<Book> findByGenre(String genre) {
        List<Book> result = new ArrayList<>();
        if (genre == null || genre.trim().isEmpty()) return result;
        for (Book book : books) {
            if (book.getGenre().equalsIgnoreCase(genre.trim())) {
                result.add(book);
            }
        }
        return result;
    }

    /**
     * U - UPDATE: Sửa thông tin sách
     */
    public boolean update(String bookId, Book updatedBook) {
        Book existingBook = findById(bookId);
        if (existingBook == null || updatedBook == null) return false;

        if (isInvalidBook(updatedBook)) {
            UIRender.renderError("Please input correct new information for this book!");
            return false;
        }

        existingBook.setTitle(updatedBook.getTitle().trim());
        existingBook.setAuthor(updatedBook.getAuthor().trim());
        existingBook.setGenre(updatedBook.getGenre().trim());
        existingBook.setPublishYear(updatedBook.getPublishYear());
        existingBook.setPrice(updatedBook.getPrice());

        int diff = updatedBook.getMaximumQuantity() - existingBook.getMaximumQuantity();
        existingBook.setMaximumQuantity(updatedBook.getMaximumQuantity());
        existingBook.setRealtimeQuantity(existingBook.getRealtimeQuantity() + diff);

        return true;
    }

    /**
     * D - DELETE: Xóa sách khỏi hệ thống (Thanh lý cứng khỏi danh sách)
     * Ràng buộc kiểm tra "Sách đã được gom hết về kho chưa" sẽ do LibraryService kiểm soát.
     */
    public boolean remove(String bookId) {
        Book book = findById(bookId);
        if (book == null) return false;

        if (book.getMaximumQuantity() != book.getRealtimeQuantity())
            return false;

        books.remove(book);
        return true;
    }

    /**
     * Độc lập xuất bảng dữ liệu Book
     */
    public void displayAllBooks() {
        UIRender.renderHeader("Library Catalog Inventory");
        String[] headers = {"Book ID", "Title", "Author", "Genre", "Year", "Price", "Available / Total"};
        List<String[]> dataRows = new ArrayList<>();

        for (Book book : books) {
            dataRows.add(new String[]{
                    book.getId(),
                    book.getTitle(),
                    book.getAuthor(),
                    book.getGenre(),
                    String.valueOf(book.getPublishYear()),
                    String.format("%.0f VND", book.getPrice()),
                    book.getRealtimeQuantity() + " / " + book.getMaximumQuantity()
            });
        }

        if (dataRows.isEmpty()) {
            UIRender.renderError("No books available in the inventory catalog.");
            return;
        }
        UIRender.renderTable(headers, dataRows);
    }
}