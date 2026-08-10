/**
 * Guidewire PolicyCenter - Interactive Reinsurance Treaty & Retention Heatmap Component
 */
export interface ReinsuranceData {
    policyNumber: string;
    totalInsuredValue: number;
    primaryRetention: number;
    quotaShareCeded: number;
    surplusTreatyCeded: number;
    facultativeCeded: number;
    reinsurers: { name: string; sharePct: number; amount: number }[];
}

export class ReinsuranceHeatmapComponent {
    private containerId: string;

    constructor(containerId: string) {
        this.containerId = containerId;
    }

    public render(data: ReinsuranceData): void {
        const container = document.getElementById(this.containerId);
        if (!container) return;

        const total = data.totalInsuredValue || 1;
        const retentionPct = ((data.primaryRetention / total) * 100).toFixed(1);
        const qsPct = ((data.quotaShareCeded / total) * 100).toFixed(1);
        const surplusPct = ((data.surplusTreatyCeded / total) * 100).toFixed(1);
        const facPct = ((data.facultativeCeded / total) * 100).toFixed(1);

        container.innerHTML = `
            <div style="background: rgba(15, 23, 42, 0.9); border: 1px solid rgba(255,255,255,0.1); backdrop-filter: blur(12px); border-radius: 12px; padding: 20px; color: #f8fafc; font-family: system-ui, -apple-system, sans-serif;">
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;">
                    <h3 style="margin: 0; font-size: 18px; color: #38bdf8; font-weight: 600;">⚡ Reinsurance Cession & Retention Heatmap</h3>
                    <span style="background: #1e293b; padding: 4px 10px; border-radius: 20px; font-size: 12px; color: #94a3b8; border: 1px solid #334155;">
                        Policy: ${data.policyNumber} | TIV: $${data.totalInsuredValue.toLocaleString()}
                    </span>
                </div>

                <div style="margin-bottom: 20px;">
                    <div style="font-size: 12px; color: #94a3b8; margin-bottom: 6px;">Capital Layer Allocation Stack</div>
                    <div style="display: flex; height: 32px; border-radius: 8px; overflow: hidden; font-size: 11px; font-weight: bold; text-align: center; line-height: 32px;">
                        <div style="width: ${retentionPct}%; background: #0284c7; color: white;" title="Insurer Retention ($${data.primaryRetention.toLocaleString()})">
                            Primary (${retentionPct}%)
                        </div>
                        <div style="width: ${qsPct}%; background: #0d9488; color: white;" title="Quota Share ($${data.quotaShareCeded.toLocaleString()})">
                            QS (${qsPct}%)
                        </div>
                        <div style="width: ${surplusPct}%; background: #d97706; color: white;" title="Surplus Treaty ($${data.surplusTreatyCeded.toLocaleString()})">
                            Surplus (${surplusPct}%)
                        </div>
                        <div style="width: ${facPct}%; background: #e11d48; color: white;" title="Facultative ($${data.facultativeCeded.toLocaleString()})">
                            Fac (${facPct}%)
                        </div>
                    </div>
                </div>

                <div>
                    <h4 style="margin: 0 0 10px 0; font-size: 13px; color: #cbd5e1;">Participating Reinsurer Panel</h4>
                    <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 10px;">
                        ${data.reinsurers.map(r => `
                            <div style="background: rgba(30, 41, 59, 0.6); border-left: 4px solid #38bdf8; padding: 10px; border-radius: 6px;">
                                <div style="font-size: 12px; font-weight: 600; color: #f1f5f9;">${r.name}</div>
                                <div style="font-size: 11px; color: #94a3b8;">Share: ${(r.sharePct * 100).toFixed(0)}% ($${r.amount.toLocaleString()})</div>
                            </div>
                        `).join('')}
                    </div>
                </div>
            </div>
        `;
    }
}
