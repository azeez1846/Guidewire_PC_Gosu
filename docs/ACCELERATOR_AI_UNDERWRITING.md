# Accelerator 5: AI Automated Underwriting Assistant & Referral Escalation

## Overview
The **AI Automated Underwriting Assistant & Referral Escalation Accelerator** uses automated risk triage rules to streamline PolicyCenter submission processing. It evaluates loss history ratios, FEIN business credit scores, and total premium thresholds to execute Straight-Through Processing (STP) for low-risk submissions or route high-risk submissions to senior underwriting queues.

---

## Technical Architecture

### 1. Java Triage Engine
- **File**: [`AIUnderwritingAssistant.java`](file:///Users/azeezmohiuddin/Downloads/Guidewire_PC_Gosu/src/main/java/com/guidewire/pc/service/AIUnderwritingAssistant.java)
- Triage Logic:
  - **Straight-Through Processing (STP)**: Total Premium < $5,000, 0.0% Loss Ratio, Credit Score $\ge$ 700.
  - **High Risk Referral**: Loss Ratio > 40% or Credit Score < 600 $\rightarrow$ Creates Escalation Activity via [`ActivityEscalationBatchProcess.java`](file:///Users/azeezmohiuddin/Downloads/Guidewire_PC_Gosu/src/main/java/com/guidewire/pc/batch/ActivityEscalationBatchProcess.java).
  - **Standard Review**: Assigns job to standard Underwriter queue.

---

## Unit Testing & Verification
- **Test File**: [`AIUnderwritingAssistantTest.java`](file:///Users/azeezmohiuddin/Downloads/Guidewire_PC_Gosu/src/test/java/com/guidewire/pc/AIUnderwritingAssistantTest.java)
- Tests STP auto-approval decisioning, high-risk referral decisioning, and standard underwriting review paths.
