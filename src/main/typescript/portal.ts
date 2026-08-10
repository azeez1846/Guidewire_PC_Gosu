/**
 * Guidewire PolicyCenter Front-End Portal Engine (TypeScript)
 * Interactive claims intake, SIU fraud scoring, AI underwriting triage, and policy administration.
 */

export interface ClaimIntakePayload {
  policyNumber: string;
  claimantName: string;
  lossAmount: number;
  lossCause: string;
  lossDescription: string;
}

export interface ClaimIntakeResponse {
  claimNumber: string;
  policyNumber: string;
  status: string;
  fraudRiskScore: number;
  siuReferralTriggered: boolean;
  riskSignals: string[];
  underwritingAction: string;
}

export interface AITriagePayload {
  policyNumber: string;
  driverScore: number;
  highFloodZone: boolean;
  annualPremium: number;
}

export interface AITriageResponse {
  policyNumber: string;
  recommendation: 'STRAIGHT_THROUGH_BIND' | 'UW_REFERRAL' | 'DECLINE';
  riskScore: number;
  rationale: string[];
  escalationRequired: boolean;
  timestamp: number;
}

export class PolicyCenterPortal {
  private apiBaseUrl: string;

  constructor(apiBaseUrl: string = '/rest/v1') {
    this.apiBaseUrl = apiBaseUrl;
  }

  /**
   * Submit FNOL Claim Intake to Backend SIU Service
   */
  public async submitClaim(payload: ClaimIntakePayload): Promise<ClaimIntakeResponse> {
    const response = await fetch(`${this.apiBaseUrl}/claims/submit`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(payload)
    });

    if (!response.ok) {
      throw new Error(`Failed to submit claim intake: ${response.statusText}`);
    }

    return await response.json();
  }

  /**
   * Execute Autonomous AI Underwriting Triage Check
   */
  public async executeAITriage(payload: AITriagePayload): Promise<AITriageResponse> {
    const response = await fetch(`${this.apiBaseUrl}/ai/triage-portal`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(payload)
    });

    if (!response.ok) {
      throw new Error(`Failed to execute AI Triage check: ${response.statusText}`);
    }

    return await response.json();
  }

  /**
   * Quick Quote Premium Calculator
   */
  public calculateQuickQuote(basePremium: number, driverScore: number, isFloodZone: boolean, highLimit: boolean): number {
    let rate = basePremium;
    if (driverScore >= 85) rate *= 0.85; // 15% telematics discount
    else if (driverScore < 60) rate *= 1.30; // 30% risk surcharge

    if (isFloodZone) rate += 450;
    if (highLimit) rate += 320;

    return Math.round(rate * 100) / 100;
  }
}

// Global window binding for browser interactive access
if (typeof window !== 'undefined') {
  (window as any).PolicyCenterPortal = PolicyCenterPortal;
}
