package services;

import exceptions.BorrowPolicyException;
import exceptions.EntityNotFoundException;
import models.Book;
import models.BorrowTransaction;
import models.Member;
import models.TransactionStatus;
import repositories.BorrowTransactionRepository;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

public class BorrowTransactionService implements BorrowPolicy {
    private final BorrowTransactionRepository txRepository;

    public BorrowTransactionService(BorrowTransactionRepository txRepository) {
        this.txRepository = txRepository;
    }

    @Override
    public boolean isEligibleToBorrow(Member member, Book book) {
        return book.getAvailableQuantity() > 0
                && member.getRemainingBorrowSlots() > 0
                && member.canBorrow(book.getBookType());
    }

    @Override
    public double calculateFine(BorrowTransaction tx, Member member, Book book) {
        LocalDate endDate = tx.getReturnDate() != null ? tx.getReturnDate() : LocalDate.now();
        long daysLate = ChronoUnit.DAYS.between(tx.getDueDate(), endDate);
        if (daysLate <= 0) return 0.0;
        return daysLate * member.getLateFinePerDay();
    }

    @Override
    public int getMaxBorrowDay(Member member) {
        return member.getLoanDurationDays();
    }

    public void executeCheckout(Member member, Book book, BorrowTransaction tx)
            throws BorrowPolicyException {
        if (book.getAvailableQuantity() <= 0)
            throw new BorrowPolicyException(
                    "Book '" + book.getTitle() + "' is out of stock.");
        if (member.getRemainingBorrowSlots() <= 0)
            throw new BorrowPolicyException(
                    "Member '" + member.getName() + "' has reached borrow limit of "
                    + member.getBorrowLimit() + ".");
        if (!member.canBorrow(book.getBookType()))
            throw new BorrowPolicyException(
                    "Tier '" + member.getTierName() + "' cannot borrow "
                    + book.getBookType() + " books.");
        if (member.getFine() > 0)
            throw new BorrowPolicyException(member.getName() + " can not borrow any book because he/she has remaining fine!");
        if (member.getBorrowHistory().stream()
                .anyMatch(bx -> bx.getBookId().equals(book.getId())))
            throw new BorrowPolicyException("Each member can borrow only one copy!");

        tx.setTransactionStatus(TransactionStatus.BORROWING);
        tx.setDueDate(tx.getBorrowDate().plusDays(getMaxBorrowDay(member)));
        txRepository.save(tx);
        book.setAvailableQuantity(book.getAvailableQuantity() - 1);
        member.incrementBorrowedCount();
        member.addTransactionInfo(tx);
    }

    public double processReturn(BorrowTransaction tx, Member member, Book book, LocalDate returnDate) {
        if (tx.getTransactionStatus() != TransactionStatus.BORROWING
                && tx.getTransactionStatus() != TransactionStatus.OVERDUE) {
            return -1.0;
        }

        tx.setReturnDate(returnDate);

        if (book != null) {
            book.setAvailableQuantity(book.getAvailableQuantity() + 1);
        }
        if (member != null) {
            member.decrementBorrowedCount();
        }

        double fine = calculateFine(tx, member, book);
        tx.setTransactionStatus(fine > 0 ? TransactionStatus.NOT_PAID : TransactionStatus.IS_PAID);
        tx.setFinePaid(fine);
        member.setFine(member.getFine() + tx.getFinePaid());

        return fine;
    }

    public void processPayment(BorrowTransaction borrowTransaction, Member member) {
        if (borrowTransaction == null || member == null) return;

        TransactionStatus status = borrowTransaction.getTransactionStatus();

        if (status != TransactionStatus.MISSING_NOT_PAID && status != TransactionStatus.NOT_PAID) {
            return;
        }

        borrowTransaction.setTransactionStatus(
                status == TransactionStatus.MISSING_NOT_PAID
                        ? TransactionStatus.MISSING_AND_IS_PAID
                        : TransactionStatus.IS_PAID
        );

// Trừ tiền phạt
        member.setFine(member.getFine() - borrowTransaction.getFinePaid());
    }

    public double[] reportMissing(BorrowTransaction tx, Member member, Book book) {
        if (tx.getTransactionStatus() != TransactionStatus.BORROWING
                && tx.getTransactionStatus() != TransactionStatus.OVERDUE) {
            return null;
        }

        tx.setReturnDate(LocalDate.now());
        tx.setTransactionStatus(TransactionStatus.MISSING_NOT_PAID);

        if (book != null) {
            book.setTotalQuantity(book.getTotalQuantity() - 1);
            if (book.getAvailableQuantity() != 0)
                book.setAvailableQuantity(book.getAvailableQuantity() - 1);
        }

        if (member != null) {
            member.decrementBorrowedCount();
        }

        double lateFine = calculateFine(tx, member, book);
        double replacementCost = member.getPolicy().getLostBookMultiplier() * book.getPrice();
        double totalBill = replacementCost + lateFine;

        tx.setFinePaid(totalBill);
        member.setFine(member.getFine() + totalBill);

        return new double[]{replacementCost, lateFine, totalBill};
    }

    public List<BorrowTransaction> getAllTransactions() {
        return txRepository.findAll();
    }

    public List<BorrowTransaction> getActiveLoansByMember(String memberId) {
        return txRepository.findAll().stream()
                .filter(tx -> tx.getMemberId().equalsIgnoreCase(memberId)
                        && (tx.getTransactionStatus() == TransactionStatus.BORROWING
                        || tx.getTransactionStatus() == TransactionStatus.OVERDUE))
                .collect(Collectors.toList());
    }

    public List<BorrowTransaction> getRequiredPaidTransactionsByMember(String memberId) {
        return txRepository.findAll()
                .stream().filter(tx -> tx.getMemberId().equals(memberId) && (
                        tx.getTransactionStatus() == TransactionStatus.MISSING_NOT_PAID
                        || tx.getTransactionStatus() == TransactionStatus.NOT_PAID
                        ))
                .collect(Collectors.toList());
    }

    public void updateOverdueTransactions() {
        LocalDate today = LocalDate.now();

        txRepository.findAll().stream()
                .filter(tx -> tx.getTransactionStatus() == TransactionStatus.BORROWING)
                .filter(tx -> tx.getDueDate() != null && tx.getDueDate().isBefore(today))
                .forEach(tx -> {
                    tx.setTransactionStatus(TransactionStatus.OVERDUE);
                    try {
                        txRepository.update(tx.getTransactionId(), tx);
                    } catch (EntityNotFoundException ignored) {
                    }
                });
    }

    public void markTransactionsAsBookRemoved(String bookId) {
        if (bookId == null || bookId.trim().isEmpty()) return;

        txRepository.findAll().stream()
                .filter(tx -> tx.getBookId().equals(bookId))
                .forEach(tx -> {
                    tx.setTransactionStatus(TransactionStatus.BOOK_REMOVED);
                    try {
                        txRepository.update(tx.getTransactionId(), tx);
                    } catch (EntityNotFoundException ignored) {
                    }
                });
    }

    public void markTransactionsAsMemberRemoved(String memberId) {
        if (memberId == null || memberId.trim().isEmpty()) return;

        txRepository.findAll().stream()
                .filter(tx -> tx.getMemberId().equals(memberId))
                .forEach(tx -> {
                    tx.setTransactionStatus(TransactionStatus.MEMBER_REMOVED);
                    try {
                        txRepository.update(tx.getTransactionId(), tx);
                    } catch (EntityNotFoundException ignored) {
                    }
                });
    }


    public int countActiveLoans(String memberId) {
        return getActiveLoansByMember(memberId).size();
    }

    public boolean isBookBorrowedByMember(String memberId, String bookId) {
        return getActiveLoansByMember(memberId).stream()
                .anyMatch(tx -> tx.getBookId().equalsIgnoreCase(bookId));
    }
}
