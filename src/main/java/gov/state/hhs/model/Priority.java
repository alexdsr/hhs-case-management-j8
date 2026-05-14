package gov.state.hhs.model;

public enum Priority {
    NORMAL("Normal"),
    HIGH("High"),
    URGENT("Urgent");

    private final String displayName;

    Priority(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() { return displayName; }
}
