# JunifyDB Demo: Advanced Multi-Engine Queries

This demonstration showcases advanced analytical querying across ANSI SQL and NoSQL engines in JunifyDB.

## Features Demonstrated

1. **Multi-Table ANSI SQL JOINs**: Multi-entity relational query joining `customers`, `orders`, and `order_items`.
2. **Aggregations & GROUP BY ... HAVING**: Statistical computation (`COUNT`, `SUM`, `AVG`, `MIN`, `MAX`) with post-aggregation group filtering.
3. **BETWEEN & Wildcard LIKE**: Range matching and wildcard string pattern matching (`%`, `_`).
4. **Fluent Entity Query API**: High-productivity Java DSL (`db.from(Customer.class).where(...).list()`).
5. **NoSQL Document Compound Filtering**: Multi-condition criteria filtering on unstructured documents.

## Running the Demo

```bash
# Run tests
mvn test

# Run interactive CLI
mvn compile exec:java -Dexec.mainClass="org.junify.db.demo.query.AdvancedQueriesDemoApplication"
```
