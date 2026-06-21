package models;

import policies.MemberPolicy;
import utilities.IDManager;

import java.util.ArrayList;
import java.util.List;

public class Member {
    private final String id;
    private String name;
    private String phone;
    private String email;
    private double fine;
    private final List<BorrowTransaction> borrowHistory = new ArrayList<>();
    private MemberPolicy policy;
    private int borrowedCount = 0;

    public Member(String name, String phone, String email, MemberPolicy policy) {
        this.id = IDManager.memberIDGenerator.newID();
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.policy = policy;
    }

    public Member(String id, String name, String phone, String email, MemberPolicy policy, int borrowedCount) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.policy = policy;
        this.borrowedCount = borrowedCount;
    }

    public int getBorrowLimit()           { return policy.getBorrowLimit(); }
    public int getLoanDurationDays()      { return policy.getLoanDurationDays(); }
    public double getLateFinePerDay()        { return policy.getLateFinePerDay(); }
    public double getLostBookMultiplier()    { return policy.getLostBookMultiplier(); }
    public boolean canBorrow(BookType type)  { return policy.canBorrow(type); }
    public String getTierName()              { return policy.getTierName(); }

    public void upgradePolicy(MemberPolicy newPolicy) {
        this.policy = newPolicy;
    }

    public int getRemainingBorrowSlots()     { return policy.getBorrowLimit() - borrowedCount; }
    public void incrementBorrowedCount()     { this.borrowedCount++; }
    public void decrementBorrowedCount()     { this.borrowedCount--; }

    public String getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public MemberPolicy getPolicy() { return policy; }
    public int getBorrowedCount() { return borrowedCount; }
    public double getFine() {
        return fine;
    }
    public void setFine(double fine) {
        if (fine > 0)
            this.fine = fine;
    }

    public void addTransactionInfo(BorrowTransaction borrowTransaction) {
        if (borrowTransaction != null)
            this.borrowHistory.add(borrowTransaction);
    }

    public List<BorrowTransaction> getBorrowHistory() {
        return new ArrayList<>(this.borrowHistory);
    }

    public void copyHistoryFrom(Member source) {
        this.borrowHistory.clear();
        this.borrowHistory.addAll(source.getBorrowHistory());
    }

    @Override
    public String toString() {
        return id + "|" + name + "|" + phone + "|" + email
                + "|" + getBorrowLimit() + "|" + getTierName();
    }
}
