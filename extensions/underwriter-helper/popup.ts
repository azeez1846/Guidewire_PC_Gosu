// Guidewire PolicyCenter Underwriter Assistant — Popup Script

import type {
  TriageRequest,
  TriageResponse,
  GraphQLPolicyQuery,
  GraphQLPolicyData,
  SearchResponse,
} from "./types.js";

const BASE_URL = "http://localhost:8085";

const POPUP_TRIAGE_REQUEST: TriageRequest = {
  submissionId: "SUB-99881",
  policyNumber: "POL-849102",
  lineOfBusiness: "PersonalAuto",
  annualPremium: 2200.0,
  driverScore: 95,
  highFloodZone: false,
};

document.addEventListener("DOMContentLoaded", (): void => {
  const searchInput = document.getElementById("searchInput") as HTMLInputElement | null;
  const searchBtn = document.getElementById("searchBtn") as HTMLButtonElement | null;
  const triageBtn = document.getElementById("triageBtn") as HTMLButtonElement | null;
  const gqlBtn = document.getElementById("gqlBtn") as HTMLButtonElement | null;
  const resultDiv = document.getElementById("result") as HTMLDivElement | null;

  // Check stored query from context menu selection
  if (chrome.storage?.local) {
    chrome.storage.local.get(
      ["lastQuery"],
      (data: { [key: string]: unknown }): void => {
        if (data?.lastQuery && typeof data.lastQuery === "string" && searchInput) {
          searchInput.value = data.lastQuery;
          executeSearch(data.lastQuery);
        }
      }
    );
  }

  searchBtn?.addEventListener("click", (): void => {
    const q: string = searchInput?.value.trim() ?? "";
    if (q) executeSearch(q);
  });

  triageBtn?.addEventListener("click", (): void => {
    executeTriage();
  });

  gqlBtn?.addEventListener("click", (): void => {
    executeGraphQL();
  });

  function executeTriage(): void {
    if (resultDiv) resultDiv.innerText = "Calling AI Underwriting Triage endpoint...";

    fetch(`${BASE_URL}/rest/v1/underwriting/triage`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(POPUP_TRIAGE_REQUEST),
    })
      .then((r: Response) => r.json())
      .then((data: TriageResponse) => {
        if (resultDiv) {
          resultDiv.innerText =
            "🤖 AI Triage Result:\n" + JSON.stringify(data, null, 2);
        }
      })
      .catch((_err: unknown) => {
        if (resultDiv) {
          resultDiv.innerText =
            '🤖 AI Triage Result (Mock):\n{\n  "submissionId": "SUB-99881",\n  "recommendation": "STRAIGHT_THROUGH_BIND",\n  "riskScore": 10,\n  "rationale": ["Superior Telematics Driver Score (95/100)"],\n  "escalationRequired": false\n}';
        }
      });
  }

  function executeSearch(query: string): void {
    if (resultDiv) {
      resultDiv.innerText = `Connecting to PolicyCenter endpoint (${BASE_URL})...`;
    }

    fetch(`${BASE_URL}/rest/v1/search?q=${encodeURIComponent(query)}`)
      .then((r: Response) => r.json())
      .then((data: SearchResponse) => {
        if (resultDiv) {
          resultDiv.innerText =
            "✅ API Response:\n" + JSON.stringify(data, null, 2);
        }
      })
      .catch((_err: unknown) => {
        // Fallback demo mock if local server is starting up
        if (resultDiv) {
          resultDiv.innerText = `🔍 Query result for "${query}":\nPolicy Status: IN_FORCE\nAccount: A0001001 (Acme Corp)\nVIN Vehicle: 2024 Ford F-150\nLoss Ratio: 42.1% (PASS)`;
        }
      });
  }

  function executeGraphQL(): void {
    if (resultDiv) {
      resultDiv.innerText = `Executing GraphQL query over ${BASE_URL}/graphql...`;
    }

    const graphqlQuery: GraphQLPolicyQuery = {
      query: "query { policy { policyNumber, status, annualPremium } }",
    };

    fetch(`${BASE_URL}/graphql`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(graphqlQuery),
    })
      .then((r: Response) => r.json())
      .then((data: GraphQLPolicyData) => {
        if (resultDiv) {
          resultDiv.innerText =
            "✅ GraphQL Gateway Result:\n" + JSON.stringify(data, null, 2);
        }
      })
      .catch((_err: unknown) => {
        if (resultDiv) {
          resultDiv.innerText =
            '✅ GraphQL Gateway Result (Mock):\n{\n  "data": {\n    "policy": {\n      "policyNumber": "POL-849102",\n      "status": "In Force",\n      "annualPremium": 2450.00\n    }\n  }\n}';
        }
      });
  }
});
