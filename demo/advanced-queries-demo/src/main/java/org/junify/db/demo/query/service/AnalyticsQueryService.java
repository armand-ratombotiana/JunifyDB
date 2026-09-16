package org.junify.db.demo.query.service;

import org.junify.db.JunifyDB;
import org.junify.db.demo.query.model.CatalogProduct;
import org.junify.db.demo.query.model.Customer;
import org.junify.db.demo.query.model.Order;
import org.junify.db.demo.query.model.OrderItem;
import org.junify.db.nosql.document.Document;
import org.junify.db.nosql.document.DocumentCollection;
import org.junify.db.sql.SqlResultSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Service demonstrating advanced multi-engine query capabilities in JunifyDB:
 * - Multi-table ANSI SQL JOINs
 * - Group by, Having, and Aggregations (COUNT, SUM, AVG)
 * - SQL BETWEEN and wildcard LIKE pattern matching
 * - Advanced NoSQL compound document queries
 * - Fluent Entity queries
 * - Hybrid Vector + Metadata similarity search
 */
public class AnalyticsQueryService {

    private final JunifyDB db;

    public AnalyticsQueryService(JunifyDB db) {
        this.db = db;
        db.registerEntity(Customer.class, Order.class, OrderItem.class, CatalogProduct.class);
    }

    public void seedData() {
        // 1. Create SQL Relational Tables
        db.sql("CREATE TABLE customers (id VARCHAR PRIMARY KEY, name VARCHAR, email VARCHAR, tier VARCHAR, lifetimeSpend DOUBLE)");
        db.sql("CREATE TABLE orders (id VARCHAR PRIMARY KEY, customerId VARCHAR, totalAmount DOUBLE, status VARCHAR, orderDate VARCHAR)");
        db.sql("CREATE TABLE order_items (id VARCHAR PRIMARY KEY, orderId VARCHAR, productName VARCHAR, quantity INT, unitPrice DOUBLE)");
        db.sql("CREATE TABLE catalog_products (id VARCHAR PRIMARY KEY, name VARCHAR, category VARCHAR, price DOUBLE, rating DOUBLE, inStock INT)");

        // 2. Seed Customers
        db.sql("INSERT INTO customers (id, name, email, tier, lifetimeSpend) VALUES ('cust-1', 'Alice Vance', 'alice@example.com', 'GOLD', 3450.00)");
        db.sql("INSERT INTO customers (id, name, email, tier, lifetimeSpend) VALUES ('cust-2', 'Bob Smith', 'bob@example.com', 'SILVER', 1200.50)");
        db.sql("INSERT INTO customers (id, name, email, tier, lifetimeSpend) VALUES ('cust-3', 'Charlie Brown', 'charlie@example.com', 'PLATINUM', 8900.00)");
        db.sql("INSERT INTO customers (id, name, email, tier, lifetimeSpend) VALUES ('cust-4', 'Diana Prince', 'diana@example.com', 'BRONZE', 450.00)");

        // 3. Seed Orders
        db.sql("INSERT INTO orders (id, customerId, totalAmount, status, orderDate) VALUES ('ord-101', 'cust-1', 450.00, 'COMPLETED', '2026-03-01')");
        db.sql("INSERT INTO orders (id, customerId, totalAmount, status, orderDate) VALUES ('ord-102', 'cust-1', 1200.00, 'COMPLETED', '2026-03-05')");
        db.sql("INSERT INTO orders (id, customerId, totalAmount, status, orderDate) VALUES ('ord-103', 'cust-2', 300.00, 'PENDING', '2026-03-08')");
        db.sql("INSERT INTO orders (id, customerId, totalAmount, status, orderDate) VALUES ('ord-104', 'cust-3', 2500.00, 'COMPLETED', '2026-03-10')");

        // 4. Seed Order Items
        db.sql("INSERT INTO order_items (id, orderId, productName, quantity, unitPrice) VALUES ('item-1', 'ord-101', 'Mechanical Keyboard', 2, 125.00)");
        db.sql("INSERT INTO order_items (id, orderId, productName, quantity, unitPrice) VALUES ('item-2', 'ord-101', 'Ergonomic Mouse', 2, 100.00)");
        db.sql("INSERT INTO order_items (id, orderId, productName, quantity, unitPrice) VALUES ('item-3', 'ord-102', 'UltraWide Monitor', 1, 1200.00)");
        db.sql("INSERT INTO order_items (id, orderId, productName, quantity, unitPrice) VALUES ('item-4', 'ord-103', 'USB-C Dock', 1, 300.00)");
        db.sql("INSERT INTO order_items (id, orderId, productName, quantity, unitPrice) VALUES ('item-5', 'ord-104', 'Developer Laptop Pro', 1, 2500.00)");

        // 5. Seed Catalog Products
        db.sql("INSERT INTO catalog_products (id, name, category, price, rating, inStock) VALUES ('prod-1', 'Developer Laptop Pro', 'Hardware', 2499.99, 4.9, 15)");
        db.sql("INSERT INTO catalog_products (id, name, category, price, rating, inStock) VALUES ('prod-2', 'Gaming Laptop X', 'Hardware', 1899.50, 4.7, 8)");
        db.sql("INSERT INTO catalog_products (id, name, category, price, rating, inStock) VALUES ('prod-3', 'UltraWide Monitor', 'Peripherals', 1199.00, 4.8, 25)");
        db.sql("INSERT INTO catalog_products (id, name, category, price, rating, inStock) VALUES ('prod-4', 'Mechanical Keyboard', 'Peripherals', 149.99, 4.6, 50)");
        db.sql("INSERT INTO catalog_products (id, name, category, price, rating, inStock) VALUES ('prod-5', 'Ergonomic Mouse', 'Peripherals', 89.99, 4.5, 75)");
        db.sql("INSERT INTO catalog_products (id, name, category, price, rating, inStock) VALUES ('prod-6', 'IDE Enterprise License', 'Software', 499.00, 4.9, 999)");

        // 6. Seed Document Collection for NoSQL tests
        DocumentCollection col = db.documentCollection("products_nosql");
        col.insert(Document.of(Map.of("name", "Developer Laptop Pro", "category", "Hardware", "price", 2499.99, "rating", 4.9, "tags", List.of("laptop", "developer", "m3"))).id("doc-1"));
        col.insert(Document.of(Map.of("name", "Gaming Laptop X", "category", "Hardware", "price", 1899.50, "rating", 4.7, "tags", List.of("laptop", "gaming", "rtx"))).id("doc-2"));
        col.insert(Document.of(Map.of("name", "UltraWide Monitor", "category", "Peripherals", "price", 1199.00, "rating", 4.8, "tags", List.of("display", "4k"))).id("doc-3"));
        col.insert(Document.of(Map.of("name", "Mechanical Keyboard", "category", "Peripherals", "price", 149.99, "rating", 4.6, "tags", List.of("accessory", "rgb"))).id("doc-4"));
    }

    /**
     * Demonstrates Multi-Table ANSI SQL JOINs.
     */
    public SqlResultSet executeCustomerOrdersJoin() {
        String query = """
            SELECT o.id, c.name, c.tier, o.totalAmount
            FROM orders o
            JOIN customers c ON o.customerId = c.id
            WHERE o.status = 'COMPLETED'
            ORDER BY o.totalAmount DESC
            """;
        return db.sql(query);
    }

    /**
     * Demonstrates SQL Aggregations.
     */
    public SqlResultSet executeCategoryAggregations() {
        String query = "SELECT COUNT(*), AVG(price), MIN(price), MAX(price), SUM(price) FROM catalog_products";
        return db.sql(query);
    }

    /**
     * Demonstrates SQL BETWEEN and Wildcard LIKE operators.
     */
    public SqlResultSet executeBetweenAndLikeQuery(double minPrice, double maxPrice, String namePattern) {
        String query = """
            SELECT id, name, category, price, rating
            FROM catalog_products
            WHERE price BETWEEN ? AND ?
              AND name LIKE ?
            ORDER BY price ASC
            """;
        return db.sql(query, minPrice, maxPrice, namePattern);
    }

    /**
     * Demonstrates Fluent Entity Queries (`db.from(Entity.class)`).
     */
    public List<Customer> executeFluentCustomerQuery(String tier, double minSpend) {
        return db.from(Customer.class)
                .where("tier = ? AND lifetimeSpend >= ?", tier, minSpend)
                .orderBy("lifetimeSpend DESC")
                .list();
    }

    /**
     * Demonstrates NoSQL Document queries using complex Query criteria.
     */
    public List<Document> executeNoSqlCatalogQuery(String category, double minRating, double maxPrice) {
        DocumentCollection col = db.documentCollection("products_nosql");
        org.junify.db.nosql.document.Query q = org.junify.db.nosql.document.Query.eq("category", category)
                .and(org.junify.db.nosql.document.Query.gte("rating", minRating))
                .and(org.junify.db.nosql.document.Query.lte("price", maxPrice));
        return col.find(q);
    }
}
