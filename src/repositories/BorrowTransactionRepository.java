package repositories;

import java.util.*;

import exceptions.EntityNotFoundException;
import models.BorrowTransaction;
import repositories.io.Database;

public class BorrowTransactionRepository {
    private final Map<String, BorrowTransaction> database = new HashMap<>();
    private final Database<BorrowTransaction> borrowTransactionDatabase;

    public BorrowTransactionRepository(Database<BorrowTransaction> borrowTransactionDatabase) {
        this.borrowTransactionDatabase = borrowTransactionDatabase;
        borrowTransactionDatabase.loadContent().forEach(
                d -> database.put(d.getTransactionId(), d)
        );
    }

    public void save(BorrowTransaction tx) {
        database.put(tx.getTransactionId(), tx);
        borrowTransactionDatabase.save(database);
    }

    public List<BorrowTransaction> findAll() {
        return new ArrayList<>(database.values());
    }

    public BorrowTransaction findById(String id) {
        return database.get(id);
    }

    public void update(String id, BorrowTransaction borrowTransaction) {
        BorrowTransaction old = database.get(id);
        if (old == null)
            return;
        database.put(borrowTransaction.getTransactionId(), borrowTransaction);
        borrowTransactionDatabase.save(database);
    }
}

