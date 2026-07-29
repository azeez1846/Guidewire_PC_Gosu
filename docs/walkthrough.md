# Guidewire PolicyCenter Gosu OOTB Features - Technical Walkthrough

This document outlines the architecture, implementation details, and verification results of the **Guidewire Out-Of-The-Box (OOTB)** features implemented in the **Gosu programming language** (`.gs` / `.gsx`) within the Guidewire PolicyCenter codebase.

---

## 1. Overview of Implemented OOTB Features

The following core Guidewire PolicyCenter domain capabilities were built using native Gosu constructs:

| Feature Category | Gosu Class / File | Description |
| :--- | :--- | :--- |
| **Domain Enhancements (`.gsx`)** | `PolicyPeriodEnhancement.gsx`<br>`CoverageEnhancement.gsx`<br>`AccountEnhancement.gsx` | Extends core Java entities with formatted properties, active term checks, unearned premium calculations, and address aggregation without modifying base entity classes. |
| **Underwriting (UW) Rules & Referral Engine** | `UWIssue.gs`<br>`ApprovalAuthority.gs`<br>`UnderwritingRulesEngine.gs` | Underwriting rule execution engine evaluating high liability limits, catastrophe zones (FL, CA), short-term terms, and issuing `UWIssue` records with blocking points (`BlocksQuote`, `BlocksBind`, `BlocksIssuance`). |
| **Multi-Level Validation Engine** | `ValidationLevel.gs`<br>`ValidationService.gs` | Multi-stage policy validation supporting levels (`Default`, `Quoteable`, `Bindable`, `Issuable`) with structured error/warning rejections. |
| **Advanced Gosu Rating Engine** | `GosuRatingEngine.gs` | Financial calculation engine processing line base rates, coverage limits, deductibles, mid-term job proration, state tax matrix (CA 8%, NY 7.5%, TX 6%, FL 9%, IL 7%), and fee itemization. |
| **Policy Jobs (MTA & Cancellation)** | `PolicyChangeJobService.gs`<br>`CancellationJobService.gs` | Job lifecycle services for processing Mid-Term Endorsements (MTA) with `EditEffectiveDate` proration, and Pro-Rata / Short-Rate cancellations with reinstatement capability. |
| **Integration Plugins (`IGosuPlugin`)** | `IGosuPlugin.gs`<br>`AddressVerificationPlugin.gs`<br>`PaymentGatewayPlugin.gs`<br>`EventMessagingPlugin.gs` | Standard Guidewire integration plugin implementations for USPS address standardization, payment processing/tokenization, and event messaging notification payloads. |

---

## 2. Architecture & Gosu Directory Layout

```
src/main/gosu/gw/pc/
├── account/
│   └── AccountEnhancement.gsx
├── job/
│   ├── CancellationJobService.gs
│   └── PolicyChangeJobService.gs
├── model/
│   ├── CoverageEnhancement.gsx
│   └── PolicyPeriodEnhancement.gsx
├── orm/
│   └── EffDatedEnhancement.gsx
├── plugin/
│   ├── AddressVerificationPlugin.gs
│   ├── EventMessagingPlugin.gs
│   ├── IGosuPlugin.gs
│   └── PaymentGatewayPlugin.gs
├── rating/
│   └── GosuRatingEngine.gs
├── service/
│   ├── AccountService.gs
│   ├── AuthenticationService.gs
│   ├── SubmissionService.gs
│   └── ValidationService.gs
├── submission/
│   └── SubmissionEnhancement.gsx
├── uw/
│   ├── ApprovalAuthority.gs
│   ├── UnderwritingRulesEngine.gs
│   └── UWIssue.gs
└── validation/
    └── ValidationLevel.gs
```

---

## 3. Key Gosu Code Highlights

### Underwriting Rules Engine (`UnderwritingRulesEngine.gs`)
```gosu
package gw.pc.uw

import com.guidewire.pc.model.PolicyPeriod
import java.util.List
import java.util.ArrayList

class UnderwritingRulesEngine {
  public static function evaluateRules(period : PolicyPeriod) : List<UWIssue> {
    var issues = new ArrayList<UWIssue>()

    if ("$1M/$1M".equalsIgnoreCase(period.BodilyInjuryLimit)) {
      issues.add(new UWIssue(
        "UW_HIGH_LIMIT",
        "Bodily Injury limit $1M/$1M exceeds standard authority threshold.",
        "BlocksBind"
      ))
    }
    return issues
  }
}
```

### Gosu Domain Enhancement (`PolicyPeriodEnhancement.gsx`)
```gosu
package gw.pc.model

import com.guidewire.pc.model.PolicyPeriod
import java.math.BigDecimal

enhancement PolicyPeriodEnhancement : PolicyPeriod {
  public function calculateUnearnedPremium(asOfDate : java.util.Date) : BigDecimal {
    if (this.TotalPremium == null or this.PeriodStart == null or this.PeriodEnd == null) {
      return BigDecimal.ZERO
    }
    var totalMillis = this.PeriodEnd.getTime() - this.PeriodStart.getTime()
    var remainingMillis = this.PeriodEnd.getTime() - asOfDate.getTime()
    var fractionRemaining = (remainingMillis as double) / (totalMillis as double)
    return new BigDecimal(String.format("%.2f", {this.TotalPremium.doubleValue() * fractionRemaining}))
  }
}
```

---

## 4. Verification & Automated Test Results

Automated unit tests were executed using Apache Maven (`mvn clean test`).

### Test Summary:
- **Total Test Suites Executed**: 11
- **Total Unit Tests Executed**: 23
- **Failures**: 0
- **Errors**: 0
- **Skipped**: 0
- **Build Status**: **`BUILD SUCCESS`**

---

## 5. Server URL & Credentials

* **PolicyCenter Application UI**: `http://localhost:8085`
* **H2 Database Web Console**: `http://localhost:8082`
* **REST API OpenAPI Spec**: `http://localhost:8085/rest/v1/openapi.json`
* **Swagger UI**: `http://localhost:8085/swagger-ui`
* **Default Superuser Credentials**:
  * **Username**: `su`
  * **Password**: `gw`
