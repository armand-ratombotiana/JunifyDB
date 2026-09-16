# JunifyDB Annotation Showcase Demo

This demonstration application showcases **JunifyDB's** multi-standard annotation capabilities and dual-engine architecture:
- **Eclipse JNoSQL (Jakarta NoSQL)** standard annotations & generic repository (`JunifyRepository`).
- **JPA Specification Standard** (`EntityManager`, `TypedQuery`, `EntityTransaction`).
- **Hibernate Annotation Extensions** (`@UuidGenerator`, `@CreationTimestamp`, `@UpdateTimestamp`, `@Formula`, `@Enumerated`).
- **Dual-Engine Persistence & Analytics**: Seamless interoperability between NoSQL document storage and ANSI SQL queries with aggregations and relational JOINs.

---

## Architecture Overview

```
demo/annotation-showcase-demo/
├── pom.xml
├── README.md
└── src/
    ├── main/java/org/junify/db/demo/annotation/
    │   ├── AnnotationShowcaseApplication.java     # Runnable CLI demonstration
    │   ├── model/
    │   │   ├── CatalogProduct.java               # Eclipse JNoSQL (@Entity, @Id, @Column)
    │   │   ├── CustomerAccount.java              # JPA Standard (@Entity, @Table, @Id, @Column)
    │   │   ├── InvoiceRecord.java                # Hibernate (@UuidGenerator, @CreationTimestamp, @Formula)
    │   │   └── InvoiceStatus.java                # Enum mapped via @Enumerated(EnumType.STRING)
    │   ├── repository/
    │   │   └── CatalogProductRepository.java     # Extends JunifyRepository<CatalogProduct, String>
    │   └── service/
    │       └── OrderInvoiceService.java          # Uses EntityManager, TypedQuery, and db.sql()
    └── test/java/org/junify/db/demo/annotation/
        └── AnnotationShowcaseTest.java           # Full integration test suite
```

---

## 1. Eclipse JNoSQL Standard

Define domain entities with standard `jakarta.nosql.*` annotations:

```java
@Entity("catalog_products")
public class CatalogProduct {
    @Id
    private String id;

    @Column("title")
    private String title;

    @Column("unit_price")
    private Double unitPrice;
    
    // Getters and setters
}
```

Use `JunifyRepository<T, ID>` for out-of-the-box CRUD and derived query operations:

```java
public class CatalogProductRepository extends JunifyRepository<CatalogProduct, String> {
    public CatalogProductRepository(DocumentTemplate template) {
        super(template, CatalogProduct.class);
    }

    public List<CatalogProduct> findByCategory(String category) {
        return findBy("category", category);
    }
}
```

---

## 2. JPA Specification Standard

Manage transactional entities using standard `jakarta.persistence.*` APIs:

```java
EntityManager em = db.createEntityManager();
EntityTransaction tx = em.getTransaction();
tx.begin();

CustomerAccount account = new CustomerAccount("CUST-1", "Alice", "alice@example.com", "PLATINUM", 500.0);
em.persist(account);
tx.commit();

// Standard JPA TypedQuery with named parameter binding
TypedQuery<CustomerAccount> query = em.createQuery(
    "SELECT * FROM customer_accounts WHERE loyalty_tier = :tier", CustomerAccount.class);
query.setParameter("tier", "PLATINUM");
List<CustomerAccount> results = query.getResultList();
```

---

## 3. Hibernate Annotations

Take advantage of Hibernate lifecycle and generation annotations without needing heavy ORM infrastructure:

```java
@Entity
@Table(name = "invoices")
public class InvoiceRecord {
    @Id
    @UuidGenerator // Automated UUID generation on persist
    private String invoiceId;

    @CreationTimestamp // Automated audit creation timestamp
    private Instant createdAt;

    @UpdateTimestamp // Automated update timestamp
    private Instant updatedAt;

    private Double amount;

    @Formula("amount * 1.20") // Automated formula evaluation (e.g. VAT calculation)
    private Double totalWithTax;

    @Enumerated(EnumType.STRING)
    private InvoiceStatus status;
}
```

---

## 4. Dual Engine: ANSI SQL over NoSQL Collections

Entities persisted through NoSQL `DocumentTemplate`, `JunifyRepository`, or JPA `EntityManager` are immediately queryable via ANSI SQL:

```java
// SQL Aggregations
SqlResultSet summary = db.sql(
    "SELECT status, COUNT(*) AS count, SUM(amount) AS total, AVG(amount) AS avg " +
    "FROM invoices GROUP BY status ORDER BY total DESC"
);

// Relational JOINs across NoSQL Document Collections
SqlResultSet joinResult = db.sql(
    "SELECT c.full_name, c.loyalty_tier, i.amount, i.total_with_tax " +
    "FROM customer_accounts c " +
    "JOIN invoices i ON c.customer_id = i.customer_id " +
    "ORDER BY i.amount DESC"
);
```

---

## Running the Demo

### Via Maven Exec Plugin:
```bash
cd demo/annotation-showcase-demo
mvn compile exec:java
```

### Running the Integration Tests:
```bash
cd demo/annotation-showcase-demo
mvn test
```
