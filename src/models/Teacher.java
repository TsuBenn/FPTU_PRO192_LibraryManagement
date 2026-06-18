package models;

public class Teacher extends PremiumMember {
    public Teacher(String name, String phone, String email) {
        super(name, phone, email);
        this.setCurrentBorrowLimit(10);
    }
}