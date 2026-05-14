package gov.state.hhs.service;

import gov.state.hhs.model.Client;
import gov.state.hhs.repository.ClientRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ClientService.
 * Focuses on name/email normalization and delegation to the repository.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ClientService")
class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private ClientService clientService;

    // ---------------------------------------------------------------
    // Name normalization via save()
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("save — name normalization")
    class SaveNormalizationTests {

        @Test
        @DisplayName("capitalizes first name with lowercase remainder")
        void capitalizesFirstName() {
            Client client = buildClient("rOBERT", "hutchins", null, "UT");
            when(clientRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            clientService.save(client);

            ArgumentCaptor<Client> captor = ArgumentCaptor.forClass(Client.class);
            verify(clientRepository).save(captor.capture());
            assertEquals("Robert", captor.getValue().getFirstName());
        }

        @Test
        @DisplayName("capitalizes last name with lowercase remainder")
        void capitalizesLastName() {
            Client client = buildClient("robert", "HUTCHINS", null, "UT");
            when(clientRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            clientService.save(client);

            ArgumentCaptor<Client> captor = ArgumentCaptor.forClass(Client.class);
            verify(clientRepository).save(captor.capture());
            assertEquals("Hutchins", captor.getValue().getLastName());
        }

        @Test
        @DisplayName("lowercases email")
        void lowercasesEmail() {
            Client client = buildClient("Robert", "Hutchins", "ROBERT@EMAIL.COM", "UT");
            when(clientRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            clientService.save(client);

            ArgumentCaptor<Client> captor = ArgumentCaptor.forClass(Client.class);
            verify(clientRepository).save(captor.capture());
            assertEquals("robert@email.com", captor.getValue().getEmail());
        }

        @Test
        @DisplayName("trims whitespace from email")
        void trimsEmail() {
            Client client = buildClient("Robert", "Hutchins", "  robert@email.com  ", "UT");
            when(clientRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            clientService.save(client);

            ArgumentCaptor<Client> captor = ArgumentCaptor.forClass(Client.class);
            verify(clientRepository).save(captor.capture());
            assertEquals("robert@email.com", captor.getValue().getEmail());
        }

        @Test
        @DisplayName("uppercases state code")
        void uppercasesStateCode() {
            Client client = buildClient("Robert", "Hutchins", null, "ut");
            when(clientRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            clientService.save(client);

            ArgumentCaptor<Client> captor = ArgumentCaptor.forClass(Client.class);
            verify(clientRepository).save(captor.capture());
            assertEquals("UT", captor.getValue().getStateCode());
        }

        @Test
        @DisplayName("handles null email without throwing")
        void nullEmailHandled() {
            Client client = buildClient("Robert", "Hutchins", null, "UT");
            when(clientRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            assertDoesNotThrow(() -> clientService.save(client));
        }
    }

    // ---------------------------------------------------------------
    // emailInUse
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("emailInUse")
    class EmailInUseTests {

        @Test
        @DisplayName("returns true when repository finds a match")
        void emailInUseTrue() {
            when(clientRepository.findByEmail(anyString()))
                .thenReturn(Optional.of(new Client()));
            assertTrue(clientService.emailInUse("taken@email.com"));
        }

        @Test
        @DisplayName("returns false when repository finds no match")
        void emailInUseFalse() {
            when(clientRepository.findByEmail(anyString()))
                .thenReturn(Optional.empty());
            assertFalse(clientService.emailInUse("free@email.com"));
        }
    }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

    private Client buildClient(String first, String last, String email, String state) {
        Client c = new Client();
        c.setFirstName(first);
        c.setLastName(last);
        c.setEmail(email);
        c.setStateCode(state);
        c.setAddressLine1("123 Main St");
        c.setCity("Salt Lake City");
        c.setZip("84101");
        return c;
    }
}
