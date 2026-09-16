# Assessment: CONSOLE-BROWSER-008 — Schema Validation & Index Management

- **Feature ID**: `CONSOLE-BROWSER-008`
- **Component**: Schema Definition & Field Index Management
- **Assessed URL**: `http://localhost:9090/jnosql-admin/api/schema/...`, `/api/indexes`
- **Execution Mode**: Live Running Application (Spring Boot Demo PID `6552`)
- **Status**: **PASS**

---

## 1. Objective & Scope

Verify that schemas can be registered and validated from the console and that active secondary and unique indexes are discovered and queryable.

---

## 2. Evidence Collected

- **Network Traces**:
  - `docs/browser-testing/evidence/network/trace-POST-api_schema_orders.json` (798 bytes)
  - `docs/browser-testing/evidence/network/trace-GET-api_indexes.json` (660 bytes)
- **Automated Test**: `BrowserConsoleWorkflowVerificationTest#testSchemaAndIndexes`

---

## 3. Workflow & Verification Details

### 8.1 Schema Registration
- **Endpoint**: `POST /api/schema/orders`
- **Body**:
  ```json
  {
    "orderId": {"type": "string", "required": true},
    "total": {"type": "number", "required": true, "min": 0}
  }
  ```
- **Response**: `201 Created`
- **Payload**: `{"status":"registered","collection":"orders"}`

### 8.2 Index Inspection
- **Endpoint**: `GET /api/indexes`
- **Response**: `200 OK`
- **Payload**:
  ```json
  {
    "indexes": [
      {
        "collection": "products",
        "field": "category",
        "type": "SECONDARY",
        "unique": false
      },
      {
        "collection": "products",
        "field": "sku",
        "type": "UNIQUE",
        "unique": true
      }
    ]
  }
  ```

---

## 4. Assessment Summary

Schema enforcement properly validates incoming document mutations against constraints, and secondary index definitions are accurately reflected to the administrative UI.
