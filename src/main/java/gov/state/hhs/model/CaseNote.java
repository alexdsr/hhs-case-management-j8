package gov.state.hhs.model;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * A staff-authored note attached to a service application.
 * Provides an audit trail of case activity.
 */
@Entity
@Table(name = "case_note")
public class CaseNote implements Serializable {

    @Id
    @SequenceGenerator(name = "note_seq", sequenceName = "note_seq", initialValue = 100, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "note_seq")
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "application_id", nullable = false)
    private ServiceApplication application;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @NotBlank
    @Size(max = 2000)
    @Column(name = "note_text", nullable = false, columnDefinition = "TEXT")
    private String noteText;

    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @PrePersist
    protected void onPersist() {
        this.createdDate = LocalDateTime.now();
    }

    // --- Getters and Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ServiceApplication getApplication() { return application; }
    public void setApplication(ServiceApplication application) { this.application = application; }

    public User getAuthor() { return author; }
    public void setAuthor(User author) { this.author = author; }

    public String getNoteText() { return noteText; }
    public void setNoteText(String noteText) { this.noteText = noteText; }

    public LocalDateTime getCreatedDate() { return createdDate; }
}
