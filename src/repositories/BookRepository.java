package repositories;

import java.util.*;

import models.Book;

public class BookRepository {
    private final Map<String, Book> database = new HashMap<>();

    public void save(Book book) {
        database.put(book.getId(), book);
    }

    public void delete(Book book) {
        database.remove(book.getId());
    }

    public void update(Book oldBook, Book newBook) {
        if (database.containsKey(oldBook.getId())) {
            database.put(oldBook.getId(), newBook);
        }
    }

    public List<Book> findAll() {
        return new ArrayList<>(database.values());
    }

    public Book findById(String id) {
        return database.get(id);
    }
}
