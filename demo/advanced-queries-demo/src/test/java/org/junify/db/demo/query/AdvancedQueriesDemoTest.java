package org.junify.db.demo.query;

import org.junify.db.JunifyDB;
import org.junify.db.demo.query.model.Customer;
import org.junify.db.demo.query.service.AnalyticsQueryService;
import org.junify.db.nosql.document.Document;
import org.junify.db.sql.SqlResultSet;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Automated test suite validating multi-table JOINs, aggregations, GROUP BY,
 * range/pattern filters, fluent entity queries, and NoSQL criteria.
 */
public class AdvancedQueriesDemoTest {

    private JunifyDB db;
    private AnalyticsQueryService service;

    @BeforeEach
    void setUp() {
        db = JunifyDB.inMemory();
        service = new AnalyticsQueryService(db);
        service.seedData();
    }

    @AfterEach
    void tearDown() {
        if (db != null && db.isOpen()) {
            db.close();
        }
    }

    @Test
    @DisplayName("QUERY-01: Multi-Table ANSI SQL JOIN (customers x orders)")
    void testMultiTableJoin() {
        SqlResultSet rs = service.executeCustomerOrdersJoin();
        assertNotNull(rs);
        assertFalse(rs.isEmpty(), "JOIN result should not be empty");
        assertTrue(rs.getColumnNames().contains("id"));
        assertTrue(rs.getColumnNames().contains("name"));
        assertTrue(rs.getColumnNames().contains("tier"));
        assertTrue(rs.getColumnNames().contains("totalAmount"));

        // Verify order descending by totalAmount
        double prevAmount = Double.MAX_VALUE;
        for (var row : rs.getRows()) {
            double amt = ((Number) row.get("totalAmount")).doubleValue();
            assertTrue(amt <= prevAmount, "Rows should be ordered descending by totalAmount");
            prevAmount = amt;
        }
    }

    @Test
    @DisplayName("QUERY-02: SQL Aggregations COUNT, AVG, MIN, MAX, SUM")
    void testCategoryAggregations() {
        SqlResultSet rs = service.executeCategoryAggregations();
        assertNotNull(rs);
        assertEquals(1, rs.size());

        var row = rs.first();
        assertEquals(6L, row.getLong("COUNT(*)"));
        assertTrue(row.getDouble("AVG(price)") > 0);
        assertEquals(89.99, row.getDouble("MIN(price)"), 0.01);
        assertEquals(2499.99, row.getDouble("MAX(price)"), 0.01);
    }

    @Test
    @DisplayName("QUERY-03: SQL BETWEEN and Wildcard LIKE pattern matching")
    void testBetweenAndWildcardLike() {
        SqlResultSet rs = service.executeBetweenAndLikeQuery(1000.0, 3000.0, "%Laptop%");
        assertNotNull(rs);
        assertEquals(2, rs.size(), "Should match Developer Laptop Pro and Gaming Laptop X");

        for (var row : rs.getRows()) {
            String name = (String) row.get("name");
            assertTrue(name.contains("Laptop"));
            double price = ((Number) row.get("price")).doubleValue();
            assertTrue(price >= 1000.0 && price <= 3000.0);
        }
    }

    @Test
    @DisplayName("QUERY-04: Fluent Entity Queries (db.from(Customer.class))")
    void testFluentCustomerQuery() {
        List<Customer> customers = service.executeFluentCustomerQuery("GOLD", 3000.0);
        assertNotNull(customers);
        assertEquals(1, customers.size());
        Customer alice = customers.get(0);
        assertEquals("Alice Vance", alice.getName());
        assertEquals("GOLD", alice.getTier());
        assertTrue(alice.getLifetimeSpend() >= 3000.0);
    }

    @Test
    @DisplayName("QUERY-05: Advanced NoSQL Document Criteria Query")
    void testNoSqlCompoundQuery() {
        List<Document> hardwareDocs = service.executeNoSqlCatalogQuery("Hardware", 4.8, 2600.0);
        assertNotNull(hardwareDocs);
        assertEquals(1, hardwareDocs.size());
        Document doc = hardwareDocs.get(0);
        assertEquals("Developer Laptop Pro", doc.get("name"));
        assertEquals("Hardware", doc.get("category"));
    }
}
