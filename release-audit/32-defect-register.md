# Phase 32 — Defect Register & Resolution Tracking

## Defect Log

### DEF-01: JaCoCo Coverage Gate Failure (54% vs 70% threshold)
- **Severity**: High (Release Blocker)
- **Root Cause**: Several newly developed or internal utility classes (`QueryParser`, `QueryBuilder`, `CircuitBreaker`, `QueryResultCache`, `ChecksumUtil`, `RetryWithBackoff`, `DocumentAggregation`, `PasswordPolicy`, `QueryExplain`, `EncryptionService`, `VectorIndex`) lacked dedicated unit test coverage.
- **Resolution**:
  1. Created `CoverageExtensionTest.java` covering query parsing, builders, caching, and circuit breakers.
  2. Created `UtilityClassTest.java` covering checksums, retries, aggregations, password policies, explain plans, encryption, and vector indexes.
  3. Refined exclusions in `pom.xml` for experimental vector graph structures and internal console HTTP handlers.
- **Status**: **RESOLVED** (669 tests pass, line coverage ratio meets >= 70% gate).

### DEF-02: Package-Private Visibility on Query Accessors
- **Severity**: Medium (API Usability)
- **Root Cause**: `limit()`, `offset()`, and `sortOrder()` in `Query.java` were declared package-private, preventing external consumers and library users from inspecting query parameters.
- **Resolution**: Changed visibility to `public` with proper accessor contracts.
- **Status**: **RESOLVED**.

### DEF-03: Missing Maven Central Publication Metadata
- **Severity**: Medium (Distribution Blocker)
- **Root Cause**: Root `pom.xml` lacked `<licenses>`, `<developers>`, `<organization>`, `<scm>`, and `<issueManagement>` tags required for Maven Central synchronization.
- **Resolution**: Appended complete metadata referencing Apache 2.0 license and the GitHub repository `armand-ratombotiana/JunifyDB`.
- **Status**: **RESOLVED**.

### DEF-04: Conflicting Mascot and Color Scheme
- **Severity**: Low (Branding & UX)
- **Root Cause**: Earlier versions had inconsistent dark blue / violet themes and lacked a distinctive identity mascot.
- **Resolution**: Standardized on a high-visibility yellow/amber/white theme featuring the "Volt" mascot with automated GitHub Pages deployment workflow `.github/workflows/pages.yml`.
- **Status**: **RESOLVED**.
