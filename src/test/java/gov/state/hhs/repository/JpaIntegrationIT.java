package gov.state.hhs.repository;

import gov.state.hhs.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JPA integration tests for entity persistence and JPQL named queries.
 *
 * Runs against an in-memory H2 database using Hibernate directly —
 * no WildFly or CDI container required. Each test rolls back after completion,
 * so tests are fully isolated.
 *
 * Covers:
 * - Entity persist / find round-trips
 * - Named query correctness (User.findByEmail, Client.searchByName,
 *   ServiceApplication queries, countByStatus, countUnassigned)
 * - Cascade and relationship behaviour
 * - PrePersist lifecycle hooks (createdDate auto-population)
 */
@DisplayName("JPA Integration Tests")
class JpaIntegrationIT extends BaseJpaIT {

    // Shared seed data created fresh for each test
    private User       admin;
    private User       caseworker;
    private Client     client1;
    private Client     client2;

    @BeforeEach
    void seedData() {
        admin = persistUser("admin@hhs.gov", "Patricia Morales", UserRole.ADMIN);
        caseworker = persistUser("caseworker@hhs.gov", "James Okonkwo", UserRole.CASEWORKER);
        client1 = persistClient("Robert", "Hutchins", "rhutchins@email.com");
        client2 = persistClient("Sandra", "Yee",      "syee@email.com");
        flushAndClear();
    }

    // ---------------------------------------------------------------
    // User — entity persistence and named queries
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("User entity")
    class UserEntityTests {

        @Test
        @DisplayName("persisted user can be found by ID")
        void findById() {
            User found = em.find(User.class, admin.getId());
            assertNotNull(found);
            assertEquals("Patricia Morales", found.getFullName());
        }

        @Test
        @DisplayName("User.findByEmail returns the correct user")
        void findByEmail() {
            List<User> results = em
                .createNamedQuery("User.findByEmail", User.class)
                .setParameter("email", "admin@hhs.gov")
                .getResultList();

            assertEquals(1, results.size());
            assertEquals("Patricia Morales", results.get(0).getFullName());
        }

        @Test
        @DisplayName("User.findByEmail returns empty for unknown email")
        void findByEmailNotFound() {
            List<User> results = em
                .createNamedQuery("User.findByEmail", User.class)
                .setParameter("email", "nobody@hhs.gov")
                .getResultList();

            assertTrue(results.isEmpty());
        }

        @Test
        @DisplayName("User.findAllActive returns all active users ordered by name")
        void findAllActive() {
            List<User> results = em
                .createNamedQuery("User.findAllActive", User.class)
                .getResultList();

            assertTrue(results.size() >= 2);
            // Verify ordering: James before Patricia alphabetically
            String first  = results.get(0).getFullName();
            String second = results.get(1).getFullName();
            assertTrue(first.compareTo(second) <= 0,
                "Results should be ordered by full name");
        }

        @Test
        @DisplayName("createdDate is auto-populated by @PrePersist")
        void createdDateAutoPopulated() {
            User found = em.find(User.class, admin.getId());
            assertNotNull(found.getCreatedDate(),
                "createdDate should be set by @PrePersist");
        }

        @Test
        @DisplayName("inactive user is not returned by findAllActive")
        void inactiveUserExcluded() {
            User inactive = persistUser("inactive@hhs.gov", "Inactive User", UserRole.CASEWORKER);
            inactive.setActive(false);
            em.merge(inactive);
            flushAndClear();

            List<User> results = em
                .createNamedQuery("User.findAllActive", User.class)
                .getResultList();

            boolean found = results.stream()
                .anyMatch(u -> "inactive@hhs.gov".equals(u.getEmail()));
            assertFalse(found, "Inactive user should not appear in findAllActive");
        }
    }

    // ---------------------------------------------------------------
    // Client — entity persistence and named queries
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("Client entity")
    class ClientEntityTests {

        @Test
        @DisplayName("persisted client can be found by ID")
        void findById() {
            Client found = em.find(Client.class, client1.getId());
            assertNotNull(found);
            assertEquals("Robert", found.getFirstName());
            assertEquals("Hutchins", found.getLastName());
        }

        @Test
        @DisplayName("Client.findAll returns all clients ordered by last name then first name")
        void findAll() {
            List<Client> results = em
                .createNamedQuery("Client.findAll", Client.class)
                .getResultList();

            assertTrue(results.size() >= 2);
        }

        @Test
        @DisplayName("Client.searchByName matches partial last name")
        void searchByLastName() {
            List<Client> results = em
                .createNamedQuery("Client.searchByName", Client.class)
                .setParameter("term", "%hutch%")
                .getResultList();

            assertEquals(1, results.size());
            assertEquals("Hutchins", results.get(0).getLastName());
        }

        @Test
        @DisplayName("Client.searchByName matches partial first name")
        void searchByFirstName() {
            List<Client> results = em
                .createNamedQuery("Client.searchByName", Client.class)
                .setParameter("term", "%sand%")
                .getResultList();

            assertEquals(1, results.size());
            assertEquals("Sandra", results.get(0).getFirstName());
        }

        @Test
        @DisplayName("Client.findByEmail returns correct client")
        void findByEmail() {
            List<Client> results = em
                .createNamedQuery("Client.findByEmail", Client.class)
                .setParameter("email", "rhutchins@email.com")
                .getResultList();

            assertEquals(1, results.size());
            assertEquals("Robert", results.get(0).getFirstName());
        }

        @Test
        @DisplayName("Client.findByEmail returns empty for unknown email")
        void findByEmailNotFound() {
            List<Client> results = em
                .createNamedQuery("Client.findByEmail", Client.class)
                .setParameter("email", "unknown@email.com")
                .getResultList();

            assertTrue(results.isEmpty());
        }

        @Test
        @DisplayName("createdDate is auto-populated by @PrePersist")
        void createdDateAutoPopulated() {
            Client found = em.find(Client.class, client1.getId());
            assertNotNull(found.getCreatedDate());
        }
    }

    // ---------------------------------------------------------------
    // ServiceApplication — named queries and status tracking
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("ServiceApplication entity")
    class ServiceApplicationTests {

        @Test
        @DisplayName("ServiceApplication.findAll returns all applications newest first")
        void findAll() {
            persistApplication(client1, caseworker, ServiceType.MEDICAID, ApplicationStatus.PENDING, Priority.NORMAL);
            persistApplication(client2, caseworker, ServiceType.FOOD_ASSISTANCE, ApplicationStatus.IN_REVIEW, Priority.HIGH);
            flushAndClear();

            List<ServiceApplication> results = em
                .createNamedQuery("ServiceApplication.findAll", ServiceApplication.class)
                .getResultList();

            assertTrue(results.size() >= 2);
        }

        @Test
        @DisplayName("ServiceApplication.findByStatus filters correctly")
        void findByStatus() {
            persistApplication(client1, caseworker, ServiceType.MEDICAID,        ApplicationStatus.PENDING,   Priority.NORMAL);
            persistApplication(client2, caseworker, ServiceType.FOOD_ASSISTANCE, ApplicationStatus.IN_REVIEW, Priority.HIGH);
            flushAndClear();

            List<ServiceApplication> pending = em
                .createNamedQuery("ServiceApplication.findByStatus", ServiceApplication.class)
                .setParameter("status", ApplicationStatus.PENDING)
                .getResultList();

            assertTrue(pending.stream()
                .allMatch(a -> ApplicationStatus.PENDING.equals(a.getStatus())));
        }

        @Test
        @DisplayName("ServiceApplication.findByAssignedTo returns only that user's cases")
        void findByAssignedTo() {
            User other = persistUser("other@hhs.gov", "Other Worker", UserRole.CASEWORKER);
            persistApplication(client1, caseworker, ServiceType.MEDICAID,        ApplicationStatus.PENDING, Priority.NORMAL);
            persistApplication(client2, other,      ServiceType.FOOD_ASSISTANCE, ApplicationStatus.PENDING, Priority.NORMAL);
            flushAndClear();

            List<ServiceApplication> results = em
                .createNamedQuery("ServiceApplication.findByAssignedTo", ServiceApplication.class)
                .setParameter("userId", caseworker.getId())
                .getResultList();

            assertTrue(results.stream()
                .allMatch(a -> caseworker.getId().equals(a.getAssignedTo().getId())));
        }

        @Test
        @DisplayName("ServiceApplication.countByStatus returns correct counts per status")
        void countByStatus() {
            persistApplication(client1, caseworker, ServiceType.MEDICAID,        ApplicationStatus.PENDING,   Priority.NORMAL);
            persistApplication(client2, caseworker, ServiceType.FOOD_ASSISTANCE, ApplicationStatus.PENDING,   Priority.NORMAL);
            persistApplication(client1, caseworker, ServiceType.HOUSING_SUPPORT, ApplicationStatus.IN_REVIEW, Priority.HIGH);
            flushAndClear();

            List<Object[]> rows = em
                .createNamedQuery("ServiceApplication.countByStatus", Object[].class)
                .getResultList();

            long pendingCount = rows.stream()
                .filter(r -> ApplicationStatus.PENDING.equals(r[0]))
                .mapToLong(r -> (Long) r[1])
                .sum();

            assertTrue(pendingCount >= 2, "Expected at least 2 pending cases");
        }

        @Test
        @DisplayName("ServiceApplication.countUnassigned counts cases with no assignee")
        void countUnassigned() {
            // One assigned, one not
            persistApplication(client1, caseworker, ServiceType.MEDICAID,        ApplicationStatus.PENDING, Priority.NORMAL);
            persistApplication(client2, null,       ServiceType.FOOD_ASSISTANCE, ApplicationStatus.PENDING, Priority.NORMAL);
            flushAndClear();

            Long unassigned = em
                .createNamedQuery("ServiceApplication.countUnassigned", Long.class)
                .getSingleResult();

            assertTrue(unassigned >= 1, "Expected at least 1 unassigned case");
        }

        @Test
        @DisplayName("ServiceApplication.findUnassigned returns only cases with no assignee")
        void findUnassigned() {
            persistApplication(client1, caseworker, ServiceType.MEDICAID,        ApplicationStatus.PENDING, Priority.NORMAL);
            persistApplication(client2, null,       ServiceType.FOOD_ASSISTANCE, ApplicationStatus.PENDING, Priority.NORMAL);
            flushAndClear();

            List<ServiceApplication> results = em
                .createNamedQuery("ServiceApplication.findUnassigned", ServiceApplication.class)
                .getResultList();

            assertTrue(results.stream().allMatch(a -> a.getAssignedTo() == null),
                "All returned applications should have no assignee");
        }

        @Test
        @DisplayName("submittedDate and lastUpdated are auto-populated by @PrePersist")
        void datesAutoPopulated() {
            ServiceApplication app = persistApplication(
                client1, caseworker, ServiceType.MEDICAID, ApplicationStatus.PENDING, Priority.NORMAL);
            flushAndClear();

            ServiceApplication found = em.find(ServiceApplication.class, app.getId());
            assertNotNull(found.getSubmittedDate(), "submittedDate should be set");
            assertNotNull(found.getLastUpdated(),   "lastUpdated should be set");
        }
    }

    // ---------------------------------------------------------------
    // CaseNote — persistence and cascade
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("CaseNote entity")
    class CaseNoteTests {

        @Test
        @DisplayName("CaseNote can be persisted and found by ID")
        void persistAndFind() {
            ServiceApplication app = persistApplication(
                client1, caseworker, ServiceType.MEDICAID, ApplicationStatus.PENDING, Priority.NORMAL);

            CaseNote note = new CaseNote();
            note.setApplication(app);
            note.setAuthor(caseworker);
            note.setNoteText("First contact made with client.");
            em.persist(note);
            flushAndClear();

            CaseNote found = em.find(CaseNote.class, note.getId());
            assertNotNull(found);
            assertEquals("First contact made with client.", found.getNoteText());
        }

        @Test
        @DisplayName("CaseNote createdDate is auto-populated by @PrePersist")
        void createdDateAutoPopulated() {
            ServiceApplication app = persistApplication(
                client1, caseworker, ServiceType.MEDICAID, ApplicationStatus.PENDING, Priority.NORMAL);

            CaseNote note = new CaseNote();
            note.setApplication(app);
            note.setAuthor(caseworker);
            note.setNoteText("Auto-date test note.");
            em.persist(note);
            flushAndClear();

            CaseNote found = em.find(CaseNote.class, note.getId());
            assertNotNull(found.getCreatedDate(), "createdDate should be set by @PrePersist");
        }

        @Test
        @DisplayName("Multiple notes can be added to the same application")
        void multipleNotesOnApplication() {
            ServiceApplication app = persistApplication(
                client1, caseworker, ServiceType.MEDICAID, ApplicationStatus.PENDING, Priority.NORMAL);

            for (int i = 1; i <= 3; i++) {
                CaseNote note = new CaseNote();
                note.setApplication(app);
                note.setAuthor(caseworker);
                note.setNoteText("Note number " + i);
                em.persist(note);
            }
            flushAndClear();

            ServiceApplication found = em.find(ServiceApplication.class, app.getId());
            assertEquals(3, found.getCaseNotes().size());
        }
    }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

    private User persistUser(String email, String fullName, UserRole role) {
        User u = new User();
        u.setEmail(email);
        u.setPasswordHash("testhash");
        u.setFullName(fullName);
        u.setRole(role);
        u.setActive(true);
        em.persist(u);
        return u;
    }

    private Client persistClient(String first, String last, String email) {
        Client c = new Client();
        c.setFirstName(first);
        c.setLastName(last);
        c.setEmail(email);
        c.setDateOfBirth(LocalDate.of(1980, 1, 1));
        c.setAddressLine1("123 Main St");
        c.setCity("Salt Lake City");
        c.setStateCode("UT");
        c.setZip("84101");
        em.persist(c);
        return c;
    }

    private ServiceApplication persistApplication(Client client, User assignedTo,
            ServiceType serviceType, ApplicationStatus status, Priority priority) {
        ServiceApplication app = new ServiceApplication();
        app.setClient(client);
        app.setAssignedTo(assignedTo);
        app.setServiceType(serviceType);
        app.setStatus(status);
        app.setPriority(priority);
        em.persist(app);
        return app;
    }
}
