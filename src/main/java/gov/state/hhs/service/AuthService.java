package gov.state.hhs.service;

import gov.state.hhs.model.User;
import gov.state.hhs.repository.UserRepository;
import javax.ejb.Stateless;
import javax.inject.Inject;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * EJB service handling staff authentication.
 *
 * NOTE: For demonstration purposes this implementation uses SHA-256 hashing,
 * which is straightforward to verify without external libraries.
 * A production system must use bcrypt or Argon2 via a library such as
 * jBCrypt or Spring Security Crypto.
 */
@Stateless
public class AuthService {

    private static final Logger LOG = Logger.getLogger(AuthService.class.getName());

    @Inject
    private UserRepository userRepository;

    /**
     * Attempts to authenticate a staff user by email and password.
     *
     * @param email    the user's email address
     * @param password the plaintext password supplied at login
     * @return an Optional containing the authenticated User, or empty on failure
     */
    public Optional<User> authenticate(String email, String password) {
        if (email == null || password == null) {
            return Optional.empty();
        }

        Optional<User> userOpt = userRepository.findByEmail(email);
        if (!userOpt.isPresent()) {
            // Constant-time comparison to avoid timing attacks
            dummyHash();
            return Optional.empty();
        }

        User user = userOpt.get();
        if (passwordMatches(password, user.getPasswordHash())) {
            LOG.info("Successful login for: " + email);
            return Optional.of(user);
        }

        LOG.warning("Failed login attempt for: " + email);
        return Optional.empty();
    }

    /**
     * Hashes a plaintext password using SHA-256 for storage or comparison.
     * Replace with bcrypt in production.
     */
    public String hashPassword(String plaintext) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(plaintext.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private boolean passwordMatches(String plaintext, String storedHash) {
        // The seed data uses plain SHA-256 hex hashes for demo simplicity
        return hashPassword(plaintext).equals(storedHash);
    }

    private void dummyHash() {
        try {
            MessageDigest.getInstance("SHA-256")
                         .digest("dummy".getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            LOG.log(Level.SEVERE, "SHA-256 unavailable", e);
        }
    }
}
