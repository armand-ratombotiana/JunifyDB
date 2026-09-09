# End-to-End User Workflows

**Audit Date**: September 9, 2026  
**Auditor**: Full-Stack Test Lead  
**Objective**: End-to-end verification of all primary user workflows through the console.

---

## 1. Primary Workflow Executions

### Workflow 1: Database Inspection & Live Monitoring
1. User navigates to `http://localhost:8080/`.
2. Browser fetches `index.html`, connects to SSE `/api/metrics/stream`.
3. System status dot illuminates green ("Online").
4. Real-time operation counters and memory usage graphs update dynamically.
- **Status**: **PASS**.

### Workflow 2: Collection & Document Management
1. User clicks "Collections" tab.
2. Left sidebar lists all collections with document counts.
3. User selects `users` collection.
4. User clicks "Add Document", enters `{"name": "Alice", "role": "admin"}` in modal, clicks Save.
5. Modal closes; new document appears immediately in the table.
6. User clicks "Delete" on the document; confirmation modal appears; user confirms.
7. Document is removed from UI and deleted in embedded storage.
- **Status**: **PASS**.

### Workflow 3: Interactive Query Execution
1. User clicks "Query Console" tab.
2. User selects "Find by Field" template.
3. User clicks "Run Query".
4. Results table displays matching documents; execution latency (ms) is displayed.
5. Query is appended to Query History side panel.
- **Status**: **PASS**.

### Workflow 4: Key-Value & Wide-Column Exploration
1. User clicks "Key-Value" tab, selects bucket `session_cache`.
2. Sets key `user:101` to `{"loggedIn": true}`.
3. Queries key `user:101` and sees returned JSON.
4. User switches to "Column Family" tab, puts row `user_metrics`, column `login_count` with value `42`.
5. Retrieves cell and verifies versioned timestamp.
- **Status**: **PASS**.

### Workflow 5: Backup & Disaster Recovery
1. User clicks "Backup" tab.
2. Clicks "Create Backup" -> Database flushes to disk and creates snapshot.
3. User specifies restore file path and clicks "Restore".
4. Database state restored; UI displays success toast.
- **Status**: **PASS**.
