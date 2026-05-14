package gov.state.hhs.repository;

import gov.state.hhs.model.ApplicationStatus;
import gov.state.hhs.model.ServiceApplication;
import javax.ejb.Stateless;

import java.util.List;

/**
 * Data access for ServiceApplication entities using JPQL named queries.
 */
@Stateless
public class ApplicationRepository extends BaseRepository<ServiceApplication, Long> {

    public ApplicationRepository() {
        super(ServiceApplication.class);
    }

    public List<ServiceApplication> findAll() {
        return em.createNamedQuery("ServiceApplication.findAll", ServiceApplication.class)
                 .getResultList();
    }

    public List<ServiceApplication> findByStatus(ApplicationStatus status) {
        return em.createNamedQuery("ServiceApplication.findByStatus", ServiceApplication.class)
                 .setParameter("status", status)
                 .getResultList();
    }

    public List<ServiceApplication> findByAssignedUser(Long userId) {
        return em.createNamedQuery("ServiceApplication.findByAssignedTo", ServiceApplication.class)
                 .setParameter("userId", userId)
                 .getResultList();
    }

    public List<ServiceApplication> findByClient(Long clientId) {
        return em.createNamedQuery("ServiceApplication.findByClient", ServiceApplication.class)
                 .setParameter("clientId", clientId)
                 .getResultList();
    }

    public List<Object[]> countByStatus() {
        return em.createNamedQuery("ServiceApplication.countByStatus", Object[].class)
                 .getResultList();
    }

    public List<Object[]> countByStatusForUser(Long userId) {
        return em.createNamedQuery("ServiceApplication.countByStatusForUser", Object[].class)
                 .setParameter("userId", userId)
                 .getResultList();
    }

    public long countUnassigned() {
        return em.createNamedQuery("ServiceApplication.countUnassigned", Long.class)
                 .getSingleResult();
    }

    public List<ServiceApplication> findUnassigned() {
        return em.createNamedQuery("ServiceApplication.findUnassigned", ServiceApplication.class)
                 .getResultList();
    }
}
