# Library to Storage Engine Traceability Matrix

**Audit Date**: September 9, 2026  
**Auditor**: Database Internals Architect  

---

## 1. Domain Library to Storage SPI Delegations

| Library Operation | Storage Engine SPI Method | Memory Engine Effect | File Engine Effect | B-Tree Engine Effect | LSM-Tree Engine Effect |
|---|---|---|---|---|---|
| `DocumentCollection.insert()` | `StorageEngine.put(col, id, bytes)` | ConcurrentHashMap put | Append to data file + WAL log | Page insertion + WAL log | MemTable put + WAL log |
| `DocumentCollection.update()` | `StorageEngine.put(col, id, bytes)` | ConcurrentHashMap replace | Overwrite/append + WAL log | B-Tree page split/update | New version in MemTable |
| `DocumentCollection.deleteById()`| `StorageEngine.delete(col, id)` | ConcurrentHashMap remove| Tombstone record written | Page entry purged | Tombstone in MemTable |
| `DocumentCollection.findAll()` | `StorageEngine.scan(col)` | Memory stream iterator | Sequenced file channel read | In-order B-Tree leaf traversal| Merged MemTable + SSTable iterator |
| `KeyValueBucket.put()` | `StorageEngine.put(bucket, key, val)` | Direct map insertion | Bucket data append | B-Tree key insertion | MemTable key insertion |
| `KeyValueBucket.get()` | `StorageEngine.get(bucket, key)` | Map lookup | Offset index read | O(log N) page search | Bloom filter check $\rightarrow$ binary SSTable search |
| `Transaction.commit()` | `StorageEngine.commit()` | Apply dirty workspace | Flush file channel + fsync | Flush dirty pages + fsync | Flush MemTable to disk SSTable |
| `Transaction.rollback()` | `Transaction.rollback()` | Discard workspace | Revert dirty in-memory pointers | Revert dirty in-memory pages | Discard uncommitted MemTable entries |
