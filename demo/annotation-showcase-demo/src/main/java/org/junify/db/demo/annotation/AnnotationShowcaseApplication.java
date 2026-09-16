package org.junify.db.demo.annotation;

import org.junify.db.JunifyDB;
import org.junify.db.adapter.jnosql.EclipseDocumentTemplate;
import org.junify.db.demo.annotation.model.CatalogProduct;
import org.junify.db.demo.annotation.model.CustomerAccount;
import org.junify.db.demo.annotation.model.InvoiceRecord;
import org.junify.db.demo.annotation.model.InvoiceStatus;
import org.junify.db.demo.annotation.repository.CatalogProductRepository;
import org.junify.db.demo.annotation.service.OrderInvoiceService;
import org.junify.db.sql.SqlResultSet;
import org.junify.db.sql.SqlRow;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

/**
 * Main application runner demonstrating:
 * 1. Eclipse JNoSQL (Jakarta NoSQL) standard annotations & JunifyRepository.
 * 2. JPA Specification standard (EntityManager, TypedQuery, EntityTransaction).
 * 3. Hibernate annotation extensions (@UuidGenerator, @CreationTimestamp, @Formula, @Enumerated).
 * 4. Embedded SQL Engine running analytics & JOINs directly over NoSQL collections.
 */
public class AnnotationShowcaseApplication {

    public static void main(String[] args) {
        String dbPath = "./target/annotation-demo-db-" + System.currentTimeMillis();
        System.out.println("==========================================================================");
        System.out.println("   JunifyDB Multi-Standard Annotation & Dual-Engine Demonstration");
        System.out.println("==========================================================================");
        System.out.println("Starting embedded JunifyDB at: " + dbPath + "\n");

        JunifyDB db = JunifyDB.create(JunifyDB.embed()
                .dataDir(Path.of(dbPath))
                .buildConfig());

        try {
            // -------------------------------------------------------------------------
            // 1. Eclipse JNoSQL (Jakarta NoSQL) Entity & Repository
            // -------------------------------------------------------------------------
            System.out.println(">>> SCENARIO 1: Eclipse JNoSQL (Jakarta NoSQL) Annotation & Repository <<<");
            CatalogProductRepository productRepo = new CatalogProductRepository(db);

            productRepo.save(new CatalogProduct("PROD-001", "SKU-LAPTOP", "ZenBook Pro 16", "Electronics", 1499.99, 15));
            productRepo.save(new CatalogProduct("PROD-002", "SKU-PHONE", "Galaxy Ultra Phone", "Electronics", 999.00, 42));
            productRepo.save(new CatalogProduct("PROD-003", "SKU-DESK", "Standing Ergonomic Desk", "Furniture", 450.00, 8));
            productRepo.save(new CatalogProduct("PROD-004", "SKU-CHAIR", "Ergonomic Mesh Chair", "Furniture", 299.50, 0));

            System.out.println("Total products saved: " + productRepo.count());

            List<CatalogProduct> electronics = productRepo.findByCategory("Electronics");
            System.out.println("Found " + electronics.size() + " products in 'Electronics':");
            for (CatalogProduct p : electronics) {
                System.out.println("  - " + p.getTitle() + " ($" + p.getUnitPrice() + ", Stock: " + p.getStockQty() + ")");
            }

            List<CatalogProduct> inStock = productRepo.findInStock(10);
            System.out.println("Found " + inStock.size() + " products with stock >= 10:");
            for (CatalogProduct p : inStock) {
                System.out.println("  - " + p.getTitle() + " (Qty: " + p.getStockQty() + ")");
            }
            System.out.println();

            // -------------------------------------------------------------------------
            // 2. JPA Standard EntityManager & Transactions
            // -------------------------------------------------------------------------
            System.out.println(">>> SCENARIO 2: JPA Specification (EntityManager & TypedQuery) <<<");
            OrderInvoiceService invoiceService = new OrderInvoiceService(db);

            CustomerAccount alice = invoiceService.registerCustomer("CUST-101", "Alice Johnson", "alice@example.com", "PLATINUM", 500.0);
            CustomerAccount bob = invoiceService.registerCustomer("CUST-102", "Bob Smith", "bob@example.com", "GOLD", 150.0);
            System.out.println("Persisted JPA entities: ");
            System.out.println("  - " + alice);
            System.out.println("  - " + bob);
            System.out.println();

            // -------------------------------------------------------------------------
            // 3. Hibernate Annotations (@UuidGenerator, @CreationTimestamp, @Formula)
            // -------------------------------------------------------------------------
            System.out.println(">>> SCENARIO 3: Hibernate Annotations In Action <<<");
            InvoiceRecord inv1 = invoiceService.createInvoice(alice.getCustomerId(), 1000.0, InvoiceStatus.PAID);
            InvoiceRecord inv2 = invoiceService.createInvoice(alice.getCustomerId(), 250.0, InvoiceStatus.PENDING);
            InvoiceRecord inv3 = invoiceService.createInvoice(bob.getCustomerId(), 500.0, InvoiceStatus.PAID);
            InvoiceRecord inv4 = invoiceService.createInvoice(bob.getCustomerId(), 150.0, InvoiceStatus.CANCELLED);

            System.out.println("Generated Invoices with Hibernate Automated Lifecycle:");
            for (InvoiceRecord inv : List.of(inv1, inv2, inv3, inv4)) {
                System.out.println("  - ID: " + inv.getInvoiceId()
                        + " | Amount: $" + inv.getAmount()
                        + " | With Tax (Formula): $" + inv.getTotalWithTax()
                        + " | Status: " + inv.getStatus()
                        + " | CreatedAt: " + inv.getCreatedAt());
            }
            System.out.println();

            // -------------------------------------------------------------------------
            // 4. Dual Engine: Direct Embedded SQL Queries & JOINs Over NoSQL Collections
            // -------------------------------------------------------------------------
            System.out.println(">>> SCENARIO 4: Dual Engine - ANSI SQL Aggregation & JOINs over NoSQL Collections <<<");
            System.out.println("[SQL] Executing aggregation query: SELECT status, COUNT(*), SUM(amount), AVG(amount)...");
            SqlResultSet summary = invoiceService.getInvoiceSummary();
            for (SqlRow row : summary.getRows()) {
                System.out.printf("  Status: %-10s | Invoices: %-2s | Total: $%8.2f | Avg: $%7.2f%n",
                        row.get("status"), row.get("count"),
                        ((Number) row.get("total_amount")).doubleValue(),
                        ((Number) row.get("avg_amount")).doubleValue());
            }
            System.out.println();

            System.out.println("[SQL] Executing relational JOIN query: customer_accounts JOIN invoices...");
            SqlResultSet joinResult = invoiceService.getCustomerInvoiceDetails();
            for (SqlRow row : joinResult.getRows()) {
                System.out.printf("  Customer: %-15s (%-8s) -> Invoice #%s | Amount: $%6.2f | Taxed: $%6.2f | Status: %s%n",
                        row.get("full_name"), row.get("loyalty_tier"),
                        row.get("invoice_id").toString().substring(0, 8) + "...",
                        ((Number) row.get("amount")).doubleValue(),
                        ((Number) row.get("total_with_tax")).doubleValue(),
                        row.get("status"));
            }
            System.out.println();

            System.out.println("==========================================================================");
            System.out.println("   Demonstration Completed Successfully!");
            System.out.println("==========================================================================");

        } finally {
            db.close();
            deleteDirectory(new File(dbPath));
        }
    }

    private static void deleteDirectory(File dir) {
        if (dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.isDirectory()) deleteDirectory(f);
                    else f.delete();
                }
            }
            dir.delete();
        }
    }
}
