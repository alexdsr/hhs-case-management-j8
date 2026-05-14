package gov.state.hhs.service;

import gov.state.hhs.model.User;
import gov.state.hhs.model.UserRole;
import gov.state.hhs.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthService.
 * Mocks UserRepository to test authentication and hashing logic in isolation.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthService authService;

    private User activeUser;

    @BeforeEach
    void setUp() {
        activeUser = new User();
        activeUser.setEmail("admin@hhs.gov");
        activeUser.setFullName("Patricia Morales");
        activeUser.setRole(UserRole.ADMIN);
        activeUser.setActive(true);
        // SHA-256 hash of "Admin1234!"
        activeUser.setPasswordHash(
            "5ce41ada64f1e8ffb0acfaafa622b141438f3a5777785e7f0b830fb73e40d3d6");
    }

    // ---------------------------------------------------------------
    // Password hashing
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("hashPassword")
    class HashPasswordTests {

        @Test
        @DisplayName("produces a 64-character hex string")
        void producesHexString() {
            String hash = authService.hashPassword("anypassword");
            assertNotNull(hash);
            assertEquals(64, hash.length());
            assertTrue(hash.matches("[0-9a-f]+"), "Hash should be lowercase hex");
        }

        @Test
        @DisplayName("same input always produces same hash (deterministic)")
        void isDeterministic() {
            String hash1 = authService.hashPassword("Admin1234!");
            String hash2 = authService.hashPassword("Admin1234!");
            assertEquals(hash1, hash2);
        }

        @Test
        @DisplayName("different inputs produce different hashes")
        void differentInputsDifferentHashes() {
            String hash1 = authService.hashPassword("Admin1234!");
            String hash2 = authService.hashPassword("Admin1234 ");
            assertNotEquals(hash1, hash2);
        }

        @Test
        @DisplayName("produces known hash for Admin1234!")
        void knownHashForAdminPassword() {
            String hash = authService.hashPassword("Admin1234!");
            assertEquals(
                "5ce41ada64f1e8ffb0acfaafa622b141438f3a5777785e7f0b830fb73e40d3d6",
                hash);
        }

        @Test
        @DisplayName("produces known hash for Case1234!")
        void knownHashForCaseworkerPassword() {
            String hash = authService.hashPassword("Case1234!");
            assertEquals(
                "932f33c32393f9fb76ff162e00a2ccae95f9b0ede44471a5a6c90258b18d080d",
                hash);
        }
    }

    // ---------------------------------------------------------------
    // Authentication
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("authenticate")
    class AuthenticateTests {

        @Test
        @DisplayName("returns user when credentials are correct")
        void successfulLogin() {
            when(userRepository.findByEmail("admin@hhs.gov"))
                .thenReturn(Optional.of(activeUser));

            Optional<User> result = authService.authenticate("admin@hhs.gov", "Admin1234!");

            assertTrue(result.isPresent());
            assertEquals("Patricia Morales", result.get().getFullName());
        }

        @Test
        @DisplayName("returns empty when password is wrong")
        void wrongPassword() {
            when(userRepository.findByEmail("admin@hhs.gov"))
                .thenReturn(Optional.of(activeUser));

            Optional<User> result = authService.authenticate("admin@hhs.gov", "WrongPassword!");

            assertTrue(!result.isPresent());
        }

        @Test
        @DisplayName("returns empty when user is not found")
        void userNotFound() {
            when(userRepository.findByEmail(anyString()))
                .thenReturn(Optional.empty());

            Optional<User> result = authService.authenticate("nobody@hhs.gov", "anypassword");

            assertTrue(!result.isPresent());
        }

        @Test
        @DisplayName("returns empty when email is null")
        void nullEmail() {
            Optional<User> result = authService.authenticate(null, "Admin1234!");
            assertTrue(!result.isPresent());
            verify(userRepository, never()).findByEmail(any());
        }

        @Test
        @DisplayName("returns empty when password is null")
        void nullPassword() {
            Optional<User> result = authService.authenticate("admin@hhs.gov", null);
            assertTrue(!result.isPresent());
            verify(userRepository, never()).findByEmail(any());
        }

        @Test
        @DisplayName("repository is called exactly once per valid authenticate attempt")
        void repositoryCalledOnce() {
            when(userRepository.findByEmail("admin@hhs.gov"))
                .thenReturn(Optional.of(activeUser));

            authService.authenticate("admin@hhs.gov", "Admin1234!");

            verify(userRepository, times(1)).findByEmail("admin@hhs.gov");
        }
    }
}
