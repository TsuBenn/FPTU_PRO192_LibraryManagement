package policies;

import models.BookType;

public class PremiumPolicy implements MemberPolicy {
    @Override public int getBorrowLimit()             { return 7; }
    @Override public int getLoanDurationDays()        { return 28; }
    @Override public double getLateFinePerDay()       { return 10000.0; }
    @Override public double getLostBookMultiplier()   { return 1.25; }
    @Override public String getTierName()             { return "Premium"; }

    @Override
    public boolean canBorrow(BookType type) {
        return type != BookType.LIMITED;
    }
}
