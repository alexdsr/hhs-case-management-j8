package gov.state.hhs.model;

public enum ServiceType {
    MEDICAID("Medicaid / Health Coverage"),
    FOOD_ASSISTANCE("Food Assistance (SNAP)"),
    HOUSING_SUPPORT("Housing Support"),
    DISABILITY_SVCS("Disability Services"),
    CHILD_WELFARE("Child Welfare Services"),
    MENTAL_HEALTH("Mental Health Services"),
    SUBSTANCE_USE("Substance Use Treatment"),
    ELDER_CARE("Elder Care Services");

    private final String displayName;

    ServiceType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() { return displayName; }
}
