# Console Action Inventory

| Action ID | User Action | Target Component | API Triggered | Expected Result |
|---|---|---|---|---|
| `ACT-001` | Submit invalid credentials | `#submitBtn` on `login.html` | `POST /api/auth/login` | Error banner displayed; no redirect |
| `ACT-002` | Submit valid credentials | `#submitBtn` on `login.html` | `POST /api/auth/login` | Session cookie set; redirect to `index.html` |
| `ACT-003` | Switch navigation tab | Sidebar `.nav-item` | None (Client routing) | Active view switches to selected section |
| `ACT-004` | Refresh metrics | Overview tab load | `GET /api/metrics/stream` | Metrics counters update in real-time |
| `ACT-005` | Select collection | `#collectionsList li` | `GET /api/collections/{name}` | Documents for collection rendered in grid |
| `ACT-006` | Open New Document Modal | `#newDocBtn` | None | Modal `#docModal` opens with empty JSON |
| `ACT-007` | Submit New Document | `#saveDocBtn` | `POST /api/collections/{name}` | New document saved; card added to grid |
| `ACT-008` | Edit existing document | Document card Edit button | `PUT /api/collections/{name}/{id}` | Document updated in database and refreshed |
| `ACT-009` | Delete document | Document card Delete button | `DELETE /api/collections/{name}/{id}` | Confirmation prompt; card removed |
| `ACT-010` | Run filter query | `#runQueryBtn` | `POST /api/collections/{name}/query` | Query results displayed in formatted JSON |
| `ACT-011` | Set KV pair | `#setKvBtn` | `PUT /api/kv/{bucket}/{key}` | Key-value row added to bucket table |
| `ACT-012` | Delete KV pair | KV table Delete button | `DELETE /api/kv/{bucket}/{key}` | Key removed from bucket |
| `ACT-013` | Trigger snapshot backup | `#createBackupBtn` | `POST /api/backup` | Backup confirmation toast and file timestamp |
| `ACT-014` | View audit logs | Audit tab load | `GET /api/audit/logs` | Audit trail events rendered in table |
| `ACT-015` | User logout | `#logoutBtn` | `POST /api/auth/logout` | Session cleared; redirected to `login.html` |
