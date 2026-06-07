package service;

import models.BorrowTransaction;
import utilities.IDGenerator;
import utilities.UIRender;

import java.util.ArrayList;
import java.util.List;

public class BorrowTransactionManagement {
    private final List<BorrowTransaction> transactions;

    public BorrowTransactionManagement() {
        this.transactions = new ArrayList<>();
    }

    public boolean addTransaction(BorrowTransaction transaction) {
        if (transaction == null) return false;
        transactions.add(transaction);
        return true;
    }

    public List<BorrowTransaction> getAllTransactions() {
        return this.transactions;
    }

    public BorrowTransaction findById(String transactionId) {
        if (transactionId == null || transactionId.trim().isEmpty()) return null;
        for (BorrowTransaction tx : transactions) {
            if (tx.getId().equalsIgnoreCase(transactionId.trim())) {
                return tx;
            }
        }
        return null;
    }

    public List<BorrowTransaction> findByBookId(String bookId) {
        List<String[]> dataRows = new ArrayList<>();
        List<BorrowTransaction> result = new ArrayList<>();
        if (bookId == null || bookId.trim().isEmpty()) return result;

        for (BorrowTransaction tx : transactions) {
            if (tx.getBookId().equalsIgnoreCase(bookId.trim())) {
                result.add(tx);
            }
        }
        return result;
    }

    public List<BorrowTransaction> findByMemberId(String memberId) {
        List<BorrowTransaction> result = new ArrayList<>();
        if (memberId == null || memberId.trim().isEmpty()) return result;

        for (BorrowTransaction tx : transactions) {
            if (tx.getMemberId().equalsIgnoreCase(memberId.trim())) {
                result.add(tx);
            }
        }
        return result;
    }

    public void displayRawTransactions() {
        UIRender.renderHeader("Global Transaction ID Journal Registry");
        String[] headers = {"Transaction ID", "Referenced Book ID", "Referenced Member ID", "Status"};
        List<String[]> dataRows = new ArrayList<>();

        for (BorrowTransaction tx : transactions) {
            dataRows.add(new String[]{
                    tx.getId(),
                    tx.getBookId(),
                    tx.getMemberId(),
                    tx.getStatus().toString()
            });
        }

        if (dataRows.isEmpty()) {
            UIRender.renderError("The global transaction registry is empty.");
            return;
        }
        UIRender.renderTable(headers, dataRows);
    }
}