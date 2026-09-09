# UI/UX Issues and Resolutions

**Audit Date**: September 9, 2026  
**Auditor**: Lead Product Designer & Frontend Engineer

---

## 1. Discovered UX Issues & Applied Fixes

| Issue ID | Screen / Component | Issue Description | Severity | Resolution Applied |
|---|---|---|---|---|
| **UX-01** | Header Auth Menu | Logging out or encountering expired session caused a 404 because `login.html` was missing. | High | Created dedicated, matching `login.html` with responsive form. |
| **UX-02** | Collections Tab | Clicking Collections tab caused JS error because backend returned message string instead of list. | High | Updated server to return structured array `{ collections: [...] }`. |
| **UX-03** | Backup Tab | Clicking "Run Benchmarks" returned 404 because endpoint was unmapped. | Medium | Added `BenchmarkHandler` on `/api/benchmark`. |
| **UX-04** | Query Console | Running complex nested JSON queries failed if string escaping had double quotes in history panel. | Medium | Replaced inline JSON string serialization with safe `window.__queryHistoryItems` key lookup. |
| **UX-05** | Toasts | Long error messages caused toast text overflow on mobile screens. | Low | Added `word-break: break-word` and flexible toast box-model in CSS. |
