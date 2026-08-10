// Guidewire PolicyCenter Underwriter Assistant — Popup Script
const BASE_URL = "http://localhost:8085";
const POPUP_TRIAGE_REQUEST = {
    submissionId: "SUB-99881",
    policyNumber: "POL-849102",
    lineOfBusiness: "PersonalAuto",
    annualPremium: 2200.0,
    driverScore: 95,
    highFloodZone: false,
};
document.addEventListener("DOMContentLoaded", () => {
    const searchInput = document.getElementById("searchInput");
    const searchBtn = document.getElementById("searchBtn");
    const triageBtn = document.getElementById("triageBtn");
    const gqlBtn = document.getElementById("gqlBtn");
    const resultDiv = document.getElementById("result");
    // Check stored query from context menu selection
    if (chrome.storage?.local) {
        chrome.storage.local.get(["lastQuery"], (data) => {
            if (data?.lastQuery && typeof data.lastQuery === "string" && searchInput) {
                searchInput.value = data.lastQuery;
                executeSearch(data.lastQuery);
            }
        });
    }
    searchBtn?.addEventListener("click", () => {
        const q = searchInput?.value.trim() ?? "";
        if (q)
            executeSearch(q);
    });
    triageBtn?.addEventListener("click", () => {
        executeTriage();
    });
    gqlBtn?.addEventListener("click", () => {
        executeGraphQL();
    });
    function executeTriage() {
        if (resultDiv)
            resultDiv.innerText = "Calling AI Underwriting Triage endpoint...";
        fetch(`${BASE_URL}/rest/v1/underwriting/triage`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(POPUP_TRIAGE_REQUEST),
        })
            .then((r) => r.json())
            .then((data) => {
            if (resultDiv) {
                resultDiv.innerText =
                    "🤖 AI Triage Result:\n" + JSON.stringify(data, null, 2);
            }
        })
            .catch((_err) => {
            if (resultDiv) {
                resultDiv.innerText =
                    '🤖 AI Triage Result (Mock):\n{\n  "submissionId": "SUB-99881",\n  "recommendation": "STRAIGHT_THROUGH_BIND",\n  "riskScore": 10,\n  "rationale": ["Superior Telematics Driver Score (95/100)"],\n  "escalationRequired": false\n}';
            }
        });
    }
    function executeSearch(query) {
        if (resultDiv) {
            resultDiv.innerText = `Connecting to PolicyCenter endpoint (${BASE_URL})...`;
        }
        fetch(`${BASE_URL}/rest/v1/search?q=${encodeURIComponent(query)}`)
            .then((r) => r.json())
            .then((data) => {
            if (resultDiv) {
                resultDiv.innerText =
                    "✅ API Response:\n" + JSON.stringify(data, null, 2);
            }
        })
            .catch((_err) => {
            // Fallback demo mock if local server is starting up
            if (resultDiv) {
                resultDiv.innerText = `🔍 Query result for "${query}":\nPolicy Status: IN_FORCE\nAccount: A0001001 (Acme Corp)\nVIN Vehicle: 2024 Ford F-150\nLoss Ratio: 42.1% (PASS)`;
            }
        });
    }
    function executeGraphQL() {
        if (resultDiv) {
            resultDiv.innerText = `Executing GraphQL query over ${BASE_URL}/graphql...`;
        }
        const graphqlQuery = {
            query: "query { policy { policyNumber, status, annualPremium } }",
        };
        fetch(`${BASE_URL}/graphql`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(graphqlQuery),
        })
            .then((r) => r.json())
            .then((data) => {
            if (resultDiv) {
                resultDiv.innerText =
                    "✅ GraphQL Gateway Result:\n" + JSON.stringify(data, null, 2);
            }
        })
            .catch((_err) => {
            if (resultDiv) {
                resultDiv.innerText =
                    '✅ GraphQL Gateway Result (Mock):\n{\n  "data": {\n    "policy": {\n      "policyNumber": "POL-849102",\n      "status": "In Force",\n      "annualPremium": 2450.00\n    }\n  }\n}';
            }
        });
    }
});
export {};
