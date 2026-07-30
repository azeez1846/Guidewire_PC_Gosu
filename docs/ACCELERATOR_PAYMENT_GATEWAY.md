# Accelerator 6: Tokenized Premium Payment Gateway & Installment Engine

## Overview
The **Tokenized Premium Payment Gateway & Installment Engine Accelerator** provides credit card/ACH payment method tokenization (`tok_...`), down-payment validation, and 12-month installment payment schedule generation for Guidewire PolicyCenter policy transactions.

---

## Technical Architecture

### 1. Gosu Plugin Implementation
- **File**: [`gw.pc.plugin.PaymentGatewayPlugin.gs`](file:///Users/azeezmohiuddin/Downloads/Guidewire_PC_Gosu/src/main/gosu/gw/pc/plugin/PaymentGatewayPlugin.gs)
- Functions:
  - `processDownPayment`: Processes upfront policy binder payments.
  - `tokenizePaymentMethod`: Converts card numbers to secure tokens (`tok_visa_4242`).
  - `generateInstallmentSchedule`: Computes 20% down payment + 12 monthly installments.

### 2. REST API Endpoints
- **Tokenize Card**: `POST /rest/v1/payments/tokenize`
- **Installment Schedule**: `GET /rest/v1/payments/schedule/{jobNumber}`

---

## Unit Testing & Verification
- **Test File**: [`PaymentGatewayTest.java`](file:///Users/azeezmohiuddin/Downloads/Guidewire_PC_Gosu/src/test/java/com/guidewire/pc/PaymentGatewayTest.java)
- Tests payment method tokenization, down-payment processing validation, and 12-month installment schedule calculations.
