package services;

import java.util.List;
import java.util.stream.Collectors;
import models.Book;
import repositories.BookRepository;
import utilities.InputController;
import utilities.UIRender;

public class BookService {
    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public boolean registerBook(Book book) {
        if (bookRepository.findById(book.getId()) != null) return false;
        if (isDuplicate(book.getTitle(), book.getAuthor()) || book.getTitle().isEmpty() || book.getAuthor().isEmpty()) {
            return false;
        }
        bookRepository.save(book);
        return true;
    }

    public boolean updateBook(Book oldBook, Book newBook) {
        if (oldBook == null || newBook == null) return false;
        int activeLoans = oldBook.getTotalQuantity() - oldBook.getAvailableQuantity();
        if (newBook.getTotalQuantity() < activeLoans) {
            return false; // Dynamic business verification inside service
        }
        bookRepository.update(oldBook, newBook);
        return true;
    }

    private boolean isDuplicate(String title, String author) {
        return bookRepository.findAll().stream()
                .anyMatch(b -> b.getTitle().equalsIgnoreCase(title.trim()) && b.getAuthor().equalsIgnoreCase(author.trim()));
    }

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    public Book getBookById(String id) {
        return bookRepository.findById(id);
    }

    public static Book searchAndSelectBook(BookRepository repository) {
        String query = InputController.getString("Enter Book Search Filter Criteria (Title/ID/Author): ");
        List<Book> matches = repository.findAll().stream()
                .filter(b -> b.getId().toLowerCase().contains(query.toLowerCase()) ||
                        b.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                        b.getAuthor().toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());

        if (matches.isEmpty()) {
            UIRender.renderError("No matching book records located.");
            UIRender.pauseEnter();
            return null;
        }

        UIRender.renderBookContentTable(matches);
        return matches.get(0);
    }
}