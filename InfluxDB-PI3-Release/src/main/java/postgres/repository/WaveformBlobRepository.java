package postgres.repository;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import java.util.UUID;

/**
 * Created by jwvicke on 11/29/17.
 */
public class WaveformBlobRepository {

    private static final EntityManagerFactory entityManagerFactory
            = Persistence.createEntityManagerFactory("influx-testing");

    public void storeWaveformBlob(WaveformBlobDao wfb) throws Exception {
        EntityManager em = entityManagerFactory.createEntityManager();
        em.getTransaction().begin();
        em.persist(wfb);
        em.flush();
        em.getTransaction().commit();
        em.close();
    }

    public WaveformBlobDao getById(UUID id) {
        EntityManager em = entityManagerFactory.createEntityManager();
        TypedQuery<WaveformBlobDao> query = em.createQuery(
                "select wf from " + WaveformBlobDao.class.getSimpleName()
                        + " wf where wf.id = ?1",
                WaveformBlobDao.class);
        return query.setParameter(1, id).getSingleResult();
    }
}
