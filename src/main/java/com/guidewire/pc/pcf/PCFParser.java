package com.guidewire.pc.pcf;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.logging.Level;

public class PCFParser {
    private static final Logger LOGGER = Logger.getLogger(PCFParser.class.getName());

    private final Map<String, File> pcfFiles = new HashMap<>();
    private final Map<String, PCFDefinition> pcfCache = new ConcurrentHashMap<>();

    public PCFParser(File baseConfigDir) {
        LOGGER.log(Level.FINE, "→ PCFParser.PCFParser");
        scanDirectory(new File(baseConfigDir, "config/web/pcf"));
        warmupCache();
    }

    private void scanDirectory(File dir) {
        LOGGER.log(Level.FINE, "→ PCFParser.scanDirectory");
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
        LOGGER.log(Level.FINE, "→ PCFParser.warmupCache");
        for (String pcfId : pcfFiles.keySet()) {
            parsePCFInternal(pcfId);
        }
    }

    public Map<String, File> getPcfFiles() {
        LOGGER.log(Level.FINE, "→ PCFParser.getPcfFiles");
        return pcfFiles;
    }

    public PCFDefinition parsePCF(String pcfId) {
        LOGGER.log(Level.FINE, "→ PCFParser.parsePCF");
        if (pcfId == null) return null;
        return pcfCache.computeIfAbsent(pcfId, this::parsePCFInternal);
    }

    private PCFDefinition parsePCFInternal(String pcfId) {
        LOGGER.log(Level.FINE, "→ PCFParser.parsePCFInternal");
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
        LOGGER.log(Level.FINE, "→ PCFParser.clearCache");
        pcfCache.clear();
        warmupCache();
    }

    public static class PCFDefinition {
        private final String id;
        private final String title;
        private final String filePath;
        private final Element rootElement;

        public PCFDefinition(String id, String title, String filePath, Element rootElement) {
        LOGGER.log(Level.FINE, "→ PCFParser.PCFDefinition");
            this.id = id;
            this.title = title;
            this.filePath = filePath;
            this.rootElement = rootElement;
        }

        public String getId() {
        LOGGER.log(Level.FINE, "→ PCFParser.getId"); return id; }
        public String getTitle() {
        LOGGER.log(Level.FINE, "→ PCFParser.getTitle"); return title; }
        public String getFilePath() {
        LOGGER.log(Level.FINE, "→ PCFParser.getFilePath"); return filePath; }
        public Element getRootElement() {
        LOGGER.log(Level.FINE, "→ PCFParser.getRootElement"); return rootElement; }
    }
}
