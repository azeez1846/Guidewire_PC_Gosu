# Commercial Lines Rating & Specialty Endorsements Walkthrough

## Overview
This document details the rating methodologies, formulas, underwriting modifiers, and endorsement structures implemented across all 6 Commercial Lines of Business in `Guidewire_PC_Java_Gosu`.

---

## 1. Commercial Property (CP) Deep Endorsements & Blanket Rating Suite
- **Core Files**: [`CPRatingService.java`](file:///Users/azeezmohiuddin/Downloads/Guidewire_PC_Java_Gosu/src/main/java/com/guidewire/pc/service/CPRatingService.java), [`CPRatingEngine.gs`](file:///Users/azeezmohiuddin/Downloads/Guidewire_PC_Java_Gosu/src/main/gosu/gw/pc/line/cp/CPRatingEngine.gs)
- **Entity Schemas**: `CPTenantsImprovementCov.eti`, `CPBusinessIncomeCov.eti`, `CPBoilerMachineryCov.eti`, `CPBlanketCoverage.eti`

### Key Capabilities:
1. **Tenants Improvements & Betterments**:
   - Rates fixtures, alterations, and permanent installations made by tenant policyholders.
   - Base Rate: $\$0.42$ per $\$100$ limit.
   - Replacement Cost valuation basis adds a $+15\%$ surcharge over Actual Cash Value (ACV).
2. **Business Income & Extra Expense**:
   - Rates business interruption based on monthly indemnity fraction options:
     - $1/3$ Monthly Limit: $0.90\times$ multiplier.
     - $1/4$ Monthly Limit: $0.80\times$ multiplier.
     - $1/6$ Monthly Limit: $0.65\times$ multiplier.
   - Ordinary payroll extension endorsement adds $+20\%$ payroll coverage surcharge.
3. **Equipment Breakdown / Boiler & Machinery**:
   - Comprehensive mechanical breakdown, pressure vessel, and electrical arcing perils.
   - Rate: $\$0.18$ per $\$100$ equipment limit. Heavy production machinery adds $+25\%$ loading.
4. **Blanket Coverage Pools**:
   - Consolidates multiple buildings or locations under a single pooled aggregate limit.
   - Applies weighted average rate across scheduled locations.
   - Grants $5\%$ discount credit for $90\%$ coinsurance agreements and $10\%$ credit for $100\%$ agreed value.
5. **Coinsurance Penalty Formula**:
   - Enforces statutory claim payout reduction for under-insured properties:
     $$\text{Gross Payout} = \text{Claim Loss} \times \left(\frac{\text{Carried Limit}}{\text{Actual Property Value} \times \text{Coinsurance \%}}\right)$$
     $$\text{Net Payout} = \max(0, \text{Gross Payout} - \text{Deductible})$$

---

## 2. Workers' Compensation (WC) Retrospective Rating Plan
- **Core Files**: [`WCRetrospectiveRatingEngine.java`](file:///Users/azeezmohiuddin/Downloads/Guidewire_PC_Java_Gosu/src/main/java/com/guidewire/pc/service/WCRetrospectiveRatingEngine.java), [`WCRatingService.java`](file:///Users/azeezmohiuddin/Downloads/Guidewire_PC_Java_Gosu/src/main/java/com/guidewire/pc/service/WCRatingService.java), [`WCRatingEngine.gs`](file:///Users/azeezmohiuddin/Downloads/Guidewire_PC_Java_Gosu/src/main/gosu/gw/pc/line/wc/WCRatingEngine.gs)

### Key Capabilities:
1. **NCCI Retrospective Rating Plan**:
   - Designed for large commercial employers whose final premium reflects actual losses during the policy term:
     $$\text{Basic Premium} = \text{Standard Manual Premium} \times \text{Basic Premium Factor (e.g. 0.22)}$$
     $$\text{Converted Losses} = \text{Incurred Losses} \times \text{Loss Conversion Factor (LCF e.g. 1.15)}$$
     $$\text{Uncapped Retro Premium} = (\text{Basic Premium} + \text{Converted Losses}) \times \text{State Tax Multiplier (1.05)}$$
2. **Boundary Cap Enforcement**:
   - Enforces contractual Minimum Premium Cap (e.g., $60\%$ of standard premium) and Maximum Premium Cap (e.g., $140\%$ of standard premium).
3. **Experience Rating (eMod) & Safety Discounts**:
   - Standard manual payroll rating across class codes (e.g. Code 8810 Clerical, Code 5606 Executive Supervisor).
   - Multiplies by NCCI Experience Modification Factor (acceptable range: $0.400 - 2.500$).
   - Certified OSHA Safety Management Programs receive an automated $5\%$ premium credit.

---

## 3. General Liability (GL) Multi-Variable Composite Rating Suite
- **Core Files**: [`GLCompositeRatingEngine.java`](file:///Users/azeezmohiuddin/Downloads/Guidewire_PC_Java_Gosu/src/main/java/com/guidewire/pc/service/GLCompositeRatingEngine.java), [`GLRatingService.java`](file:///Users/azeezmohiuddin/Downloads/Guidewire_PC_Java_Gosu/src/main/java/com/guidewire/pc/service/GLRatingService.java), [`GLRatingEngine.gs`](file:///Users/azeezmohiuddin/Downloads/Guidewire_PC_Java_Gosu/src/main/gosu/gw/pc/line/gl/GLRatingEngine.gs)

### Key Capabilities:
1. **Multi-Exposure Composite Rating**:
   - Consolidates distinct exposure metrics into a unified general liability quote:
     - Gross Annual Sales: $\$4.50$ per $\$1,000$ sales.
     - Premises Square Footage: $\$85.00$ per $1,000$ sq ft.
     - Operational Payroll: $\$1.80$ per $\$100$ payroll.
2. **Owners & Contractors Protective (OCP) Liability**:
   - Rates independent contractor liability endorsements at $0.12\%$ of the designated OCP limit.
3. **Tiered Liquor Liability Hazards**:
   - Rates alcohol sales volume with risk-tiered multipliers:
     - Tier 1 (Family Dining / Restaurant): $1.00\times$ base rate ($\$8.50$ per $\$1k$).
     - Tier 2 (Bar / Tavern / Sports Pub): $1.60\times$ multiplier.
     - Tier 3 (Nightclub / High-Density Venue): $2.50\times$ multiplier.
4. **Products-Completed Operations**:
   - Tracks aggregate limits and rates completed operations exposure at $\$1.20$ per $\$1,000$ limit.

---

## 4. Commercial Auto (CA) Fleet Radius & Hazmat Surcharge Engine
- **Core Files**: [`AutoFleetRadiusHazmatEngine.java`](file:///Users/azeezmohiuddin/Downloads/Guidewire_PC_Java_Gosu/src/main/java/com/guidewire/pc/service/AutoFleetRadiusHazmatEngine.java), [`CommercialAutoRatingService.java`](file:///Users/azeezmohiuddin/Downloads/Guidewire_PC_Java_Gosu/src/main/java/com/guidewire/pc/service/CommercialAutoRatingService.java)
- **Entity Schemas**: `AutoPollutionCoverage.eti`

### Key Capabilities:
1. **Operating Radius Classification**:
   - Classifies power units by maximum operating distance from home terminal:
     - Local ($<50$ miles): $1.00\times$ baseline.
     - Intermediate ($50 - 200$ miles): $1.25\times$ factor.
     - Long Distance ($>200$ miles): $1.60\times$ factor.
2. **DOT Hazardous Materials (Hazmat) Surcharge Matrix**:
   - Class 3 Flammable Liquids: $+40\%$ surcharge.
   - Class 8 Corrosives / Class 2 Compressed Gases: $+50\%$ surcharge.
   - Class 1 Explosives / Class 7 Radioactive Materials: $+85\%$ surcharge.
3. **CA 99 48 Broadened Pollution Liability**:
   - Extends auto coverage for discharge or release of transported pollutants ($\$1,250$ flat endorsement base).

---

## 5. Inland Marine (IM) Contractors' Equipment Schedule & Floater
- **Core Files**: [`ContractorsEquipmentEngine.java`](file:///Users/azeezmohiuddin/Downloads/Guidewire_PC_Java_Gosu/src/main/java/com/guidewire/pc/service/ContractorsEquipmentEngine.java), [`IMRatingService.java`](file:///Users/azeezmohiuddin/Downloads/Guidewire_PC_Java_Gosu/src/main/java/com/guidewire/pc/service/IMRatingService.java)

### Key Capabilities:
1. **Scheduled Equipment Schedule**:
   - Stated value rating for heavy construction machinery (excavators, cranes, bulldozers) at $\$1.65$ per $\$100$ value.
   - Replacement Cost valuation basis applies a $+12\%$ loading over ACV.
2. **Rented, Leased & Borrowed Equipment Floater**:
   - Sub-limit protection for third-party hired equipment at $\$1.90$ per $\$100$ limit.
   - Miscellaneous unscheduled small tools floater at $\$2.25$ per $\$100$ limit.
3. **Boom & Overload Collision Perils**:
   - Hydraulic crane boom collapse and equipment upset perils endorsement ($+20\%$ surcharge).
4. **Deductible Buy-Down Credits**:
   - Baseline deductible: $\$2,500$.
   - Buy-down to $\$1,000$: $+10\%$ charge.
   - Buy-up to $\$5,000$: $-12\%$ credit; $\$10,000$: $-20\%$ credit.

---

## 6. Commercial Umbrella (CU) Excess Liability
- **Core Files**: `UmbrellaRatingEngine.gs`, `CommercialUmbrellaService.java`
- **Capabilities**:
  - Provides excess coverage layers above underlying Commercial Auto ($\$1\text{M}$), General Liability ($\$1\text{M}/\text{\$}2\text{M}$), and Employers' Liability ($\$1\text{M}$).
  - Automatically calculates drop-down coverage triggers upon underlying aggregate exhaustion.
