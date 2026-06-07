package managers;

import models.Book;

import java.util.ArrayList;
import java.util.List;

public class BookManager {
    private List<Book> books;

    public BookManager() {
        this.books = new ArrayList<>();
    }

    public boolean addBook(Book book) {
        if (findBookById(book.getId()) != null) return false;
        // Strict duplication verification check
        if (isDuplicateBook(book.getTitle(), book.getAuthor())) return false;
        books.add(book);
        return true;
    }

    public boolean isDuplicateBook(String title, String author) {
        for (Book b : books) {
            if (b.getTitle().equalsIgnoreCase(title.trim()) && b.getAuthor().equalsIgnoreCase(author.trim())) {
                return true;
            }
        }
        return false;
    }

    public boolean removeBook(String id) {
        Book b = findBookById(id);
        if (b == null)
            return false;

        if (b.getTotalQuantity() != b.getAvailableQuantity())
            return false;

        books.remove(b);
        return true;
    }

    public Book findBookById(String id) {
        for (Book b : books) {
            if (b.getId().equalsIgnoreCase(id.trim())) return b;
        }
        return null;
    }

    public List<Book> searchBooks(String query) {
        List<Book> matches = new ArrayList<>();
        if (query == null)
            return matches;

        String lowerQuery = query.toLowerCase().trim();
        for (Book b : books) {
            if (b.getId().toLowerCase().contains(lowerQuery) ||
                    b.getTitle().toLowerCase().contains(lowerQuery) ||
                    b.getAuthor().toLowerCase().contains(lowerQuery) ||
                    b.getGenre().toLowerCase().contains(lowerQuery)) {
                matches.add(b);
            }
        }
        return matches;
    }

    public List<Book> getAllBooks() {
        return books;
    }
}
