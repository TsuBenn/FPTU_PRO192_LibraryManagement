package models;

public class TextBook extends Book {
    public TextBook(String title, String author, String genre, int publicationYear, int totalQuantity) {
        super(title, author, genre, publicationYear, totalQuantity);
    }

    @Override
    public String toString() {
        return super.toString()  + " | Type: Text Book";
    }
}