package services;

import exceptions.DuplicateEntryException;
import exceptions.EntityNotFoundException;
import exceptions.InvalidBookException;
import exceptions.InvalidOperationException;
import models.Book;
import models.Member;
import repositories.BookRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class BookService {
    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public void registerBook(Book book) throws DuplicateEntryException, InvalidBookException {
        validateBook(book);
        if (isDuplicate(book.getTitle(), book.getAuthor())
                || book.getTitle().isEmpty() || book.getAuthor().isEmpty()) {
            throw new DuplicateEntryException("Title+Author",
                    book.getTitle() + " / " + book.getAuthor());
        }
        bookRepository.save(book);
    }

    public void updateBook(Book oldBook, Book newBook)
            throws EntityNotFoundException, InvalidOperationException, InvalidBookException {
        if (oldBook == null)
            throw new EntityNotFoundException("Book", "unknown");
        int activeLoans = oldBook.getTotalQuantity() - oldBook.getAvailableQuantity();
        if (newBook.getTotalQuantity() < activeLoans)
            throw new InvalidOperationException(
                    "New quantity (" + newBook.getTotalQuantity() +
                    ") is less than active loans (" + activeLoans + ").");
        validateBook(newBook);
        bookRepository.update(oldBook, newBook);
    }

    public void deleteBook(Book book) throws InvalidOperationException {
        if (book.getAvailableQuantity() != book.getTotalQuantity())
            throw new InvalidOperationException(
                    "Cannot delete book with active loans outstanding.");
        bookRepository.delete(book);
    }

    private boolean isDuplicate(String title, String author) {
        return bookRepository.findAll().stream()
                .anyMatch(b -> b.getTitle().equalsIgnoreCase(title.trim())
                        && b.getAuthor().equalsIgnoreCase(author.trim()));
    }

    public void validateBook(Book book) throws InvalidBookException {
        if (book == null) {
            throw new InvalidBookException("Book data cannot be null.");
        }
        if (book.getTitle() == null || book.getTitle().trim().isEmpty()) {
            throw new InvalidBookException("Book title cannot be empty.");
        }
        if (book.getAuthor() == null || book.getAuthor().trim().isEmpty()) {
            throw new InvalidBookException("Book author cannot be empty.");
        }
        if (book.getPrice() <= 0) {
            throw new InvalidBookException("Price must be greater than 0.");
        }
        if (book.getTotalQuantity() <= 0) {
            throw new InvalidBookException("Total quantity must be greater than 0.");
        }
        if (book.getAvailableQuantity() < 0) {
            throw new InvalidBookException("Available quantity cannot be negative.");
        }
        if (book.getAvailableQuantity() > book.getTotalQuantity()) {
            throw new InvalidBookException("Available quantity cannot exceed total quantity.");
        }

        int currentYear = LocalDate.now().getYear();
        if (book.getPublicationYear() < 0 || book.getPublicationYear() > currentYear) {
            throw new InvalidBookException("Publication year must be between 0 and " + currentYear + ".");
        }
    }

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    public Book getBookById(String id) {
        return bookRepository.findById(id);
    }

    public List<Book> getByQuery(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllBooks();
        }
        String lowerCaseQuery = query.toLowerCase();
        return bookRepository.findAll().stream()
                .filter(m -> m.getId().toLowerCase().contains(lowerCaseQuery)
                        || m.getTitle().toLowerCase().contains(lowerCaseQuery) || m.getAuthor().toLowerCase().contains(lowerCaseQuery))
                .collect(Collectors.toList());
    }
}
