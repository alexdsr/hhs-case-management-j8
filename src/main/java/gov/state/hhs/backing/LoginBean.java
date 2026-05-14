package gov.state.hhs.backing;

import gov.state.hhs.auth.SessionBean;
import gov.state.hhs.model.User;
import gov.state.hhs.service.AuthService;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import java.util.Optional;

/**
 * JSF backing bean for the staff login page.
 */
@Named
@RequestScoped
public class LoginBean {

    @Inject
    private AuthService authService;

    @Inject
    private SessionBean sessionBean;

    private String email;
    private String password;

    public String login() {
        Optional<User> result = authService.authenticate(email, password);

        if (result.isPresent()) {
            sessionBean.login(result.get());
            return "/staff/dashboard?faces-redirect=true";
        }

        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_ERROR,
                "Invalid email or password.",
                "Please check your credentials and try again."));

        password = null;
        return null;
    }

    public String logout() {
        sessionBean.logout();
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        return "/login?faces-redirect=true";
    }

    // --- Getters and Setters ---

    public String getEmail()    { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
