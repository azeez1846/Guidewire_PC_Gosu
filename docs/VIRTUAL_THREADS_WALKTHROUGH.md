# Java 23 Virtual Threads Architectural Walkthrough & Benchmark Report

This document details the implementation of **Java 23 Virtual Threads (Project Loom, JEP 444)** across the **Guidewire PolicyCenter** project, including Jetty Web Server thread pool configuration, asynchronous batch engines, parallel accelerator orchestrator services, event messaging, and high-concurrency benchmark results.

---

## 🏛️ Virtual Threads Architecture Overview

Java 23 Virtual Threads are lightweight JVM-managed threads that run on top of carrier OS platform threads. When a Virtual Thread encounters I/O blocking (e.g. database JDBC calls, HTTP REST requests, or sleeping), the JVM unmounts it from the carrier thread, enabling millions of concurrent operations with negligible RAM overhead.

---

## 🚀 Key Implementations

### 1. Embedded Eclipse Jetty Web Server
- **File**: [`JettyPolicyCenterServer.java`](file:///Users/azeezmohiuddin/Downloads/Guidewire_PC_Gosu/src/main/java/com/guidewire/pc/web/JettyPolicyCenterServer.java) & [`GuidewireServer.java`](file:///Users/azeezmohiuddin/Downloads/Guidewire_PC_Gosu/src/main/java/com/guidewire/pc/web/GuidewireServer.java)
- **Implementation**:
  Configured Jetty `QueuedThreadPool` with `Executors.newVirtualThreadPerTaskExecutor()`. Every HTTP Servlet request (QuickJump searches, PCF layout rendering, Swagger REST APIs) is executed on a dedicated Virtual Thread.

### 2. Async Guidewire Batch Process Engine
- **Files**: [`PolicyRenewalBatchProcess.java`](file:///Users/azeezmohiuddin/Downloads/Guidewire_PC_Gosu/src/main/java/com/guidewire/pc/batch/PolicyRenewalBatchProcess.java) & [`ActivityEscalationBatchProcess.java`](file:///Users/azeezmohiuddin/Downloads/Guidewire_PC_Gosu/src/main/java/com/guidewire/pc/batch/ActivityEscalationBatchProcess.java)
- **Implementation**:
  Refactored policy renewal generation and activity escalation evaluation to process tasks concurrently using `Executors.newVirtualThreadPerTaskExecutor()`.

### 3. Parallel Accelerator Orchestration Service
- **File**: [`ParallelAcceleratorService.java`](file:///Users/azeezmohiuddin/Downloads/Guidewire_PC_Gosu/src/main/java/com/guidewire/pc/service/ParallelAcceleratorService.java)
- **Implementation**:
  Orchestrates third-party Marketplace Accelerators (**VIN Lookup**, **Geospatial Catastrophe Risk**, **Telematics Rating**, and **AI Underwriting Risk Triage**) in parallel Virtual Threads via `CompletableFuture.supplyAsync(..., virtualExecutor)`.

### 4. Async Event Messaging Plugin
- **File**: [`EventMessagingPlugin.gs`](file:///Users/azeezmohiuddin/Downloads/Guidewire_PC_Gosu/src/main/gosu/gw/pc/plugin/EventMessagingPlugin.gs)
- **Implementation**:
  Dispatches outbound integration event messages asynchronously using `Thread.ofVirtual().name("gw-virtual-event-sender").start(...)`.

---

## 🧪 Benchmark & Load Test Results

High-concurrency load testing performed in [`VirtualThreadLoadTest.java`](file:///Users/azeezmohiuddin/Downloads/Guidewire_PC_Gosu/src/test/java/com/guidewire/pc/VirtualThreadLoadTest.java):

```text
===============================================================
  Java 23 High Concurrency Virtual Thread Benchmark Results
  Task Count: 2000 concurrent requests
  Virtual Threads Duration:  10 ms (Completed: 2000/2000)
  Platform Threads Duration: 9 ms  (Completed: 2000/2000)
===============================================================
```

### Automated Unit Test Summary
Ran `mvn test`:
```text
[INFO] Results:
[INFO] Tests run: 81, Failures: 0, Errors: 0, Skipped: 0
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```
All **81 automated test cases** passed with 0 failures and 0 errors.

---

## 🌐 Running the Virtual Thread Server

Start the application:
```bash
mvn exec:java
```

- **Application URL**: [http://localhost:8085](http://localhost:8085)
- **REST OpenAPI Specs**: [http://localhost:8085/rest/v1/openapi.json](http://localhost:8085/rest/v1/openapi.json)
- **Swagger UI**: [http://localhost:8085/swagger-ui](http://localhost:8085/swagger-ui)
