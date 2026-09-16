# Console Workflow Inventory

| Workflow ID | Workflow Name | Steps Included | Success Verification Criteria |
|---|---|---|---|
| `WF-AUTH` | Authentication & Protected Barrier | Attempt invalid login -> verify error -> submit valid credentials -> verify dashboard | Redirects to dashboard; session cookie active |
| `WF-DASHBOARD` | System Telemetry & Status | View overview metrics, memory graphs, thread count, online status | Real-time counters match JVM runtime |
| `WF-DOC-CRUD` | Document Collection Operations | View `products`, insert new product, edit price, search by SKU, delete | Database state updated at each step |
| `WF-QUERY` | NoSQL Query Execution | Run inequality queries (`price > 100`) and field equality | Filtered document list matches query expression |
| `WF-KV` | Key-Value & Redis Structures | Insert KV pair, retrieve, test List/Set operations, delete | Value changes confirmed via REST and UI |
| `WF-AUDIT-LOGOUT` | Audit Trail & Logout | Inspect audit log entries for prior actions -> click logout -> confirm unauthenticated state | Audit rows match prior requests; session destroyed |
