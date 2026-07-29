package com.guidewire.pc.typelist;

public interface TypeKey {
    String getTypelistName();
    String getCode();
    String getDisplayName();
    int getPriority();
    String getDescription();

    static TypeKey get(String typelistName, String code) {
        return TypelistLoader.getInstance().getTypeKey(typelistName, code);
    }
}
