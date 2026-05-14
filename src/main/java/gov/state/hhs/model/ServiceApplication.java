package gov.state.hhs.model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a citizen's application for a specific HHS service.
 * Tracks the full lifecycle from submission through case resolution.
 */
@Entity
@Table(name = "service_application")
@NamedQueries({
    @NamedQuery(
        name  = "ServiceApplication.findAll",
        query = "SELECT a FROM ServiceApplication a ORDER BY a.submittedDate DESC"
    ),
    @NamedQuery(
        name  = "ServiceApplication.findByStatus",
        query = "SELECT a FROM ServiceApplication a WHERE a.status = :status ORDER BY a.priority DESC, a.submittedDate ASC"
    ),
    @NamedQuery(
        name  = "ServiceApplication.findByAssignedTo",
        query = "SELECT a FROM ServiceApplication a WHERE a.assignedTo.id = :userId ORDER BY a.priority DESC, a.submittedDate ASC"
    ),
    @NamedQuery(
        name  = "ServiceApplication.findByClient",
        query = "SELECT a FROM ServiceApplication a WHERE a.client.id = :clientId ORDER BY a.submittedDate DESC"
    ),
    @NamedQuery(
        name  = "ServiceApplication.countByStatus",
        query = "SELECT a.status, COUNT(a) FROM ServiceApplication a GROUP BY a.status"
    ),
    @NamedQuery(
        name  = "ServiceApplication.countByStatusForUser",
        query = "SELECT a.status, COUNT(a) FROM ServiceApplication a WHERE a.assignedTo.id = :userId GROUP BY a.status"
    ),
    @NamedQuery(
        name  = "ServiceApplication.countUnassigned",
        query = "SELECT COUNT(a) FROM ServiceApplication a WHERE a.assignedTo IS NULL"
    ),
    @NamedQuery(
        name  = "ServiceApplication.findUnassigned",
        query = "SELECT a FROM ServiceApplication a WHERE a.assignedTo IS NULL ORDER BY a.priority DESC, a.submittedDate ASC"
    )
})
public class ServiceApplication implements Serializable {

    @Id
    @SequenceGenerator(name = "app_seq", sequenceName = "app_seq", initialValue = 100, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "app_seq")
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assigned_to_id")
    private User assignedTo;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "service_type", nullable = false, length = 30)
    private ServiceType serviceType;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ApplicationStatus status = ApplicationStatus.PENDING;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Priority priority = Priority.NORMAL;

    @Column(name = "submitted_date", nullable = false, updatable = false)
    private LocalDateTime submittedDate;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @OrderBy("createdDate DESC")
    private List<CaseNote> caseNotes = new ArrayList<>();

    @PrePersist
    protected void onPersist() {
        this.submittedDate = LocalDateTime.now();
        this.lastUpdated   = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.lastUpdated = LocalDateTime.now();
    }

    // --- Getters and Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }

    public User getAssignedTo() { return assignedTo; }
    public void setAssignedTo(User assignedTo) { this.assignedTo = assignedTo; }

    public ServiceType getServiceType() { return serviceType; }
    public void setServiceType(ServiceType serviceType) { this.serviceType = serviceType; }

    public ApplicationStatus getStatus() { return status; }
    public void setStatus(ApplicationStatus status) { this.status = status; }

    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }

    public LocalDateTime getSubmittedDate() { return submittedDate; }

    public LocalDateTime getLastUpdated() { return lastUpdated; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public List<CaseNote> getCaseNotes() { return caseNotes; }
}
