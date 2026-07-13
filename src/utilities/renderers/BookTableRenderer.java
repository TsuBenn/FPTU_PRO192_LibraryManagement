package utilities.renderers;

import models.Book;
import utilities.TableRenderer;

public class BookTableRenderer implements TableRenderer<Book> {
    @Override
    public String[] getHeaders() {
        return new String[]{"ID", "Title", "Author", "Genre", "Year", "Prices", "Available", "Total", "Type"};
    }

    @Override
    public String[] toRow(Book b) {
        return new String[]{
                b.getId(), b.getTitle(), b.getAuthor(), b.getGenre(),
                String.valueOf(b.getPublicationYear()),
                String.valueOf(b.getPrice()),
                String.valueOf(b.getAvailableQuantity()),
                String.valueOf(b.getTotalQuantity()),
                b.getBookType().name()
        };
    }
}
