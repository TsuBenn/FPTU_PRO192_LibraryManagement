package managers;

import models.Book;
import models.Member;
import models.BorrowingTransaction;
import utilities.UIRender;
import java.time.LocalDate;
import java.util.ArrayList;

public class BorrowingManager {
    private ArrayList<BorrowingTransaction> transactions = new ArrayList<>();

    // --- HÀM MƯỢN SÁCH (Giữ nguyên có xác thực) ---
    public void processBorrow(String txId, Member member, Book book) {
        if (member == null || book == null) {
            UIRender.renderError("Invalid Member or Book data.");
            return;
        }

        if (book.getQuantity() <= 0) {
            UIRender.renderError("Book is out of stock.");
            return;
        }

        if (member.getBorrowedCount() >= member.getBorrowLimit()) {
            UIRender.renderError("Member reached their borrowing limit.");
            return;
        }

        // Chốt chặn chống trùng ID Giao dịch
        if (findTransactionById(txId) != null) {
            UIRender.renderError("Transaction ID '" + txId + "' already exists!");
            return;
        }

        book.setQuantity(book.getQuantity() - 1);
        member.setBorrowedCount(member.getBorrowedCount() + 1);

        BorrowingTransaction tx = new BorrowingTransaction(txId, member, book, LocalDate.now());
        transactions.add(tx);
        UIRender.renderSuccess("Book loaned successfully!");
    }

    // --- HÀM TRẢ SÁCH MỚI BỔ SUNG (Có xác thực căn bản) ---
    public void processReturn(String txId) {
        BorrowingTransaction tx = findTransactionById(txId);

        // 1. Xác thực: Kiểm tra mã giao dịch tồn tại
        if (tx == null) {
            UIRender.renderError("Transaction ID '" + txId + "' not found in system.");
            return;
        }

        // 2. Xác thực: Kiểm tra xem sách này đã được trả từ trước chưa
        if (tx.isReturned()) {
            UIRender.renderError("This transaction was already closed. Book has already been returned.");
            return;
        }

        // 3. Thực thi nghiệp vụ trả sách
        Book borrowedBook = tx.getBook();
        Member borrowingMember = tx.getMember();

        // Hoàn trả số lượng vật lý vào kho bãi và giải phóng slot mượn
        borrowedBook.setQuantity(borrowedBook.getQuantity() + 1);
        borrowingMember.setBorrowedCount(borrowingMember.getBorrowedCount() - 1);

        // Ghi nhận ngày trả thực tế (lấy ngày hệ thống hiện tại)
        tx.setReturnDate(LocalDate.now());

        UIRender.renderSuccess("Book returned successfully! Inventory and Member loan slot updated.");
    }

    // Hàm bổ trợ tìm kiếm giao dịch trong mảng
    public BorrowingTransaction findTransactionById(String id) {
        if (id == null || id.trim().isEmpty()) return null;
        for (BorrowingTransaction tx : transactions) {
            if (tx.getTransactionId().equalsIgnoreCase(id.trim())) {
                return tx;
            }
        }
        return null;
    }

    public ArrayList<BorrowingTransaction> getTransactions() {
        return this.transactions;
    }

    // Cập nhật hàm in để người dùng phân biệt rõ ràng vé nào ACTIVE, vé nào đã RETURNED
    public void displayAllTransactions() {
        UIRender.renderHeader("Transaction Ledger");
        if (transactions.isEmpty()) {
            System.out.println("No transaction logs recorded.");
            return;
        }
        for (BorrowingTransaction tx : transactions) {
            String status = tx.isReturned() ? "RETURNED (On: " + tx.getReturnDate() + ")" : "ACTIVE (Borrowed)";
            System.out.printf("TxID: %s | Reader: %s | Book: %s | Status: %s\n",
                    tx.getTransactionId(),
                    tx.getMember().getName(),
                    tx.getBook().getTitle(),
                    status);
        }
    }
}