# UI-to-API Traceability Matrix

**Audit Date**: September 9, 2026  
**Auditor**: Frontend Integration Lead  
**Scope**: All UI interactive buttons, forms, and triggers mapped to REST endpoints.

---

## 1. UI-to-API Route Mapping

| UI Action / Trigger | JavaScript Function | HTTP Method | REST Endpoint | Payload / Query | Response Target |
|---|---|---|---|---|---|
| **Page Load / Health Check** | `checkAuth()` | `GET` | `/api/health` | None | Header Status Dot & Text |
| **SSE Metrics Stream** | `initMetricsStream()` | `GET` | `/api/metrics/stream` | Text/event-stream | Dashboard Top Stat Cards |
| **Refresh Metrics** | `loadMetrics()` | `GET` | `/api/metrics` | None | Dashboard Counters |
| **List Collections** | `loadCollections()` | `GET` | `/api/collections` | None | Collections Sidebar List |
| **Select Collection** | `selectCollection(name)` | `GET` | `/api/collections/{col}` | None | Document Table View |
| **Create Document** | `saveDocument()` | `POST` | `/api/collections/{col}` | Document JSON | Refresh Document Table |
| **Update Document** | `saveDocument()` | `PUT` | `/api/collections/{col}/{id}` | Document JSON | Refresh Document Table |
| **Delete Document** | `deleteDocument(id)` | `DELETE` | `/api/collections/{col}/{id}` | None | Document Table Row Removal |
| **Run Query** | `runQuery()` | `POST` | `/api/collections/{col}/query` | Query JSON | Query Results Table |
| **Get KV Key** | `getKVValue()` | `GET` | `/api/kv/{bucket}/{key}` | None | KV Value Display Card |
| **Set KV Key** | `setKVValue()` | `POST` | `/api/kv/{bucket}/{key}` | Raw / JSON Body | Success Notification |
| **Delete KV Key** | `deleteKVKey()` | `DELETE` | `/api/kv/{bucket}/{key}` | None | KV Table Refresh |
| **Get Column Cell** | `getColumnCell()` | `GET` | `/api/columns/{family}/{key}` | None | Column Viewer Table |
| **Put Column Cell** | `putColumnCell()` | `POST` | `/api/columns/{family}/{key}` | Qualifier & Value | Column Viewer Table |
| **View Indexes** | `loadIndexes(col)` | `GET` | `/api/indexes/{col}` | None | Index List Table |
| **Create Index** | `createIndex()` | `POST` | `/api/indexes/{col}` | `{ field: name }` | Index List Refresh |
| **Inspect Schema** | `loadSchema(col)` | `GET` | `/api/schema/{col}` | None | Schema Builder Grid |
| **Register Schema** | `saveSchema()` | `POST` | `/api/schema/{col}` | `{ fields, strict }` | Schema Details Card |
| **Delete Schema** | `deleteSchema(col)` | `DELETE` | `/api/schema/{col}` | None | Empty Schema State |
| **Trigger Backup** | `createBackup()` | `POST` | `/api/backup` | `{ path: ... }` | Backup Status Toast |
| **Restore Backup** | `restoreBackup()` | `POST` | `/api/backup` | `{ restorePath: ... }` | Restore Status Toast |
| **Begin Transaction** | `beginTransaction()` | `POST` | `/api/transactions` | `{ action: "begin" }` | Active Tx Badge Display |
| **Commit Transaction** | `commitTransaction()` | `POST` | `/api/transactions` | `{ action: "commit", id }` | Tx Closed Toast |
| **Rollback Transaction** | `rollbackTransaction()` | `POST` | `/api/transactions` | `{ action: "rollback", id }` | Tx Reverted Toast |
| **Run Benchmarks** | `runBenchmarks()` | `POST` | `/api/benchmark` | `{}` | Benchmark Results Toast |
| **User Login** | `login()` | `POST` | `/api/auth/login` | `{ apiKey, user }` | Redirect to Dashboard |
| **User Logout** | `logout()` | `POST` | `/api/auth/logout` | None | Redirect to `login.html` |

---

## 2. Integrity Confirmation

Every documented UI action connects to a registered backend handler. No mocked responses or dead-end buttons remain in `index.html` or `enhancements.js`.
