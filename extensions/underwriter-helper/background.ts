// Manifest V3 Background Service Worker for PolicyCenter Underwriter Assistant

chrome.runtime.onInstalled.addListener((): void => {
  console.log("Guidewire PolicyCenter Extension Installed.");

  chrome.contextMenus.create({
    id: "lookupPolicyCenter",
    title: "🔍 Lookup in Guidewire PolicyCenter: '%s'",
    contexts: ["selection"]
  });
});

chrome.contextMenus.onClicked.addListener(
  (info: chrome.contextMenus.OnClickData, tab?: chrome.tabs.Tab): void => {
    if (info.menuItemId === "lookupPolicyCenter" && info.selectionText) {
      const query: string = info.selectionText.trim();
      console.log("Searching PolicyCenter for text:", query);

      chrome.storage.local.set({ lastQuery: query }, (): void => {
        chrome.action.openPopup().catch((): void => {
          // Fallback open sidepanel if supported
          if (chrome.sidePanel && tab?.id !== undefined) {
            chrome.sidePanel.open({ tabId: tab.id });
          }
        });
      });
    }
  }
);
