package com.guidewire.pc;

import com.guidewire.pc.pcf.PcfValidationEngine;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Web PCF Studio Drag & Drop Container Validation Engine Tests")
public class PcfStudioTest {

    @Test
    @DisplayName("Test 1: Valid Container Placement - TextInput inside InputColumn")
    public void testValidTextInputPlacement() {
        assertTrue(PcfValidationEngine.isValidParent("TextInput", "InputColumn"));
        List<PcfValidationEngine.ValidationError> errors = PcfValidationEngine.validateWidgetPlacement("TextInput_1", "TextInput", "InputColumn");
        assertTrue(errors.isEmpty(), "TextInput inside InputColumn should be valid");
    }

    @Test
    @DisplayName("Test 2: Valid Container Placement - TextCell inside Row")
    public void testValidTextCellPlacement() {
        assertTrue(PcfValidationEngine.isValidParent("TextCell", "Row"));
        List<PcfValidationEngine.ValidationError> errors = PcfValidationEngine.validateWidgetPlacement("TextCell_1", "TextCell", "Row");
        assertTrue(errors.isEmpty(), "TextCell inside Row should be valid");
    }

    @Test
    @DisplayName("Test 3: Invalid Drop Rejection - TextCell directly inside DetailViewPanel")
    public void testInvalidTextCellInDetailViewRejection() {
        assertFalse(PcfValidationEngine.isValidParent("TextCell", "DetailViewPanel"));
        List<PcfValidationEngine.ValidationError> errors = PcfValidationEngine.validateWidgetPlacement("TextCell_Bad", "TextCell", "DetailViewPanel");
        assertFalse(errors.isEmpty());
        assertEquals("ERROR", errors.get(0).getSeverity());
        assertTrue(errors.get(0).getMessage().contains("cannot be placed inside '<DetailViewPanel>'"));
    }

    @Test
    @DisplayName("Test 4: Invalid Drop Rejection - ToolbarButton directly inside InputColumn")
    public void testInvalidToolbarButtonInInputColumnRejection() {
        assertFalse(PcfValidationEngine.isValidParent("ToolbarButton", "InputColumn"));
        List<PcfValidationEngine.ValidationError> errors = PcfValidationEngine.validateWidgetPlacement("Btn_Bad", "ToolbarButton", "InputColumn");
        assertFalse(errors.isEmpty());
        assertTrue(errors.get(0).getMessage().contains("cannot be placed inside '<InputColumn>'"));
    }

    @Test
    @DisplayName("Test 5: Valid Toolbar Button placement inside Toolbar")
    public void testValidToolbarButtonPlacement() {
        assertTrue(PcfValidationEngine.isValidParent("ToolbarButton", "Toolbar"));
        List<PcfValidationEngine.ValidationError> errors = PcfValidationEngine.validateWidgetPlacement("Btn_Good", "ToolbarButton", "Toolbar");
        assertTrue(errors.isEmpty());
    }
}
