package managers;

import java.util.ArrayList;
import java.util.List;
import models.Book;

public class BookManager {
    private List<Book> books;

    public BookManager() {
        this.books = new ArrayList<>();
    }

    public boolean addBook(Book book) {
        if (findBookById(book.getId()) != null) {
            return false;
        }
        books.add(book);
        return true;
    }

    public Book findBookById(String id) {
        for (Book b : books) {
            if (b.getId().equalsIgnoreCase(id)) {
                return b;
            }
        }
        return null;
    }

    public List<Book> getAllBooks() {
        return books;
    }
}
