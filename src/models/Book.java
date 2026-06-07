package models;

import java.util.UUID;

public class Book {
    private final String id;
    private String title;
    private String author;
    private String genre;
    private int publishYear;
    private int maximumQuantity;
    private int realtimeQuantity;
    private double price;

    public Book(String id, String title, String author, String genre, int publishYear, int maximumQuantity, double price) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.publishYear = publishYear;
        this.maximumQuantity = maximumQuantity;
        this.realtimeQuantity = maximumQuantity;
        this.price = price;
    }

    // Getters and Setters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }
    public int getPublishYear() { return publishYear; }
    public void setPublishYear(int publishYear) { this.publishYear = publishYear; }
    public int getMaximumQuantity() { return maximumQuantity; }
    public void setMaximumQuantity(int maximumQuantity) { this.maximumQuantity = maximumQuantity; }
    public int getRealtimeQuantity() { return realtimeQuantity; }
    public void setRealtimeQuantity(int realtimeQuantity) { this.realtimeQuantity = realtimeQuantity; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
}