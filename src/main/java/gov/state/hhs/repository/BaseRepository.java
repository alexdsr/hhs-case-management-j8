package gov.state.hhs.repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import java.util.List;
import java.util.Optional;

/**
 * Base repository providing common JPA operations via JPQL.
 * Concrete repositories extend this class for entity-specific queries.
 */
public abstract class BaseRepository<T, ID> {

    @PersistenceContext(unitName = "hhsPU")
    protected EntityManager em;

    private final Class<T> entityClass;

    protected BaseRepository(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    public T save(T entity) {
        em.persist(entity);
        em.flush();
        return entity;
    }

    public T update(T entity) {
        T merged = em.merge(entity);
        em.flush();
        return merged;
    }

    public void delete(T entity) {
        em.remove(em.contains(entity) ? entity : em.merge(entity));
        em.flush();
    }

    public Optional<T> findById(ID id) {
        return Optional.ofNullable(em.find(entityClass, id));
    }

    protected TypedQuery<T> createNamedQuery(String queryName) {
        return em.createNamedQuery(queryName, entityClass);
    }

    protected <R> TypedQuery<R> createNamedQuery(String queryName, Class<R> resultClass) {
        return em.createNamedQuery(queryName, resultClass);
    }

    protected List<T> getResultList(String queryName) {
        return createNamedQuery(queryName).getResultList();
    }
}
