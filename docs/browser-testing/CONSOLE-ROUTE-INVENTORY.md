# Console Route Inventory

| Route | Method | Type | Description | Auth Required |
|---|---|---|---|---|
| `/` | `GET` | HTML / Redirect | Root URL, redirects to `${contextPath}/` | No |
| `/login.html` | `GET` | Static HTML | Authentication sign-in page | No |
| `/index.html` | `GET` | Static HTML | Main Administration Console SPA | Yes (Session/Cookie) |
| `/api/auth/login` | `POST` | REST Endpoint | Authenticates user and issues session cookie | No |
| `/api/auth/logout` | `POST` | REST Endpoint | Invalidates session and clears cookie | Yes |
| `/api/health` | `GET` | REST Endpoint | Database and runtime health telemetry | Yes |
| `/api/metrics` | `GET` | REST Endpoint | Real-time performance and resource metrics | Yes |
| `/api/metrics/stream` | `GET` | SSE Stream | Real-time Server-Sent Events metrics feed | Yes |
| `/api/collections` | `GET` | REST Endpoint | List collections and document counts | Yes |
| `/api/collections/{col}` | `GET` | REST Endpoint | Retrieve all documents in collection | Yes |
| `/api/collections/{col}` | `POST` | REST Endpoint | Insert new document into collection | Yes + CSRF |
| `/api/collections/{col}/{id}` | `GET` | REST Endpoint | Retrieve single document by ID | Yes |
| `/api/collections/{col}/{id}` | `PUT` | REST Endpoint | Update document by ID | Yes + CSRF |
| `/api/collections/{col}/{id}` | `DELETE` | REST Endpoint | Delete document by ID | Yes + CSRF |
| `/api/collections/{col}/query` | `POST` | REST Endpoint | Query collection using query expressions | Yes + CSRF |
| `/api/kv/{bucket}` | `GET` | REST Endpoint | List all keys in a key-value bucket | Yes |
| `/api/kv/{bucket}/{key}` | `GET` | REST Endpoint | Get value for key in bucket | Yes |
| `/api/kv/{bucket}/{key}` | `PUT` | REST Endpoint | Put value for key in bucket | Yes + CSRF |
| `/api/kv/{bucket}/{key}` | `DELETE` | REST Endpoint | Delete key in bucket | Yes + CSRF |
| `/api/columns/{family}/{row}` | `GET` | REST Endpoint | Read wide-column row | Yes |
| `/api/columns/{family}/{row}` | `POST` | REST Endpoint | Write wide-column row | Yes + CSRF |
| `/api/schema/{collection}` | `GET` | REST Endpoint | Get schema definition for collection | Yes |
| `/api/schema/{collection}` | `POST` | REST Endpoint | Register schema definition | Yes + CSRF |
| `/api/indexes` | `GET` | REST Endpoint | List database secondary indexes | Yes |
| `/api/vectors` | `GET/POST` | REST Endpoint | Vector embeddings similarity index | Yes + CSRF |
| `/api/backup` | `POST` | REST Endpoint | Trigger database snapshot / backup | Yes + CSRF |
| `/api/audit/logs` | `GET` | REST Endpoint | Retrieve security and CRUD audit log events | Yes |
