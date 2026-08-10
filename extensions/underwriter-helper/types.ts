/**
 * Guidewire PolicyCenter Underwriter Assistant — Shared Type Definitions
 *
 * Types used across background, content, and popup scripts.
 */

// ── Underwriting Triage API ──────────────────────────────────────────────────

export interface TriageRequest {
  submissionId: string;
  policyNumber: string;
  lineOfBusiness: string;
  annualPremium: number;
  driverScore: number;
  highFloodZone: boolean;
}

export type TriageRecommendation =
  | "STRAIGHT_THROUGH_BIND"
  | "UW_REFERRAL"
  | "DECLINE"
  | "BIND_READY";

export interface TriageResponse {
  submissionId?: string;
  recommendation: TriageRecommendation;
  riskScore: number;
  rationale: string[];
  escalationRequired?: boolean;
}

// ── Chrome Extension Messaging ───────────────────────────────────────────────

export interface ExtractTextMessage {
  action: "extractSelectedText";
}

export interface TriggerTriageMessage {
  action: "triggerTriage";
}

export type CopilotMessage = ExtractTextMessage | TriggerTriageMessage;

export interface ExtractTextResponse {
  text: string;
}

export interface TriggerTriageResponse {
  status: "triggered";
}

// ── GraphQL Gateway ──────────────────────────────────────────────────────────

export interface GraphQLPolicyQuery {
  query: string;
}

export interface GraphQLPolicyData {
  data: {
    policy: {
      policyNumber: string;
      status: string;
      annualPremium: number;
    };
  };
}

// ── Search API ───────────────────────────────────────────────────────────────

export interface SearchResponse {
  [key: string]: unknown;
}
