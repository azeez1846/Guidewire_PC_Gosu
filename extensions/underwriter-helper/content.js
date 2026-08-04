// Content Script for Guidewire PolicyCenter Helper
console.log("[Guidewire PC Assistant] Content script injected.");

chrome.runtime.onMessage.addListener((request, sender, sendResponse) => {
  if (request.action === "extractSelectedText") {
    const selected = window.getSelection().toString().trim();
    sendResponse({ text: selected });
  }
});
