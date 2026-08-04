package com.guidewire.pc.pcf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.logging.Level;

public class PcfValidationEngine {
    private static final Logger LOGGER = Logger.getLogger(PcfValidationEngine.class.getName());


    public static class ValidationError {
        private final String widgetId;
        private final String widgetType;
        private final String parentType;
        private final String message;
        private final String severity; // ERROR or WARNING

        public ValidationError(String widgetId, String widgetType, String parentType, String message, String severity) {
        LOGGER.log(Level.FINE, "→ PcfValidationEngine.ValidationError");
            this.widgetId = widgetId;
            this.widgetType = widgetType;
            this.parentType = parentType;
            this.message = message;
            this.severity = severity;
        }

        public String getWidgetId() {
        LOGGER.log(Level.FINE, "→ PcfValidationEngine.getWidgetId"); return widgetId; }
        public String getWidgetType() {
        LOGGER.log(Level.FINE, "→ PcfValidationEngine.getWidgetType"); return widgetType; }
        public String getParentType() {
        LOGGER.log(Level.FINE, "→ PcfValidationEngine.getParentType"); return parentType; }
        public String getMessage() {
        LOGGER.log(Level.FINE, "→ PcfValidationEngine.getMessage"); return message; }
        public String getSeverity() {
        LOGGER.log(Level.FINE, "→ PcfValidationEngine.getSeverity"); return severity; }
    }

    // Allowed parent-child mapping rules according to Guidewire PCF schema
    private static final Map<String, List<String>> ALLOWED_PARENTS = new HashMap<>();

    static {
        // Page / Screen level
        ALLOWED_PARENTS.put("Page", List.of("PCF"));
        ALLOWED_PARENTS.put("Screen", List.of("Page", "WizardStep", "PCF"));
        ALLOWED_PARENTS.put("Toolbar", List.of("Screen", "PanelRef"));
        ALLOWED_PARENTS.put("ToolbarButton", List.of("Toolbar"));

        // DetailView Level
        ALLOWED_PARENTS.put("DetailViewPanel", List.of("Screen", "PanelRef"));
        ALLOWED_PARENTS.put("DetailViewTile", List.of("Screen", "PanelRef"));
        ALLOWED_PARENTS.put("InputColumn", List.of("DetailViewPanel", "DetailViewTile"));
        ALLOWED_PARENTS.put("TextInput", List.of("InputColumn"));
        ALLOWED_PARENTS.put("SelectInput", List.of("InputColumn"));
        ALLOWED_PARENTS.put("DateInput", List.of("InputColumn"));
        ALLOWED_PARENTS.put("CheckBoxInput", List.of("InputColumn"));
        ALLOWED_PARENTS.put("PasswordInput", List.of("InputColumn"));
        ALLOWED_PARENTS.put("ButtonInput", List.of("InputColumn"));

        // ListView Level
        ALLOWED_PARENTS.put("ListViewTile", List.of("Screen", "PanelRef"));
        ALLOWED_PARENTS.put("ListViewPanel", List.of("Screen", "PanelRef"));
        ALLOWED_PARENTS.put("RowIterator", List.of("ListViewTile", "ListViewPanel"));
        ALLOWED_PARENTS.put("Row", List.of("RowIterator"));
        ALLOWED_PARENTS.put("TextCell", List.of("Row"));
        ALLOWED_PARENTS.put("DateCell", List.of("Row"));
        ALLOWED_PARENTS.put("CheckBoxCell", List.of("Row"));
        ALLOWED_PARENTS.put("FormatCell", List.of("Row"));
    }

    public static boolean isValidParent(String childType, String parentType) {
        LOGGER.log(Level.FINE, "→ PcfValidationEngine.isValidParent");
        if (childType == null || parentType == null) return false;
        List<String> allowed = ALLOWED_PARENTS.get(childType);
        return allowed != null && allowed.contains(parentType);
    }

    public static List<ValidationError> validateWidgetPlacement(String widgetId, String childType, String parentType) {
        LOGGER.log(Level.FINE, "→ PcfValidationEngine.validateWidgetPlacement");
        List<ValidationError> errors = new ArrayList<>();

        if (!isValidParent(childType, parentType)) {
            List<String> allowed = ALLOWED_PARENTS.getOrDefault(childType, List.of("specific containers"));
            String msg = String.format("Guidewire PCF Schema Error: Widget '<%s>' cannot be placed inside '<%s>'. Valid parents: %s",
                    childType, parentType, allowed);
            errors.add(new ValidationError(widgetId, childType, parentType, msg, "ERROR"));
        }

        return errors;
    }
}
