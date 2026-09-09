# UI Feature Inventory

**Audit Date**: September 9, 2026  
**Auditor**: UI Product Specialist

---

## 1. Complete Screen & Component Inventory

| Tab ID | View Title | Main Interactive Elements | Data Bound |
|---|---|---|---|
| `tab-dashboard` | System Overview | Refresh button, stat cards, live throughput chart | System metrics, memory, ops/sec |
| `tab-collections` | Collections & Docs | Collection list sidebar, add doc button, doc table, pagination, edit/delete buttons | Document collections & records |
| `tab-query` | Query Console | Mode toggle (NoSQL/SQL), template selector, query editor, run button, history drawer | Query results JSON & table |
| `tab-kv` | Key-Value Store | Bucket selector, get/set key inputs, delete button, list/set/hash sub-tabs | Raw strings, JSON, arrays |
| `tab-columns` | Wide-Column Store | Family selector, row key input, column qualifier input, put/get buttons | Multi-version cell values |
| `tab-schema` | Schema & Indexes | Collection selector, strictness toggle, add field input, index creator | Registered schema & index list |
| `tab-vectors` | Vector Search | Index name input, dimension input, vector values input, k-NN search button | Similarity search results |
| `tab-backup` | Backup & System | Create backup button, restore path input, run benchmarks button | File backup status & bench report |
| `tab-logs` | Activity Log | Level filter dropdown (All, Success, Warn, Error), clear button | Ring buffer audit events |
| `modal-auth` | Login Dialog | Username, API Key, password inputs, submit button | Auth session cookie |
