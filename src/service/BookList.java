package service;

import models.Book;
import utilities.UIRender;
import java.util.ArrayList;

public class BookList {
    private ArrayList<Book> books = new ArrayList<>();

    public boolean addBook(Book book) {
        if (book == null) {
            UIRender.renderError("Cannot add empty book data!");
            return false;
        }

        // CHỐT CHẶN XÁC THỰC: Chống trùng lặp mã ID Sách trong danh sách
        if (searchById(book.getBookId()) != null) {
            UIRender.renderError("Book ID '" + book.getBookId() + "' already exists in database!");
            return false;
        }

        books.add(book);
        return true;
    }

    public Book searchById(String id) {
        if (id == null || id.trim().isEmpty()) return null;
        for (Book b : books) {
            if (b.getBookId().equalsIgnoreCase(id.trim())) {
                return b;
            }
        }
        return null;
    }

    public void displayAll() {
        UIRender.renderHeader("Book Inventory");
        if (books.isEmpty()) {
            System.out.println("No books cataloged in the inventory.");
            return;
        }
        for (Book b : books) {
            System.out.printf("ID: %s | Title: %s | Author: %s | Genre: %s | Year: %d | Stock: %d\n",
                    b.getBookId(), b.getTitle(), b.getAuthor(), b.getGenre(), b.getPubYear(), b.getQuantity());
        }
    }
}