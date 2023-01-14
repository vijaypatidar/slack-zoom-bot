package com.consultadd.slackzoom.enums;

public enum AccountType {
    ZOOM("ZOOM", "Zoom Account", true),
    GV("GV", "Google voice", true),
    VPN("VPN", "VPN Account", false);
    private final String displayName;
    private final String type;

    private final boolean blocking;

    AccountType(String type, String displayName, boolean blocking) {
        this.displayName = displayName;
        this.type = type;
        this.blocking = blocking;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getType() {
        return type;
    }

    public boolean isBlocking() {
        return blocking;
    }
}
