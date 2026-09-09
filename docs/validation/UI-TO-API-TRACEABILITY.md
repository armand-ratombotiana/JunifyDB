# UI to API Traceability Matrix

**Audit Date**: September 9, 2026  
**Auditor**: Systems Integration Lead  

---

## 1. UI Action to Backend Endpoint Mapping

| UI Action / Control | Trigger Handler | HTTP Method | Target Endpoint | Payload Structure |
|---|---|---|---|---|
| Sign In Form | `handleLogin(e)` | `POST` | `/api/auth/login` | `{ username, password, apiKey }` |
| Sign Out Button | `handleLogout()` | `POST` | `/api/auth/logout` | None |
| Auto-Refresh Metric Poll | `fetchMetrics()` | `GET` | `/api/metrics` | None |
| Telemetry Stream | SSE EventSource | `GET` | `/api/metrics/stream` | None |
| Storage Stats Card | `fetchStats()` | `GET` | `/api/stats` | None |
| Collections Sidebar | `loadCollections()` | `GET` | `/api/collections` | None |
| Collection Document Table | `loadDocs(col)` | `GET` | `/api/collections/{col}` | Optional query parameters |
| Insert Document Modal | `saveDoc()` | `POST` | `/api/collections/{col}` | Raw Document JSON |
| Update Document Modal | `updateDoc()` | `PUT` | `/api/collections/{col}/{id}` | Updated Document JSON |
| Delete Document Button | `deleteDoc(id)` | `DELETE`| `/api/collections/{col}/{id}` | None |
| Query Console Run | `runQuery()` | `POST` | `/api/collections/{col}/query` | Query JSON |
| KV Value Read | `readKv(k)` | `GET` | `/api/kv/{bucket}/{key}` | None |
| KV Value Write | `saveKv()` | `POST` | `/api/kv/{bucket}/{key}` | Value string/JSON |
| KV Key Delete | `deleteKv(k)` | `DELETE`| `/api/kv/{bucket}/{key}` | None |
| Column Family Cell Put | `putCell()` | `POST` | `/api/columns/{family}/{key}`| `{ qualifier, value, ttl }` |
| Begin Transaction | `beginTx()` | `POST` | `/api/transactions` | `{"action":"begin"}` |
| Commit Transaction | `commitTx()` | `POST` | `/api/transactions` | `{"action":"commit"}` |
| Rollback Transaction | `rollbackTx()` | `POST` | `/api/transactions` | `{"action":"rollback"}` |
| Create Index Form | `createIndex()` | `POST` | `/api/indexes/{col}` | `{ field: ... }` |
| Trigger Backup Button | `triggerBackup()`| `POST` | `/api/backup` | `{ path: ... }` |
| Change Data Capture Poll | `loadCdc()` | `GET` | `/api/cdc/events` | None |
| Audit Log Search | `searchAudit()` | `GET` | `/api/audit/logs` | Query parameters |
