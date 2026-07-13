package policies;

import models.BookType;

public interface MemberPolicy {
    static MemberPolicy valueOf(String value) {
        switch (value) {
            case "Premium":
                return new PremiumPolicy();
            case "Student":
                return new StudentPolicy();
            case "Teacher":
                return new TeacherPolicy();
            default:
                return new RegularPolicy();
        }
    }

    int getBorrowLimit();
    int getLoanDurationDays();
    double getLateFinePerDay();
    double getLostBookMultiplier();
    boolean canBorrow(BookType bookType);
    String getTierName();
}
