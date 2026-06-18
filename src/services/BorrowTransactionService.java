package services;

import models.*;
import repositories.BorrowTransactionRepository;
import utilities.UIRender;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

public class BorrowTransactionService implements BorrowPolicy {
    private final BorrowTransactionRepository txRepository;

    public BorrowTransactionService(BorrowTransactionRepository txRepository) {
        this.txRepository = txRepository;
    }

    // =========================================================================
    // 1. TRIỂN KHAI BORROW POLICY (Quy tắc nghiệp vụ cốt lõi)
    // =========================================================================

    @Override
    public boolean isEligibleToBorrow(Member member, Book book) {
        if (book.getAvailableQuantity() <= 0) {
            UIRender.renderError("Invalid quantity!");
            return false;
        }
        if (member.getCurrentBorrowLimit() <= 0) {

            UIRender.renderError("Invalid borrow number" + member.getCurrentBorrowLimit());
            return false;
        }

        if (book instanceof LimitedDocument) {
            UIRender.renderError("Limited document can not be borrowed!");
            return false;
        }

        if (book instanceof Document) {
            return (member instanceof PremiumMember);
        }

        return true;
    }

    @Override
    public double calculateFine(BorrowTransaction tx, Member member, Book book) {
        LocalDate endDate = (tx.getReturnDate() != null) ? tx.getReturnDate() : LocalDate.now();
        long daysLate = ChronoUnit.DAYS.between(tx.getDueDate(), endDate);

        if (daysLate <= 0) return 0.0;

        double fineRate = 5000.0;

        if (member instanceof PremiumMember)
            fineRate = 10000.0;

        return daysLate * fineRate;
    }

    @Override
    public int getMaxBorrowDay(Member member) {
        if (member instanceof PremiumMember)
            return 28;
        return 14;
    }

    // =========================================================================
    // 2. LUỒNG THỰC THI CHÍNH (Core Workflows tích hợp State Machine)
    // =========================================================================

    /**
     * Khởi tạo giao dịch mượn sách.
     * @return true nếu thành công, false nếu vi phạm chính sách mượn.
     */
    public boolean executeCheckout(Member member, Book book, BorrowTransaction tx) {
        if (!isEligibleToBorrow(member, book)) {
            System.out.println(member);
            System.out.println(book);
            return false;
        }

        tx.setTransactionStatus(TransactionStatus.BORROWING);
        tx.setDueDate(tx.getBorrowDate().plusDays(getMaxBorrowDay(member)));
        txRepository.save(tx);

        book.setAvailableQuantity(book.getAvailableQuantity() - 1);
        member.setCurrentBorrowLimit(member.getCurrentBorrowLimit() - 1);
        member.addTransactionInfo(tx);

        return true;
    }

    /**
     * Xử lý trả sách.
     * @return Tiền phạt phát sinh. Trả về -1.0 nếu trạng thái giao dịch không hợp lệ.
     */
    public double processReturn(BorrowTransaction tx, Member member, Book book, LocalDate returnDate) {
        if (tx.getTransactionStatus() != TransactionStatus.BORROWING && tx.getTransactionStatus() != TransactionStatus.OVERDUE) {
            return -1.0;
        }

        tx.setReturnDate(returnDate);

        if (book != null) {
            book.setAvailableQuantity(book.getAvailableQuantity() + 1);
        }
        if (member != null) {
            member.setCurrentBorrowLimit(member.getCurrentBorrowLimit() + 1);
        }

        // Tính phạt và kết toán
        double fine = calculateFine(tx, member, book);
        tx.setFinePaid(fine);

        tx.setTransactionStatus(fine > 0 ? TransactionStatus.NOT_PAID : TransactionStatus.IS_PAID);

        return fine;
    }

    /**
     * Xử lý báo mất sách khẩn cấp.
     * @return Mảng [Tiền đền gốc, Tiền phạt trễ, Tổng hóa đơn]. Trả về null nếu trạng thái không hợp lệ.
     */
    public double[] reportMissing(BorrowTransaction tx, Member member, Book book, double replacementCost) {
        // STATE MACHINE GUARD
        if (tx.getTransactionStatus() != TransactionStatus.BORROWING && tx.getTransactionStatus() != TransactionStatus.OVERDUE) {
            return null;
        }

        tx.setReturnDate(LocalDate.now());
        tx.setTransactionStatus(TransactionStatus.MISSING_NOT_PAID);

        // Giảm vĩnh viễn sức chứa tổng của kho sách
        if (book != null) {
            book.setTotalQuantity(book.getTotalQuantity() - 1);
        }

        // Giải phóng hạn mức thẻ (vì thành viên đang chịu trách nhiệm tài chính riêng)
        if (member != null) {
            member.setCurrentBorrowLimit(member.getCurrentBorrowLimit() + 1);
        }

        double lateFine = calculateFine(tx, member, book);
        double totalBill = replacementCost + lateFine;
        tx.setFinePaid(totalBill);

        return new double[]{replacementCost, lateFine, totalBill};
    }

    // =========================================================================
    // 3. CÁC HÀM TRUY VẤN DỮ LIỆU (Utility Queries)
    // =========================================================================

    public List<BorrowTransaction> getAllTransactions() {
        return txRepository.findAll();
    }

    public List<BorrowTransaction> getActiveLoansByMember(String memberId) {
        return txRepository.findAll().stream()
                .filter(tx -> tx.getMemberId().equalsIgnoreCase(memberId) &&
                        (tx.getTransactionStatus() == TransactionStatus.BORROWING || tx.getTransactionStatus() == TransactionStatus.OVERDUE))
                .collect(Collectors.toList());
    }

    public int countActiveLoans(String memberId) {
        return getActiveLoansByMember(memberId).size();
    }

    public boolean isBookBorrowedByMember(String memberId, String bookId) {
        return getActiveLoansByMember(memberId).stream()
                .anyMatch(tx -> tx.getBookId().equalsIgnoreCase(bookId));
    }
}