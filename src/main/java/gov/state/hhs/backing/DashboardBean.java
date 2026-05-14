package gov.state.hhs.backing;

import gov.state.hhs.auth.SessionBean;
import gov.state.hhs.model.ApplicationStatus;
import gov.state.hhs.model.ServiceApplication;
import gov.state.hhs.service.CaseService;
import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * JSF backing bean for the staff dashboard view.
 *
 * Caseworkers see counts and cases scoped to their own assignment only.
 * Admins see system-wide counts including an unassigned cases tile.
 */
@Named
@ViewScoped
public class DashboardBean implements Serializable {

    @Inject
    private CaseService caseService;

    @Inject
    private SessionBean sessionBean;

    private List<ServiceApplication> myCases;
    private Map<ApplicationStatus, Long> statusSummary;
    private long unassignedCount;
    private String filterStatus;

    @PostConstruct
    public void init() {
        loadData();
    }

    public void loadData() {
        if (sessionBean.isAdmin()) {
            // Admin: system-wide counts and full case list (filterable)
            statusSummary   = caseService.getStatusSummary();
            unassignedCount = caseService.countUnassignedCases();

            if ("UNASSIGNED".equals(filterStatus)) {
                myCases = caseService.findUnassignedCases();
            } else if (filterStatus == null || filterStatus.trim().isEmpty()) {
                myCases = caseService.findAll();
            } else {
                myCases = caseService.findByStatus(ApplicationStatus.valueOf(filterStatus));
            }
        } else {
            // Caseworker: counts and cases scoped to their own assignments only
            Long userId    = sessionBean.getCurrentUserId();
            statusSummary  = caseService.getStatusSummaryForUser(userId);
            unassignedCount = 0;
            myCases        = caseService.findAssignedTo(userId);
        }
    }

    public void applyFilter() {
        loadData();
    }

    public long getCountForStatus(ApplicationStatus status) {
        return statusSummary.getOrDefault(status, 0L);
    }

    public long getTotalCases() {
        return statusSummary.values().stream().mapToLong(Long::longValue).sum();
    }

    public ApplicationStatus[] getAllStatuses() {
        return ApplicationStatus.values();
    }

    public String formatCaseNumber(Long id) {
        if (id == null) return "HHS-?????";
        return String.format("HHS-%05d", id);
    }

    // --- Getters and Setters ---

    public List<ServiceApplication> getMyCases()             { return myCases; }
    public Map<ApplicationStatus, Long> getStatusSummary()   { return statusSummary; }
    public long getUnassignedCount()                         { return unassignedCount; }
    public String getFilterStatus()                          { return filterStatus; }
    public void setFilterStatus(String filterStatus)         { this.filterStatus = filterStatus; }
}
