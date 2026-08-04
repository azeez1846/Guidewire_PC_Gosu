package com.guidewire.pc.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

public class RateTable implements Serializable {
    private static final Logger LOGGER = Logger.getLogger(RateTable.class.getName());

    private static final long serialVersionUID = 1L;

    private String tableName;
    private String tableCode;
    private String productCode;
    private final List<RateTableEntry> entries = new ArrayList<>();

    public RateTable() {
        LOGGER.log(Level.FINE, "→ RateTable.RateTable");}

    public RateTable(String tableName, String tableCode, String productCode) {
        LOGGER.log(Level.FINE, "→ RateTable.RateTable");
        this.tableName = tableName;
        this.tableCode = tableCode;
        this.productCode = productCode;
    }

    public void addEntry(RateTableEntry entry) {
        LOGGER.log(Level.FINE, "→ RateTable.addEntry");
        if (entry != null) {
            this.entries.add(entry);
        }
    }

    public String getTableName() {
        LOGGER.log(Level.FINE, "→ RateTable.getTableName"); return tableName; }
    public void setTableName(String tableName) {
        LOGGER.log(Level.FINE, "→ RateTable.setTableName"); this.tableName = tableName; }

    public String getTableCode() {
        LOGGER.log(Level.FINE, "→ RateTable.getTableCode"); return tableCode; }
    public void setTableCode(String tableCode) {
        LOGGER.log(Level.FINE, "→ RateTable.setTableCode"); this.tableCode = tableCode; }

    public String getProductCode() {
        LOGGER.log(Level.FINE, "→ RateTable.getProductCode"); return productCode; }
    public void setProductCode(String productCode) {
        LOGGER.log(Level.FINE, "→ RateTable.setProductCode"); this.productCode = productCode; }

    public List<RateTableEntry> getEntries() {
        LOGGER.log(Level.FINE, "→ RateTable.getEntries"); return entries; }
}
