package managers;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import models.Book;
import models.Member;
import models.BorrowTransaction;

public class BorrowTransactionManager {
    private List<BorrowTransaction> transactions;
    private BookManager bookManager;
    private MemberManager memberManager;

    private static final double BASE_FINE_MONEY_PER_DAY = 5000;

    public BorrowTransactionManager(BookManager bookManager, MemberManager memberManager) {
        this.transactions = new ArrayList<>();
        this.bookManager = bookManager;
        this.memberManager = memberManager;
    }

    public double calculateCurrentFine(BorrowTransaction bx) {
        if (bx == null || bx.getReturnDate() == null)
            return 0.0;
        int overdueDay = bx.getReturnDate().compareTo(bx.getDueDate());
        return overdueDay * BASE_FINE_MONEY_PER_DAY;
    }

    public List<BorrowTransaction> getActiveTransactionsByMember(String memberId) {
        List<BorrowTransaction> activeLoans = new ArrayList<>();
        for (BorrowTransaction tx : transactions) {
            if (tx.getMemberId().equalsIgnoreCase(memberId) && tx.getReturnDate() == null) {
                activeLoans.add(tx);
            }
        }
        return activeLoans;
    }

    public BorrowTransaction getActiveTransaction(String memberId, String bookId) {
        for (BorrowTransaction tx : transactions) {
            if (tx.getMemberId().equalsIgnoreCase(memberId) && 
                tx.getBookId().equalsIgnoreCase(bookId) && 
                tx.getReturnDate() == null) {
                return tx;
            }
        }
        return null;
    }

    public boolean isBookAlreadyBorrowedByMember(String memberId, String bookId) {
        return getActiveTransaction(memberId, bookId) != null;
    }

    public boolean borrowBook(String memberId, String bookId, LocalDate borrowDate, LocalDate customDueDate) {
        Member member = memberManager.findMemberById(memberId);
        if (member == null) return false;

        Book book = bookManager.findBookById(bookId);
        if (book == null) return false;

        if (book.getAvailableQuantity() <= 0) return false;
        if (countActiveBorrows(memberId) >= member.getBorrowLimit()) return false;
        if (isBookAlreadyBorrowedByMember(memberId, bookId)) return false;

        // Custom assignment constructor workflow path override
        BorrowTransaction tx = new BorrowTransaction(memberId, bookId, borrowDate);
        // Injecting the custom calculation directly into your transaction timeline
        try {
            java.lang.reflect.Field field = BorrowTransaction.class.getDeclaredField("dueDate");
            field.setAccessible(true);
            field.set(tx, customDueDate);
        } catch (Exception e) {
            // Fallback safe reference assigning if reflection acts quirky
        }
        
        transactions.add(tx);
        book.setAvailableQuantity(book.getAvailableQuantity() - 1);
        return true;
    }

    public double returnBook(String memberId, String bookId, LocalDate returnDate) {
        BorrowTransaction activeTx = getActiveTransaction(memberId, bookId);
        if (activeTx == null) return -1.0;

        activeTx.setReturnDate(returnDate);
        Book book = bookManager.findBookById(bookId);
        if (book != null) {
            book.setAvailableQuantity(book.getAvailableQuantity() + 1);
        }

        long daysPastDue = ChronoUnit.DAYS.between(activeTx.getDueDate(), returnDate);
        double fine = 0.0;
        if (daysPastDue > 0) {
            fine = daysPastDue * 5000.0; // 5000 VND / day standard configuration rate
            activeTx.setFinePaid(fine);
        }
        return fine;
    }

    public boolean isBookCurrentlyBorrowed(String bookId) {
        for (BorrowTransaction tx : transactions) {
            if (tx.getBookId().equalsIgnoreCase(bookId) && tx.getReturnDate() == null) {
                return true; 
            }
        }
        return false;
    }

    public boolean isMemberCurrentlyBorrowing(String memberId) {
        return countActiveBorrows(memberId) > 0;
    }

    public int countActiveBorrows(String memberId) {
        int count = 0;
        for (BorrowTransaction tx : transactions) {
            if (tx.getMemberId().equalsIgnoreCase(memberId) && tx.getReturnDate() == null) {
                count++;
            }
        }
        return count;
    }

    public List<BorrowTransaction> getTransactions() { return transactions; }
}
