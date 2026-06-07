package service;

import models.Book;
import models.Member;
import models.BorrowTransaction;
import models.TransactionStatus;
import utilities.IDGenerator;
import utilities.UIRender;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class LibraryService {
    private final BookManagement bookManagement;
    private final MemberManagement memberManagement;
    private final BorrowTransactionManagement transactionManagement;
    private final IDGenerator idGenerator;

    public LibraryService(BookManagement bookManagement, MemberManagement memberManagement, BorrowTransactionManagement transactionManagement) {
        this.bookManagement = bookManagement;
        this.memberManagement = memberManagement;
        this.transactionManagement = transactionManagement;
        idGenerator = new IDGenerator("BTS", 4);
    }

    /**
     * LUỒNG NGHIỆP VỤ 1: MƯỢN SÁCH (Borrow Book)
     */
    public boolean borrowBook(String memberId, String bookId) {
        Member member = memberManagement.findById(memberId);
        Book book = bookManagement.findById(bookId);

        if (member == null) { UIRender.renderError("Borrow Denied! Member ID does not exist."); return false; }
        if (book == null) { UIRender.renderError("Borrow Denied! Book ID does not exist."); return false; }

        if (book.getRealtimeQuantity() <= 0) {
            UIRender.renderError("Borrow Denied! '" + book.getTitle() + "' is currently out of stock.");
            return false;
        }

        if (member.getMaxBorrowLimit() <= 0) {
            UIRender.renderError("Borrow Denied! Member '" + member.getName() + "' has reached their maximum borrow limit.");
            return false;
        }

        LocalDate today = LocalDate.now();
        LocalDate dueDate = today.plusDays(14);
        BorrowTransaction tx = new BorrowTransaction(idGenerator.newID(), bookId, memberId, today, dueDate);

        transactionManagement.addTransaction(tx);

        member.getTransactionIds().add(tx.getId());

        book.setRealtimeQuantity(book.getRealtimeQuantity() - 1);
        member.setMaxBorrowLimit(member.getMaxBorrowLimit() - 1);

        UIRender.renderSuccess("Book '" + book.getTitle() + "' successfully checked out to " + member.getName() + "!");
        return true;
    }

    /**
     * LUỒNG NGHIỆP VỤ 2: TRẢ SÁCH (Return Book)
     */
    public boolean returnBook(String transactionId, double amountPaid) {
        BorrowTransaction tx = transactionManagement.findById(transactionId);
        if (tx == null) { UIRender.renderError("Return Denied! Transaction record not found."); return false; }
        if (tx.getStatus() != TransactionStatus.ACTIVE) { UIRender.renderError("Return Denied! Transaction is already closed."); return false; }

        Book book = bookManagement.findById(tx.getBookId());
        Member member = memberManagement.findById(tx.getMemberId());

        LocalDate today = LocalDate.now();
        tx.setReturnDate(today);
        tx.setStatus(TransactionStatus.RETURNED);

        // 3. Tính tiền phạt dựa trên số ngày trễ hạn
        if (today.isAfter(tx.getDueDate())) {
            long overdueDays = ChronoUnit.DAYS.between(tx.getDueDate(), today);

            // Công thức phạt: Mỗi ngày trễ phạt 5% giá trị cuốn sách
            if (book != null) {
                double penalty = overdueDays * (0.05 * book.getPrice());

                // 4. Cộng dồn tiền phạt vào ví nợ của Member sở hữu transaction đó
                if (member != null) {
                    member.setFineMoney(member.getFineMoney() + penalty);
                    UIRender.renderError("🚨 OVERDUE ALERT: Book returned " + overdueDays + " days late! Penalty of " + penalty + " VND applied to " + member.getName());
                }
            }
        }

        // 5. Trả lại sách vật lý lên kệ kho và hoàn lại 1 slot mượn cho member
        if (book != null) {
            book.setRealtimeQuantity(book.getRealtimeQuantity() + 1);
        }
        if (member != null) {
            member.setMaxBorrowLimit(member.getMaxBorrowLimit() + 1);

            // Xử lý thanh toán phạt ngay quầy (nếu có nạp tiền mặt)
            if (amountPaid > 0) {
                memberManagement.processFinePayment(member.getId(), amountPaid);
            }
        }

        UIRender.renderSuccess("Transaction " + transactionId.substring(0,8) + "... safely checked-in and closed.");
        return true;
    }

    /**
     * LUỒNG NGHIỆP VỤ 3: XÓA SÁCH (Thanh lý cứng đầu sách)
     */
    public boolean deleteBook(String bookId) {
        // 1. Lấy toàn bộ transaction liên quan đến book_id này
        List<BorrowTransaction> relatedTxs = transactionManagement.findByBookId(bookId);

        // 2. Nếu tồn tại bất kỳ transaction nào có status = ACTIVE -> TỪ CHỐI
        for (BorrowTransaction tx : relatedTxs) {
            if (tx.getStatus() == TransactionStatus.ACTIVE) {
                UIRender.renderError("Deletion Aborted! This book is currently being held by an active reader.");
                return false;
            }
        }

        // 3. Với các transaction đã kết thúc -> Đổi trạng thái lịch sử thành BOOK_REMOVED
        for (BorrowTransaction tx : relatedTxs) {
            tx.setStatus(TransactionStatus.BOOK_REMOVED);
        }

        // 4. Xóa cứng book khỏi BookManagement
        boolean isRemoved = bookManagement.remove(bookId);
        if (isRemoved) {
            UIRender.renderSuccess("Book ID: " + bookId + " has been successfully expunged from database catalog.");
            return true;
        }
        return false;
    }

    /**
     * LUỒNG NGHIỆP VỤ 4: XÓA THÀNH VIÊN (Xóa tài cứng khoản)
     */
    public boolean deleteMember(String memberId) {
        // 1. Lấy toàn bộ transaction liên quan đến member_id này
        List<BorrowTransaction> relatedTxs = transactionManagement.findByMemberId(memberId);

        // 2. Nếu tồn tại bất kỳ transaction nào có status = ACTIVE -> TỪ CHỐI
        for (BorrowTransaction tx : relatedTxs) {
            if (tx.getStatus() == TransactionStatus.ACTIVE) {
                UIRender.renderError("Deletion Aborted! This member is holding unreturned materials.");
                return false;
            }
        }

        // 3. Với các transaction đã kết thúc -> Đổi trạng thái lịch sử thành MEMBER_REMOVED
        for (BorrowTransaction tx : relatedTxs) {
            tx.setStatus(TransactionStatus.MEMBER_REMOVED);
        }

        // 4. Xóa cứng member khỏi MemberManagement (Hàm removeMember bên dưới tự động validate nợ tiền phạt)
        boolean isWiped = memberManagement.removeMember(memberId);
        if (isWiped) {
            UIRender.renderSuccess("Member ID: " + memberId + " successfully expunged from library databases.");
            return true;
        }
        return false;
    }
}