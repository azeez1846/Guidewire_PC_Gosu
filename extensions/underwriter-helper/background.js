// Manifest V3 Background Service Worker for PolicyCenter Underwriter Assistant

chrome.runtime.onInstalled.addListener(() => {
  console.log("Guidewire PolicyCenter Extension Installed.");

  chrome.contextMenus.create({
    id: "lookupPolicyCenter",
    title: "🔍 Lookup in Guidewire PolicyCenter: '%s'",
    contexts: ["selection"]
  });
});

chrome.contextMenus.onClicked.addListener((info, tab) => {
  if (info.menuItemId === "lookupPolicyCenter" && info.selectionText) {
    const query = info.selectionText.trim();
    console.log("Searching PolicyCenter for text:", query);

    chrome.storage.local.set({ lastQuery: query }, () => {
      chrome.action.openPopup().catch(() => {
        // Fallback open sidepanel if supported
        if (chrome.sidePanel && tab && tab.id) {
          chrome.sidePanel.open({ tabId: tab.id });
        }
      });
    });
  }
});
