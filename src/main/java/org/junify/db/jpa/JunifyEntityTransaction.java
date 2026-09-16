package org.junify.db.jpa;

import jakarta.persistence.EntityTransaction;
import jakarta.persistence.RollbackException;
import org.junify.db.JunifyDB;
import org.junify.db.transaction.mvcc.Transaction;

public class JunifyEntityTransaction implements EntityTransaction {

    private final JunifyDB db;
    private Transaction activeTx;
    private boolean rollbackOnly = false;

    public JunifyEntityTransaction(JunifyDB db) {
        this.db = db;
    }

    @Override
    public void begin() {
        if (isActive()) {
            throw new IllegalStateException("Transaction is already active");
        }
        this.activeTx = db.beginTransaction();
        this.rollbackOnly = false;
    }

    @Override
    public void commit() {
        if (!isActive()) {
            throw new IllegalStateException("No active transaction to commit");
        }
        if (rollbackOnly) {
            rollback();
            throw new RollbackException("Transaction marked as rollback only");
        }
        try {
            activeTx.commit();
        } finally {
            activeTx = null;
        }
    }

    @Override
    public void rollback() {
        if (!isActive()) {
            throw new IllegalStateException("No active transaction to rollback");
        }
        try {
            activeTx.rollback();
        } finally {
            activeTx = null;
            rollbackOnly = false;
        }
    }

    @Override
    public void setRollbackOnly() {
        if (!isActive()) {
            throw new IllegalStateException("No active transaction");
        }
        this.rollbackOnly = true;
    }

    @Override
    public boolean getRollbackOnly() {
        if (!isActive()) {
            throw new IllegalStateException("No active transaction");
        }
        return rollbackOnly;
    }

    @Override
    public boolean isActive() {
        return activeTx != null && activeTx.status() == Transaction.Status.ACTIVE;
    }

    public Transaction getActiveTx() {
        return activeTx;
    }
}
