package models;

public class Student extends Member {
    public Student(String name, String phone, String email) {
        super(name, phone, email);
        this.setCurrentBorrowLimit(3); // Hạn mức chuẩn (nhưng được Service ưu đãi riêng)
    }
}