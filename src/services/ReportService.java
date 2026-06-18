package services;

import models.*;
import repositories.BookRepository;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class ReportService {
    private final BookRepository bookRepo;
    private final BorrowTransactionService transactionService;

    private static final Comparator<BorrowTransaction> RECENCY_COMPARATOR = (a, b) -> {
        LocalDate dateA = (a.getReturnDate() != null) ? a.getReturnDate() : a.getBorrowDate();
        LocalDate dateB = (b.getReturnDate() != null) ? b.getReturnDate() : b.getBorrowDate();
        return dateB.compareTo(dateA);
    };

    public ReportService(BookRepository bookRepo, BorrowTransactionService transactionService) {
        this.bookRepo = bookRepo;
        this.transactionService = transactionService;
    }

    public List<BorrowTransaction> getSortedMemberHistory(Member member) {
        if (member == null) return new ArrayList<>();
        return transactionService.getAllTransactions().stream()
                .filter(tx -> tx.getMemberId().equalsIgnoreCase(member.getId()))
                .filter(tx -> tx.getTransactionStatus() != TransactionStatus.MEMBER_REMOVED)
                .sorted(RECENCY_COMPARATOR)
                .collect(Collectors.toList());
    }

    public List<BorrowTransaction> getMasterLogSorted() {
        return transactionService.getAllTransactions().stream()
                .filter(tx -> tx.getTransactionStatus() != TransactionStatus.MEMBER_REMOVED &&
                        tx.getTransactionStatus() != TransactionStatus.BOOK_REMOVED)
                .sorted(RECENCY_COMPARATOR)
                .collect(Collectors.toList());
    }

    public List<BorrowTransaction> getOverdueAssets() {
        LocalDate today = LocalDate.now();
        return transactionService.getAllTransactions().stream()
                .filter(tx -> tx.getReturnDate() == null) // Chỉ xét sách chưa trả
                .filter(tx -> tx.getTransactionStatus() == TransactionStatus.BORROWING || tx.getTransactionStatus() == TransactionStatus.OVERDUE)
                .filter(tx -> today.isAfter(tx.getDueDate()))
                .collect(Collectors.toList());
    }

    public List<Book> getPopularBooks(int threshold) {
        int cap = threshold <= 0 ? 5 : threshold;

        Map<String, Long> frequencyMap = transactionService.getAllTransactions().stream()
                .collect(Collectors.groupingBy(BorrowTransaction::getBookId, Collectors.counting()));

        return frequencyMap.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(cap)
                .map(entry -> bookRepo.findById(entry.getKey()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}