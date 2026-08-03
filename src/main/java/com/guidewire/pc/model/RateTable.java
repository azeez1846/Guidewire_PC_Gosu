package com.guidewire.pc.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class RateTable implements Serializable {
    private static final long serialVersionUID = 1L;

    private String tableName;
    private String tableCode;
    private String productCode;
    private final List<RateTableEntry> entries = new ArrayList<>();

    public RateTable() {}

    public RateTable(String tableName, String tableCode, String productCode) {
        this.tableName = tableName;
        this.tableCode = tableCode;
        this.productCode = productCode;
    }

    public void addEntry(RateTableEntry entry) {
        if (entry != null) {
            this.entries.add(entry);
        }
    }

    public String getTableName() { return tableName; }
    public void setTableName(String tableName) { this.tableName = tableName; }

    public String getTableCode() { return tableCode; }
    public void setTableCode(String tableCode) { this.tableCode = tableCode; }

    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }

    public List<RateTableEntry> getEntries() { return entries; }
}
