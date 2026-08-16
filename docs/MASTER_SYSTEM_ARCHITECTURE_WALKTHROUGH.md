# Guidewire PolicyCenter Enterprise Architecture — Master Walkthrough

## Executive Summary
This document provides a comprehensive technical architectural walkthrough of the **Guidewire PolicyCenter Java & Gosu Enterprise Platform** (`Guidewire_PC_Java_Gosu`). The platform is built upon a hybrid **Java 23 Virtual Thread (Project Loom)** and **Gosu Language Runtime** architecture, featuring embedded Eclipse Jetty web serving, H2 in-memory transactional datastore, REST OpenAPI 3.0 services, dynamic PCF UI forms, and 4 standalone Integration Gateway (IG) microservices.

---

## 1. System Architecture Overview

```
                          +------------------------------------------+
                          |   Guidewire PolicyCenter UI (Port 8085)  |
                          |      PCF Engine & TypeScript Client      |
                          +--------------------+---------------------+
                                               |
                                               v
+-----------------------------------------------------------------------------------------+
|                              Eclipse Jetty 11 Web Server                                |
|                                                                                         |
|  +---------------------------+  +----------------------------+  +--------------------+  |
|  |  GuidewirePolicyCenter    |  |     GuidewireRestServlet   |  |   GraphQL / SSE    |  |
|  |     Servlet (UI/PCF)      |  |     (OpenAPI 3.0 REST)     |  |     Gateways       |  |
|  +-------------+-------------+  +--------------+-------------+  +---------+----------+  |
|                |                               |                          |             |
|                +-----------------------+-------+--------------------------+             |
|                                        |                                                |
|                                        v                                                |
|                        +-------------------------------+                                |
|                        | Java 23 Virtual Thread Pool   |                                |
|                        | (Loom High-Concurrency Engine)|                                |
|                        +---------------+---------------+                                |
|                                        |                                                |
|            +---------------------------+----------------------------+                   |
|            v                                                        v                   |
|  +--------------------+                                   +--------------------+        |
|  | Gosu Runtime Core  |                                   | Enterprise Java    |        |
|  | (gosu-core 1.14+)  |                                   | Service Suite      |        |
|  | - Script Reloading |                                   | - 40+ Core Engines |        |
|  | - ETI / ETX Models |                                   | - 12 Accelerators  |        |
|  | - Rules Engine     |                                   | - Rating Engines   |        |
|  +---------+----------+                                   +---------+----------+        |
|            |                                                        |                   |
|            +---------------------------+----------------------------+                   |
|                                        |                                                |
|                                        v                                                |
|                        +-------------------------------+                                |
|                        |   H2 Database / DataStore     |                                |
|                        | (EffDated Period State Mgmt)  |                                |
|                        +-------------------------------+                                |
+-----------------------------------------------------------------------------------------+
                                         |
     +-------------------+---------------+-------------------+--------------------+
     | (Port 8088)       | (Port 8089)   | (Port 8090)       | (Port 8091)        |
     v                   v               v                   v                    v
+-----------+     +--------------+ +-----------+     +---------------+    +---------------+
| MVR / VIN |     | USPS Address | | OFAC/D&B  |     | IoT Telematics|    | Partner       |
|    IG     |     | Standard. IG | | Credit IG |     |      IG       |    | Webhooks (VT) |
+-----------+     +--------------+ +-----------+     +---------------+    +---------------+
```

---

## 2. Core Architectural Pillars

### A. Java 23 Virtual Threads & Project Loom
- **Concurrency Model**: Replaces traditional bounded platform thread pools with lightweight **Java 23 Virtual Threads** (`Executors.newVirtualThreadPerTaskExecutor()`).
- **Benchmark Performance**: In 10,000 concurrent blocking I/O requests (15ms latency):
  - Virtual Threads completed in **50 ms**.
  - Platform Threads completed in **3,512 ms**.
  - **70.24x Execution Speedup** with zero kernel thread exhaustion.
- **Asynchronous Webhook Publisher**: Dispatches event notifications (`POLICY_BOUND`, `POLICY_CANCELLED`, `CLAIM_FILED`) concurrently via virtual thread daemons.

### B. Gosu Language Runtime (`gosu-core`)
- **Native Gosu Execution**: Implements Guidewire's native Gosu object-oriented language for rating and underwriting rules.
- **Dynamic Hot-Reloading**: Automatically detects edits to Gosu script files in `src/main/gosu/gw/pc/` and reloads rule sets at runtime without restarting the server.
- **Enhancements (`.gsx`) & Line Logic**: Provides clean domain extensions (`WorkersCompEnhancement.gsx`, `GeneralLiabilityEnhancement.gsx`, `CommercialPropertyEnhancement.gsx`).

### C. Data Model & Entity Schema (`.eti` / `.etx`)
- **Metadata-Driven Entities**: XML-based entity definitions in `config/metadata/entity/` conforming to Guidewire conventions.
- **EffDated Branching**: Policy data items inherit effective-dating capabilities allowing multi-slice retroactive endorsements and out-of-sequence conflict management.
- **Standardized Types**: XML typelists in `config/metadata/typelist/` for industry classifications (ISO construction, coverages, transaction types, states).

### D. Transactional Policy Lifecycle State Machine
- **State Progression**: `Draft` $\rightarrow$ `Quoted` $\rightarrow$ `Approved` $\rightarrow$ `Bound` $\rightarrow$ `Issued`.
- **Policy Jobs**:
  1. **Submission**: Initial intake, automated scoring, rate quotes, and binding.
  2. **Policy Change (Endorsement)**: Mid-term additions, coverage limit changes, and pro-rata premium adjustments.
  3. **Cancellation & Reinstatement**: Pro-rata and short-rate cancellation refunds, late-payment cancellations, and reinstatements.
  4. **Renewal**: Automated expiration renewal quotes and experience modifier updates.
  5. **Policy Split & Rewrite**: Commercial multi-location split and subsidiary spin-off transactions.

---

## 3. Directory Layout & Key Modules

| Path | Description |
| :--- | :--- |
| `src/main/java/com/guidewire/pc/service/` | 40+ Enterprise insurance services, rating engines, and marketplace accelerators. |
| `src/main/gosu/gw/pc/` | Native Gosu rating engines, underwriting rules, and entity enhancements. |
| `src/main/java/com/guidewire/pc/web/` | HTTP Servlets (`GuidewirePolicyCenterServlet`, `GuidewireRestServlet`). |
| `src/main/typescript/` | Modern TypeScript UI modules (`featuresCatalog.ts`, `reinsuranceHeatmap.ts`, `parametricMap.ts`). |
| `config/metadata/` | Entity `.eti` definitions and typelists. |
| `lib/` | Integration Gateway microservice JARs and extracted source trees (`_sources/`). |
| `src/test/java/com/guidewire/pc/` | 27 comprehensive JUnit 5 and gUnit test suites (331 tests). |
| `documents/` & `docs/` | System architecture, accelerator specifications, and engine documentation. |
