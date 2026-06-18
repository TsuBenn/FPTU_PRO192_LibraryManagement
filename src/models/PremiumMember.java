package models;

public class PremiumMember extends Member {
    public PremiumMember(String name, String phone, String email) {
        super(name, phone, email);
        this.setCurrentBorrowLimit(7); // Đặc quyền hạn mức cao
    }
}