package managers;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import models.BorrowTransaction;

public class BorrowTransactionManager {
    private List<BorrowTransaction> transactions;

    public BorrowTransactionManager() {
        this.transactions = new ArrayList<>();
    }

    public boolean borrowBook(String txId, String memberId, String bookId, LocalDate borrowDate) {
        BorrowTransaction tx = new BorrowTransaction(txId, memberId, bookId, borrowDate);
        transactions.add(tx);
        return true;
    }

    public List<BorrowTransaction> getTransactions() {
        return transactions;
    }
}
