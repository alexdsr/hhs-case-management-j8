package gov.state.hhs.auth;

import gov.state.hhs.model.User;
import gov.state.hhs.model.UserRole;

import java.io.Serializable;

/**
 * Plain value object holding the logged-in user's data.
 * Stores only primitive values — no JPA-managed entity references —
 * to prevent LazyInitializationException when the JPA session closes
 * after the login transaction completes.
 */
public class UserSession implements Serializable {

    private final Long   id;
    private final String email;
    private final String fullName;
    private final UserRole role;

    public UserSession(User user) {
        this.id       = user.getId();
        this.email    = user.getEmail();
        this.fullName = user.getFullName();
        this.role     = user.getRole();
    }

    public Long     getId()       { return id; }
    public String   getEmail()    { return email; }
    public String   getFullName() { return fullName; }
    public UserRole getRole()     { return role; }

    public boolean isAdmin() {
        return UserRole.ADMIN.equals(this.role);
    }

    public String getRoleDisplayName() {
        return role != null ? role.getDisplayName() : "";
    }
}
