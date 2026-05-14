package gov.state.hhs.model;

public enum UserRole {
    ADMIN("Administrator"),
    CASEWORKER("Case Worker");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() { return displayName; }
}
