package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.jpa;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.repository.DataExistsException;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.RawStationDataFrame;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.RawStationDataFrameRepositoryInterface;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.RepositoryExceptionUtils;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.jpa.dataaccessobjects.RawStationDataFrameDao;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RawStationDataFrameRepositoryJpa implements RawStationDataFrameRepositoryInterface {

  private static final Logger logger = LoggerFactory.getLogger(RawStationDataFrameRepositoryJpa.class);

  private final EntityManagerFactory entityManagerFactory;

  private static final String byStationNamePayloadStartTime
      = " ORDER BY stationName ASC, payloadDataStartTime ASC ";

  /**
   * Default constructor.
   */
  public RawStationDataFrameRepositoryJpa() {
    this(WaveformsEntityManagerFactories.create());
  }

  public RawStationDataFrameRepositoryJpa(EntityManagerFactory entityManagerFactory) {
    this.entityManagerFactory = entityManagerFactory;
  }

  /**
   * @inheritDoc
   */
  @Override
  public void storeRawStationDataFrame(RawStationDataFrame frame) throws Exception {
    Validate.notNull(frame);
    EntityManager entityManager = null;
    try {
      entityManager = this.entityManagerFactory.createEntityManager();
      if (frameExists(entityManager, frame)) {
        throw new DataExistsException("Attempt to store this frame, already persisted: " + frame);
      }
      RawStationDataFrameDao frameDao = new RawStationDataFrameDao(frame);
      entityManager.getTransaction().begin();
      entityManager.persist(frameDao);
      entityManager.getTransaction().commit();
    } catch (Exception ex) {
      throw RepositoryExceptionUtils.wrap(ex);
    } finally {
      if (entityManager != null) {
        entityManager.close();
      }
    }
  }

  /**
   * @inheritDoc
   */
  @Override
  public List<RawStationDataFrame> retrieveAll(Instant start, Instant end) throws Exception {
    EntityManager entityManager = null;

    try {
      entityManager = this.entityManagerFactory.createEntityManager();
      TypedQuery<RawStationDataFrameDao> query = entityManager
          .createQuery("select f from " + RawStationDataFrameDao.class.getSimpleName()
                  + " f where f.payloadDataEndTime >= :start and f.payloadDataStartTime <= :end "
                  + byStationNamePayloadStartTime,
              RawStationDataFrameDao.class);

      List<RawStationDataFrameDao> frameDaos = query
          .setParameter("start", start)
          .setParameter("end", end)
          .getResultList();
      return frameDaos.stream().map(RawStationDataFrameDao::toCoi)
          .collect(Collectors.toList());
    }
    catch(Exception ex) {
      throw RepositoryExceptionUtils.wrap(ex);
    } finally{
      if (entityManager != null) {
        entityManager.close();
      }
    }
  }

  /**
   * @inheritDoc
   */
  @Override
  public List<RawStationDataFrame> retrieveByStationName(String stationName, Instant start,
      Instant end) throws Exception {
    EntityManager entityManager = null;

    try {
      entityManager = this.entityManagerFactory.createEntityManager();
      TypedQuery<RawStationDataFrameDao> query = entityManager
          .createQuery("select f from " + RawStationDataFrameDao.class.getSimpleName()
                  + " f where f.stationName = :name and f.payloadDataEndTime >= :start"
                  + " and f.payloadDataStartTime <= :end " + byStationNamePayloadStartTime,
              RawStationDataFrameDao.class);

      List<RawStationDataFrameDao> frameDaos = query
          .setParameter("name", stationName)
          .setParameter("start", start)
          .setParameter("end", end)
          .getResultList();
      return frameDaos.stream().map(RawStationDataFrameDao::toCoi)
          .collect(Collectors.toList());
    }
    catch(Exception ex) {
      throw RepositoryExceptionUtils.wrap(ex);
    } finally{
      if (entityManager != null) {
        entityManager.close();
      }
    }
  }

  private static boolean frameExists(EntityManager em, RawStationDataFrame frame) {
    return !em
        .createQuery("SELECT f.id FROM " + RawStationDataFrameDao.class.getSimpleName()
            + " f where f.stationId = :staId and f.acquisitionProtocol = :protocol"
            + " and f.payloadDataStartTime = :start")
        .setParameter("staId", frame.getStationId())
        .setParameter("protocol", frame.getAcquisitionProtocol())
        .setParameter("start", frame.getPayloadDataStartTime())
        .setMaxResults(1)
        .getResultList()
        .isEmpty();
  }
}
