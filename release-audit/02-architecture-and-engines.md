# Phase 02 — Architecture & Multi-Engine Validation

## System Architecture

JunifyDB is an embedded, zero-dependency, multi-model NoSQL database engine engineered for modern Java applications.

```
+-------------------------------------------------------------------------+
|                        Application Layer                                |
|  Spring Boot Starter  |  Quarkus Extension  |  Micronaut  |  Vert.x     |
+-------------------------------------------------------------------------+
                                    |
+-------------------------------------------------------------------------+
|                         JunifyDB API & SPI                              |
|   Query Parser & DSL  |  Schema & Annotations  |  Transactions (ACID)   |
|   HTTP Administration Console  |  PortManager  |  Security & Audit Logs |
+-------------------------------------------------------------------------+
                                    |
+-------------------------------------------------------------------------+
|                           Engine Core                                   |
|   Document Engine   |   Key-Value Engine   |   Column Family Engine     |
|   Graph Engine      |   Time-Series Engine |   SQL Engine Layer         |
+-------------------------------------------------------------------------+
                                    |
+-------------------------------------------------------------------------+
|                         Storage & Index SPI                             |
|   Memory Engine     |   File Engine (MMap) |   LSM-Tree Engine (SST)    |
|   B-Tree Index      |   Text Index         |   HNSW / Vector Index      |
+-------------------------------------------------------------------------+
```

---

## Validated Engine Implementations

### 1. Document Engine (`org.junify.db.nosql.document`)
- **JSON Serialization/Deserialization**: Powered by internal `JsonSerde` using Jackson Databind.
- **Indexing**: Secondary B-Tree and Text inverted indexes with automated query optimization.
- **Aggregation**: Group-by, distinct, min, max, average, sum across arbitrary fields with `DocumentAggregation`.
- **Query Parser**: Supports MongoDB-style operator expressions (`$eq`, `$ne`, `$gt`, `$gte`, `$lt`, `$lte`, `$in`, `$nin`, `$regex`, `$exists`).

### 2. Key-Value Engine (`org.junify.db.nosql.kv`)
- High-throughput primary key lookups with sub-microsecond latency.
- Time-to-Live (TTL) automatic expiration and background cleanup.
- Compare-and-Swap (CAS) atomic operations for distributed locking and state management.

### 3. Column Family Store (`org.junify.db.nosql.column`)
- Multidimensional row key, column family, and timestamped cell storage.
- Wide-column querying and range slicing.

### 4. Graph Engine (`org.junify.db.nosql.graph`)
- In-memory property graph with nodes, edges, labels, and directional queries (IN, OUT, BOTH).
- Traversal APIs for shortest path, breadth-first search, and depth-first search.

### 5. Time-Series Engine (`org.junify.db.nosql.timeseries`)
- High-frequency metrics ingestion timestamped with epoch milliseconds/nanoseconds.
- Downsampling aggregations (1m, 5m, 1h, 1d) with rolling window rollups.
