# API to Library Traceability Matrix

**Audit Date**: September 9, 2026  
**Auditor**: Backend Lead  

---

## 1. REST Handler to JunifyDB Library Mapping

| Endpoint | Server Handler Class | JunifyDB Domain Method | Target Domain Class |
|---|---|---|---|
| `GET /api/health` | `HealthHandler` | `JunifyDB.isOpen()`, `JunifyDB.config()` | `JunifyDB` |
| `GET /api/metrics` | `MetricsHandler` | `DatabaseMetrics.snapshot()` | `DatabaseMetrics` |
| `GET /api/collections` | `CollectionsHandler` | `JunifyDB.getCollectionNames()` | `JunifyDB` |
| `GET /api/collections/{col}` | `CollectionsHandler` | `DocumentCollection.findAll()` | `DocumentCollection` |
| `POST /api/collections/{col}` | `CollectionsHandler` | `DocumentCollection.insert(doc)` | `DocumentCollection` |
| `PUT /api/collections/{col}/{id}` | `CollectionsHandler` | `DocumentCollection.update(doc)` | `DocumentCollection` |
| `DELETE /api/collections/{col}/{id}`| `CollectionsHandler` | `DocumentCollection.deleteById(id)` | `DocumentCollection` |
| `POST /api/collections/{col}/query`| `CollectionsHandler` | `DocumentCollection.find(query)` | `DocumentCollection` |
| `GET /api/kv/{bucket}/{key}` | `KeyValueHandler` | `KeyValueBucket.get(key)` | `KeyValueBucket` |
| `POST /api/kv/{bucket}/{key}` | `KeyValueHandler` | `KeyValueBucket.put(key, value)` | `KeyValueBucket` |
| `DELETE /api/kv/{bucket}/{key}` | `KeyValueHandler` | `KeyValueBucket.remove(key)` | `KeyValueBucket` |
| `POST /api/columns/{family}/{key}`| `ColumnHandler` | `ColumnFamily.put(row, qual, val, ttl)` | `ColumnFamily` |
| `POST /api/transactions` | `TransactionHandler` | `JunifyDB.beginTransaction()`, `tx.commit()`, `tx.rollback()` | `Transaction` |
| `POST /api/indexes/{col}` | `IndexHandler` | `DocumentCollection.createIndex(field)`| `DocumentCollection` |
| `POST /api/auth/login` | `AuthLoginHandler` | `SecureSessionManager.generateSessionId()`, `setSessionCookie()` | `SecureSessionManager` |
| `POST /api/auth/logout` | `AuthLogoutHandler` | `SecureSessionManager.clearSessionCookie()` | `SecureSessionManager` |
