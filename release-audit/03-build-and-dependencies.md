# Phase 03 — Build & Dependency Audit

## Build Verification

- **Command Executed**: `mvn clean verify -P coverage-check --batch-mode`
- **Result**: `BUILD SUCCESS`
- **Execution Time**: 48.879s
- **Compiler Target**: `--release 17`
- **Core Tests Passed**: 669 tests, 0 failures, 0 errors, 0 skipped
- **Output Artifact**: `target/junify-db-core-1.0.0.jar`
- **Shaded Artifact**: Shaded jar replaces original with embedded Jackson and ByteBuddy dependencies.

---

## Dependency & Shading Strategy

To guarantee seamless embedding into Spring Boot, Quarkus, Micronaut, and Vert.x applications without classpath conflicts:

1. **Logging Separation**:
   - `slf4j-simple` is marked `<scope>runtime</scope>` and `<optional>true</optional>` in the root `pom.xml`.
   - In `maven-shade-plugin`, `org.slf4j:slf4j-simple` is explicitly excluded from the shaded JAR so host applications control their logging provider (Logback, JBoss Logging, Log4j2).
2. **Bundled Dependencies**:
   - `com.fasterxml.jackson.core:jackson-databind:2.17.0`
   - `com.fasterxml.jackson.core:jackson-core:2.17.0`
   - `com.fasterxml.jackson.core:jackson-annotations:2.17.0`
   - `com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.17.0`
   - `net.bytebuddy:byte-buddy:1.14.9`
3. **Maven Central Metadata**:
   - License: Apache License, Version 2.0 (`https://www.apache.org/licenses/LICENSE-2.0.txt`)
   - Developer: Armand Ratombotiana (`armand.ratombotiana@gmail.com`)
   - SCM: `https://github.com/armand-ratombotiana/JunifyDB`
   - Issue Management: `https://github.com/armand-ratombotiana/JunifyDB/issues`
