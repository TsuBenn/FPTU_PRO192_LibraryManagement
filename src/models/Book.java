package models;

import utilities.IDManager;

public class Book {
    private final String id;
    private String title;
    private String author;
    private String genre;
    private int publicationYear;
    private int totalQuantity;
    private int availableQuantity;

    public Book(String title, String author, String genre, int publicationYear, int totalQuantity) {
        this.id = IDManager.bookIDGenerator.newID();
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.publicationYear = publicationYear;
        this.totalQuantity = totalQuantity;
        this.availableQuantity = totalQuantity;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }
    public int getPublicationYear() { return publicationYear; }
    public void setPublicationYear(int publicationYear) { this.publicationYear = publicationYear; }
    public int getTotalQuantity() { return totalQuantity; }

    public void setTotalQuantity(int totalQuantity) {
        int borrowedCount = this.totalQuantity - this.availableQuantity;
        this.totalQuantity = totalQuantity;
        this.availableQuantity = totalQuantity - borrowedCount;
    }

    public int getAvailableQuantity() { return availableQuantity; }
    public void setAvailableQuantity(int availableQuantity) { this.availableQuantity = availableQuantity; }

    @Override
    public String toString() {
        return id + "|" + title + "|" + author + "|" + genre + "|" + publicationYear + "|" + availableQuantity + "|" + totalQuantity;
    }
}