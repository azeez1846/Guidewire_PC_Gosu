/**
 * Guidewire PolicyCenter - Real-Time Parametric Catastrophe Map Component
 */
export interface ParametricTriggerData {
    policyNumber: string;
    perilType: 'HURRICANE_WIND' | 'EARTHQUAKE_SEISMIC' | 'FLOOD_STAGE';
    postalCode: string;
    metricRecorded: string;
    threshold: string;
    isTriggered: boolean;
    payoutAmount: number;
    status: string;
}

export class ParametricMapComponent {
    private containerId: string;

    constructor(containerId: string) {
        this.containerId = containerId;
    }

    public render(data: ParametricTriggerData): void {
        const container = document.getElementById(this.containerId);
        if (!container) return;

        const isTriggered = data.isTriggered;
        const statusColor = isTriggered ? '#ef4444' : '#10b981';

        container.innerHTML = `
            <div style="background: rgba(15, 23, 42, 0.95); border: 1px solid rgba(255,255,255,0.1); border-radius: 12px; padding: 20px; color: #f8fafc; font-family: system-ui, -apple-system, sans-serif;">
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;">
                    <div>
                        <h3 style="margin: 0; font-size: 18px; color: #38bdf8; font-weight: 600;">🌪️ Parametric Catastrophe Live Trigger Monitor</h3>
                        <span style="font-size: 12px; color: #94a3b8;">Zone Postal Code: ${data.postalCode} | Peril: ${data.perilType}</span>
                    </div>
                    <div style="display: flex; align-items: center; gap: 8px;">
                        <div style="width: 10px; height: 10px; border-radius: 50%; background: ${statusColor}; box-shadow: 0 0 10px ${statusColor};"></div>
                        <span style="font-size: 12px; font-weight: bold; color: ${statusColor};">
                            ${isTriggered ? 'CAT STRIKE TRIGGERED' : 'MONITORING NORMAL'}
                        </span>
                    </div>
                </div>

                <div style="display: grid; grid-template-columns: repeat(3, 1fr); gap: 12px; margin-bottom: 16px;">
                    <div style="background: #1e293b; padding: 12px; border-radius: 8px; text-align: center;">
                        <div style="font-size: 11px; color: #94a3b8;">Observed Telemetry</div>
                        <div style="font-size: 18px; font-weight: bold; color: #f1f5f9; margin-top: 4px;">${data.metricRecorded}</div>
                    </div>
                    <div style="background: #1e293b; padding: 12px; border-radius: 8px; text-align: center;">
                        <div style="font-size: 11px; color: #94a3b8;">Trigger Threshold</div>
                        <div style="font-size: 18px; font-weight: bold; color: #f1f5f9; margin-top: 4px;">${data.threshold}</div>
                    </div>
                    <div style="background: #1e293b; padding: 12px; border-radius: 8px; text-align: center;">
                        <div style="font-size: 11px; color: #94a3b8;">Automated Payout</div>
                        <div style="font-size: 18px; font-weight: bold; color: ${isTriggered ? '#34d399' : '#94a3b8'}; margin-top: 4px;">
                            $${data.payoutAmount.toLocaleString()}
                        </div>
                    </div>
                </div>

                <div style="background: rgba(30, 41, 59, 0.6); padding: 10px; border-radius: 6px; font-size: 12px; color: #cbd5e1;">
                    <strong>Status:</strong> ${data.status} | <strong>Policy:</strong> ${data.policyNumber}
                </div>
            </div>
        `;
    }
}
