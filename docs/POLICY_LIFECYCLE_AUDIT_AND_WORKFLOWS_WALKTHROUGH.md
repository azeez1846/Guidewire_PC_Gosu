# Policy Lifecycle, Multi-Class Audit & Enterprise Workflows Walkthrough

## Overview
This document details the transaction lifecycle management, multi-class exposure audit processing, out-of-sequence conflict resolution, risk engineering surveys, parametric weather triggers, and reinsurance accounting engines in `Guidewire_PC_Java_Gosu`.

---

## 1. Commercial Multi-Class Exposure Audit Engine
- **Core File**: [`CommercialAuditEngine.java`](file:///Users/azeezmohiuddin/Downloads/Guidewire_PC_Java_Gosu/src/main/java/com/guidewire/pc/service/CommercialAuditEngine.java)
- **Endpoint**: `POST /rest/v1/audit/multiclass/execute`

### Key Capabilities:
1. **Multi-Classification Code Exposure Reconciliation**:
   - Reconciles estimated vs actual audited payrolls across multiple classification codes (e.g., Code 8810 Clerical at $\$0.45/\$100$ vs Code 5183 Plumbing/HVAC at $\$3.85/\$100$).
   - Calculates exposure variances and revised standard earned premiums.
2. **State Statutory Assessments**:
   - Computes state guaranty fund and workers' compensation board surcharges ($3.5\%$).
3. **Formal Audit Dispute Workflow**:
   - Handles policyholder payroll disputes, re-evaluates subcontractor certificates of insurance (COIs), and computes adjusted final settlement invoices.

---

## 2. Out-of-Sequence (OOS) Conflict Resolution & Proration Engine
- **Core File**: [`OOSConflictResolver.java`](file:///Users/azeezmohiuddin/Downloads/Guidewire_PC_Java_Gosu/src/main/java/com/guidewire/pc/service/OOSConflictResolver.java)
- **Endpoints**: `POST /rest/v1/oos/resolve`, `POST /rest/v1/policy/proration-refund`

### Key Capabilities:
1. **EffDated Timeline Slice Conflict Detection**:
   - Identifies attribute conflicts when a retroactive mid-term endorsement is applied prior to subsequent policy changes.
2. **Automated Forward Slice Merging**:
   - Auto-merges non-conflicting slices forward along the timeline or generates underwriting escalation flags.
3. **Exact Calendar-Day Proration**:
   - **Pro-Rata Cancellation**: Refunds unearned premium based on exact elapsed days:
     $$\text{Pro-Rata Refund} = \text{Annual Premium} \times \left(\frac{\text{Unearned Days}}{\text{Total Policy Days}}\right)$$
   - **Short-Rate Cancellation**: Retains a $10\%$ short-rate penalty on unearned premium:
     $$\text{Short-Rate Penalty} = \text{Pro-Rata Refund} \times 10\%$$
     $$\text{Net Refund Payable} = \text{Pro-Rata Refund} - \text{Short-Rate Penalty}$$

---

## 3. Commercial Policy Split & Subsidiary Spin-Off Workflow
- **Core File**: [`PolicySplitRewriteService.java`](file:///Users/azeezmohiuddin/Downloads/Guidewire_PC_Java_Gosu/src/main/java/com/guidewire/pc/service/PolicySplitRewriteService.java)
- **Endpoint**: `POST /rest/v1/policy/split-rewrite`

### Key Capabilities:
1. **Corporate Divestiture & Spin-Off**:
   - Splits a multi-location master commercial policy (e.g. `POL-PARENT-1001`) into separate legal entity contracts upon corporate reorganization.
2. **Asset & Premium Allocation**:
   - Automatically transfers designated locations and vehicle schedules to the new spin-off policy (e.g. `POL-SPIN-2001`).
   - Re-allocates inception premiums and adjusts retained parent policy terms.
3. **Claims History Continuity**:
   - Preserves historical loss triangle linkages across both parent and child policies for actuarial reporting.

---

## 4. Loss Control Survey & Safety Recommendation Engine
- **Core File**: [`LossControlInspectionService.java`](file:///Users/azeezmohiuddin/Downloads/Guidewire_PC_Java_Gosu/src/main/java/com/guidewire/pc/service/LossControlInspectionService.java)
- **Endpoint**: `POST /rest/v1/loss-control/recommendations`

### Key Capabilities:
1. **Engineering Risk Surveys**:
   - Evaluates commercial facilities and issues Mandatory (30/60-day deadline) vs Advisory (90-day deadline) safety recommendations.
   - Examples: UL-300 wet chemical kitchen fire suppression retrofit, replacement of obsolete FPE Stab-Lok circuit breaker panels.
2. **Automated Direct Notice of Cancellation (DNOC)**:
   - Evaluates compliance status; if mandatory recommendations remain overdue, automatically initiates a 30-day Direct Notice of Cancellation (DNOC) workflow.

---

## 5. Parametric Weather & Special Event Cancellation Endorsement
- **Core File**: [`ParametricEventCancellationEngine.java`](file:///Users/azeezmohiuddin/Downloads/Guidewire_PC_Java_Gosu/src/main/java/com/guidewire/pc/service/ParametricEventCancellationEngine.java)
- **Endpoint**: `POST /rest/v1/parametric/event-cancellation`

### Key Capabilities:
1. **Parametric Contract Structure**:
   - Endorsement covering gross ticket and vendor revenue for outdoor festivals, concerts, and sporting events.
   - Triggers: NOAA rainfall accumulation ($>1.25$ inches) or sustained wind speed ($>45$ mph) during designated event hours.
2. **Frictionless Automatic Indemnity**:
   - Evaluates real-time IoT weather sensor telemetry against contract thresholds.
   - Dispatches $100\%$ stated value indemnity payout instantly without loss adjuster delays upon breach.

---

## 6. Broker Agency Bill & Monthly Account Current Settlement
- **Core File**: [`AgencyBillAccountCurrentService.java`](file:///Users/azeezmohiuddin/Downloads/Guidewire_PC_Java_Gosu/src/main/java/com/guidewire/pc/service/AgencyBillAccountCurrentService.java)
- **Endpoint**: `POST /rest/v1/billing/agency-account-current`

### Key Capabilities:
1. **Monthly Statement Reconciliation**:
   - Aggregates all monthly policy transactions (New Business, Renewal, Endorsement, Cancellation) for an independent broker agency.
2. **Commission Retention & Net Remittance**:
   - Computes broker commission retention (e.g. $15\%$) and net balance payable to the carrier via ACH.

---

## 7. Catastrophe Reinsurance Treaty Reinstatement Premium Engine
- **Core File**: [`CatReinsuranceReinstatementEngine.java`](file:///Users/azeezmohiuddin/Downloads/Guidewire_PC_Java_Gosu/src/main/java/com/guidewire/pc/service/CatReinsuranceReinstatementEngine.java)
- **Endpoint**: `POST /rest/v1/reinsurance/cat-reinstatement`

### Key Capabilities:
1. **CAT XOL Treaty Limit Restoration**:
   - When a major hurricane or earthquake claim impairs a catastrophe excess of loss layer (e.g. $\$50\text{M}$ xs $\$25\text{M}$ layer), calculates the reinstatement premium due to restore full capacity:
     $$\text{Reinstatement Premium} = \text{Annual Treaty Premium} \times \left(\frac{\text{Loss Amount}}{\text{Layer Limit}}\right) \times \left(\frac{\text{Unexpired Days}}{\text{Total Days}}\right) \times \text{Reinstatement Rate \%}$$
