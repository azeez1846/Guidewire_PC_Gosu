package com.guidewire.pc.pcf;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PCFParser {
    private final Map<String, File> pcfFiles = new HashMap<>();
    private final Map<String, PCFDefinition> pcfCache = new ConcurrentHashMap<>();

    public PCFParser(File baseConfigDir) {
        scanDirectory(new File(baseConfigDir, "config/web/pcf"));
        warmupCache();
    }

    private void scanDirectory(File dir) {
        if (!dir.exists() || !dir.isDirectory()) return;
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File f : files) {
            if (f.isDirectory()) {
                scanDirectory(f);
            } else if (f.getName().endsWith(".pcf")) {
                String id = f.getName().replace(".pcf", "");
                pcfFiles.put(id, f);
            }
        }
    }

    private void warmupCache() {
        for (String pcfId : pcfFiles.keySet()) {
            parsePCFInternal(pcfId);
        }
    }

    public Map<String, File> getPcfFiles() {
        return pcfFiles;
    }

    public PCFDefinition parsePCF(String pcfId) {
        if (pcfId == null) return null;
        return pcfCache.computeIfAbsent(pcfId, this::parsePCFInternal);
    }

    private PCFDefinition parsePCFInternal(String pcfId) {
        File file = pcfFiles.get(pcfId);
        if (file == null) return null;

        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(file);
            doc.getDocumentElement().normalize();

            Element root = doc.getDocumentElement();
            String id = root.getAttribute("id");
            String title = root.getAttribute("title");

            return new PCFDefinition(id, title, file.getAbsolutePath(), root);
        } catch (javax.xml.parsers.ParserConfigurationException | org.xml.sax.SAXException | java.io.IOException e) {
            System.err.println("[PCF Parser Warning] Error parsing PCF " + pcfId + ": " + e.getMessage());
            return null;
        }
    }

    public void clearCache() {
        pcfCache.clear();
        warmupCache();
    }

    public static class PCFDefinition {
        private final String id;
        private final String title;
        private final String filePath;
        private final Element rootElement;

        public PCFDefinition(String id, String title, String filePath, Element rootElement) {
            this.id = id;
            this.title = title;
            this.filePath = filePath;
            this.rootElement = rootElement;
        }

        public String getId() { return id; }
        public String getTitle() { return title; }
        public String getFilePath() { return filePath; }
        public Element getRootElement() { return rootElement; }
    }
}
