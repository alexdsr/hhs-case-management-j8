package gov.state.hhs.backing;

import gov.state.hhs.auth.SessionBean;
import gov.state.hhs.model.*;
import gov.state.hhs.service.CaseService;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import java.io.Serializable;
import java.util.List;

/**
 * JSF backing bean for the case detail and management view.
 */
@Named
@ViewScoped
public class CaseDetailBean implements Serializable {

    @Inject
    private CaseService caseService;

    @Inject
    private SessionBean sessionBean;

    private Long applicationId;
    private ServiceApplication application;
    private List<User> caseworkers;
    private Long selectedCaseworkerId;
    private String newNoteText;
    private String selectedStatus;

    @PostConstruct
    public void init() {
        caseworkers = caseService.findAllCaseworkers();
    }

    public void loadApplication() {
        if (applicationId == null) return;

        application = caseService.findById(applicationId).orElse(null);
        if (application != null) {
            selectedStatus = application.getStatus().name();
            if (application.getAssignedTo() != null) {
                selectedCaseworkerId = application.getAssignedTo().getId();
            }
        }
    }

    public String assignCase() {
        if (selectedCaseworkerId == null) {
            addError("Please select a caseworker to assign.");
            return null;
        }
        try {
            caseService.assignCase(applicationId, selectedCaseworkerId);
            addInfo("Case assigned successfully.");
            loadApplication();
        } catch (Exception e) {
            addError("Could not assign case. Please try again.");
        }
        return null;
    }

    public String updateStatus() {
        if (selectedStatus == null || selectedStatus.trim().isEmpty()) {
            addError("Please select a status.");
            return null;
        }
        try {
            ApplicationStatus status = ApplicationStatus.valueOf(selectedStatus);
            caseService.updateStatus(applicationId, status);
            addInfo("Status updated to: " + status.getDisplayName());
            loadApplication();
        } catch (Exception e) {
            addError("Could not update status. Please try again.");
        }
        return null;
    }

    public String addNote() {
        if (newNoteText == null || newNoteText.trim().isEmpty()) {
            addError("Note cannot be empty.");
            return null;
        }
        try {
            caseService.addNote(applicationId, sessionBean.getCurrentUserId(), newNoteText);
            newNoteText = null;
            addInfo("Note added.");
            loadApplication();
        } catch (Exception e) {
            addError("Could not save note. Please try again.");
        }
        return null;
    }

    public ApplicationStatus[] getAllStatuses() {
        return ApplicationStatus.values();
    }

    public String getFormattedCaseNumber() {
        if (application == null || application.getId() == null) return "HHS-?????";
        return String.format("HHS-%05d", application.getId());
    }

    private void addInfo(String message) {
        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_INFO, message, null));
    }

    private void addError(String message) {
        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_ERROR, message, null));
    }

    // --- Getters and Setters ---

    public Long getApplicationId()                        { return applicationId; }
    public void setApplicationId(Long applicationId)      { this.applicationId = applicationId; }

    public ServiceApplication getApplication()            { return application; }

    public List<User> getCaseworkers()                    { return caseworkers; }

    public Long getSelectedCaseworkerId()                 { return selectedCaseworkerId; }
    public void setSelectedCaseworkerId(Long id)          { this.selectedCaseworkerId = id; }

    public String getNewNoteText()                        { return newNoteText; }
    public void setNewNoteText(String newNoteText)        { this.newNoteText = newNoteText; }

    public String getSelectedStatus()                     { return selectedStatus; }
    public void setSelectedStatus(String selectedStatus)  { this.selectedStatus = selectedStatus; }
}
