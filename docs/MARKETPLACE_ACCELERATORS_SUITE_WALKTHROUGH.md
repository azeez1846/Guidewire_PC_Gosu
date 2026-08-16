# Guidewire Marketplace Accelerators Suite (#1 through #12) Walkthrough

## Overview
This document provides complete technical documentation for all **12 Guidewire Marketplace Accelerators** built into `Guidewire_PC_Java_Gosu`. Each accelerator delivers drop-in ecosystem integration for underwriting, compliance, rating, and distribution.

---

## Accelerators Matrix

| # | Accelerator Title | Java Service Class | Domain & Key Capability |
| :-: | :--- | :--- | :--- |
| **#1** | **VIN Lookup & Vehicle Safety** | `VINLookupService.java` | NHTSA API query for vehicle make, model, GVWR, ADAS safety features (AEB, Lane Assist), and ISO vehicle series rating. |
| **#2** | **Geospatial Catastrophe & Wildfire** | `GeospatialRiskService.java` | USGS & NOAA GIS spatial hazard scoring, FEMA flood zone mapping, and California WUI Tier 1/2 wildfire zone surcharges. |
| **#3** | **Telematics & Connected IoT UBI** | `TelematicsRatingService.java` | Ingestion of Samsara/Geotab vehicle telemetry (hard braking, harsh acceleration, night driving) for Usage-Based Insurance discounts (up to $-25\%$). |
| **#4** | **DocuSign Digital E-Signature** | `DocuSignService.java` | Automated generation of DocuSign e-signature envelope packages for digital policy binder execution. |
| **#5** | **AI Underwriting Assistant & Triage** | `AIUnderwritingService.java` | Automated risk intake triage, loss ratio analytics, policy referral escalation, and straight-through binding recommendations. |
| **#6** | **Tokenized Payment Gateway (Stripe)** | `PaymentGatewayService.java` | PCI-DSS compliant credit card tokenization, ACH recurring bank drafts, and installment collection. |
| **#7** | **Property Pre-Fill & Hazard Intel** | `PropertyPreFillService.java` | Pre-fills ISO Construction Classes (1–6), roof age/materials, distance to hydrant/station, protection class (1–10), and wildfire/flood scores from property address. |
| **#8** | **Prior Loss & C.L.U.E. History** | `PriorLossService.java` | 3-year commercial loss lookup, loss ratio scoring, experience modifier credit ($-15\%$) / debit ($+30\%$), and automated underwriter referral flags. |
| **#9** | **Reinsurance Treaty & Placement Slip** | `ReinsuranceSlipGenerator.java` | Generates ACORD Reinsurance Placement Slips, Quota Share cessions, $25\%$ ceding commissions, and syndicate bordereaux (Swiss Re, Munich Re, Hannover Re). |
| **#10** | **Secretary of State (SOS) Verifier** | `SOSEntityVerificationService.java` | Queries state corporate registries (Active, Delinquent, Suspended), officer records, and D&B Paydex / Financial Stress scores to detect fronting fraud and shell entities. |
| **#11** | **OFAC / PEP Sanctions & AML Screener** | `SanctionsComplianceService.java` | Fuzzy matching against US Treasury OFAC SDN & Politically Exposed Persons (PEP) lists with automated Underwriting Binding Lock enforcement. |
| **#12** | **AI Policy Binder Document Explainer** | `PolicyBinderExplainerService.java` | Auto-synthesizes multi-line commercial policy binders into a 1-page executive summary covering primary limits, warranties, exclusions, and payment installment plans. |

---

## Detailed Technical Specifications

### Accelerator #7: Property Pre-Fill & Hazard Intelligence
- **Service**: [`PropertyPreFillService.java`](file:///Users/azeezmohiuddin/Downloads/Guidewire_PC_Java_Gosu/src/main/java/com/guidewire/pc/service/PropertyPreFillService.java)
- **Endpoint**: `POST /rest/v1/accelerator/property-prefill`
- **Output Profile**:
  - ISO Construction Class: Class 1 (Frame) to Class 6 (Fire-Resistive Concrete Frame).
  - Roof Geometry & Covering: Flat Built-Up Membrane, Hip Class A Fire-Treated Tile, Single-Ply EPDM.
  - Fire Protection Metrics: Distance to Fire Station (miles), Distance to Hydrant (feet), ISO PPC ($1 - 10$).
  - Environmental Hazard: FEMA Flood Zone (AE, X, V), Wildfire Hazard Risk Score ($0 - 100$).
  - Valuation: Automated 360Value building replacement cost valuation.

### Accelerator #8: Prior Loss & C.L.U.E. History & Experience Modifier
- **Service**: [`PriorLossService.java`](file:///Users/azeezmohiuddin/Downloads/Guidewire_PC_Java_Gosu/src/main/java/com/guidewire/pc/service/PriorLossService.java)
- **Endpoint**: `POST /rest/v1/accelerator/prior-loss`
- **Scoring Logic**:
  - Zero 3-year losses: Loss Ratio $0\% \rightarrow$ Modifier $0.85$ ($-15\%$ Preferred Discount).
  - 1 minor claim: Loss Ratio $<30\% \rightarrow$ Modifier $1.00$ (Standard Rate).
  - Multiple losses or Loss Ratio $>50\% \rightarrow$ Modifier $1.30$ ($+30\%$ Surcharge) + Mandatory Underwriting Referral Trigger.

### Accelerator #9: Reinsurance Treaty & Placement Slip Generator
- **Service**: [`ReinsuranceSlipGenerator.java`](file:///Users/azeezmohiuddin/Downloads/Guidewire_PC_Java_Gosu/src/main/java/com/guidewire/pc/service/ReinsuranceSlipGenerator.java)
- **Endpoint**: `POST /rest/v1/reinsurance/slip`
- **Accounting & Syndication**:
  - Treaty Type: Quota Share or Excess of Loss (XOL).
  - Ceded Percentage: e.g. $40\%$ Quota Share cession.
  - Ceding Commission: $25\%$ of ceded premium credited back to primary carrier.
  - Syndicate Participants: Swiss Re ($45\%$), Munich Re ($35\%$), Hannover Re ($20\%$).

### Accelerator #10: Secretary of State & D&B Commercial Entity Verifier
- **Service**: [`SOSEntityVerificationService.java`](file:///Users/azeezmohiuddin/Downloads/Guidewire_PC_Java_Gosu/src/main/java/com/guidewire/pc/service/SOSEntityVerificationService.java)
- **Endpoint**: `POST /rest/v1/accelerator/sos-verify`
- **Fraud Prevention**:
  - Validates legal corporate name, FEIN, state of formation (DE, CA, TX, NY, FL), and filing standing.
  - Checks D&B Paydex Credit Score ($0 - 100$) and Financial Stress Score ($1 - 5$).
  - If filing is `SUSPENDED` or `DISSOLVED` $\rightarrow$ Flags shell company risk and blocks policy binding.

### Accelerator #11: OFAC / PEP Sanctions & AML Compliance Screener
- **Service**: [`SanctionsComplianceService.java`](file:///Users/azeezmohiuddin/Downloads/Guidewire_PC_Java_Gosu/src/main/java/com/guidewire/pc/service/SanctionsComplianceService.java)
- **Endpoint**: `POST /rest/v1/accelerator/ofac-screen`
- **Compliance Controls**:
  - Real-time screening against US Treasury Specially Designated Nationals (SDN) and Politically Exposed Persons (PEP).
  - Confidence matching score ($0 - 100\%$).
  - Scores $>85\%$ trigger `HARD_BLOCK_SANCTIONED` status, placing an Underwriting Binding Lock and recommending Suspicious Activity Report (SAR) filing.

### Accelerator #12: AI Policy Binder Document Explainer & Broker Briefing
- **Service**: [`PolicyBinderExplainerService.java`](file:///Users/azeezmohiuddin/Downloads/Guidewire_PC_Java_Gosu/src/main/java/com/guidewire/pc/service/PolicyBinderExplainerService.java)
- **Endpoint**: `POST /rest/v1/accelerator/binder-explainer`
- **Briefing Output**:
  - Translates 40+ page insurance contracts into a concise 1-page executive summary.
  - Extracts active primary coverages, endorsements, warranties (e.g. alarm warranty, coinsurance clauses), and exclusions.
  - Calculates $20\%$ down payment requirement and 11-month installment schedules.
