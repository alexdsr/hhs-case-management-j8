package gov.state.hhs.repository;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

/**
 * Base class for JPA integration tests.
 *
 * Manages a single EntityManagerFactory shared across all tests in a subclass,
 * and wraps each test in a transaction that is rolled back after completion —
 * ensuring tests are isolated and the database is always in a clean state.
 */
public abstract class BaseJpaIT {

    protected static EntityManagerFactory emf;
    protected EntityManager em;

    @BeforeAll
    static void setUpFactory() {
        emf = Persistence.createEntityManagerFactory("hhsTestPU");
    }

    @AfterAll
    static void tearDownFactory() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }

    @BeforeEach
    void setUpEntityManager() {
        em = emf.createEntityManager();
        em.getTransaction().begin();
    }

    @AfterEach
    void tearDownEntityManager() {
        if (em.getTransaction().isActive()) {
            em.getTransaction().rollback();
        }
        if (em.isOpen()) {
            em.close();
        }
    }

    /**
     * Flush pending changes to the database and clear the first-level cache,
     * forcing subsequent reads to hit the database. Useful for verifying
     * that persisted data survives the entity manager cache.
     */
    protected void flushAndClear() {
        em.flush();
        em.clear();
    }
}
