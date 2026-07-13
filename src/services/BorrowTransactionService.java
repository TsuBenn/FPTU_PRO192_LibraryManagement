package services;

import exceptions.BorrowPolicyException;
import models.Book;
import models.BookType;
import models.BorrowTransaction;
import models.Member;
import models.TransactionStatus;
import repositories.BookRepository;
import repositories.BorrowTransactionRepository;
import repositories.MemberRepository;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

public class BorrowTransactionService {
    private final BorrowTransactionRepository txRepository;
    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;

    public BorrowTransactionService(BorrowTransactionRepository txRepository, BookRepository bookRepository, MemberRepository memberRepository) {
        this.txRepository = txRepository;
        this.bookRepository = bookRepository;
        this.memberRepository = memberRepository;
    }

    public void executeCheckout(Member member, Book book, BorrowTransaction tx) throws BorrowPolicyException {
        if (book.getAvailableQuantity() <= 0)
            throw new BorrowPolicyException("Book '" + book.getTitle() + "' is out of stock.");

        if (member.getRemainingBorrowSlots() <= 0)
            throw new BorrowPolicyException("Member '" + member.getName() + "' has reached borrow limit of " + member.getBorrowLimit() + ".");

        if (!member.canBorrow(book.getBookType()))
            throw new BorrowPolicyException("Tier '" + member.getTierName() + "' cannot borrow " + book.getBookType() + " books.");

        if (member.getFine() > 0)
            throw new BorrowPolicyException(member.getName() + " cannot borrow any book because he/she has remaining fine!");

        if (member.getBorrowHistory().stream().anyMatch(bx -> bx.getBookId().equals(book.getId())))
            throw new BorrowPolicyException("Each member can borrow only one copy!");

        tx.setTransactionStatus(TransactionStatus.BORROWING);
        tx.setDueDate(tx.getBorrowDate().plusDays(getMaxBorrowDay(member)));


        book.setAvailableQuantity(book.getAvailableQuantity() - 1);

        member.incrementBorrowedCount();
        member.addTransactionInfo(tx);

        bookRepository.update(book);
        memberRepository.update(member);
        txRepository.save(tx);
    }

    public double processReturn(BorrowTransaction tx, Member member, Book book, LocalDate returnDate) {
        if (tx.getTransactionStatus() != TransactionStatus.BORROWING && tx.getTransactionStatus() != TransactionStatus.OVERDUE) {
            return -1.0;
        }

        tx.setReturnDate(returnDate);

        if (book != null) {
            book.setAvailableQuantity(book.getAvailableQuantity() + 1);
        }

        if (member != null) {
            member.decrementBorrowedCount();
            if (memberRepository != null) {
                memberRepository.update(member);
            }
        }

        double fine = calculateFine(tx, member, book);
        tx.setTransactionStatus(fine > 0 ? TransactionStatus.NOT_PAID : TransactionStatus.IS_PAID);
        tx.setFinePaid(fine);

        member.setFine(member.getFine() + tx.getFinePaid());

        txRepository.update(tx.getTransactionId(), tx);
        memberRepository.update(member);
        bookRepository.update(book);

        return fine;
    }

    public double[] reportMissing(BorrowTransaction tx, Member member, Book book) {
        if (tx.getTransactionStatus() != TransactionStatus.BORROWING && tx.getTransactionStatus() != TransactionStatus.OVERDUE) {
            return null;
        }

        if (book != null) {
            book.setTotalQuantity(book.getTotalQuantity() - 1);
            book.setAvailableQuantity(book.getAvailableQuantity() + 1);
        }

        if (member != null) {
            member.decrementBorrowedCount();
        }

        double replacement = member.getLostBookMultiplier() * book.getPrice();
        double lateFine = calculateFine(tx, member, book);
        double total = replacement + lateFine;

        tx.setReturnDate(LocalDate.now());
        tx.setTransactionStatus(TransactionStatus.MISSING_NOT_PAID);
        tx.setFinePaid(total);

        member.setFine(member.getFine() + total);

        txRepository.update(tx.getTransactionId(), tx);
        memberRepository.update(member);
        bookRepository.save(book);

        return new double[]{replacement, lateFine, total};
    }

    public void processPayment(BorrowTransaction borrowTransaction, Member member) {
        if (borrowTransaction == null || member == null) return;
        TransactionStatus status = borrowTransaction.getTransactionStatus();
        if (status != TransactionStatus.MISSING_NOT_PAID && status != TransactionStatus.NOT_PAID) {
            return;
        }

        borrowTransaction.setTransactionStatus(status == TransactionStatus.MISSING_NOT_PAID ? TransactionStatus.MISSING_AND_IS_PAID : TransactionStatus.IS_PAID);
        member.setFine(member.getFine() - borrowTransaction.getFinePaid());

        txRepository.update(borrowTransaction.getTransactionId(), borrowTransaction);
        memberRepository.update(member);
    }

    public double calculateFine(BorrowTransaction tx, Member member, Book book) {
        if (tx == null || tx.getDueDate() == null || tx.getReturnDate() == null) {
            return 0.0;
        }

        long daysOverdue = ChronoUnit.DAYS.between(tx.getDueDate(), tx.getReturnDate());
        if (daysOverdue <= 0) return 0.0;

        return daysOverdue * member.getLateFinePerDay();
    }

    public int getMaxBorrowDay(Member member) {
        return member.getLoanDurationDays();
    }

    public List<BorrowTransaction> getActiveLoansByMember(String memberId) {
        return txRepository.findAll().stream().filter(tx -> tx.getMemberId().equals(memberId)).filter(tx -> tx.getTransactionStatus() == TransactionStatus.BORROWING || tx.getTransactionStatus() == TransactionStatus.OVERDUE).collect(Collectors.toList());
    }

    public List<BorrowTransaction> getRequiredPaidTransactionsByMember(String memberId) {
        return txRepository.findAll().stream().filter(tx -> tx.getMemberId().equals(memberId)).filter(tx -> tx.getTransactionStatus() == TransactionStatus.NOT_PAID || tx.getTransactionStatus() == TransactionStatus.MISSING_NOT_PAID).collect(Collectors.toList());
    }

    public int countActiveLoans(String memberId) {
        return (int) txRepository.findAll().stream().filter(tx -> tx.getMemberId().equals(memberId)).filter(tx -> tx.getTransactionStatus() == TransactionStatus.BORROWING || tx.getTransactionStatus() == TransactionStatus.OVERDUE).count();
    }

    public void updateOverdueTransactions() {
        LocalDate today = LocalDate.now();
        txRepository.findAll().stream().filter(tx -> tx.getTransactionStatus() == TransactionStatus.BORROWING).filter(tx -> tx.getDueDate() != null && tx.getDueDate().isBefore(today)).forEach(tx -> {
            tx.setTransactionStatus(TransactionStatus.OVERDUE);
            txRepository.update(tx.getTransactionId(), tx);
        });
    }

    public void markTransactionsAsMemberRemoved(String memberId) {
        txRepository.findAll().stream().filter(tx -> tx.getMemberId().equals(memberId)).filter(tx -> tx.getTransactionStatus() == TransactionStatus.BORROWING || tx.getTransactionStatus() == TransactionStatus.OVERDUE).forEach(tx -> {
            tx.setTransactionStatus(TransactionStatus.MEMBER_REMOVED);
            txRepository.update(tx.getTransactionId(), tx);
        });
    }

    public void markTransactionAsBookRemove(String bookId) {
        txRepository.findAll().stream().filter(tx -> tx.getBookId().equals(bookId)).filter(tx -> tx.getTransactionStatus() == TransactionStatus.IS_PAID || tx.getTransactionStatus() == TransactionStatus.MISSING_AND_IS_PAID).forEach(tx -> {
            tx.setTransactionStatus(TransactionStatus.BOOK_REMOVED);
            txRepository.save(tx);
        });
    }

    public List<BorrowTransaction> getAllTransactions() {
        return txRepository.findAll();
    }
}