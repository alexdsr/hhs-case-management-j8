package gov.state.hhs.backing;

import gov.state.hhs.model.Client;
import gov.state.hhs.model.Priority;
import gov.state.hhs.model.ServiceApplication;
import gov.state.hhs.model.ServiceType;
import gov.state.hhs.service.CaseService;
import gov.state.hhs.service.ClientService;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * JSF backing bean for the public-facing client intake / service application form.
 * Uses SessionScoped to support a two-step review flow (SC 3.3.4 — Error Prevention).
 * Step 1: user fills the form and clicks "Review".
 * Step 2: user reviews their data and confirms or goes back to edit.
 *
 * Date of birth is collected as three separate fields (month/day/year) following
 * federal government UX standards and WCAG accessibility best practices.
 */
@Named
@SessionScoped
public class IntakeBean implements Serializable {

    @Inject
    private ClientService clientService;

    @Inject
    private CaseService caseService;

    private Client client = new Client();
    private String selectedServiceType;
    private String confirmationNumber;

    // Date of birth collected as three separate fields
    private String dobMonth;
    private String dobDay;
    private String dobYear;

    /** True when the user has reviewed and is ready to confirm submission. */
    private boolean reviewMode = false;

    /**
     * Called when the user clicks "Review My Application".
     * Assembles and validates the date of birth before switching to review mode.
     */
    public String reviewApplication() {
        if (!assembleDateOfBirth()) {
            return null; // validation error added — stay on form
        }
        reviewMode = true;
        return null;
    }

    /**
     * Assembles the three DOB fields into a LocalDate on the client object.
     * Adds a FacesMessage and returns false if the date is invalid.
     */
    private boolean assembleDateOfBirth() {
        if (isBlank(dobMonth) || isBlank(dobDay) || isBlank(dobYear)) {
            addError("dobMonth", "Date of birth is required. Please enter month, day, and year.");
            return false;
        }
        try {
            int month = Integer.parseInt(dobMonth.trim());
            int day   = Integer.parseInt(dobDay.trim());
            int year  = Integer.parseInt(dobYear.trim());

            if (month < 1 || month > 12) {
                addError("dobMonth", "Month must be between 1 and 12.");
                return false;
            }
            if (day < 1 || day > 31) {
                addError("dobDay", "Day must be between 1 and 31.");
                return false;
            }
            if (year < 1900 || year > LocalDate.now().getYear()) {
                addError("dobYear", "Please enter a valid 4-digit birth year.");
                return false;
            }

            LocalDate dob = LocalDate.of(year, month, day);
            if (dob.isAfter(LocalDate.now())) {
                addError("dobMonth", "Date of birth cannot be in the future.");
                return false;
            }

            client.setDateOfBirth(dob);
            return true;

        } catch (NumberFormatException e) {
            addError("dobMonth", "Please enter numbers only for month, day, and year.");
            return false;
        } catch (Exception e) {
            addError("dobMonth", "The date entered is not valid. Please check month, day, and year.");
            return false;
        }
    }

    /**
     * Called when the user clicks "Go Back and Edit".
     * Restores the three DOB fields from the assembled date so fields repopulate.
     */
    public String editApplication() {
        if (client.getDateOfBirth() != null) {
            dobMonth = String.valueOf(client.getDateOfBirth().getMonthValue());
            dobDay   = String.valueOf(client.getDateOfBirth().getDayOfMonth());
            dobYear  = String.valueOf(client.getDateOfBirth().getYear());
        }
        reviewMode = false;
        return null;
    }

    /**
     * Called when the user confirms submission from the review screen.
     * Saves data and redirects to confirmation page.
     */
    public String submitApplication() {
        try {
            if (client.getEmail() != null && !client.getEmail().trim().isEmpty()) {
                client = clientService.findAll().stream()
                    .filter(c -> client.getEmail().equalsIgnoreCase(c.getEmail()))
                    .findFirst()
                    .orElseGet(() -> clientService.save(client));
            } else {
                clientService.save(client);
            }

            ServiceApplication application = new ServiceApplication();
            application.setClient(client);
            application.setServiceType(ServiceType.valueOf(selectedServiceType));
            application.setPriority(Priority.NORMAL);

            ServiceApplication saved = caseService.submit(application);
            confirmationNumber = String.format("HHS-%05d", saved.getId());

            // Reset for next use
            client = new Client();
            selectedServiceType = null;
            dobMonth = null;
            dobDay   = null;
            dobYear  = null;
            reviewMode = false;

            return "/public/confirmation?faces-redirect=true&cn=" + confirmationNumber;

        } catch (Exception e) {
            reviewMode = false;
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "We were unable to submit your application. Please try again or call 1-800-555-0100.",
                    null));
            return null;
        }
    }

    /** Formatted DOB for display on the review panel (MM/DD/YYYY). */
    public String getFormattedDateOfBirth() {
        if (client.getDateOfBirth() == null) return "";
        return client.getDateOfBirth().format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
    }

    public ServiceType[] getServiceTypes() {
        return ServiceType.values();
    }

    public String getSelectedServiceTypeLabel() {
        if (selectedServiceType == null || selectedServiceType.trim().isEmpty()) return "";
        try {
            return ServiceType.valueOf(selectedServiceType).getDisplayName();
        } catch (IllegalArgumentException e) {
            return selectedServiceType;
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private void addError(String clientId, String message) {
        FacesContext.getCurrentInstance().addMessage(clientId,
            new FacesMessage(FacesMessage.SEVERITY_ERROR, message, null));
    }

    // --- Getters and Setters ---

    public Client getClient()                             { return client; }
    public void setClient(Client client)                  { this.client = client; }

    public String getSelectedServiceType()                { return selectedServiceType; }
    public void setSelectedServiceType(String type)       { this.selectedServiceType = type; }

    public String getConfirmationNumber()                 { return confirmationNumber; }
    public boolean isReviewMode()                         { return reviewMode; }

    public String getDobMonth()                           { return dobMonth; }
    public void setDobMonth(String dobMonth)              { this.dobMonth = dobMonth; }

    public String getDobDay()                             { return dobDay; }
    public void setDobDay(String dobDay)                  { this.dobDay = dobDay; }

    public String getDobYear()                            { return dobYear; }
    public void setDobYear(String dobYear)                { this.dobYear = dobYear; }
}
