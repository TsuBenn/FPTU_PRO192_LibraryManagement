package models;

import utilities.UIRender;

public class Member {
    private String memberId;
    private String name;
    private String phone;
    private String email;
    private int borrowedCount;

    public Member(String memberId, String name, String phone, String email) {
        this.memberId = (memberId == null || memberId.trim().isEmpty()) ? "UNKNOWN_MEM" : memberId.trim();
        this.name = (name == null || name.trim().isEmpty()) ? "Unknown Member" : name.trim();
        this.phone = (phone == null || phone.trim().isEmpty()) ? "N/A" : phone.trim();
        this.email = (email == null || email.trim().isEmpty()) ? "N/A" : email.trim();
        this.borrowedCount = 0; // Ban đầu đăng ký chưa mượn sách nào
    }

    public int getBorrowLimit() {
        return 3; // Luật Milestone 1: Giới hạn mượn mặc định là 3
    }

    // Getters and Setters
    public String getMemberId() { return memberId; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public int getBorrowedCount() { return borrowedCount; }

    public void setBorrowedCount(int borrowedCount) {
        if (borrowedCount >= 0) {
            this.borrowedCount = borrowedCount;
        } else {
            UIRender.renderError("Borrowed count cannot be negative!");
        }
    }
}