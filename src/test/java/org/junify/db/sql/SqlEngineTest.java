package org.junify.db.sql;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import org.junify.db.JunifyDB;
import org.junify.db.config.JunifyDBConfig;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Embedded SQL Engine Integration Tests")
class SqlEngineTest {

    private JunifyDB db;

    @BeforeEach
    void setUp() {
        db = JunifyDB.create(JunifyDB.embed()
                .storageEngine(JunifyDBConfig.StorageEngineType.IN_MEMORY)
                .buildConfig());
    }

    @AfterEach
    void tearDown() throws Exception {
        if (db != null) db.close();
    }

    @Entity(name = "customers")
    static class CustomerEntity {
        @Id
        private String id;
        private String name;
        private int age;
        private double balance;

        public CustomerEntity() {}
        public CustomerEntity(String id, String name, int age, double balance) {
            this.id = id;
            this.name = name;
            this.age = age;
            this.balance = balance;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public int getAge() { return age; }
        public double getBalance() { return balance; }
    }

    @Test
    @DisplayName("SQL: Basic INSERT, SELECT, WHERE, ORDER BY, LIMIT")
    void testBasicCrudAndQuery() {
        db.sql("INSERT INTO customers (id, name, age, balance) VALUES ('C1', 'Alice', 30, 1500.50)");
        db.sql("INSERT INTO customers (id, name, age, balance) VALUES ('C2', 'Bob', 25, 300.00)");
        db.sql("INSERT INTO customers (id, name, age, balance) VALUES ('C3', 'Charlie', 35, 2200.75)");

        // Simple SELECT *
        SqlResultSet all = db.sql("SELECT * FROM customers");
        assertEquals(3, all.size());

        // WHERE and ORDER BY DESC
        SqlResultSet filtered = db.sql("SELECT name, age, balance FROM customers WHERE age > 26 ORDER BY age DESC");
        assertEquals(2, filtered.size());
        assertEquals("Charlie", filtered.getRows().get(0).getString("name"));
        assertEquals(35, filtered.getRows().get(0).getInt("age"));
        assertEquals("Alice", filtered.getRows().get(1).getString("name"));
        assertEquals(30, filtered.getRows().get(1).getInt("age"));

        // LIMIT and OFFSET
        SqlResultSet paged = db.sql("SELECT name FROM customers ORDER BY age ASC LIMIT 1 OFFSET 1");
        assertEquals(1, paged.size());
        assertEquals("Alice", paged.first().getString("name"));
    }

    @Test
    @DisplayName("SQL: Aggregations COUNT, SUM, AVG, MIN, MAX")
    void testAggregates() {
        db.sql("INSERT INTO items (name, price) VALUES ('Widget', 10.0)");
        db.sql("INSERT INTO items (name, price) VALUES ('Gadget', 20.0)");
        db.sql("INSERT INTO items (name, price) VALUES ('Doohickey', 30.0)");

        SqlResultSet agg = db.sql("SELECT COUNT(*), SUM(price), AVG(price), MIN(price), MAX(price) FROM items");
        assertEquals(1, agg.size());
        SqlRow row = agg.first();

        assertEquals(3L, row.getLong("COUNT(*)"));
        assertEquals(60.0, row.getDouble("SUM(price)"), 0.001);
        assertEquals(20.0, row.getDouble("AVG(price)"), 0.001);
        assertEquals(10.0, row.getDouble("MIN(price)"), 0.001);
        assertEquals(30.0, row.getDouble("MAX(price)"), 0.001);
    }

    @Test
    @DisplayName("SQL: Parameter binding (?, :param) and LIKE, IN")
    void testParametersAndPredicates() {
        db.sql("INSERT INTO products (name, category) VALUES ('iPhone', 'Electronics')");
        db.sql("INSERT INTO products (name, category) VALUES ('iPad', 'Electronics')");
        db.sql("INSERT INTO products (name, category) VALUES ('Desk Chair', 'Furniture')");

        // Positional parameter
        SqlResultSet res1 = db.sql("SELECT * FROM products WHERE category = ?", "Electronics");
        assertEquals(2, res1.size());

        // LIKE
        SqlResultSet res2 = db.sql("SELECT * FROM products WHERE name LIKE 'iPh%'");
        assertEquals(1, res2.size());
        assertEquals("iPhone", res2.first().getString("name"));

        // IN
        SqlResultSet res3 = db.sql("SELECT * FROM products WHERE category IN ('Electronics', 'Furniture')");
        assertEquals(3, res3.size());
    }

    @Test
    @DisplayName("SQL: UPDATE and DELETE statements")
    void testUpdateAndDelete() {
        db.sql("INSERT INTO accounts (id, status) VALUES ('A1', 'ACTIVE')");
        db.sql("INSERT INTO accounts (id, status) VALUES ('A2', 'PENDING')");

        SqlResultSet updateRes = db.sql("UPDATE accounts SET status = 'SUSPENDED' WHERE id = 'A2'");
        assertEquals(1, updateRes.getUpdateCount());

        SqlResultSet recheck = db.sql("SELECT status FROM accounts WHERE id = 'A2'");
        assertEquals("SUSPENDED", recheck.first().getString("status"));

        SqlResultSet deleteRes = db.sql("DELETE FROM accounts WHERE status = 'SUSPENDED'");
        assertEquals(1, deleteRes.getUpdateCount());

        assertEquals(1, db.sql("SELECT * FROM accounts").size());
    }

    @Test
    @DisplayName("SQL: JOIN between two collections")
    void testJoins() {
        db.sql("INSERT INTO users (id, username) VALUES ('U1', 'alice')");
        db.sql("INSERT INTO users (id, username) VALUES ('U2', 'bob')");

        db.sql("INSERT INTO orders (id, userId, total) VALUES ('O101', 'U1', 99.50)");
        db.sql("INSERT INTO orders (id, userId, total) VALUES ('O102', 'U1', 149.00)");
        db.sql("INSERT INTO orders (id, userId, total) VALUES ('O103', 'U2', 45.00)");

        SqlResultSet joined = db.sql("SELECT o.id, u.username, o.total FROM orders o JOIN users u ON o.userId = u.id ORDER BY o.total ASC");
        assertEquals(3, joined.size());

        assertEquals("bob", joined.getRows().get(0).getString("username"));
        assertEquals(45.0, joined.getRows().get(0).getDouble("total"), 0.001);

        assertEquals("alice", joined.getRows().get(1).getString("username"));
        assertEquals(99.50, joined.getRows().get(1).getDouble("total"), 0.001);
    }

    @Test
    @DisplayName("SQL: Direct Entity Mapping via sql(query, EntityClass)")
    void testEntityMapping() {
        db.sql("INSERT INTO customers (id, name, age, balance) VALUES ('C-100', 'Diana', 28, 4500.00)");
        db.sql("INSERT INTO customers (id, name, age, balance) VALUES ('C-101', 'Evan', 40, 6000.00)");

        List<CustomerEntity> list = db.sql("SELECT * FROM customers WHERE age >= 28 ORDER BY age ASC", CustomerEntity.class);
        assertEquals(2, list.size());
        assertEquals("Diana", list.get(0).getName());
        assertEquals(28, list.get(0).getAge());
        assertEquals("Evan", list.get(1).getName());
        assertEquals(40, list.get(1).getAge());
    }

    @Test
    @DisplayName("SQL: BETWEEN and NOT BETWEEN expressions")
    void testBetweenExpression() {
        db.sql("INSERT INTO customers (id, name, age, balance) VALUES ('C10', 'Kelly', 22, 100.0)");
        db.sql("INSERT INTO customers (id, name, age, balance) VALUES ('C11', 'Leo', 30, 500.0)");
        db.sql("INSERT INTO customers (id, name, age, balance) VALUES ('C12', 'Mona', 45, 900.0)");

        SqlResultSet res = db.sql("SELECT * FROM customers WHERE age BETWEEN ? AND ? ORDER BY age ASC", 25, 40);
        assertEquals(1, res.size());
        assertEquals("Leo", res.getRows().get(0).getString("name"));

        SqlResultSet notBetween = db.sql("SELECT * FROM customers WHERE age NOT BETWEEN 25 AND 40 ORDER BY age ASC");
        assertEquals(2, notBetween.size());
        assertEquals("Kelly", notBetween.getRows().get(0).getString("name"));
        assertEquals("Mona", notBetween.getRows().get(1).getString("name"));
    }

    @Test
    @DisplayName("SQL: Wildcard LIKE % and _ matching")
    void testWildcardLike() {
        db.sql("INSERT INTO customers (id, name, age, balance) VALUES ('C20', 'Alexander', 29, 200.0)");
        db.sql("INSERT INTO customers (id, name, age, balance) VALUES ('C21', 'Alexis', 31, 300.0)");
        db.sql("INSERT INTO customers (id, name, age, balance) VALUES ('C22', 'Bob', 45, 400.0)");

        SqlResultSet prefixMatch = db.sql("SELECT * FROM customers WHERE name LIKE 'Alex%' ORDER BY name ASC");
        assertEquals(2, prefixMatch.size());
        assertEquals("Alexander", prefixMatch.getRows().get(0).getString("name"));
        assertEquals("Alexis", prefixMatch.getRows().get(1).getString("name"));

        SqlResultSet containsMatch = db.sql("SELECT * FROM customers WHERE name LIKE '%ex%' ORDER BY name ASC");
        assertEquals(2, containsMatch.size());
    }

    @Test
    @DisplayName("DX: JunifyDB.inMemory() factory and fluent db.from() query builder")
    void testFluentEntityQueryAndInMemoryFactory() throws Exception {
        try (var memDb = JunifyDB.inMemory()) {
            memDb.sql("INSERT INTO customers (id, name, age, balance) VALUES ('M1', 'Zoe', 26, 1200.0)");
            memDb.sql("INSERT INTO customers (id, name, age, balance) VALUES ('M2', 'Zach', 34, 3400.0)");
            memDb.sql("INSERT INTO customers (id, name, age, balance) VALUES ('M3', 'Zelda', 41, 5600.0)");

            List<CustomerEntity> list = memDb.from(CustomerEntity.class)
                    .where("age >= ?", 30)
                    .orderBy("age ASC")
                    .list();

            assertEquals(2, list.size());
            assertEquals("Zach", list.get(0).getName());
            assertEquals("Zelda", list.get(1).getName());

            var first = memDb.from(CustomerEntity.class)
                    .where("balance > ?", 5000.0)
                    .first();
            assertTrue(first.isPresent());
            assertEquals("Zelda", first.get().getName());

            assertEquals(3, memDb.from(CustomerEntity.class).count());
        }
    }
}

