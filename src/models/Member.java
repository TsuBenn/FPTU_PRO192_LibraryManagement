package models;

import utilities.IDManager;

import java.util.ArrayList;
import java.util.List;

public class Member {
    private final String id;
    private String name;
    private String phone;
    private String email;
    private final List<BorrowTransaction> borrowHistory = new ArrayList<>();
    private int currentBorrowLimit;

    public Member(String name, String phone, String email) {
        this.id = IDManager.memberIDGenerator.newID();
        this.name = name;
        this.phone = phone;
        this.email = email;
        currentBorrowLimit = 3;
    }

    public int getBorrowLimit() {
        return 3;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getCurrentBorrowLimit() {
        return currentBorrowLimit;
    }

    public void setCurrentBorrowLimit(int currentBorrowLimit) {
        this.currentBorrowLimit = currentBorrowLimit;
    }

    public void addTransactionInfo(BorrowTransaction borrowTransaction) {
        if (borrowTransaction != null)
            this.borrowHistory.add(borrowTransaction);
    }

    public List<BorrowTransaction> getBorrowHistory() {
        return new ArrayList<>(this.borrowHistory);
    }


    @Override
    public String toString() {
        return id + "|" + name + "|" + phone + "|" + email + "|" + getBorrowLimit();
    }
}