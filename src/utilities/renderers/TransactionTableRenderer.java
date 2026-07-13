package utilities.renderers;

import models.BorrowTransaction;
import utilities.TableRenderer;

public class TransactionTableRenderer implements TableRenderer<BorrowTransaction> {
    @Override
    public String[] getHeaders() {
        return new String[]{"TX ID", "Member", "Book", "Borrow", "Due", "Return", "Status", "Fine"};
    }

    @Override
    public String[] toRow(BorrowTransaction tx) {
        return new String[]{
            tx.getTransactionId(),
            tx.getMemberName(),
            tx.getBookTitle(),
            tx.getBorrowDate().toString(),
            tx.getDueDate() != null ? tx.getDueDate().toString() : "-",
            tx.getReturnDate() != null ? tx.getReturnDate().toString() : "OUT",
            tx.getTransactionStatus().name(),
            String.format("%.0f", tx.getFinePaid())
        };
    }
}
