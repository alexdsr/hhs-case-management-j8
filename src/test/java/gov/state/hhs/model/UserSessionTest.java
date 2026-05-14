package gov.state.hhs.model;

import gov.state.hhs.auth.UserSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the UserSession value object.
 * Verifies that values are correctly copied from the JPA User entity
 * and that role convenience methods work as expected.
 */
@DisplayName("UserSession")
class UserSessionTest {

    private User adminUser;
    private User caseworkerUser;

    @BeforeEach
    void setUp() {
        adminUser = new User();
        adminUser.setFullName("Patricia Morales");
        adminUser.setEmail("admin@hhs.gov");
        adminUser.setRole(UserRole.ADMIN);

        caseworkerUser = new User();
        caseworkerUser.setFullName("James Okonkwo");
        caseworkerUser.setEmail("caseworker@hhs.gov");
        caseworkerUser.setRole(UserRole.CASEWORKER);
    }

    @Test
    @DisplayName("Copies full name from User entity")
    void copiesFullName() {
        UserSession session = new UserSession(adminUser);
        assertEquals("Patricia Morales", session.getFullName());
    }

    @Test
    @DisplayName("Copies email from User entity")
    void copiesEmail() {
        UserSession session = new UserSession(adminUser);
        assertEquals("admin@hhs.gov", session.getEmail());
    }

    @Test
    @DisplayName("Copies role from User entity")
    void copiesRole() {
        UserSession session = new UserSession(adminUser);
        assertEquals(UserRole.ADMIN, session.getRole());
    }

    @Test
    @DisplayName("isAdmin returns true for ADMIN role")
    void isAdminTrue() {
        UserSession session = new UserSession(adminUser);
        assertTrue(session.isAdmin());
    }

    @Test
    @DisplayName("isAdmin returns false for CASEWORKER role")
    void isAdminFalseForCaseworker() {
        UserSession session = new UserSession(caseworkerUser);
        assertFalse(session.isAdmin());
    }

    @Test
    @DisplayName("getRoleDisplayName returns human-readable role name")
    void roleDisplayNameAdmin() {
        UserSession session = new UserSession(adminUser);
        assertEquals("Administrator", session.getRoleDisplayName());
    }

    @Test
    @DisplayName("getRoleDisplayName returns Case Worker for caseworker")
    void roleDisplayNameCaseworker() {
        UserSession session = new UserSession(caseworkerUser);
        assertEquals("Case Worker", session.getRoleDisplayName());
    }
}
