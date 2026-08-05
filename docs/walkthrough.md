# Guidewire PolicyCenter Enterprise Architecture — Features & Integration Gateway (IG) Layer Walkthrough

## Overview

This document provides a comprehensive overview of the **36+ Enterprise Insurance Engines** and **4 Guidewire Cloud Integration Gateway (IG) Microservices** in `Guidewire_PC_Java_Gosu`. It covers the system architecture, extracted IG layer sources, interactive UI Features tab, automated test suite results, and version control status.

---

## 1. Integration Gateway (IG) Microservices & Extracted Sources

The application integrates with 4 standalone microservice gateways packaged as JAR files in `lib/` with fully decompiled, readable Java source trees in their corresponding `_sources` directories:

| Microservice Gateway | Executable JAR | Extracted Source Directory | Core Controller / Client |
| :--- | :--- | :--- | :--- |
| **Vehicle Details & MVR** | `lib/vehicledetails_IG-1.0.0.jar` | [vehicledetails_IG-1.0.0_sources](file:///Users/azeezmohiuddin/Downloads/Guidewire_PC_Java_Gosu/lib/vehicledetails_IG-1.0.0_sources) | `VehicleDetailsIGController.java`, `VehicleDetailsIGClient.java` |
| **Address Standardization** | `lib/addressstandardization_IG-1.0.0.jar` | [addressstandardization_IG-1.0.0_sources](file:///Users/azeezmohiuddin/Downloads/Guidewire_PC_Java_Gosu/lib/addressstandardization_IG-1.0.0_sources) | `AddressStandardizationIGController.java`, `AddressStandardizationIGClient.java` |
| **Credit Score & OFAC** | `lib/creditfraud_IG-1.0.0.jar` | [creditfraud_IG-1.0.0_sources](file:///Users/azeezmohiuddin/Downloads/Guidewire_PC_Java_Gosu/lib/creditfraud_IG-1.0.0_sources) | `CreditFraudIGController.java`, `CreditFraudIGClient.java` |
| **IoT Fleet Telematics** | `lib/telematics_IG-1.0.0.jar` | [telematics_IG-1.0.0_sources](file:///Users/azeezmohiuddin/Downloads/Guidewire_PC_Java_Gosu/lib/telematics_IG-1.0.0_sources) | `TelematicsIGController.java`, `TelematicsIGClient.java` |

### Key Capabilities of Extracted IG Layers:
1. **Vehicle Details & MVR**: Queries external DMV/MVR records, verifies VIN specifications, safety ratings, and driver license history.
2. **Address Standardization**: Real-time USPS DPV deliverability validation, ZIP+4 resolution, and lat/long geocoding.
3. **Credit Scoring & OFAC**: Experian/D&B credit score checks, credit-based insurance scoring (CBIS), and US Treasury OFAC sanctions screening.
4. **IoT Fleet Telematics**: Samsara/Geotab vehicle telemetry ingestion (hard brakes, rapid acceleration, mileage, late-night driving) for Usage-Based Insurance (UBI) pricing.

---

## 2. Interactive Features Suite on UI (`/?page=features`)

The PolicyCenter web application includes an interactive **Features Suite** accessible at `http://localhost:8085/?page=features` (and served via [GuidewirePolicyCenterServlet.java](file:///Users/azeezmohiuddin/Downloads/Guidewire_PC_Java_Gosu/src/main/java/com/guidewire/pc/web/GuidewirePolicyCenterServlet.java)).

### Features UI Highlights:
- **36 Enterprise Feature Cards**: Filterable by *Specialty Lines*, *Commercial Rating*, *Underwriting & Risk*, *Compliance & Regulatory*, and *Reinsurance & Portfolio*.
- **Interactive Field Inputs**: Dynamic form inputs (text boxes, numeric fields, dropdown selects) pre-populated with realistic test defaults.
- **REST Driver Execution**: Each card features a `⚡ Run Module Calculation` button executing asynchronous `fetch()` calls against backend REST endpoints.
- **Instant Result Rendering**: Returns styled, formatted JSON response payloads (`✅ Module Execution Success`).

---

## 3. Container Topology (`docker-compose.yml`)

The multi-container Docker stack orchestrates PolicyCenter alongside all 4 Integration Gateway microservices:

| Container Name | Service ID | External Port | Internal Port | Service Description |
| :--- | :--- | :--- | :--- | :--- |
| `vehicledetails_ig_service` | `vehicledetails-ig` | `8088` | `8080` | MVR & VIN Vehicle Details Microservice |
| `addressstandardization_ig_service` | `addressstandardization-ig` | `8089` | `8080` | USPS Address Standardization Microservice |
| `creditfraud_ig_service` | `creditfraud-ig` | `8090` | `8090` | Credit Scoring & OFAC Sanctions Microservice |
| `telematics_ig_service` | `telematics-ig` | `8091` | `8091` | IoT Fleet Telematics Microservice |
| `guidewire_policycenter_app` | `guidewire-policycenter` | `8085` | `8085` | PolicyCenter Main Application & Web UI |

---

## 4. Test Suite Execution & Verification Results

Full automated testing was performed using Apache Maven:

```bash
mvn test
```

### Test Results Summary:
- **Total Tests Executed**: **259 Tests**
- **Test Failures**: **0**
- **Test Errors**: **0**
- **Skipped Tests**: **0**
- **Build Status**: **`BUILD SUCCESS`**

---

## 5. Git Version Control & Repository Status

All modified files, extracted IG layer sources, unit tests, and documentation have been committed and pushed to the remote GitHub repository.
