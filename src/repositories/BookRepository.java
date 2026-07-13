package repositories;

import java.util.*;

import models.Book;
import repositories.io.Database;

public class BookRepository {
    private final Map<String, Book> database = new HashMap<>();
    private final Database<Book> bookDatabase;

    public BookRepository(Database<Book> bookDatabase) {
        this.bookDatabase = bookDatabase;
        bookDatabase.loadContent()
                .forEach(d -> database.put(d.getId(), d));
    }

    public void save(Book book) {
        database.put(book.getId(), book);
        bookDatabase.save(database);
    }

    public void delete(Book book) {
        database.remove(book.getId());
        bookDatabase.save(database);
    }

    public void update(Book oldBook, Book newBook) {
        System.out.println("Update vào database! dạng 2 tham số!");
        if (database.containsKey(oldBook.getId())) {
            System.out.println("Tìm thấy sách và cập nhật");
            database.put(oldBook.getId(), newBook);
            bookDatabase.save(database);
        }
    }

    public void update(Book book) {
        System.out.println("Update vào database");
        if (database.containsKey(book.getId())) {
            System.out.println("Đã tìm thấy giờ cập nhật!");
            database.put(book.getId(), book);
            bookDatabase.save(database);
        }
    }

    public List<Book> findAll() {
        return new ArrayList<>(database.values());
    }

    public Book findById(String id) {
        return database.get(id);
    }

}