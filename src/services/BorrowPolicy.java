package services;

import models.Book;
import models.BorrowTransaction;
import models.Member;

public interface BorrowPolicy {
    boolean isEligibleToBorrow(Member member, Book book);
    double calculateFine(BorrowTransaction borrowTransaction, Member member, Book book);
    int getMaxBorrowDay(Member member);
}
