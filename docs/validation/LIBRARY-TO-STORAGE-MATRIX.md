# Library-to-Storage Validation Matrix

**Audit Date**: September 9, 2026  
**Auditor**: Storage Systems Specialist

---

## 1. Storage Engine Invocation Proof

| Library Operation | Target SPI Method | Storage Engines Reached | Verification Proof |
|---|---|---|---|
| `DocumentCollection.insert` | `engine.put` | InMemory, File, LSM, BTree | Byte serialized JSON written to storage |
| `DocumentCollection.findById` | `engine.get` | InMemory, File, LSM, BTree | Bytes retrieved and deserialized to `Document` |
| `DocumentCollection.deleteById`| `engine.delete`| InMemory, File, LSM, BTree | Key tombstoned / removed from storage |
| `KeyValueBucket.put` | `engine.put` | InMemory, File, LSM, BTree | Raw or serialized value stored |
| `ColumnFamily.put` | `engine.put` | InMemory, File, LSM, BTree | Multi-version cell record stored |
| `Transaction.commit` | `engine.flush` | File, LSM, BTree | fsync invoked on WAL channel |
| `Transaction.rollback` | `engine.rollback` | InMemory, File, LSM, BTree | Undo log replayed, mutations reverted |

---

## 2. Verdict
Domain classes do not bypass the `StorageEngine` SPI. All persistence flows through the configured engine implementation.
