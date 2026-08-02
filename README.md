# Guidewire_PC_Java_Gosu (Gosu Engine + Eclipse Jetty Web Application)

A full-stack insurance policy administration system built on **Guidewire PolicyCenter** architecture, featuring an embedded **Gosu Language Engine (`gosu-core`)**, PCF web rendering engine on Eclipse Jetty, H2 embedded relational database, 6 Guidewire Marketplace Accelerators, Security Hardening, and complete Gosu Unit Test (gUnit) coverage.

---

## 🌟 Key System Features

- **Embedded Gosu Runtime Engine**: Executes Gosu script files (`.gs`), enhancements (`.gsx`), rating engines, and underwriting rules natively.
- **Java 23 Virtual Threads (JEP 444)**: Powered by Virtual Threads across Eclipse Jetty HTTP web server, async batch engines, parallel accelerator orchestrator services, and event messaging.
- **Embedded Web Server (Eclipse Jetty)**: Serves Guidewire PCF layout definitions dynamically with modern UI themes.
- **UI QuickJump & Search Engine (`Search.pcf`)**: Fast direct navigation by Account Number (e.g. `A0001001`), Job Number (e.g. `S0005001`), or Policy Number (`POL-849102`), plus multi-entity search results screen.
- **Guidewire Marketplace Accelerators**:
  1. **VIN Lookup & Vehicle Auto-Populate**: Auto-populates vehicle specs via NHTSA API.
  2. **Geospatial Catastrophe Risk Assessment**: Evaluates flood/hurricane zone risks.
  3. **Telematics & UBI Rating**: Discounts premiums based on driving behavior score.
  4. **Digital E-Signature & Policy Binders**: PDF binder generation & DocuSign integration.
  5. **AI Underwriting Assistant**: Automated risk triage & activity referral escalation.
  6. **Tokenized Payment Gateway**: Credit card tokenization & 4-part installment scheduler.
- **Security Hardening**: Session token management, constant-time password check, security headers (`X-Frame-Options`, `Content-Security-Policy`), and audit logging.
- **REST OpenAPI & Swagger UI**: Serves OpenAPI v3 specification at `/rest/v1/openapi.json` and interactive Swagger UI at `/swagger-ui`.

---

## 📁 Documentation & Guides (`docs/`)

All feature guides and architecture documents are maintained in the [`docs/`](file:///Users/azeezmohiuddin/Downloads/Guidewire_PC_Gosu/docs/) folder:

- [`docs/walkthrough.md`](file:///Users/azeezmohiuddin/Downloads/Guidewire_PC_Gosu/docs/walkthrough.md) - Complete Technical Walkthrough & Verification Summary
- [`docs/SECURITY_HARDENING_GUIDE.md`](file:///Users/azeezmohiuddin/Downloads/Guidewire_PC_Gosu/docs/SECURITY_HARDENING_GUIDE.md) - Security Hardening & Penetration Testing Report
- [`docs/ACCELERATOR_VIN_LOOKUP.md`](file:///Users/azeezmohiuddin/Downloads/Guidewire_PC_Gosu/docs/ACCELERATOR_VIN_LOOKUP.md) - VIN Lookup Accelerator Reference
- [`docs/ACCELERATOR_GEOSPATIAL_RISK.md`](file:///Users/azeezmohiuddin/Downloads/Guidewire_PC_Gosu/docs/ACCELERATOR_GEOSPATIAL_RISK.md) - Geospatial Risk Accelerator Reference
- [`docs/ACCELERATOR_TELEMATICS_UBI.md`](file:///Users/azeezmohiuddin/Downloads/Guidewire_PC_Gosu/docs/ACCELERATOR_TELEMATICS_UBI.md) - Telematics UBI Accelerator Reference
- [`docs/ACCELERATOR_ESIGNATURE.md`](file:///Users/azeezmohiuddin/Downloads/Guidewire_PC_Gosu/docs/ACCELERATOR_ESIGNATURE.md) - E-Signature & PDF Binder Reference
- [`docs/ACCELERATOR_AI_UNDERWRITING.md`](file:///Users/azeezmohiuddin/Downloads/Guidewire_PC_Gosu/docs/ACCELERATOR_AI_UNDERWRITING.md) - AI Underwriting Referral Reference
- [`docs/ACCELERATOR_PAYMENT_GATEWAY.md`](file:///Users/azeezmohiuddin/Downloads/Guidewire_PC_Gosu/docs/ACCELERATOR_PAYMENT_GATEWAY.md) - Tokenized Payment Gateway Reference

---

## 🚀 Running the Application Server

Start the application server with Maven:

```bash
mvn exec:java
```

- **Application Web UI**: [http://localhost:8085](http://localhost:8085)
- **Search & QuickJump Screen**: [http://localhost:8085/?page=search](http://localhost:8085/?page=search)
- **H2 Database Console**: [http://localhost:8082](http://localhost:8082)
- **REST OpenAPI Spec**: [http://localhost:8085/rest/v1/openapi.json](http://localhost:8085/rest/v1/openapi.json)
- **Swagger UI**: [http://localhost:8085/swagger-ui](http://localhost:8085/swagger-ui)
- **Default Credentials**: `Username = su` | `Password = gw`

---

## 🧪 Running Unit Tests (gUnits & JUnit)

Execute the full suite of 78 automated unit and integration tests:

```bash
mvn test
```
