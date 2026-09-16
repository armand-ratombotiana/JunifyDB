package org.junify.db.demo.annotation.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import org.junify.db.JunifyDB;
import org.junify.db.demo.annotation.model.CustomerAccount;
import org.junify.db.demo.annotation.model.InvoiceRecord;
import org.junify.db.demo.annotation.model.InvoiceStatus;
import org.junify.db.sql.SqlResultSet;

import java.util.List;

/**
 * Service demonstrating JPA EntityManager standard persistence, transaction handling,
 * TypedQuery execution, and direct SQL engine queries over the underlying collections.
 */
public class OrderInvoiceService {

    private final JunifyDB db;
    private final EntityManager em;

    public OrderInvoiceService(JunifyDB db) {
        this.db = db;
        this.em = org.junify.db.jpa.JunifyPersistence.createEntityManager(db);
    }

    public CustomerAccount registerCustomer(String id, String name, String email, String tier, double credit) {
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        try {
            CustomerAccount account = new CustomerAccount(id, name, email, tier, credit);
            em.persist(account);
            tx.commit();
            return account;
        } catch (Exception e) {
            tx.rollback();
            throw new RuntimeException("Failed to register customer", e);
        }
    }

    public InvoiceRecord createInvoice(String customerId, double amount, InvoiceStatus status) {
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        try {
            InvoiceRecord invoice = new InvoiceRecord(customerId, amount, status);
            // JPA persist triggers Hibernate @UuidGenerator, @CreationTimestamp, @UpdateTimestamp, @Formula
            em.persist(invoice);
            tx.commit();
            return invoice;
        } catch (Exception e) {
            tx.rollback();
            throw new RuntimeException("Failed to create invoice", e);
        }
    }

    public CustomerAccount findCustomer(String id) {
        return em.find(CustomerAccount.class, id);
    }

    public InvoiceRecord findInvoice(String id) {
        return em.find(InvoiceRecord.class, id);
    }

    public List<InvoiceRecord> findInvoicesByCustomer(String customerId) {
        TypedQuery<InvoiceRecord> query = em.createQuery(
                "SELECT * FROM invoices WHERE customer_id = :cid ORDER BY created_at DESC",
                InvoiceRecord.class
        );
        query.setParameter("cid", customerId);
        return query.getResultList();
    }

    public SqlResultSet getInvoiceSummary() {
        // Dual engine: query NoSQL-persisted entities with ANSI SQL Aggregations & JOINs!
        return db.sql(
                "SELECT i.status, COUNT(*) AS count, SUM(i.amount) AS total_amount, AVG(i.amount) AS avg_amount " +
                "FROM invoices i " +
                "GROUP BY i.status " +
                "ORDER BY total_amount DESC"
        );
    }

    public SqlResultSet getCustomerInvoiceDetails() {
        return db.sql(
                "SELECT c.customer_id, c.full_name, c.loyalty_tier, i.invoice_id, i.amount, i.total_with_tax, i.status " +
                "FROM customer_accounts c " +
                "JOIN invoices i ON c.customer_id = i.customer_id " +
                "ORDER BY i.amount DESC"
        );
    }

    public void close() {
        em.close();
    }
}
