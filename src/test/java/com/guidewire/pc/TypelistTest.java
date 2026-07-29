package com.guidewire.pc;

import com.guidewire.pc.typelist.TypeKey;
import com.guidewire.pc.typelist.TypelistLoader;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TypelistTest {

    @Test
    public void testTypelistLoadingFromTTL() {
        TypelistLoader loader = TypelistLoader.getInstance();
        loader.loadTypelists();

        List<TypeKey> policyStatuses = loader.getTypeKeys("PolicyStatus");
        assertFalse(policyStatuses.isEmpty(), "PolicyStatus typelist should not be empty");

        TypeKey draft = loader.getTypeKey("PolicyStatus", "Draft");
        assertNotNull(draft);
        assertEquals("Draft", draft.getCode());
        assertEquals("Draft", draft.getDisplayName());
        assertEquals(1, draft.getPriority());

        TypeKey issued = loader.getTypeKey("PolicyStatus", "Issued");
        assertNotNull(issued);
        assertEquals("Issued", issued.getCode());
        assertEquals("Policy period is in force / issued", issued.getDescription());

        TypeKey personalAuto = loader.getTypeKey("Product", "PersonalAuto");
        assertNotNull(personalAuto);
        assertEquals("Personal Auto", personalAuto.getDisplayName());
    }

    @Test
    public void testTypeKeyEquality() {
        TypeKey status1 = TypeKey.get("PolicyStatus", "Quoted");
        TypeKey status2 = TypeKey.get("PolicyStatus", "quoted");

        assertEquals(status1, status2);
        assertEquals("PolicyStatus.Quoted", status1.toString());
    }
}
