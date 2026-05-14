package gov.state.hhs.auth;

import gov.state.hhs.model.User;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

import java.io.Serializable;

/**
 * CDI session-scoped bean holding the currently authenticated staff user.
 * Stores a UserSession value object (not a JPA entity) to avoid
 * LazyInitializationException after the login transaction closes.
 */
@Named
@SessionScoped
public class SessionBean implements Serializable {

    private UserSession currentUser;
    private boolean loggedIn = false;

    public void login(User user) {
        // Copy values out of the JPA entity immediately while the session is open
        this.currentUser = new UserSession(user);
        this.loggedIn    = true;
    }

    public void logout() {
        this.currentUser = null;
        this.loggedIn    = false;
    }

    public boolean isLoggedIn() { return loggedIn; }
    public boolean isAdmin()    { return loggedIn && currentUser != null && currentUser.isAdmin(); }

    public UserSession getCurrentUser()     { return currentUser; }
    public String getCurrentUserName()      { return currentUser != null ? currentUser.getFullName() : ""; }
    public String getCurrentUserRole()      { return currentUser != null ? currentUser.getRoleDisplayName() : ""; }

    public Long getCurrentUserId() {
        return currentUser != null ? currentUser.getId() : null;
    }
}
