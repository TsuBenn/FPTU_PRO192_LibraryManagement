package models;

public class NovelBook extends Book {
    public NovelBook(String title, String author, String genre, int publicationYear, int totalQuantity) {
        super(title, author, genre, publicationYear, totalQuantity);
    }

    @Override
    public String toString() {
        return super.toString()  + " | Type: Novel Book";
    }
}