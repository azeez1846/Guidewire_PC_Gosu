/**
 * Guidewire PolicyCenter - AI Autonomous Underwriting Workbench Component
 */
export interface SubmissionRiskProfile {
    submissionNumber: string;
    lineOfBusiness: string;
    annualRevenue: number;
    lossHistoryCount: number;
    aiConfidenceScore: number;
    recommendation: 'AUTO_ACCEPT' | 'UNDERWRITER_REFERRAL' | 'DECLINE';
    rationale: string;
    riskFactors: string[];
}

export class AIUnderwritingWorkbenchComponent {
    private containerId: string;

    constructor(containerId: string) {
        this.containerId = containerId;
    }

    public render(profile: SubmissionRiskProfile): void {
        const container = document.getElementById(this.containerId);
        if (!container) return;

        let statusBg = '#059669';
        let statusText = 'AUTO ACCEPT';
        if (profile.recommendation === 'UNDERWRITER_REFERRAL') {
            statusBg = '#d97706';
            statusText = 'REFER TO UNDERWRITER';
        } else if (profile.recommendation === 'DECLINE') {
            statusBg = '#dc2626';
            statusText = 'DECLINE SUBMISSION';
        }

        const confidencePct = Math.round(profile.aiConfidenceScore * 100);

        container.innerHTML = `
            <div style="background: rgba(15, 23, 42, 0.95); border: 1px solid rgba(255,255,255,0.1); border-radius: 12px; padding: 20px; color: #f8fafc; font-family: system-ui, -apple-system, sans-serif;">
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;">
                    <div>
                        <h3 style="margin: 0; font-size: 18px; color: #a855f7; font-weight: 600;">🤖 AI Underwriting Copilot & Triage</h3>
                        <span style="font-size: 12px; color: #94a3b8;">Submission ID: ${profile.submissionNumber} | LOB: ${profile.lineOfBusiness}</span>
                    </div>
                    <div style="background: ${statusBg}; color: white; padding: 6px 14px; border-radius: 20px; font-weight: bold; font-size: 12px; letter-spacing: 0.5px;">
                        ${statusText}
                    </div>
                </div>

                <div style="display: grid; grid-template-columns: 1fr 2fr; gap: 16px; margin-bottom: 16px;">
                    <div style="background: #1e293b; border-radius: 8px; padding: 14px; text-align: center;">
                        <div style="font-size: 11px; color: #94a3b8; text-transform: uppercase;">AI Model Confidence</div>
                        <div style="font-size: 32px; font-weight: 800; color: ${confidencePct > 80 ? '#34d399' : '#fbbf24'}; margin: 4px 0;">
                            ${confidencePct}%
                        </div>
                        <div style="font-size: 11px; color: #cbd5e1;">Evaluated against carrier risk appetite</div>
                    </div>

                    <div style="background: #1e293b; border-radius: 8px; padding: 14px;">
                        <div style="font-size: 11px; color: #94a3b8; text-transform: uppercase; margin-bottom: 4px;">Automated Decision Rationale</div>
                        <div style="font-size: 13px; color: #e2e8f0; line-height: 1.4;">${profile.rationale}</div>
                    </div>
                </div>

                <div>
                    <div style="font-size: 12px; font-weight: 600; color: #cbd5e1; margin-bottom: 8px;">Extracted Risk Indicators</div>
                    ${profile.riskFactors.length === 0 ? `
                        <div style="font-size: 12px; color: #34d399;">✓ No adverse risk indicators detected. Risk within standard tolerance.</div>
                    ` : `
                        <div style="display: flex; flex-wrap: wrap; gap: 6px;">
                            ${profile.riskFactors.map(factor => `
                                <span style="background: rgba(239, 68, 68, 0.15); border: 1px solid rgba(239, 68, 68, 0.3); color: #fca5a5; padding: 4px 10px; border-radius: 12px; font-size: 11px;">
                                    ⚠️ ${factor}
                                </span>
                            `).join('')}
                        </div>
                    `}
                </div>
            </div>
        `;
    }
}
