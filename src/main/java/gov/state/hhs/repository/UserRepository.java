package gov.state.hhs.repository;

import gov.state.hhs.model.User;
import javax.ejb.Stateless;

import java.util.List;
import java.util.Optional;

/**
 * Data access for User entities using JPQL named queries.
 */
@Stateless
public class UserRepository extends BaseRepository<User, Long> {

    public UserRepository() {
        super(User.class);
    }

    public Optional<User> findByEmail(String email) {
        return em.createNamedQuery("User.findByEmail", User.class)
                 .setParameter("email", email.toLowerCase().trim())
                 .getResultStream()
                 .findFirst();
    }

    public List<User> findAllActive() {
        return em.createNamedQuery("User.findAllActive", User.class)
                 .getResultList();
    }
}
