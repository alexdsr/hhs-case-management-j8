package gov.state.hhs.service;

import gov.state.hhs.model.*;
import gov.state.hhs.repository.ApplicationRepository;
import gov.state.hhs.repository.CaseNoteRepository;
import gov.state.hhs.repository.UserRepository;
import javax.ejb.Stateless;
import javax.inject.Inject;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * EJB service layer for service application and case note operations.
 */
@Stateless
public class CaseService {

    @Inject
    private ApplicationRepository applicationRepository;

    @Inject
    private UserRepository userRepository;

    @Inject
    private CaseNoteRepository caseNoteRepository;

    // --- Service Application operations ---

    public ServiceApplication submit(ServiceApplication application) {
        application.setStatus(ApplicationStatus.PENDING);
        return applicationRepository.save(application);
    }

    public ServiceApplication update(ServiceApplication application) {
        return applicationRepository.update(application);
    }

    public Optional<ServiceApplication> findById(Long id) {
        return applicationRepository.findById(id);
    }

    public List<ServiceApplication> findAll() {
        return applicationRepository.findAll();
    }

    public List<ServiceApplication> findByStatus(ApplicationStatus status) {
        return applicationRepository.findByStatus(status);
    }

    public List<ServiceApplication> findAssignedTo(Long userId) {
        return applicationRepository.findByAssignedUser(userId);
    }

    public List<ServiceApplication> findByClient(Long clientId) {
        return applicationRepository.findByClient(clientId);
    }

    /**
     * Assigns a case to a caseworker and moves status to IN_REVIEW.
     */
    public ServiceApplication assignCase(Long applicationId, Long caseworkerId) {
        ServiceApplication app = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new IllegalArgumentException("Application not found: " + applicationId));

        User caseworker = userRepository.findById(caseworkerId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + caseworkerId));

        app.setAssignedTo(caseworker);
        if (ApplicationStatus.PENDING.equals(app.getStatus())) {
            app.setStatus(ApplicationStatus.IN_REVIEW);
        }
        return applicationRepository.update(app);
    }

    public ServiceApplication updateStatus(Long applicationId, ApplicationStatus newStatus) {
        ServiceApplication app = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new IllegalArgumentException("Application not found: " + applicationId));
        app.setStatus(newStatus);
        return applicationRepository.update(app);
    }

    // --- Case Note operations ---

    public ServiceApplication addNote(Long applicationId, Long authorId, String noteText) {
        ServiceApplication app = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new IllegalArgumentException("Application not found: " + applicationId));

        User author = userRepository.findById(authorId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + authorId));

        // Persist the note directly rather than adding to the collection
        // and merging the whole graph — the latter causes H2 sequence conflicts
        // when manually seeded IDs are out of sync with the auto-increment counter.
        CaseNote note = new CaseNote();
        note.setApplication(app);
        note.setAuthor(author);
        note.setNoteText(noteText.trim());
        caseNoteRepository.saveNote(note);

        // Refresh the application so the caller sees the new note in caseNotes
        return applicationRepository.findById(applicationId)
            .orElseThrow(() -> new IllegalArgumentException("Application not found after note insert: " + applicationId));
    }

    // --- Dashboard summary ---

    /**
     * Returns counts of all applications grouped by status — for admin dashboard.
     */
    public Map<ApplicationStatus, Long> getStatusSummary() {
        return applicationRepository.countByStatus()
            .stream()
            .collect(Collectors.toMap(
                row -> (ApplicationStatus) row[0],
                row -> (Long) row[1]
            ));
    }

    /**
     * Returns counts of a specific caseworker's cases grouped by status.
     */
    public Map<ApplicationStatus, Long> getStatusSummaryForUser(Long userId) {
        return applicationRepository.countByStatusForUser(userId)
            .stream()
            .collect(Collectors.toMap(
                row -> (ApplicationStatus) row[0],
                row -> (Long) row[1]
            ));
    }

    /**
     * Returns the count of cases not yet assigned to any caseworker — for admin tile.
     */
    public long countUnassignedCases() {
        return applicationRepository.countUnassigned();
    }

    /**
     * Returns all cases not yet assigned to any caseworker — for admin filter.
     */
    public List<ServiceApplication> findUnassignedCases() {
        return applicationRepository.findUnassigned();
    }

    public List<User> findAllCaseworkers() {
        return userRepository.findAllActive();
    }
}
