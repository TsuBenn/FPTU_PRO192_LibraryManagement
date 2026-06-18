package models;

public class Document extends Book {
    public Document(String title, String author, String genre, int publicationYear, int totalQuantity) {
        super(title, author, genre, publicationYear, totalQuantity);
    }

    @Override
    public String toString() {
        return super.toString()  + " | Type: Document";
    }
}