package repositories;

import java.util.*;

import exceptions.EntityNotFoundException;
import models.BorrowTransaction;

public class BorrowTransactionRepository {
    private final Map<String, BorrowTransaction> database = new HashMap<>();

    public void save(BorrowTransaction tx) {
        database.put(tx.getTransactionId(), tx);
    }

    public List<BorrowTransaction> findAll() {
        return new ArrayList<>(database.values());
    }

    public BorrowTransaction findById(String id) {
        return database.get(id);
    }

    public void update(String id, BorrowTransaction borrowTransaction) throws EntityNotFoundException {
        BorrowTransaction old = database.get(id);
        if (old == null)
            throw new EntityNotFoundException("Borrow transaction", "Can not found borrow transaction with this id " + id);
        database.put(id, borrowTransaction);
    }
}