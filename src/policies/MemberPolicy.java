package policies;

import models.BookType;

public interface MemberPolicy {
    int getBorrowLimit();
    int getLoanDurationDays();
    double getLateFinePerDay();
    double getLostBookMultiplier();
    boolean canBorrow(BookType bookType);
    String getTierName();
}
