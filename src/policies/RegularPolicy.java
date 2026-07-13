package policies;

import models.BookType;

public class RegularPolicy implements MemberPolicy {
    @Override public int getBorrowLimit()             { return 3; }
    @Override public int getLoanDurationDays()        { return 14; }
    @Override public double getLateFinePerDay()       { return 5000.0; }
    @Override public double getLostBookMultiplier()   { return 1.0; }
    @Override public String getTierName()             { return "Regular"; }

    @Override
    public boolean canBorrow(BookType type) {
        return type == BookType.NOVEL || type == BookType.TEXTBOOK;
    }
}
