package repositories;

import java.util.ArrayList;
import java.util.List;
import models.BorrowTransaction;

public class BorrowTransactionRepository {
    private final List<BorrowTransaction> database = new ArrayList<>();

    public void save(BorrowTransaction tx) {
        database.add(tx);
    }

    public List<BorrowTransaction> findAll() {
        return database;
    }
}