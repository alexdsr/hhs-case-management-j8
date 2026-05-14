package gov.state.hhs.model;

public enum ApplicationStatus {
    PENDING("Pending Review"),
    IN_REVIEW("In Review"),
    APPROVED("Approved"),
    DENIED("Denied"),
    CLOSED("Closed");

    private final String displayName;

    ApplicationStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() { return displayName; }
}
