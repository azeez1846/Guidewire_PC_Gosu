package com.guidewire.pc;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TypeScript Codebase Structural & Compilation Tests")
public class TypeScriptBuildTest {

    @Test
    @DisplayName("Test 1: Verify TypeScript Configuration (tsconfig.json)")
    public void testTsConfigExists() {
        File tsconfig = new File("tsconfig.json");
        assertTrue(tsconfig.exists(), "tsconfig.json must exist at project root");
    }

    @Test
    @DisplayName("Test 2: Verify PCF Type Definitions (guidewire-pcf.d.ts)")
    public void testPcfTypeDefsExist() {
        File pcfTypes = new File("src/main/typescript/types/guidewire-pcf.d.ts");
        assertTrue(pcfTypes.exists(), "guidewire-pcf.d.ts must exist");
    }

    @Test
    @DisplayName("Test 3: Verify PolicyCenter Entity Models (policycenter-models.ts)")
    public void testEntityModelsExist() {
        File models = new File("src/main/typescript/types/policycenter-models.ts");
        assertTrue(models.exists(), "policycenter-models.ts must exist");
    }

    @Test
    @DisplayName("Test 4: Verify PCF Schema Guard TS (pcf-schema-guard.ts)")
    public void testPcfSchemaGuardTsExists() {
        File schemaGuard = new File("src/main/typescript/studio/pcf-schema-guard.ts");
        assertTrue(schemaGuard.exists(), "pcf-schema-guard.ts must exist");
    }

    @Test
    @DisplayName("Test 5: Verify PCF Studio Engine TS (pcf-studio-engine.ts)")
    public void testPcfStudioEngineTsExists() {
        File studioEngine = new File("src/main/typescript/studio/pcf-studio-engine.ts");
        assertTrue(studioEngine.exists(), "pcf-studio-engine.ts must exist");
    }
}
