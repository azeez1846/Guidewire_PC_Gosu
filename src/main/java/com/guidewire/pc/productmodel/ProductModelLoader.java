package com.guidewire.pc.productmodel;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ProductModelLoader {
    private static final ProductModelLoader INSTANCE = new ProductModelLoader();
    private final Map<String, Map<String, CoveragePattern>> productPatterns = new ConcurrentHashMap<>();

    private ProductModelLoader() {
        loadProductModels();
    }

    public static ProductModelLoader getInstance() {
        return INSTANCE;
    }

    public synchronized void loadProductModels() {
        productPatterns.clear();
        File dir = new File("config/resources/productmodel");
        if (!dir.exists() || !dir.isDirectory()) {
            dir = new File("../config/resources/productmodel");
        }

        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles((d, name) -> name.endsWith(".xml"));
            if (files != null) {
                for (File file : files) {
                    parseProductFile(file);
                }
            }
        }
    }

    private void parseProductFile(File file) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(file);
            doc.getDocumentElement().normalize();

            Element root = doc.getDocumentElement();
            String productId = root.getAttribute("id");

            Map<String, CoveragePattern> patternMap = productPatterns.computeIfAbsent(productId, k -> new LinkedHashMap<>());

            NodeList covNodes = root.getElementsByTagName("coverage-pattern");
            for (int i = 0; i < covNodes.getLength(); i++) {
                Element elem = (Element) covNodes.item(i);
                String code = elem.getAttribute("code");
                String name = elem.getAttribute("name");
                String category = elem.getAttribute("category");

                BigDecimal defaultVal = BigDecimal.ZERO;
                NodeList limitNodes = elem.getElementsByTagName("limit");
                NodeList dedNodes = elem.getElementsByTagName("deductible");
                Element valElem = null;

                if (limitNodes.getLength() > 0) valElem = (Element) limitNodes.item(0);
                else if (dedNodes.getLength() > 0) valElem = (Element) dedNodes.item(0);

                if (valElem != null && valElem.hasAttribute("default")) {
                    try {
                        defaultVal = new BigDecimal(valElem.getAttribute("default"));
                    } catch (Exception ignored) {}
                }

                CoveragePattern pattern = new CoveragePattern(code, name, category, defaultVal);

                if (valElem != null) {
                    NodeList optionNodes = valElem.getElementsByTagName("option");
                    for (int j = 0; j < optionNodes.getLength(); j++) {
                        Element opt = (Element) optionNodes.item(j);
                        pattern.addOption(opt.getAttribute("display"));
                    }
                }

                patternMap.put(code, pattern);
            }
        } catch (Exception e) {
            System.err.println("[ProductModelLoader] Failed to parse " + file.getName() + ": " + e.getMessage());
        }
    }

    public CoveragePattern getCoveragePattern(String productId, String patternCode) {
        Map<String, CoveragePattern> map = productPatterns.get(productId);
        if (map != null) return map.get(patternCode);
        return null;
    }

    public Collection<CoveragePattern> getCoveragePatterns(String productId) {
        Map<String, CoveragePattern> map = productPatterns.get(productId);
        if (map == null) return Collections.emptyList();
        return map.values();
    }
}
