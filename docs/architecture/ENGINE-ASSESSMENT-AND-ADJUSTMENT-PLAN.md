# JunifyDB Engine Assessment and Adjustment Plan

**Assessment date:** 2026-09-17  
**Baseline:** repository state at `6b8cece`  
**Scope:** storage engines, SQL/NoSQL surfaces, transactions, integrations, documentation, and developer experience.

## Executive assessment

JunifyDB has a strong and differentiated idea: a pure-Java, in-process multi-model database with a small API and optional framework integrations. The project is strongest as a test/prototyping database and as a unified NoSQL API. The embedded lifecycle, zero-daemon setup, document/KV/column-family breadth, admin console, and framework demos are coherent product choices.

The main risk is not lack of features; it is contract clarity. The repository presents SQL, ACID, WAL, B-Tree, LSM, CDC, vector search, and standards support with production-grade language, while several capabilities are partial, scan-based, or not exposed through the storage SPI. The next release should prefer fewer, explicitly tested guarantees over a wider feature matrix.

## What is good

| Area | Evidence | Assessment |
|---|---|---|
| Embedded ergonomics | `JunifyDB.inMemory()`, try-with-resources, no external daemon | Excellent fit for unit tests, demos, local tools, and small single-process services. |
| Multi-model API | Document, KV, list/set/hash, and column-family APIs share one lifecycle | Valuable differentiation from H2, which is relational/JDBC-first. |
| Storage seam | `StorageEngine` isolates basic reads/writes and permits multiple implementations | Correct architectural direction; it makes test and persistence policies replaceable. |
| Developer surface | Spring Boot, Quarkus, Micronaut, Vert.x demos plus HTTP console | Good adoption funnel and useful integration evidence. |
| Test breadth | Core tests cover CRUD, queries, persistence, KV structures, console, and framework demos | Strong regression base; it needs invariant/property tests for storage semantics. |
| Documentation intent | Architecture, ADRs, runbooks, demos, and standards documents exist | The project invests in explainability, even though some claims need calibration. |

## What is wrong or overstated

### Storage engines

* **InMemory** is appropriate for volatile state, but it is not a relational compatibility layer. `scan` returns values, while `getAll` preserves missing entries as `null`; those semantics should be documented and tested as SPI rules.
* **FileEngine** keeps the full dataset in a `ConcurrentHashMap` and periodically rewrites JSON snapshots. It is a snapshot engine with a WAL, not an append-only database or bounded-memory store. Flush and WAL error paths currently report to stderr rather than exposing a failure to the caller.
* **BTreeEngine** is an in-memory map persisted as one sorted binary file. It provides useful sorted/prefix helpers, but it is not a page-oriented B-tree and does not support datasets larger than available RAM. The name and README comparison should be corrected or the implementation should be replaced.
* **LSMTreeEngine** has the right conceptual components (memtable, WAL, SSTables, Bloom filter, compaction), but its read model must reconcile versions across layers. Before this change, scans could return old versions and tombstones could fail to suppress older records; restart ordering also loaded SSTables oldest-first. This adjustment fixes newest-value reconciliation and adds regression coverage.
* None of the concrete engines override `supportsTransactions()`, `supportsIndexes()`, or `isPersistent()`. The database-level MVCC layer therefore must not be described as engine-level ACID durability without an explicit transaction protocol and capability contract.

### SQL and NoSQL

* The SQL engine is a useful relational query facade over documents, but it is an in-memory execution pipeline: joins are nested-loop joins and collections are scanned before predicates are evaluated. Calling it “ANSI SQL” should be qualified as a supported subset, not compatibility with a full SQL database.
* Existing assessment text correctly identifies subqueries, index-aware planning, and complete `LIKE` semantics as gaps. Those should be release criteria, not hidden assumptions.
* NoSQL breadth is a strength, but vector/HNSW, CDC connectors, migration, backup, and security need capability-specific durability, concurrency, and operational tests before being positioned as production equivalents.

### Documentation and vision

* The strongest positioning is “H2-like developer friction for embedded multi-model NoSQL,” not “H2 plus a full SQL database.” Keep the comparison, but make the boundary explicit.
* Claims such as “production-grade,” “ACID,” “guaranteeing crash recovery,” “page-based B-Tree,” and “full ANSI SQL” should be tied to a tested guarantee or softened.
* The documentation should publish an engine decision table: data-loss behavior, durability point, memory model, concurrency, transactions, indexes, recovery, and recommended workload.

## Comparison with the Baeldung philosophy

The Baeldung articles describe embedded databases primarily as a convenience boundary: add a dependency, configure an in-memory URL, run tests or local services, and accept that data disappears at process shutdown. H2 adds mature SQL/JDBC/JPA integration, file mode, seed scripts, and an optional browser console.

JunifyDB shares the same excellent defaults:

* process-local lifecycle and no infrastructure;
* fast disposable databases for tests;
* an explicit file-backed alternative;
* framework integration and a developer console.

JunifyDB deliberately differs in model and contract:

* H2 is relational and JDBC-compatible; JunifyDB is document/KV/column-family first, with a SQL query facade.
* H2 benefits from mature SQL dialect, schema, transaction, and tooling expectations; JunifyDB trades that maturity for native multi-model operations and pure-Java embedding.
* Spring Boot's H2 pattern makes configuration discoverable through standard `DataSource` properties and SQL initialization. JunifyDB should mirror that ergonomics with clear engine profiles, deterministic schema/seed hooks, and observable startup configuration rather than asking users to infer semantics from an enum.
* Both projects must warn that in-memory mode is not production durability. JunifyDB's file, LSM, and B-Tree modes need especially precise durability wording because their implementations differ materially.

References: [Java In-Memory Databases](https://www.baeldung.com/java-in-memory-databases) and [Spring Boot and H2](https://www.baeldung.com/spring-boot-h2-database).

## Adjustment plan

### P0: make contracts honest and safe

1. Keep the LSM newest-value/tombstone reconciliation fix in this release.
2. Add a capability matrix to the public API or documentation; return capabilities from each engine instead of inheriting misleading defaults.
3. Define and test durability points (`put`, `flush`, `close`, crash recovery) for every persistent engine.
4. Stop converting persistence failures into stderr-only messages; return or throw typed exceptions from flush, checkpoint, and background writer failures.
5. Rename or reframe BTreeEngine as a sorted file-backed map until a real paged B+Tree exists.

### P1: make the useful path fast

1. Add engine contract tests that run against every implementation: CRUD, missing keys, deletes, scans, restart, concurrent access, and close behavior.
2. Add LSM property tests for repeated updates/deletes, restart, compaction, and duplicate suppression.
3. Add SQL planner tests for indexed equality, null semantics, joins, grouping, ordering, and parameter binding; document unsupported subqueries and DDL constraints.
4. Make secondary indexes visible to SQL execution, or explicitly document that SQL is scan-based.

### P2: improve adoption

1. Publish “choose your engine” recipes: `IN_MEMORY` for tests, `FILE` for small durable local state, LSM for write-heavy workloads, and a future paged tree for bounded-memory range workloads.
2. Add Spring Boot properties and initialization examples analogous to `spring.datasource.*`, while preserving the standalone API.
3. Separate “implemented,” “experimental,” and “planned” features in README and website tables.
4. Add benchmark methodology and reproducible results instead of unsupported latency/startup numbers.

### P3: expand only after the contracts stabilize

Subqueries, cost-based/index-aware planning, real paged storage, stronger transaction coordination, online backup/restore verification, and production CDC connectors should follow the contract work. Do not add more data models until recovery and capability semantics are externally testable.

## Definition of done for the next release

* Every engine has a documented memory, durability, concurrency, and transaction profile.
* Persistent-engine failures are observable by callers and covered by restart tests.
* The SQL support table lists supported syntax and deliberate omissions.
* README and website claims match executable tests and benchmark methodology.
* A single engine contract suite runs against all engines without engine-specific skips except for explicitly declared capabilities.

