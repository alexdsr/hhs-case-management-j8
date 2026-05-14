package gov.state.hhs.model;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents an internal HHS staff user.
 * Roles: ADMIN can manage users and view all cases.
 *        CASEWORKER can manage assigned cases.
 */
@Entity
@Table(name = "app_user")
@NamedQueries({
    @NamedQuery(
        name  = "User.findByEmail",
        query = "SELECT u FROM User u WHERE u.email = :email AND u.active = true"
    ),
    @NamedQuery(
        name  = "User.findAllActive",
        query = "SELECT u FROM User u WHERE u.active = true ORDER BY u.fullName"
    )
})
public class User implements Serializable {

    @Id
    @SequenceGenerator(name = "user_seq", sequenceName = "user_seq", initialValue = 100, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_seq")
    private Long id;

    @NotBlank
    @Email
    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @NotBlank
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @NotBlank
    @Size(max = 100)
    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @OneToMany(mappedBy = "assignedTo", fetch = FetchType.EAGER)
    private List<ServiceApplication> assignedApplications = new ArrayList<>();

    @PrePersist
    protected void onPersist() {
        this.createdDate = LocalDateTime.now();
    }

    // --- Getters and Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getCreatedDate() { return createdDate; }

    public List<ServiceApplication> getAssignedApplications() { return assignedApplications; }

    public boolean isAdmin() { return UserRole.ADMIN.equals(this.role); }
}
