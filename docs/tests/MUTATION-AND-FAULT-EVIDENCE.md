# Mutation and Fault-Injection Evidence

**Audit Date**: September 9, 2026  
**Auditor**: Principal Reliability Engineer  
**Objective**: Prove that the test suite detects intentional defects and regressions.

---

## 1. Mutation Testing Methodology

To verify that existing tests are not merely checking for absence of exceptions, we injected specific mutations into critical database components and observed whether the test suite failed appropriately.

```text
Mutation Injected ──> Execute Test Suite ──> Test Fails (Mutation Killed: PASS)
                                         ──> Test Passes (Mutation Survived: FAIL)
```

---

## 2. Fault Injection Experiments & Results

| Experiment ID | Target Component | Injected Mutation | Target Test | Result | Status |
|---|---|---|---|---|---|
| **MUT-01** | `Transaction.java` | Commented out undo log replay in `rollback()` | `TransactionTest.testRollback()` | `AssertionFailedError: expected 0 but was 1` | **KILLED (PASS)** |
| **MUT-02** | `WriteAheadLog.java` | Bypassed `channel.force(true)` (suppressed fsync) | `DeepInfrastructureTest.testCrashRecovery()` | `AssertionFailedError: missing persisted record after kill` | **KILLED (PASS)** |
| **MUT-03** | `SecondaryIndex.java` | Skipped index eviction on `deleteById()` | `FullFeatureTest.testSecondaryIndexOnDelete()` | `AssertionFailedError: expected empty list but had 1 element` | **KILLED (PASS)** |
| **MUT-04** | `MVCCManager.java` | Forced snapshot read to read uncommitted dirty map | `DeepTransactionTest.testSnapshotIsolation()` | `AssertionFailedError: dirty read observed` | **KILLED (PASS)** |
| **MUT-05** | `SchemaValidator.java`| Always return `ValidationResult.valid()` (bypass check)| `FullFeatureTest.testStrictSchemaValidation()` | `AssertionFailedError: expected SchemaValidationException` | **KILLED (PASS)** |
| **MUT-06** | `CollectionsHandler` | Returned HTTP 200 instead of HTTP 404 on missing doc | `JNoSQLServerTest.testMissingDocument()` | `AssertionFailedError: expected 404 but was 200` | **KILLED (PASS)** |

---

## 3. Evaluation

- **Mutation Kill Rate**: **100%** across tested core paths.
- None of the injected architectural mutations survived undetected.
- The test suite effectively safeguards against silent regressions in transactions, WAL durability, indexing, and REST API contract enforcement.
