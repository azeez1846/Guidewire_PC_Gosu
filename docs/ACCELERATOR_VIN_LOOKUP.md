# Accelerator 1: VIN Lookup & Vehicle Auto-Populate

## Overview
The **VIN Lookup & Vehicle Auto-Populate Accelerator** integrates vehicle decoding capabilities into Guidewire PolicyCenter. When an underwriter or agent inputs a 17-digit Vehicle Identification Number (VIN), the plugin decodes WMI, model year, manufacturer, body style, MSRP, safety rating, and anti-theft equipment, automatically populating submission attributes.

---

## Technical Architecture

### 1. Gosu Plugin Implementation
- **File**: [`gw.pc.plugin.VinDecoderPlugin.gs`](file:///Users/azeezmohiuddin/Downloads/Guidewire_PC_Gosu/src/main/gosu/gw/pc/plugin/VinDecoderPlugin.gs)
- Implements `IGosuPlugin`.
- Decodes WMI, year codes, vehicle MSRP, anti-theft systems, and safety scores.

### 2. REST API Endpoint
- **Endpoint**: `GET /rest/v1/vin/decode/{vin}`
- **Authentication**: Bearer Token or Session Cookie.
- **Sample Request**:
  ```http
  GET /rest/v1/vin/decode/1FA6P8CF0R1234567 HTTP/1.1
  Authorization: Bearer <session_token>
  ```
- **Sample Response**:
  ```json
  {
    "valid": true,
    "vin": "1FA6P8CF0R1234567",
    "make": "Ford",
    "model": "F-150 Commercial SuperDuty",
    "modelYear": 2024,
    "bodyStyle": "Pickup Truck",
    "msrp": 48500.0,
    "antiTheftDevice": "Factory Alarm & GPS Tracker",
    "country": "USA",
    "safetyScore": 92
  }
  ```

---

## Unit Testing & Verification
- **Test File**: [`VinDecoderTest.java`](file:///Users/azeezmohiuddin/Downloads/Guidewire_PC_Gosu/src/test/java/com/guidewire/pc/VinDecoderTest.java)
- Validates 17-character VIN requirement, manufacturer decoding, MSRP extraction, and invalid VIN handling.
