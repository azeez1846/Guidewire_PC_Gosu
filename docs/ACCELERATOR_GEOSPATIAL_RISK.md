# Accelerator 2: Geospatial Property Catastrophe & Risk Assessment

## Overview
The **Geospatial Property Catastrophe & Risk Assessment Accelerator** integrates location intelligence into Guidewire PolicyCenter rule engines. It evaluates Wildfire hazard scores, Flood Zone classification (Zone A, V, X), and coastline proximity, triggering automated Underwriting Holds and mandatory deductible adjustments.

---

## Technical Architecture

### 1. Gosu Plugin Implementation
- **File**: [`gw.pc.plugin.GeospatialRiskPlugin.gs`](file:///Users/azeezmohiuddin/Downloads/Guidewire_PC_Gosu/src/main/gosu/gw/pc/plugin/GeospatialRiskPlugin.gs)
- Evaluates location parameters (`city`, `state`, `postalCode`).
- Returns hazard scores and underwriting hold recommendations.

### 2. Underwriting Rule Integration
- **File**: [`RulesEngine.java`](file:///Users/azeezmohiuddin/Downloads/Guidewire_PC_Gosu/src/main/java/com/guidewire/pc/rules/RulesEngine.java)
- `GeospatialCatastropheRiskRule`: Triggers Underwriting Hold on `CA` (Wildfire score 85) and `FL` (Special Flood Hazard Zone A).

### 3. REST API Endpoint
- **Endpoint**: `POST /rest/v1/risk/geospatial`
- **Sample Payload**:
  ```json
  {
    "city": "San Jose",
    "state": "CA",
    "postalCode": "95113"
  }
  ```

---

## Unit Testing & Verification
- **Test File**: [`GeospatialRiskTest.java`](file:///Users/azeezmohiuddin/Downloads/Guidewire_PC_Gosu/src/test/java/com/guidewire/pc/GeospatialRiskTest.java)
- Tests high wildfire risk detection in CA, flood zone detection in FL, and normal risk scoring in TX.
