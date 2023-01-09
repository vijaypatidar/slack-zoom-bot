package com.consultadd.slackzoom.services;

public enum AccountType {
    ZOOM("ZOOM", "Zoom Account"),
    GV("GV", "Google voice");
    private final String displayName;
    private final String type;

    AccountType(String type, String displayName) {
        this.displayName = displayName;
        this.type = type;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getType() {
        return type;
    }
}
