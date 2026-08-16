# Guidewire Integration Gateway (IG) Microservices & Extracted Sources Walkthrough

## Overview
This document covers the **4 Guidewire Cloud Integration Gateway (IG) Microservices**, their extracted source trees in `lib/`, and the multi-container Docker topology in `Guidewire_PC_Java_Gosu`.

---

## 1. Integration Gateway Architecture

```
+-----------------------------------------------------------------------------------------+
|                                    Docker Compose Mesh                                  |
|                                                                                         |
|  +-----------------------------------------------------------------------------------+  |
|  |                     Guidewire PolicyCenter App (Port 8085)                        |  |
|  +-------------------+--------------------+-------------------+------------------+---+  |
|                      |                    |                   |                  |      |
|                      v                    v                   v                  v      |
|            +------------------+ +------------------+ +------------------+ +-----------+  |
|            | vehicledetails-IG| | addressstandard. | |  creditfraud-IG  | |telematics |  |
|            |   (Port 8088)    | |  IG (Port 8089)  | |   (Port 8090)    | |IG (8091)  |  |
|            +------------------+ +------------------+ +------------------+ +-----------+  |
+-----------------------------------------------------------------------------------------+
```

---

## 2. Microservice Gateway Profiles

| Microservice Gateway | Executable JAR | Extracted Source Directory | Internal Port | External Port | Core Controller & Client |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Vehicle Details & MVR** | `lib/vehicledetails_IG-1.0.0.jar` | `lib/vehicledetails_IG-1.0.0_sources/` | `8080` | `8088` | `VehicleDetailsIGController.java`, `VehicleDetailsIGClient.java` |
| **Address Standardization** | `lib/addressstandardization_IG-1.0.0.jar` | `lib/addressstandardization_IG-1.0.0_sources/` | `8080` | `8089` | `AddressStandardizationIGController.java`, `AddressStandardizationIGClient.java` |
| **Credit Score & OFAC** | `lib/creditfraud_IG-1.0.0.jar` | `lib/creditfraud_IG-1.0.0_sources/` | `8090` | `8090` | `CreditFraudIGController.java`, `CreditFraudIGClient.java` |
| **IoT Fleet Telematics** | `lib/telematics_IG-1.0.0.jar` | `lib/telematics_IG-1.0.0_sources/` | `8091` | `8091` | `TelematicsIGController.java`, `TelematicsIGClient.java` |

---

## 3. Microservice Capabilities

### 1. Vehicle Details & MVR (`vehicledetails_IG`)
- **DMV / MVR Ingestion**: Verifies driver license validity, points, moving violations, and suspensions.
- **VIN Specification Lookup**: Decodes NHTSA vehicle attributes, curb weight, ADAS safety equipment, and ISO vehicle symbol rating.

### 2. Address Standardization (`addressstandardization_IG`)
- **USPS Delivery Point Validation (DPV)**: Validates street addresses, resolves ZIP+4 codes, and standardizes mailing addresses.
- **Geocoding**: Generates high-accuracy latitude/longitude coordinates for GIS catastrophe and flood mapping.

### 3. Credit Scoring & OFAC Sanctions (`creditfraud_IG`)
- **Credit-Based Insurance Scoring (CBIS)**: Translates Experian and D&B credit profiles into insurance tiers.
- **Sanctions Screening**: Screens applicants against the US Treasury OFAC Specially Designated Nationals list.

### 4. IoT Fleet Telematics (`telematics_IG`)
- **Telemetry Ingestion**: Ingests vehicle CAN bus events (hard braking, rapid acceleration, late-night driving, GPS speed differentials).
- **UBI Factor Computation**: Computes driving score multipliers ($0.75 - 1.25$) for commercial fleet pricing.
