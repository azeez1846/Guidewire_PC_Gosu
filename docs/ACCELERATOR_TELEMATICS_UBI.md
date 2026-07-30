# Accelerator 3: Telematics & Usage-Based Insurance (UBI) Discount

## Overview
The **Telematics & Usage-Based Insurance (UBI) Discount Accelerator** connects driving telemetry data to Guidewire PolicyCenter rating engines. It analyzes miles driven, hard braking events, and night driving ratios to compute a dynamic Driver Safety Score (0–100) and apply premium discounts or risk surcharges.

---

## Technical Architecture

### 1. Gosu Plugin Implementation
- **File**: [`gw.pc.plugin.TelematicsPlugin.gs`](file:///Users/azeezmohiuddin/Downloads/Guidewire_PC_Gosu/src/main/gosu/gw/pc/plugin/TelematicsPlugin.gs)
- Calculates Driver Safety Score from driving metrics:
  - **Preferred Safe Driver (Score $\ge$ 85)**: 15% Premium Discount (Modifier: `0.85`).
  - **Good Driver (Score 70–84)**: 5% Premium Discount (Modifier: `0.95`).
  - **High Risk Driver (Score < 60)**: 10% Premium Surcharge (Modifier: `1.10`).

### 2. Rating Engine Integration
- **File**: [`RatingEngine.java`](file:///Users/azeezmohiuddin/Downloads/Guidewire_PC_Gosu/src/main/java/com/guidewire/pc/service/RatingEngine.java)
- Applies dynamic rate modifiers to written premiums.

### 3. REST API Endpoint
- **Endpoint**: `POST /rest/v1/telematics/score`
- **Sample Request Payload**:
  ```json
  {
    "milesDriven": 450,
    "hardBrakingEvents": 1,
    "nightDrivingRatio": 0.05
  }
  ```
- **Sample Response**:
  ```json
  {
    "safetyScore": 97,
    "ratingTier": "Preferred Safe Driver (15% Discount)",
    "modifierFactor": 0.85
  }
  ```

---

## Unit Testing & Verification
- **Test File**: [`TelematicsRatingTest.java`](file:///Users/azeezmohiuddin/Downloads/Guidewire_PC_Gosu/src/test/java/com/guidewire/pc/TelematicsRatingTest.java)
- Verifies preferred driver score calculation, aggressive driver surcharge calculation, and score bounding between 0 and 100.
