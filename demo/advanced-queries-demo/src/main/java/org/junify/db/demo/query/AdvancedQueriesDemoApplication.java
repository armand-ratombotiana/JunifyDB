package org.junify.db.demo.query;

import org.junify.db.JunifyDB;
import org.junify.db.demo.query.model.Customer;
import org.junify.db.demo.query.service.AnalyticsQueryService;
import org.junify.db.nosql.document.Document;
import org.junify.db.sql.SqlResultSet;

import java.util.List;

/**
 * Interactive CLI runner demonstrating advanced multi-engine query capabilities in JunifyDB.
 */
public class AdvancedQueriesDemoApplication {

    public static void main(String[] args) {
        System.out.println("=========================================================");
        System.out.println("   JunifyDB Demo: Advanced Multi-Engine Query Engine     ");
        System.out.println("=========================================================");

        try (JunifyDB db = JunifyDB.inMemory()) {
            AnalyticsQueryService service = new AnalyticsQueryService(db);
            service.seedData();
            System.out.println("Seeded database with relational tables and document collections.\n");

            // 1. Multi-Table ANSI SQL JOIN
            System.out.println("[1/4] Executing Multi-Table ANSI SQL JOIN (customers x orders x items)...");
            SqlResultSet joinResults = service.executeCustomerOrdersJoin();
            System.out.println("  Columns: " + joinResults.getColumnNames());
            System.out.println("  Matched Rows (" + joinResults.size() + "):");
            for (var row : joinResults.getRows()) {
                System.out.printf("   - %s (%s) | Order: %s | Item: %s (qty: %s, unit: $%s)\n",
                        row.get("customer_name"), row.get("tier"), row.get("order_id"),
                        row.get("productName"), row.get("quantity"), row.get("unitPrice"));
            }

            // 2. Aggregations & GROUP BY ... HAVING
            System.out.println("\n[2/4] Executing SQL Aggregations (GROUP BY category HAVING count > 1)...");
            SqlResultSet aggResults = service.executeCategoryAggregations();
            for (var row : aggResults.getRows()) {
                System.out.printf("   - Category: %-12s | Count: %s | Avg Price: $%s | Total: $%s\n",
                        row.get("category"), row.get("product_count"), row.get("avg_price"), row.get("total_inventory_value"));
            }

            // 3. SQL BETWEEN and Wildcard LIKE
            System.out.println("\n[3/4] Executing SQL BETWEEN and Wildcard LIKE (price BETWEEN 1000 AND 3000 AND name LIKE '%Laptop%')...");
            SqlResultSet likeResults = service.executeBetweenAndLikeQuery(1000.0, 3000.0, "%Laptop%");
            for (var row : likeResults.getRows()) {
                System.out.printf("   - %s | Price: $%s | Rating: %s\n",
                        row.get("name"), row.get("price"), row.get("rating"));
            }

            // 4. Fluent Entity Query
            System.out.println("\n[4/4] Executing Fluent Entity Query (db.from(Customer.class).where(...))...");
            List<Customer> goldCustomers = service.executeFluentCustomerQuery("GOLD", 3000.0);
            for (Customer c : goldCustomers) {
                System.out.printf("   - %s | Email: %s | Tier: %s | Spend: $%.2f\n",
                        c.getName(), c.getEmail(), c.getTier(), c.getLifetimeSpend());
            }

            System.out.println("\n=========================================================");
            System.out.println("   Advanced Queries Demo Completed Successfully!         ");
            System.out.println("=========================================================");
        }
    }
}
