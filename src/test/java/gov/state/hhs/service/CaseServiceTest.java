package gov.state.hhs.service;

import gov.state.hhs.model.*;
import gov.state.hhs.repository.ApplicationRepository;
import gov.state.hhs.repository.CaseNoteRepository;
import gov.state.hhs.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CaseService.
 * Covers submission defaults, case assignment, status updates,
 * note creation, and dashboard summary aggregation.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CaseService")
class CaseServiceTest {

    @Mock private ApplicationRepository applicationRepository;
    @Mock private UserRepository         userRepository;
    @Mock private CaseNoteRepository     caseNoteRepository;

    @InjectMocks
    private CaseService caseService;

    private User caseworker;
    private Client client;
    private ServiceApplication application;

    @BeforeEach
    void setUp() {
        caseworker = new User();
        caseworker.setFullName("James Okonkwo");
        caseworker.setRole(UserRole.CASEWORKER);

        client = new Client();
        client.setFirstName("Robert");
        client.setLastName("Hutchins");

        application = new ServiceApplication();
        application.setClient(client);
        application.setServiceType(ServiceType.MEDICAID);
    }

    // ---------------------------------------------------------------
    // submit
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("submit")
    class SubmitTests {

        @Test
        @DisplayName("forces status to PENDING regardless of what was set")
        void forcesPendingStatus() {
            application.setStatus(ApplicationStatus.APPROVED); // should be overridden
            when(applicationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            ServiceApplication saved = caseService.submit(application);

            assertEquals(ApplicationStatus.PENDING, saved.getStatus());
        }

        @Test
        @DisplayName("delegates to repository save exactly once")
        void delegatesToRepository() {
            when(applicationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            caseService.submit(application);

            verify(applicationRepository, times(1)).save(application);
        }
    }

    // ---------------------------------------------------------------
    // assignCase
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("assignCase")
    class AssignCaseTests {

        @Test
        @DisplayName("sets assignedTo on the application")
        void setsAssignedTo() {
            when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));
            when(userRepository.findById(2L)).thenReturn(Optional.of(caseworker));
            when(applicationRepository.update(any())).thenAnswer(inv -> inv.getArgument(0));

            ServiceApplication result = caseService.assignCase(1L, 2L);

            assertEquals(caseworker, result.getAssignedTo());
        }

        @Test
        @DisplayName("transitions PENDING status to IN_REVIEW on assignment")
        void transitionsPendingToInReview() {
            application.setStatus(ApplicationStatus.PENDING);
            when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));
            when(userRepository.findById(2L)).thenReturn(Optional.of(caseworker));
            when(applicationRepository.update(any())).thenAnswer(inv -> inv.getArgument(0));

            ServiceApplication result = caseService.assignCase(1L, 2L);

            assertEquals(ApplicationStatus.IN_REVIEW, result.getStatus());
        }

        @Test
        @DisplayName("does not change status if already IN_REVIEW")
        void doesNotOverrideNonPendingStatus() {
            application.setStatus(ApplicationStatus.IN_REVIEW);
            when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));
            when(userRepository.findById(2L)).thenReturn(Optional.of(caseworker));
            when(applicationRepository.update(any())).thenAnswer(inv -> inv.getArgument(0));

            ServiceApplication result = caseService.assignCase(1L, 2L);

            assertEquals(ApplicationStatus.IN_REVIEW, result.getStatus());
        }

        @Test
        @DisplayName("throws IllegalArgumentException when application not found")
        void throwsWhenApplicationNotFound() {
            when(applicationRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class,
                () -> caseService.assignCase(99L, 1L));
        }

        @Test
        @DisplayName("throws IllegalArgumentException when caseworker not found")
        void throwsWhenCaseworkerNotFound() {
            when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class,
                () -> caseService.assignCase(1L, 99L));
        }
    }

    // ---------------------------------------------------------------
    // updateStatus
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("updateStatus")
    class UpdateStatusTests {

        @Test
        @DisplayName("sets the new status on the application")
        void setsNewStatus() {
            application.setStatus(ApplicationStatus.PENDING);
            when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));
            when(applicationRepository.update(any())).thenAnswer(inv -> inv.getArgument(0));

            ServiceApplication result = caseService.updateStatus(1L, ApplicationStatus.APPROVED);

            assertEquals(ApplicationStatus.APPROVED, result.getStatus());
        }

        @Test
        @DisplayName("throws when application not found")
        void throwsWhenNotFound() {
            when(applicationRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class,
                () -> caseService.updateStatus(99L, ApplicationStatus.APPROVED));
        }
    }

    // ---------------------------------------------------------------
    // addNote
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("addNote")
    class AddNoteTests {

        @Test
        @DisplayName("persists a new CaseNote via CaseNoteRepository")
        void persistsNote() {
            when(applicationRepository.findById(1L))
                .thenReturn(Optional.of(application));
            when(userRepository.findById(2L))
                .thenReturn(Optional.of(caseworker));
            when(caseNoteRepository.saveNote(any()))
                .thenAnswer(inv -> inv.getArgument(0));
            when(applicationRepository.findById(1L))
                .thenReturn(Optional.of(application));

            caseService.addNote(1L, 2L, "Initial contact made.");

            ArgumentCaptor<CaseNote> captor = ArgumentCaptor.forClass(CaseNote.class);
            verify(caseNoteRepository, times(1)).saveNote(captor.capture());
            assertEquals("Initial contact made.", captor.getValue().getNoteText());
        }

        @Test
        @DisplayName("trims whitespace from note text before saving")
        void trimsNoteText() {
            when(applicationRepository.findById(1L))
                .thenReturn(Optional.of(application));
            when(userRepository.findById(2L))
                .thenReturn(Optional.of(caseworker));
            when(caseNoteRepository.saveNote(any()))
                .thenAnswer(inv -> inv.getArgument(0));

            caseService.addNote(1L, 2L, "  Note with spaces.  ");

            ArgumentCaptor<CaseNote> captor = ArgumentCaptor.forClass(CaseNote.class);
            verify(caseNoteRepository).saveNote(captor.capture());
            assertEquals("Note with spaces.", captor.getValue().getNoteText());
        }

        @Test
        @DisplayName("sets the correct author on the note")
        void setsAuthor() {
            when(applicationRepository.findById(1L))
                .thenReturn(Optional.of(application));
            when(userRepository.findById(2L))
                .thenReturn(Optional.of(caseworker));
            when(caseNoteRepository.saveNote(any()))
                .thenAnswer(inv -> inv.getArgument(0));

            caseService.addNote(1L, 2L, "Note text.");

            ArgumentCaptor<CaseNote> captor = ArgumentCaptor.forClass(CaseNote.class);
            verify(caseNoteRepository).saveNote(captor.capture());
            assertEquals(caseworker, captor.getValue().getAuthor());
        }
    }

    // ---------------------------------------------------------------
    // getStatusSummary
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("getStatusSummary")
    class StatusSummaryTests {

        @Test
        @DisplayName("aggregates repository rows into a status-to-count map")
        void aggregatesRows() {
            List<Object[]> rows = Arrays.asList(
                new Object[]{ ApplicationStatus.PENDING,   3L },
                new Object[]{ ApplicationStatus.IN_REVIEW, 2L },
                new Object[]{ ApplicationStatus.APPROVED,  1L }
            );
            when(applicationRepository.countByStatus()).thenReturn(rows);

            Map<ApplicationStatus, Long> summary = caseService.getStatusSummary();

            assertEquals(3L, summary.get(ApplicationStatus.PENDING));
            assertEquals(2L, summary.get(ApplicationStatus.IN_REVIEW));
            assertEquals(1L, summary.get(ApplicationStatus.APPROVED));
            assertNull(summary.get(ApplicationStatus.DENIED));
        }

        @Test
        @DisplayName("returns empty map when no cases exist")
        void returnsEmptyMapWhenNoCases() {
            when(applicationRepository.countByStatus()).thenReturn(Arrays.asList());

            Map<ApplicationStatus, Long> summary = caseService.getStatusSummary();

            assertTrue(summary.isEmpty());
        }
    }
}
