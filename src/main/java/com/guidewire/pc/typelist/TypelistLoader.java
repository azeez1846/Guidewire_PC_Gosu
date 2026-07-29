package com.guidewire.pc.typelist;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TypelistLoader {
    private static final TypelistLoader INSTANCE = new TypelistLoader();
    private final Map<String, Map<String, TypeKey>> typelists = new ConcurrentHashMap<>();

    private TypelistLoader() {
        loadTypelists();
    }

    public static TypelistLoader getInstance() {
        return INSTANCE;
    }

    public synchronized void loadTypelists() {
        typelists.clear();
        File dir = new File("config/metadata/typelist");
        if (!dir.exists() || !dir.isDirectory()) {
            // Fallback for current working directory variation
            dir = new File("../config/metadata/typelist");
        }

        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles((d, name) -> name.endsWith(".ttl"));
            if (files != null) {
                for (File file : files) {
                    parseTypelistFile(file);
                }
            }
        }
    }

    private void parseTypelistFile(File file) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(file);
            doc.getDocumentElement().normalize();

            Element root = doc.getDocumentElement();
            String typelistName = root.getAttribute("name");

            Map<String, TypeKey> typecodeMap = typelists.computeIfAbsent(typelistName, k -> new LinkedHashMap<>());

            NodeList typecodeNodes = root.getElementsByTagName("typecode");
            for (int i = 0; i < typecodeNodes.getLength(); i++) {
                Element elem = (Element) typecodeNodes.item(i);
                String code = elem.getAttribute("code");
                String name = elem.getAttribute("name");
                String priorityStr = elem.getAttribute("priority");
                String desc = elem.getAttribute("desc");

                int priority = 99;
                if (priorityStr != null && !priorityStr.isEmpty()) {
                    try { priority = Integer.parseInt(priorityStr); } catch (NumberFormatException ignored) {}
                }

                Typecode typecode = new Typecode(typelistName, code, name, priority, desc);
                typecodeMap.put(code.toLowerCase(), typecode);
            }
        } catch (Exception e) {
            System.err.println("[TypelistLoader] Failed to parse " + file.getName() + ": " + e.getMessage());
        }
    }

    public TypeKey getTypeKey(String typelistName, String code) {
        if (typelistName == null || code == null) return null;
        Map<String, TypeKey> map = typelists.get(typelistName);
        if (map != null) {
            TypeKey tk = map.get(code.toLowerCase());
            if (tk != null) return tk;
        }
        // Fallback dynamic Typecode if code not in XML file
        return new Typecode(typelistName, code, code, 99, "");
    }

    public List<TypeKey> getTypeKeys(String typelistName) {
        Map<String, TypeKey> map = typelists.get(typelistName);
        if (map == null) return Collections.emptyList();
        return new ArrayList<>(map.values());
    }
}
