# JunifyDB: Java Developer Productivity Analysis & Strategic Roadmap

**Target Persona**: Java / JVM Enterprise and Cloud-Native Developers  
**Primary Mission**: Make embedding, testing, and managing multi-model and relational data as fast, pleasant, and productive as possible.  

---

## 1. Productivity Friction Points Identified

### A. Setup & Initialization Boilerplate
- **Current State**: Developers configure `JunifyDBConfig.builder().engineType(...).baseDirectory(...).build()` and then pass to `JunifyDB.open(config)`.
- **Friction**: New developers wanting an instant in-memory database for a unit test or quick prototype have to write 5 lines of configuration.
- **Solution**: Provide static factory methods with zero configuration:
  ```java
  JunifyDB db = JunifyDB.inMemory(); // Instant ephemeral database
  JunifyDB db = JunifyDB.openTemp(); // Temporary disk database with auto-delete
  ```

### B. Fluent Query Building & SQL Sugar
- **Current State**: Developers write SQL strings: `db.sql("SELECT * FROM users WHERE age > ?", 21)`.
- **Friction**: Typos in column names or table names are only detected at runtime; string concatenation can be cumbersome.
- **Solution**: Provide fluent query helpers and record mapper shortcuts:
  ```java
  List<User> users = db.from(User.class)
                       .where("age > ?", 21)
                       .orderBy("name")
                       .limit(10)
                       .list();
  ```

### C. Automated Schema & Table DDL Generation from Entities
- **Current State**: Document collections are created implicitly on first document insert; SQL queries over new entity types expect collections to exist.
- **Friction**: Developers creating `@Entity` classes must insert at least one document before querying via SQL.
- **Solution**: Provide `db.registerEntity(Class<?> entityClass)` to automatically create the collection, secondary indexes from `@Id` and `@Index`, and schema constraints.

### D. Tri-Standard Annotation Showcase Completeness
- **Current State**: `annotation-showcase-demo` shows `@Entity`, `@Id`, `@Column`, `@Table`, `@CreationTimestamp`, `@UpdateTimestamp`, `@UuidGenerator`, and `@Formula`.
- **Friction**: Lifecycle callbacks (`@PrePersist`, `@PreUpdate`) are parsed by `AnnotationResolver` but not invoked by `EntityMapper`.
- **Solution**: Implement entity lifecycle listener invocation during `EntityMapper.toDocument()` and `EntityMapper.fromDocument()`.

---

## 2. Productivity Enhancement Roadmap

1. **Phase 1: Fluent Developer Factories**
   - Add `JunifyDB.inMemory()` and `JunifyDB.openTemp()`.
   - Add `db.from(Class<T>)` fluent querying helper.

2. **Phase 2: Entity Lifecycle & Annotation Enhancements**
   - Wire `@PrePersist`, `@PreUpdate`, `@PostLoad` in `EntityMapper`.
   - Support `db.registerEntity(Class<?>)` for zero-boilerplate collection auto-registration.

3. **Phase 3: SQL Engine Syntax Additions**
   - Add `BETWEEN` expression support in `SqlParser` and `Expression`.
   - Add wildcards to `LIKE` evaluation.

4. **Phase 4: Demos & Showcase Modernization**
   - Add SQL queries into `spring-boot-demo` and `quarkus-demo`.
   - Verify all tests across all modules.
