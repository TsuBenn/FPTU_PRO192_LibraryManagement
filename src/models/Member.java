package models;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Member {
    private final String id;
    private String name;
    private String email;
    private String phoneNumber;
    private int maxBorrowLimit;
    private double fineMoney; // Tiền phạt tích lũy, mặc định = 0
    private List<String> transactionIds; // Chỉ lưu ID tham chiếu, append-only

    public Member(String id, String name, String email, String phoneNumber, int maxBorrowLimit) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.maxBorrowLimit = maxBorrowLimit;
        this.fineMoney = 0.0;
        this.transactionIds = new ArrayList<>();
    }

    // Getters and Setters
    public String getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public int getMaxBorrowLimit() { return maxBorrowLimit; }
    public void setMaxBorrowLimit(int maxBorrowLimit) { this.maxBorrowLimit = maxBorrowLimit; }
    public double getFineMoney() { return fineMoney; }
    public void setFineMoney(double fineMoney) { this.fineMoney = fineMoney; }
    public List<String> getTransactionIds() { return transactionIds; }
    public void setTransactionIds(List<String> transactionIds) { this.transactionIds = transactionIds; }
}