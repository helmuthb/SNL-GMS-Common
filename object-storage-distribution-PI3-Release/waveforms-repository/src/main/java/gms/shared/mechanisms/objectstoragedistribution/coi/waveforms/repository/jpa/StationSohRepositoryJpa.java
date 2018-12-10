package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.jpa;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.ParameterValidation;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.repository.DataExistsException;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSoh;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSohAnalog;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSohBoolean;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.RepositoryExceptionUtils;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.StationSohRepositoryInterface;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.jpa.dataaccessobjects.AcquiredChannelSohAnalogDao;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.jpa.dataaccessobjects.AcquiredChannelSohBooleanDao;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implement interface for storing and retrieving objects related to State of Health (SOH) from the
 * relational database.
 */
public class StationSohRepositoryJpa implements StationSohRepositoryInterface {

  private static final Logger logger = LoggerFactory.getLogger(StationSohRepositoryJpa.class);

  private final EntityManagerFactory entityManagerFactory;

  /**
   * Default constructor.
   */
  public StationSohRepositoryJpa() {
    this(WaveformsEntityManagerFactories.create());
  }

  public StationSohRepositoryJpa(EntityManagerFactory entityManagerFactory) {
    this.entityManagerFactory = entityManagerFactory;
  }

  /**
   * Initialize the persistence databases and perform other setup.
   *
   * @return True if successful, otherwise false.
   */
  @Override
  public boolean init() {

    return true;
  }

  /**
   * Close persistence databases and perform other shutdown tasks.
   *
   * @return True if successful, otherwise false.
   */
  @Override
  public boolean close() {
    return true;
  }

  /**
   * Insert a channel SOH object containing analog data into the relational database.
   *
   * @param soh The SOH object.
   */
  @Override
  public void storeAnalogSoh(AcquiredChannelSohAnalog soh) throws Exception {
    Validate.notNull(soh);
    try {
      if (analogSohExists(this.entityManagerFactory.createEntityManager(), soh)) {
        throw new DataExistsException("Attempt to store this soh, already persisted: " + soh);
      }
      AcquiredChannelSohAnalogDao channelSohDao = new AcquiredChannelSohAnalogDao(soh);
      JpaUtilities.saveObjectAndCommit(
          this.entityManagerFactory.createEntityManager(), channelSohDao);
    } catch (Exception ex) {
      throw RepositoryExceptionUtils.wrap(ex);
    }
  }

  /**
   * Insert a channel SOH object containing boolean data into the relational database.
   *
   * @param soh The SOH object.
   */
  @Override
  public void storeBooleanSoh(AcquiredChannelSohBoolean soh) throws Exception {
    Validate.notNull(soh);
    try {
      if (booleanSohExists(this.entityManagerFactory.createEntityManager(), soh)) {
        throw new DataExistsException("Attempt to store this soh, already persisted: " + soh);
      }
      AcquiredChannelSohBooleanDao channelSohDao = new AcquiredChannelSohBooleanDao(soh);
      JpaUtilities
          .saveObjectAndCommit(this.entityManagerFactory.createEntityManager(), channelSohDao);
    } catch (Exception ex) {
      throw RepositoryExceptionUtils.wrap(ex);
    }
  }

  /**
   * Retrieve all SOH double objects in the relational database.
   *
   * @return A collection containing the list of SOH double objects.
   */
  @Override
  public Collection<AcquiredChannelSohAnalog> retrieveAllAnalogSoh() throws Exception {
    EntityManager entityManager = null;
    try {
      entityManager = this.entityManagerFactory.createEntityManager();

      CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
      CriteriaQuery<AcquiredChannelSohAnalogDao> criteriaQuery = criteriaBuilder
          .createQuery(AcquiredChannelSohAnalogDao.class);
      Root<AcquiredChannelSohAnalogDao> rootEntry = criteriaQuery
          .from(AcquiredChannelSohAnalogDao.class);
      CriteriaQuery<AcquiredChannelSohAnalogDao> all = criteriaQuery.select(rootEntry);
      TypedQuery<AcquiredChannelSohAnalogDao> allQuery = entityManager.createQuery(all);

      return allQuery.getResultList().stream()
          .map(AcquiredChannelSohAnalogDao::toCoi)
          .collect(Collectors.toList());
    } catch (Exception ex) {
      throw RepositoryExceptionUtils.wrap(ex);
    } finally {
      if (entityManager != null) {
        entityManager.close();
      }
    }
  }

  /**
   * Retrieve all SOH boolean objects in the relational database.
   *
   * @return A collection of SOH boolean objects.
   */
  @Override
  public Collection<AcquiredChannelSohBoolean> retrieveAllBooleanSoh() throws Exception {
    EntityManager entityManager = null;
    try {
      entityManager = this.entityManagerFactory.createEntityManager();

      CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
      CriteriaQuery<AcquiredChannelSohBooleanDao> criteriaQuery = criteriaBuilder
          .createQuery(AcquiredChannelSohBooleanDao.class);
      Root<AcquiredChannelSohBooleanDao> rootEntry = criteriaQuery
          .from(AcquiredChannelSohBooleanDao.class);
      CriteriaQuery<AcquiredChannelSohBooleanDao> all = criteriaQuery.select(rootEntry);
      TypedQuery<AcquiredChannelSohBooleanDao> allQuery = entityManager.createQuery(all);

      return allQuery.getResultList().stream()
          .map(AcquiredChannelSohBooleanDao::toCoi)
          .collect(Collectors.toList());
    } catch (Exception ex) {
      throw RepositoryExceptionUtils.wrap(ex);
    } finally {
      if (entityManager != null) {
        entityManager.close();
      }
    }
  }

  @Override
  public Optional<AcquiredChannelSohBoolean> retrieveAcquiredChannelSohBooleanById(
      UUID acquiredChannelSohId) throws Exception {

    Objects.requireNonNull(acquiredChannelSohId,
        "retrieveAcquiredChannelSohBooleanById requires non-null acquiredChannelSohId");
    return querySohById(AcquiredChannelSohBooleanDao.class, AcquiredChannelSohBooleanDao::toCoi,
        acquiredChannelSohId);
  }

  @Override
  public Optional<AcquiredChannelSohAnalog> retrieveAcquiredChannelSohAnalogById(
      UUID acquiredChannelSohId) throws Exception {

    Objects.requireNonNull(acquiredChannelSohId,
        "retrieveAcquiredChannelSohAnalogById requires non-null acquiredChannelSohId");
    return querySohById(AcquiredChannelSohAnalogDao.class, AcquiredChannelSohAnalogDao::toCoi,
        acquiredChannelSohId);
  }

  /**
   * Queries the JPA entity of type J for an {@link AcquiredChannelSoh} object with the provided
   * identity.  Uses the converter to convert from an instance of J to an AcquiredChannelSoh. Output
   * {@link Optional} is empty when the query does not find an entity.
   *
   * @param entityType JPA entity type (e.g. {@link AcquiredChannelSohBooleanDao}, not null
   * @param converter converts from an entityType object to an AcquiredChannelSoh, not null
   * @param acquiredChannelSohId {@link UUID} of the desired AcquiredChannelSoh, not null
   * @param <D> JPA entity type
   * @return Optional AcquiredChannelSoh, not null
   */
  private <D, B> Optional<B> querySohById(Class<D> entityType, Function<D, B> converter,
      UUID acquiredChannelSohId) throws Exception {

    EntityManager entityManager = null;
    try {
      entityManager = entityManagerFactory.createEntityManager();

      B result;
      final TypedQuery<D> query = entityManager.createQuery(
          "SELECT s FROM " + entityType.getTypeName() + " s WHERE s.id = :id", entityType);
      query.setParameter("id", acquiredChannelSohId);
      result = converter.apply(query.getSingleResult());
      return Optional.ofNullable(result);
    } catch(NoResultException ex) {
      return Optional.empty();
    } catch (Exception ex) {
      throw RepositoryExceptionUtils.wrap(ex);
    } finally {
      if (entityManager != null) {
        entityManager.close();
      }
    }
  }

  @Override
  public List<AcquiredChannelSohBoolean> retrieveBooleanSohByProcessingChannelAndTimeRange(
      UUID processingChannelId, Instant startTime, Instant endTime) throws Exception {

    return querySohByProcessingChannelAndTimeRange(AcquiredChannelSohBooleanDao.class,
        AcquiredChannelSohBooleanDao::toCoi, entityManagerFactory.createEntityManager(),
        processingChannelId, startTime, endTime);
  }

  @Override
  public List<AcquiredChannelSohAnalog> retrieveAnalogSohByProcessingChannelAndTimeRange(
      UUID processingChannelId, Instant startTime, Instant endTime) throws Exception {

    return querySohByProcessingChannelAndTimeRange(AcquiredChannelSohAnalogDao.class,
        AcquiredChannelSohAnalogDao::toCoi, entityManagerFactory.createEntityManager(),
        processingChannelId, startTime, endTime);
  }

  /**
   * Queries for JPA entities of type J from a particular ProcessingChannel within a time interval
   *
   * @param entityType JPA entity type (e.g. Class J), not null
   * @param converter converts from a JPA entity type J to the business object type B
   * @param entityManager JPA {@link EntityManager}, not null
   * @param processingChannelId Id for the processing channel the SOH was measured on.
   * @param startTime Inclusive start from time range for the query.
   * @param endTime Inclusive end from time range for the query.
   * @param <J> type of acquired channel SOH JPA entity (either {@link AcquiredChannelSohBooleanDao}
   * or {@link AcquiredChannelSohAnalogDao})
   * @param <B> type of acquired channel SOH business object (either {@link
   * AcquiredChannelSohBoolean} or {@link AcquiredChannelSohAnalog})
   * @return All SOH objects that meet the query criteria.
   */
  private static <J, B> List<B> querySohByProcessingChannelAndTimeRange(Class<J> entityType,
      Function<J, B> converter, EntityManager entityManager,
      UUID processingChannelId, Instant startTime, Instant endTime) throws Exception {

    Objects
        .requireNonNull(processingChannelId, "Cannot run query with null processing channel id");
    Objects.requireNonNull(startTime, "Cannot run query with null start time");
    Objects.requireNonNull(endTime, "Cannot run query with null end time");

    //this allows startTime == endTime
    ParameterValidation.requireFalse(Instant::isAfter, startTime, endTime,
        "Cannot run query with start time greater than end time");

    try {
      TypedQuery<J> query = entityManager.createQuery(
          "SELECT s FROM " + entityType.getTypeName() + " s "
              + "WHERE s.processingChannelId = :id "
              + "AND s.endTime >= :start "
              + "AND s.startTime <= :end",
          entityType);

      query.setParameter("id", processingChannelId);
      query.setParameter("start", startTime);
      query.setParameter("end", endTime);

      return query.getResultList().stream()
          .map(converter)
          .collect(Collectors.toList());
    } catch (Exception ex) {
      throw RepositoryExceptionUtils.wrap(ex);
    } finally {
      if (entityManager != null) {
        entityManager.close();
      }
    }
  }

  private static boolean analogSohExists(EntityManager em, AcquiredChannelSohAnalog soh) {
    return sohExists(em, AcquiredChannelSohAnalogDao.class.getSimpleName(), soh);
  }

  private static boolean booleanSohExists(EntityManager em, AcquiredChannelSohBoolean soh) {
    return sohExists(em, AcquiredChannelSohBooleanDao.class.getSimpleName(), soh);
  }

  private static <T extends AcquiredChannelSoh> boolean sohExists(EntityManager em, String tableName, T soh) {
    return !em
        .createQuery("SELECT soh.id FROM " + tableName
            + " soh where soh.processingChannelId = :chan_id and soh.type = :type "
            + "and soh.startTime = :start and soh.endTime = :end")
        .setParameter("chan_id", soh.getProcessingChannelId())
        .setParameter("type", soh.getType())
        .setParameter("start", soh.getStartTime())
        .setParameter("end", soh.getEndTime())
        .setMaxResults(1)
        .getResultList()
        .isEmpty();
  }
}
