package models;

public class RegularMember extends Member {
    public RegularMember(String name, String phone, String email) {
        super(name, phone, email);
        this.setCurrentBorrowLimit(3); // Hạn mức chuẩn
    }
}