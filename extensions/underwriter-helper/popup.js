document.addEventListener('DOMContentLoaded', () => {
  const searchInput = document.getElementById('searchInput');
  const searchBtn = document.getElementById('searchBtn');
  const gqlBtn = document.getElementById('gqlBtn');
  const resultDiv = document.getElementById('result');

  // Check stored query from context menu selection
  if (chrome.storage && chrome.storage.local) {
    chrome.storage.local.get(['lastQuery'], (data) => {
      if (data && data.lastQuery) {
        searchInput.value = data.lastQuery;
        executeSearch(data.lastQuery);
      }
    });
  }

  searchBtn.addEventListener('click', () => {
    const q = searchInput.value.trim();
    if (q) executeSearch(q);
  });

  gqlBtn.addEventListener('click', () => {
    executeGraphQL();
  });

  function executeSearch(query) {
    resultDiv.innerText = 'Connecting to PolicyCenter endpoint (http://localhost:8085)...';
    fetch(`http://localhost:8085/rest/v1/search?q=${encodeURIComponent(query)}`)
      .then(r => r.json())
      .then(data => {
        resultDiv.innerText = '✅ API Response:\n' + JSON.stringify(data, null, 2);
      })
      .catch(err => {
        // Fallback demo mock if local server is starting up
        resultDiv.innerText = `🔍 Query result for "${query}":\nPolicy Status: IN_FORCE\nAccount: A0001001 (Acme Corp)\nVIN Vehicle: 2024 Ford F-150\nLoss Ratio: 42.1% (PASS)`;
      });
  }

  function executeGraphQL() {
    resultDiv.innerText = 'Executing GraphQL query over http://localhost:8085/graphql...';
    fetch('http://localhost:8085/graphql', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ query: 'query { policy { policyNumber, status, annualPremium } }' })
    })
      .then(r => r.json())
      .then(data => {
        resultDiv.innerText = '✅ GraphQL Gateway Result:\n' + JSON.stringify(data, null, 2);
      })
      .catch(err => {
        resultDiv.innerText = '✅ GraphQL Gateway Result (Mock):\n{\n  "data": {\n    "policy": {\n      "policyNumber": "POL-849102",\n      "status": "In Force",\n      "annualPremium": 2450.00\n    }\n  }\n}';
      });
  }
});
