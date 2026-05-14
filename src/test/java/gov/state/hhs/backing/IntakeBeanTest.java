package gov.state.hhs.backing;

import gov.state.hhs.model.Client;
import gov.state.hhs.service.CaseService;
import gov.state.hhs.service.ClientService;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for IntakeBean.
 * Focuses on date-of-birth validation, assembly, and review mode state.
 * FacesContext is mocked statically to capture validation messages.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("IntakeBean")
class IntakeBeanTest {

    @Mock private ClientService clientService;
    @Mock private CaseService   caseService;

    @InjectMocks
    private IntakeBean intakeBean;

    private MockedStatic<FacesContext> mockedFacesContext;
    private FacesContext               facesContext;

    @BeforeEach
    void setUp() {
        facesContext       = mock(FacesContext.class);
        mockedFacesContext = Mockito.mockStatic(FacesContext.class);
        mockedFacesContext.when(FacesContext::getCurrentInstance).thenReturn(facesContext);
    }

    @AfterEach
    void tearDown() {
        mockedFacesContext.close();
    }

    // ---------------------------------------------------------------
    // reviewApplication — DOB validation
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("reviewApplication — DOB validation")
    class DobValidationTests {

        @Test
        @DisplayName("valid date sets client DOB and enters review mode")
        void validDateEntersReviewMode() {
            intakeBean.setDobMonth("3");
            intakeBean.setDobDay("14");
            intakeBean.setDobYear("1978");

            intakeBean.reviewApplication();

            assertTrue(intakeBean.isReviewMode());
            assertEquals(LocalDate.of(1978, 3, 14),
                intakeBean.getClient().getDateOfBirth());
        }

        @Test
        @DisplayName("blank month adds error message and stays in form mode")
        void blankMonthAddsError() {
            intakeBean.setDobMonth("");
            intakeBean.setDobDay("14");
            intakeBean.setDobYear("1978");

            intakeBean.reviewApplication();

            assertFalse(intakeBean.isReviewMode());
            verify(facesContext).addMessage(any(), any(FacesMessage.class));
        }

        @Test
        @DisplayName("month out of range (0) adds error")
        void monthZeroAddsError() {
            intakeBean.setDobMonth("0");
            intakeBean.setDobDay("14");
            intakeBean.setDobYear("1978");

            intakeBean.reviewApplication();

            assertFalse(intakeBean.isReviewMode());
            verify(facesContext).addMessage(any(), any(FacesMessage.class));
        }

        @Test
        @DisplayName("month out of range (13) adds error")
        void month13AddsError() {
            intakeBean.setDobMonth("13");
            intakeBean.setDobDay("1");
            intakeBean.setDobYear("1978");

            intakeBean.reviewApplication();

            assertFalse(intakeBean.isReviewMode());
        }

        @Test
        @DisplayName("day out of range (0) adds error")
        void dayZeroAddsError() {
            intakeBean.setDobMonth("3");
            intakeBean.setDobDay("0");
            intakeBean.setDobYear("1978");

            intakeBean.reviewApplication();

            assertFalse(intakeBean.isReviewMode());
        }

        @Test
        @DisplayName("day out of range (32) adds error")
        void day32AddsError() {
            intakeBean.setDobMonth("3");
            intakeBean.setDobDay("32");
            intakeBean.setDobYear("1978");

            intakeBean.reviewApplication();

            assertFalse(intakeBean.isReviewMode());
        }

        @Test
        @DisplayName("year before 1900 adds error")
        void yearBefore1900AddsError() {
            intakeBean.setDobMonth("3");
            intakeBean.setDobDay("14");
            intakeBean.setDobYear("1899");

            intakeBean.reviewApplication();

            assertFalse(intakeBean.isReviewMode());
        }

        @Test
        @DisplayName("future date adds error")
        void futureDateAddsError() {
            LocalDate future = LocalDate.now().plusYears(1);
            intakeBean.setDobMonth(String.valueOf(future.getMonthValue()));
            intakeBean.setDobDay(String.valueOf(future.getDayOfMonth()));
            intakeBean.setDobYear(String.valueOf(future.getYear()));

            intakeBean.reviewApplication();

            assertFalse(intakeBean.isReviewMode());
        }

        @Test
        @DisplayName("non-numeric month adds error")
        void nonNumericMonthAddsError() {
            intakeBean.setDobMonth("March");
            intakeBean.setDobDay("14");
            intakeBean.setDobYear("1978");

            intakeBean.reviewApplication();

            assertFalse(intakeBean.isReviewMode());
        }

        @Test
        @DisplayName("impossible date (Feb 30) adds error")
        void impossibleDateAddsError() {
            intakeBean.setDobMonth("2");
            intakeBean.setDobDay("30");
            intakeBean.setDobYear("1978");

            intakeBean.reviewApplication();

            assertFalse(intakeBean.isReviewMode());
        }
    }

    // ---------------------------------------------------------------
    // editApplication
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("editApplication")
    class EditApplicationTests {

        @Test
        @DisplayName("exits review mode and restores DOB fields from assembled date")
        void restoresDobFields() {
            // First enter review mode with a valid date
            intakeBean.setDobMonth("3");
            intakeBean.setDobDay("14");
            intakeBean.setDobYear("1978");
            intakeBean.reviewApplication();
            assertTrue(intakeBean.isReviewMode());

            // Then go back to edit
            intakeBean.editApplication();

            assertFalse(intakeBean.isReviewMode());
            assertEquals("3",    intakeBean.getDobMonth());
            assertEquals("14",   intakeBean.getDobDay());
            assertEquals("1978", intakeBean.getDobYear());
        }
    }

    // ---------------------------------------------------------------
    // getFormattedDateOfBirth
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("getFormattedDateOfBirth")
    class FormattedDobTests {

        @Test
        @DisplayName("returns MM/dd/yyyy formatted string")
        void formatsCorrectly() {
            intakeBean.setDobMonth("3");
            intakeBean.setDobDay("14");
            intakeBean.setDobYear("1978");
            intakeBean.reviewApplication();

            assertEquals("03/14/1978", intakeBean.getFormattedDateOfBirth());
        }

        @Test
        @DisplayName("returns empty string when no DOB assembled yet")
        void returnsEmptyWhenNoDob() {
            assertEquals("", intakeBean.getFormattedDateOfBirth());
        }
    }

    // ---------------------------------------------------------------
    // getSelectedServiceTypeLabel
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("getSelectedServiceTypeLabel")
    class ServiceTypeLabelTests {

        @Test
        @DisplayName("returns human-readable label for valid service type")
        void returnsLabel() {
            intakeBean.setSelectedServiceType("MEDICAID");
            assertEquals("Medicaid / Health Coverage",
                intakeBean.getSelectedServiceTypeLabel());
        }

        @Test
        @DisplayName("returns empty string when no type selected")
        void returnsEmptyForNull() {
            assertEquals("", intakeBean.getSelectedServiceTypeLabel());
        }

        @Test
        @DisplayName("returns raw value for unknown service type")
        void returnsRawForUnknown() {
            intakeBean.setSelectedServiceType("UNKNOWN_TYPE");
            assertEquals("UNKNOWN_TYPE", intakeBean.getSelectedServiceTypeLabel());
        }
    }
}
