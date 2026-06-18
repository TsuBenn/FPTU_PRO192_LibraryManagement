package repositories;

import java.util.ArrayList;
import java.util.List;
import models.Book;

public class BookRepository {
    private final List<Book> database = new ArrayList<>();

    public void save(Book book) {
        database.add(book);
    }

    public void delete(Book book) {
        database.remove(book);
    }

    public void update(Book oldBook, Book newBook) {
        int index = database.indexOf(oldBook);
        if (index != -1) {
            database.set(index, newBook);
        }
    }

    public List<Book> findAll() {
        return database;
    }

    public Book findById(String id) {
        return database.stream()
                .filter(b -> b.getId().equalsIgnoreCase(id.trim()))
                .findFirst()
                .orElse(null);
    }
}