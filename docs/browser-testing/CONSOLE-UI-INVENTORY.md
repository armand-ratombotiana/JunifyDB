# Console UI Inventory

| UI Component ID | Component Name | Screen / Container | Associated Feature | State Handled |
|---|---|---|---|---|
| `UI-LOGIN-FORM` | Sign-in Form | `login.html` | Authentication | Initial, submitting, error display |
| `UI-NAV-SIDEBAR` | Primary Navigation Sidebar | `index.html` (`.sidebar`) | Navigation | Active tab switching |
| `UI-STATUS-BAR` | Engine Status Bar | Header (`#statusDot`, `#statusText`) | Telemetry | Online/Offline status indicator |
| `UI-METRICS-CARD` | Live Metrics Cards | Overview Tab (`#metricsGrid`) | System Health | Real-time memory, threads, uptime |
| `UI-COLLECTIONS-LIST`| Collections Sidebar Panel | Documents View (`#collectionsList`) | Document Storage | List, count badge, collection selection |
| `UI-DOC-GRID` | Document Cards / Grid | Documents View (`#docContainer`) | Document Storage | Card view with JSON snippets & actions |
| `UI-DOC-MODAL` | Document Editor Modal | Modal Dialog (`#docModal`) | Document CRUD | Insert, update, JSON syntax check |
| `UI-QUERY-RUNNER` | Query Runner Form | Query Tab (`#queryEditor`) | Search & Filter | Filter input, execute button, result JSON |
| `UI-KV-MANAGER` | Key-Value Table | Key-Value Tab (`#kvTable`) | Key-Value Store | Bucket selector, key list, add/edit/delete |
| `UI-DATA-STRUCTURES`| Redis Structures Tab | Redis Tab (`#structuresPanel`) | Data Structures | Lists, Sets, Hashes controls |
| `UI-COLUMN-FAMILIES`| Wide Column Explorer | Columns Tab (`#columnPanel`) | Wide Column Store | Family selection, row key lookup |
| `UI-SCHEMA-MANAGER` | Schema Validator Panel | Schema Tab (`#schemaPanel`) | Schema Governance | Register schema, drop schema |
| `UI-INDEXES-PANEL` | Index Manager | Indexes Tab (`#indexesPanel`) | Indexing | Index list, create secondary index |
| `UI-VECTORS-PANEL` | Vector Search Explorer | Vectors Tab (`#vectorsPanel`) | Vector Search | Embeddings indexing, nearest neighbor search |
| `UI-BACKUP-PANEL` | Backup & Snapshot Manager | Maintenance Tab (`#backupPanel`) | Operations | Create snapshot, restore |
| `UI-AUDIT-TABLE` | Audit Log Viewer | Audit Tab (`#auditTable`) | Security & Compliance | Filterable audit events table |
| `UI-USER-MENU` | User Profile Dropdown | Top-right Header (`#userMenu`) | Session Management | Username display, logout button |
