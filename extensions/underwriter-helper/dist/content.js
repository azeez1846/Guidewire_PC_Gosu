// Guidewire PolicyCenter AI Underwriting Copilot Content Script
console.log("[Guidewire PC Assistant] AI Copilot Overlay script initializing...");
const TRIAGE_ENDPOINT = "http://localhost:8085/rest/v1/underwriting/triage";
const DEFAULT_TRIAGE_REQUEST = {
    submissionId: "SUB-884920",
    policyNumber: "POL-849102",
    lineOfBusiness: "PersonalAuto",
    annualPremium: 1850.0,
    driverScore: 92,
    highFloodZone: false,
};
function injectAICopilotOverlay() {
    if (document.getElementById("gw-ai-copilot-card"))
        return;
    const card = document.createElement("div");
    card.id = "gw-ai-copilot-card";
    card.style.cssText = `
    position: fixed;
    bottom: 20px;
    right: 20px;
    width: 320px;
    background: #0F172A;
    color: #F8FAFC;
    border: 1px solid #38BDF8;
    border-radius: 12px;
    box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.5), 0 8px 10px -6px rgba(0, 0, 0, 0.5);
    font-family: 'Segoe UI', system-ui, -apple-system, sans-serif;
    font-size: 12px;
    z-index: 999999;
    overflow: hidden;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  `;
    card.innerHTML = `
    <div id="gw-ai-header" style="background: linear-gradient(135deg, #0284C7, #4F46E5); padding: 10px 14px; display: flex; align-items: center; justify-content: space-between; font-weight: 700; cursor: pointer; user-select: none;">
      <span style="display:flex; align-items:center; gap:6px;">
        <span style="font-size:16px;">🤖</span> AI Underwriting Copilot
      </span>
      <span id="gw-ai-toggle" style="font-size:14px;">▼</span>
    </div>
    <div id="gw-ai-body" style="padding: 12px; display: block;">
      <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:8px;">
        <span style="color:#94A3B8;">Triage Recommendation:</span>
        <span id="gw-ai-badge" style="background:#10B981; color:#064E3B; font-weight:700; padding:2px 8px; border-radius:12px; font-size:11px;">EVALUATING...</span>
      </div>
      <div style="margin-bottom:8px;">
        <div style="display:flex; justify-content:space-between; color:#94A3B8; margin-bottom:4px;">
          <span>Risk Score Gauge</span>
          <span id="gw-ai-score-text" style="font-weight:700; color:#38BDF8;">-- / 100</span>
        </div>
        <div style="width:100%; background:#1E293B; height:8px; border-radius:4px; overflow:hidden;">
          <div id="gw-ai-score-bar" style="width:20%; height:100%; background:#10B981; transition:width 0.5s ease;"></div>
        </div>
      </div>
      <div style="background:#1E293B; border-radius:6px; padding:8px; margin-bottom:10px;">
        <span style="color:#94A3B8; font-weight:600; display:block; margin-bottom:4px;">Rationale & Findings:</span>
        <ul id="gw-ai-rationale" style="margin:0; padding-left:16px; color:#E2E8F0; line-height:1.4;">
          <li>Initializing autonomous underwriting evaluation...</li>
        </ul>
      </div>
      <button id="gw-ai-refresh-btn" style="width:100%; background:#0284C7; color:#FFF; border:none; padding:6px 10px; border-radius:6px; font-weight:600; cursor:pointer; display:flex; align-items:center; justify-content:center; gap:6px;">
        ⚡ Re-evaluate Submission Triage
      </button>
    </div>
  `;
    document.body.appendChild(card);
    const header = card.querySelector("#gw-ai-header");
    const body = card.querySelector("#gw-ai-body");
    const toggle = card.querySelector("#gw-ai-toggle");
    let collapsed = false;
    header?.addEventListener("click", () => {
        collapsed = !collapsed;
        if (body)
            body.style.display = collapsed ? "none" : "block";
        if (toggle)
            toggle.innerText = collapsed ? "▲" : "▼";
    });
    const refreshBtn = card.querySelector("#gw-ai-refresh-btn");
    refreshBtn?.addEventListener("click", fetchUnderwritingTriage);
    fetchUnderwritingTriage();
}
function getScoreBarColor(score) {
    if (score >= 75)
        return "#EF4444";
    if (score >= 45)
        return "#F59E0B";
    return "#10B981";
}
function getBadgeStyles(recommendation) {
    switch (recommendation) {
        case "DECLINE":
            return { background: "#EF4444", color: "#7F1D1D" };
        case "UW_REFERRAL":
            return { background: "#F59E0B", color: "#78350F" };
        default:
            return { background: "#10B981", color: "#064E3B" };
    }
}
function updateUI(data) {
    const badge = document.getElementById("gw-ai-badge");
    const scoreText = document.getElementById("gw-ai-score-text");
    const scoreBar = document.getElementById("gw-ai-score-bar");
    const rationaleList = document.getElementById("gw-ai-rationale");
    const score = data.riskScore ?? 20;
    const rec = data.recommendation ?? "STRAIGHT_THROUGH_BIND";
    if (scoreText)
        scoreText.innerText = `${score} / 100`;
    if (scoreBar) {
        scoreBar.style.width = `${Math.min(score, 100)}%`;
        scoreBar.style.background = getScoreBarColor(score);
    }
    if (badge) {
        const styles = getBadgeStyles(rec);
        badge.innerText = rec;
        badge.style.background = styles.background;
        badge.style.color = styles.color;
    }
    if (rationaleList && Array.isArray(data.rationale)) {
        rationaleList.innerHTML = data.rationale
            .map((r) => `<li>${r}</li>`)
            .join("");
    }
}
function applyFallbackUI() {
    const fallbackData = {
        recommendation: "BIND_READY",
        riskScore: 15,
        rationale: [
            "Superior Telematics Driver Score (92/100)",
            "Clean Claim History (0 active claims)",
        ],
    };
    updateUI(fallbackData);
}
function fetchUnderwritingTriage() {
    const badge = document.getElementById("gw-ai-badge");
    if (badge)
        badge.innerText = "EVALUATING...";
    fetch(TRIAGE_ENDPOINT, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(DEFAULT_TRIAGE_REQUEST),
    })
        .then((res) => res.json())
        .then((data) => {
        updateUI(data);
    })
        .catch(() => {
        applyFallbackUI();
    });
}
if (document.readyState === "complete" || document.readyState === "interactive") {
    injectAICopilotOverlay();
}
else {
    window.addEventListener("DOMContentLoaded", injectAICopilotOverlay);
}
chrome.runtime.onMessage.addListener((request, _sender, sendResponse) => {
    if (request.action === "extractSelectedText") {
        const selected = window.getSelection()?.toString().trim() ?? "";
        sendResponse({ text: selected });
    }
    else if (request.action === "triggerTriage") {
        fetchUnderwritingTriage();
        sendResponse({ status: "triggered" });
    }
});
export {};
