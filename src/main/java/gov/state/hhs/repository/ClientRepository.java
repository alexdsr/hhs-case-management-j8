package gov.state.hhs.repository;

import gov.state.hhs.model.Client;
import javax.ejb.Stateless;

import java.util.List;
import java.util.Optional;

/**
 * Data access for Client entities using JPQL named queries.
 */
@Stateless
public class ClientRepository extends BaseRepository<Client, Long> {

    public ClientRepository() {
        super(Client.class);
    }

    public List<Client> findAll() {
        return em.createNamedQuery("Client.findAll", Client.class)
                 .getResultList();
    }

    public List<Client> searchByName(String term) {
        String pattern = "%" + term.toLowerCase().trim() + "%";
        return em.createNamedQuery("Client.searchByName", Client.class)
                 .setParameter("term", pattern)
                 .getResultList();
    }

    public Optional<Client> findByEmail(String email) {
        return em.createNamedQuery("Client.findByEmail", Client.class)
                 .setParameter("email", email.toLowerCase().trim())
                 .getResultStream()
                 .findFirst();
    }
}
