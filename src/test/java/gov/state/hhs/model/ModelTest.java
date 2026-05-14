package gov.state.hhs.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for entity helper methods and enum display names.
 */
@DisplayName("Model")
class ModelTest {

    // ---------------------------------------------------------------
    // User
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("User")
    class UserTests {

        @Test
        @DisplayName("getFullName concatenates first and last name")
        void fullName() {
            User u = new User();
            u.setFullName("Patricia Morales");
            assertEquals("Patricia Morales", u.getFullName());
        }

        @Test
        @DisplayName("isAdmin returns true for ADMIN role")
        void isAdminTrue() {
            User u = new User();
            u.setRole(UserRole.ADMIN);
            assertTrue(u.isAdmin());
        }

        @Test
        @DisplayName("isAdmin returns false for CASEWORKER role")
        void isAdminFalse() {
            User u = new User();
            u.setRole(UserRole.CASEWORKER);
            assertFalse(u.isAdmin());
        }

        @Test
        @DisplayName("active defaults to true on new User")
        void activeDefault() {
            User u = new User();
            assertTrue(u.isActive());
        }
    }

    // ---------------------------------------------------------------
    // Client
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("Client")
    class ClientTests {

        @Test
        @DisplayName("getFullName concatenates first and last name with a space")
        void fullName() {
            Client c = new Client();
            c.setFirstName("Robert");
            c.setLastName("Hutchins");
            assertEquals("Robert Hutchins", c.getFullName());
        }

        @Test
        @DisplayName("getFullName handles single-word names gracefully")
        void fullNameSingleWord() {
            Client c = new Client();
            c.setFirstName("Madonna");
            c.setLastName("");
            assertEquals("Madonna ", c.getFullName());
        }
    }

    // ---------------------------------------------------------------
    // ServiceApplication
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("ServiceApplication")
    class ServiceApplicationTests {

        @Test
        @DisplayName("status defaults to PENDING on new application")
        void defaultStatus() {
            ServiceApplication a = new ServiceApplication();
            assertEquals(ApplicationStatus.PENDING, a.getStatus());
        }

        @Test
        @DisplayName("priority defaults to NORMAL on new application")
        void defaultPriority() {
            ServiceApplication a = new ServiceApplication();
            assertEquals(Priority.NORMAL, a.getPriority());
        }

        @Test
        @DisplayName("caseNotes list is initialized and empty by default")
        void caseNotesInitialized() {
            ServiceApplication a = new ServiceApplication();
            assertNotNull(a.getCaseNotes());
            assertTrue(a.getCaseNotes().isEmpty());
        }
    }

    // ---------------------------------------------------------------
    // Enum display names
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("Enum display names")
    class EnumDisplayNames {

        @Test
        @DisplayName("UserRole display names are human-readable")
        void userRoleDisplayNames() {
            assertEquals("Administrator", UserRole.ADMIN.getDisplayName());
            assertEquals("Case Worker",   UserRole.CASEWORKER.getDisplayName());
        }

        @Test
        @DisplayName("ApplicationStatus display names are human-readable")
        void statusDisplayNames() {
            assertEquals("Pending Review", ApplicationStatus.PENDING.getDisplayName());
            assertEquals("In Review",      ApplicationStatus.IN_REVIEW.getDisplayName());
            assertEquals("Approved",       ApplicationStatus.APPROVED.getDisplayName());
            assertEquals("Denied",         ApplicationStatus.DENIED.getDisplayName());
            assertEquals("Closed",         ApplicationStatus.CLOSED.getDisplayName());
        }

        @Test
        @DisplayName("Priority display names are human-readable")
        void priorityDisplayNames() {
            assertEquals("Normal", Priority.NORMAL.getDisplayName());
            assertEquals("High",   Priority.HIGH.getDisplayName());
            assertEquals("Urgent", Priority.URGENT.getDisplayName());
        }

        @Test
        @DisplayName("ServiceType display names are descriptive")
        void serviceTypeDisplayNames() {
            assertEquals("Medicaid / Health Coverage", ServiceType.MEDICAID.getDisplayName());
            assertEquals("Food Assistance (SNAP)",     ServiceType.FOOD_ASSISTANCE.getDisplayName());
            assertEquals("Housing Support",            ServiceType.HOUSING_SUPPORT.getDisplayName());
            assertEquals("Disability Services",        ServiceType.DISABILITY_SVCS.getDisplayName());
        }

        @Test
        @DisplayName("All ApplicationStatus values have non-blank display names")
        void allStatusesHaveDisplayNames() {
            for (ApplicationStatus s : ApplicationStatus.values()) {
                assertNotNull(s.getDisplayName(), s.name() + " has null display name");
                assertFalse(s.getDisplayName().trim().isEmpty(), s.name() + " has blank display name");
            }
        }

        @Test
        @DisplayName("All ServiceType values have non-blank display names")
        void allServiceTypesHaveDisplayNames() {
            for (ServiceType t : ServiceType.values()) {
                assertNotNull(t.getDisplayName(), t.name() + " has null display name");
                assertFalse(t.getDisplayName().trim().isEmpty(), t.name() + " has blank display name");
            }
        }
    }
}
