# Accelerator 4: Digital E-Signature & Policy Binder Distribution

## Overview
The **Digital E-Signature & Policy Binder Distribution Accelerator** automates the delivery and tracking of Policy Binder PDF documents for electronic signatures (DocuSign / Adobe Sign). Upon policy binding, the accelerator packages the generated PDF binder and sends an e-signature transaction envelope to the insured.

---

## Technical Architecture

### 1. Gosu Plugin Implementation
- **File**: [`gw.pc.plugin.ESignaturePlugin.gs`](file:///Users/azeezmohiuddin/Downloads/Guidewire_PC_Gosu/src/main/gosu/gw/pc/plugin/ESignaturePlugin.gs)
- Creates DocuSign/Adobe Sign envelope transactions (`EnvelopeId`, `Status: Sent/Signed`, `SigningUrl`).
- Handles incoming webhook callbacks (`processSignatureCallback`).

### 2. REST API Endpoints
- **Envelope Creation**: `POST /rest/v1/documents/policy/{jobNumber}/esign`
- **Webhook Callback**: `POST /rest/v1/documents/policy/{jobNumber}/esign/callback`

---

## Unit Testing & Verification
- **Test File**: [`ESignatureTest.java`](file:///Users/azeezmohiuddin/Downloads/Guidewire_PC_Gosu/src/test/java/com/guidewire/pc/ESignatureTest.java)
- Verifies envelope ID generation, signer email validation, status transition to `Sent`, and callback handling for `Signed` events.
