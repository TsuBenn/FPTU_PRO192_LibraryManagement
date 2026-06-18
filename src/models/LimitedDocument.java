package models;

public class LimitedDocument extends Book {
    public LimitedDocument(String title, String author, String genre, int publicationYear, int totalQuantity) {
        super(title, author, genre, publicationYear, totalQuantity);
    }

    @Override
    public String toString() {
        return super.toString()  + " | Type: Limited";
    }
}