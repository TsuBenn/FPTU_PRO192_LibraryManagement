package controllers.handlers;

import config.AppContext;
import exceptions.*;
import models.Book;
import repositories.BookRepository;
import services.BookService;
import services.BorrowTransactionService;
import utilities.*;

import java.util.List;
import java.util.stream.Collectors;

public class BookHandler {
    private static final TableRenderer<Book> RENDERER = new utilities.renderers.BookTableRenderer();

    private final BookService bookService;
    private final InputController input;
    private final BorrowTransactionService borrowTransactionService;

    public BookHandler(AppContext ctx) {
        this.bookService = ctx.bookService;
        this.input = ctx.input;
        this.borrowTransactionService = ctx.transactionService;
    }

    public void handle() {
        boolean active = true;
        String[] choices = {"Add Book", "Remove Book", "Update Book", "List Books"};
        while (active) {
            UIRender.clearScreen();
            UIRender.renderMenu("Book Management", choices);
            switch (input.getChoice("Select: ", 4)) {
                case 1: handleAdd(); break;
                case 2: handleDelete(); break;
                case 3: handleUpdate(); break;
                case 4: handleList(); break;
                case 0: active = false; break;
            }
        }
    }

    private void handleAdd() {
        UIRender.clearScreen();
        try {
            Book book = input.inputNewBookData();
            UIRender.renderBookPreview(book);
            ConfirmResult result = input.getConfirmation();
            if (result == ConfirmResult.CONFIRM) {
                bookService.registerBook(book);
                UIRender.renderSuccess("Book registered.");
            } else if (result == ConfirmResult.EDIT) {
                book = input.updateBookModelFields(book);
                UIRender.renderBookPreview(book);
                if (input.getConfirmation() == ConfirmResult.CONFIRM) {
                    bookService.registerBook(book);
                    UIRender.renderSuccess("Book registered.");
                }
            }
        }
        catch (AbortInputException e) {
            UIRender.renderError("Cancelled. Returning to menu.");
        } catch (DuplicateEntryException | InvalidBookException e) {
            UIRender.renderError(e.getMessage());
        }
        UIRender.pauseEnter();
    }

    private void handleDelete() {
        UIRender.clearScreen();
        try {
            Book target = input.searchAndSelectBook(
                    bookService.getByQuery(
                            input.getString("Enter book search criteria (Title/Author/ID): ")
                    )
            );
            if (target == null) return;
            bookService.deleteBook(target);
            borrowTransactionService.markTransactionsAsBookRemoved(target.getId());
            UIRender.renderSuccess("Book removed.");
        } catch (AbortInputException e) {
            UIRender.renderError("Cancelled. Returning to menu.");
        } catch (InvalidOperationException e) {
            UIRender.renderError(e.getMessage());
        }
        UIRender.pauseEnter();
    }

    private void handleUpdate() {
        UIRender.clearScreen();
        try {
            Book oldBook = input.searchAndSelectBook(
                    bookService.getByQuery(
                            input.getString("Enter book search criteria (Title/Author/ID): ")
                    )
            );
            if (oldBook == null) return;

            Book newBook = input.updateBookModelFields(oldBook);
            UIRender.renderBookPreview(newBook);
            ConfirmResult result = input.getConfirmation();
            if (result == ConfirmResult.CONFIRM) {
                bookService.updateBook(oldBook, newBook);
                UIRender.renderSuccess("Book updated.");
            } else if (result == ConfirmResult.EDIT) {
                newBook = input.updateBookModelFields(newBook);
                bookService.updateBook(oldBook, newBook);
                UIRender.renderSuccess("Book updated.");
            }
        } catch (AbortInputException e) {
            UIRender.renderError("Cancelled. Returning to menu.");
        } catch (LibraryException e) {
            UIRender.renderError(e.getMessage());
        }
        UIRender.pauseEnter();
    }

    private void handleList() {
        UIRender.clearScreen();
        UIRender.renderTable(bookService.getAllBooks(), "Book Inventory", RENDERER);
        UIRender.pauseEnter();
    }
}
