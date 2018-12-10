package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.repository.RepositoryException;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetection;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetectionHypothesis;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.SignalDetectionRepository;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.dataaccessobjects.SignalDetectionDao;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.dataaccessobjects.SignalDetectionHypothesisDao;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.utility.SignalDetectionDaoConverter;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.utility.SignalDetectionHypothesisDaoConverter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Repository class responsible for storage and retrieval operations on {@link
 * gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetection}s
 * and their related classes via JPA and DAO objects.
 */
public class SignalDetectionRepositoryJpa implements SignalDetectionRepository {

  private static final Logger logger = LoggerFactory.getLogger(SignalDetectionRepositoryJpa.class);

  private final EntityManagerFactory entityManagerFactory;

  /**
   * Obtain an instance of {@link SignalDetectionRepositoryJpa} using a default {@link
   * EntityManagerFactory}
   */
  public SignalDetectionRepositoryJpa() {
    this(SignalDetectionEntityManagerFactories.create());
  }

  /**
   * Default constructor, requiring a DI'd EntityManagerFactory for generating EntityManagers during
   * a repository call.
   *
   * @param entityManagerFactory Factory to create new {@link EntityManager}s
   */
  private SignalDetectionRepositoryJpa(EntityManagerFactory entityManagerFactory) {
    this.entityManagerFactory = entityManagerFactory;
  }

  /**
   * Obtain a new {@link SignalDetectionRepositoryJpa} which uses the provided {@link
   * EntityManagerFactory}
   *
   * @param entityManagerFactory EntityManagerFactory for the filter definition entity classes, not
   * null
   * @return SignalDetectionRepositoryJpa, not null
   * @throws NullPointerException if entityManagerFactory is null
   */
  public static SignalDetectionRepositoryJpa create(EntityManagerFactory entityManagerFactory) {
    Objects.requireNonNull(entityManagerFactory,
        "Cannot create SignalDetectionRepositoryJpa with a null EntityManagerFactory");

    return new SignalDetectionRepositoryJpa(entityManagerFactory);
  }

  /**
   * Convenience method for handling exceptions and resource closing whenever any repository call is
   * made.
   *
   * @param consumer Any repository call returning void (e.g. store calls)
   */
  private void acceptInEntitySession(Consumer<EntityManager> consumer) {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    try {
      consumer.accept(entityManager);
    } catch (PersistenceException e) {
      throw new RuntimeException(e);
    } finally {
      entityManager.close();
    }
  }

  /**
   * Convenience method for handling exceptions and resource closing whenever any repository call is
   * made.
   *
   * @param applier Any repository call returning not void (e.g. find calls)
   */
  private <T> T applyInEntitySession(Function<EntityManager, T> applier) {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    try {
      return applier.apply(entityManager);
    } catch (PersistenceException e) {
      throw new RuntimeException(e);
    } finally {
      entityManager.close();
    }
  }

  @Override
  public void store(SignalDetection detection) {
    Objects.requireNonNull(detection, "Cannot store a null SignalDetection");
    acceptInEntitySession(em -> storeInternal(em, detection));
  }

  /**
   * Internal method used to handle storing SignalDetections.
   *
   * @param entityManager EntityManager used to handle queries and storage.
   * @param signalDetection SignalDetection to store.
   */
  private static void storeInternal(EntityManager entityManager, SignalDetection signalDetection) {
    SignalDetectionDao signalDetectionDao = getSignalDetectionOrCreate(entityManager,
        signalDetection);

    Function<SignalDetectionHypothesis, SignalDetectionHypothesisDao> converterClosure
        = v -> SignalDetectionHypothesisDaoConverter.toDao(signalDetectionDao, v);

    List<UUID> signalDetectionHypothesisIds = getSignalDetectionHypothesisForOwnerSignalDetectionId(
        entityManager, signalDetection.getId());

    //if empty, means a brand new SignalDetection, so skip the intersections and just persist
    final boolean persistSignalDetection;
    final List<SignalDetectionHypothesis> hypothesesToStore;

    if (signalDetectionHypothesisIds.isEmpty()) {
      persistSignalDetection = true;
      hypothesesToStore = signalDetection.getSignalDetectionHypotheses();
    } else {
      persistSignalDetection = false;
      hypothesesToStore = getHypothesisToStore(signalDetection, signalDetectionHypothesisIds);
    }

    List<SignalDetectionHypothesisDao> hypothesisToPersist = hypothesesToStore.stream()
        .map(converterClosure)
        .collect(Collectors.toList());

    try {
      entityManager.getTransaction().begin();
      if (persistSignalDetection) {
        entityManager.persist(signalDetectionDao);
      }

      hypothesisToPersist.forEach(entityManager::persist);
      entityManager.getTransaction().commit();
    } catch (IllegalArgumentException | PersistenceException e) {
      logger.error("Error storing SignalDetection", e);
      if (entityManager.getTransaction().isActive()) {
        entityManager.getTransaction().rollback();
      }
      throw e;
    }
  }

  /**
   * Queries the database for a {@link SignalDetectionDao} with an id matching the input {@link
   * SignalDetection}. If one is not found, creates a new SignalDetectionDao from the input
   * SignalDetection.
   *
   * @param entityManager Provides connection to the database.
   * @param signalDetection SignalDetectionDao we are searching for in the database.
   * @return Either a previously persisted or newly created SignalDetectionDao.
   */
  private static SignalDetectionDao getSignalDetectionOrCreate(EntityManager entityManager,
      SignalDetection signalDetection) {
    TypedQuery<SignalDetectionDao> query = entityManager
        .createQuery("SELECT s FROM SignalDetectionDao s WHERE s.signalDetectionId = :id",
            SignalDetectionDao.class);

    query.setParameter("id", signalDetection.getId());

    List<SignalDetectionDao> signalDetectionDaos = query.getResultList();

    return signalDetectionDaos.isEmpty() ? SignalDetectionDaoConverter.toDao(signalDetection)
        : signalDetectionDaos.get(0);
  }

  /**
   * Queries the database for all {@link SignalDetectionHypothesis} ids related to the input {@link
   * SignalDetection} id.
   *
   * @param entityManager Provides connection to the database.
   * @param id SignalDetection id used to search for SignalDetectionHypothesis
   * @return All Identities from SignalDetectionHypothesis that belong to the input SignalDetection.
   */
  private static List<UUID> getSignalDetectionHypothesisForOwnerSignalDetectionId(
      EntityManager entityManager,
      UUID id) {

    TypedQuery<UUID> query = entityManager
        .createQuery("SELECT s.signalDetectionHypothesisId "
                + "FROM SignalDetectionHypothesisDao s "
                + "WHERE s.parentSignalDetection.signalDetectionId = :id",
            UUID.class);

    return query.setParameter("id", id).getResultList();
  }

  /**
   * Filters out {@link SignalDetectionHypothesis} objects that have already been persisted.
   *
   * @param signalDetection SignalDetection containing signalDetectionHypotheses we wish to filter
   * out
   * @param signalDetectionHypothesisIds Ids for {@link SignalDetectionHypothesisDao} already stored
   * in the database
   * @return SignalDetectionHypotheses that need to be persisted
   */
  private static List<SignalDetectionHypothesis> getHypothesisToStore(
      SignalDetection signalDetection,
      List<UUID> signalDetectionHypothesisIds) {

    Set<UUID> hypothesisSet = signalDetection.getSignalDetectionHypotheses().stream()
        .map(SignalDetectionHypothesis::getId).collect(Collectors.toSet());

    hypothesisSet.removeAll(signalDetectionHypothesisIds);

    return signalDetection.getSignalDetectionHypotheses().stream()
        .filter(v -> hypothesisSet.contains((v.getId())))
        .collect(Collectors.toList());
  }

  @Override
  public Collection<SignalDetection> retrieveAll() {

    Collection<SignalDetectionHypothesis> signalDetectionHypotheses = retrieveAllSignalDetectionHypothesis();

    TypedQuery<SignalDetectionDao> query = entityManagerFactory.createEntityManager()
        .createQuery("SELECT s FROM SignalDetectionDao s", SignalDetectionDao.class);
    List<SignalDetectionDao> signalDetectionDaos = query.getResultList();

    return mapSignalDetectionHypothesisToSignalDetection(signalDetectionDaos,
        signalDetectionHypotheses);
  }

  private Collection<SignalDetection> mapSignalDetectionHypothesisToSignalDetection(
      List<SignalDetectionDao> signalDetectionDaos,
      Collection<SignalDetectionHypothesis> signalDetectionHypotheses) {

    HashMap<SignalDetectionDao, List<SignalDetectionHypothesis>> signalDetectionHypothesisHashMap =
        new HashMap<>(signalDetectionDaos.size());

    for (SignalDetectionDao signalDetectionDao : signalDetectionDaos) {
      if (!signalDetectionHypothesisHashMap.containsKey(signalDetectionDao)) {
        signalDetectionHypothesisHashMap
            .put(signalDetectionDao, new ArrayList<>());
      }

      for (SignalDetectionHypothesis sdh : signalDetectionHypotheses) {
        if (signalDetectionDao.getSignalDetectionId().equals(sdh.getParentSignalDetectionId())) {
          signalDetectionHypothesisHashMap.get(signalDetectionDao).add(sdh);
        }
      }
    }

    ArrayList<SignalDetection> signalDetections = new ArrayList<>(
        signalDetectionHypothesisHashMap.size());
    for (SignalDetectionDao signalDetectionDao : signalDetectionHypothesisHashMap.keySet()) {
      signalDetections.add(SignalDetectionDaoConverter.fromDao(
          signalDetectionDao, signalDetectionHypothesisHashMap.get(signalDetectionDao)));
    }

    return signalDetections;
  }

  /**
   * Internal method called when searching for all SignalDetections to retrieve the associated
   * hypotheses.
   */
  private Collection<SignalDetectionHypothesis> retrieveAllSignalDetectionHypothesis() {
    final Function<EntityManager, Collection<SignalDetectionHypothesisDao>> findAllSignalDetectionHypothesis =
        em -> em.createQuery("SELECT s FROM SignalDetectionHypothesisDao s",
            SignalDetectionHypothesisDao.class)
            .getResultList();

    return applyInEntitySession(findAllSignalDetectionHypothesis)
        .stream()
        .map(SignalDetectionHypothesisDaoConverter::fromDao)
        .collect(Collectors.toList());
  }

  @Override
  public Optional<SignalDetection> findSignalDetectionById(UUID id) {
    Objects.requireNonNull(id, "Cannot query using a null SignalDetection id");

    Collection<SignalDetectionHypothesis> signalDetectionHypotheses = findSignalDetectionHypothesesByParentSignalDetectionId(
        id);

    TypedQuery<SignalDetectionDao> query = entityManagerFactory.createEntityManager()
        .createQuery("SELECT s FROM SignalDetectionDao s WHERE s.signalDetectionId = :id",
            SignalDetectionDao.class);
    query.setParameter("id", id);

    List<SignalDetectionDao> signalDetectionDaos = query.getResultList();
    List<SignalDetection> signalDetections = signalDetectionDaos.stream()
        .map(v -> SignalDetectionDaoConverter
            .fromDao(v, new ArrayList<SignalDetectionHypothesis>(signalDetectionHypotheses)))
        .collect(Collectors.toList());

    if (signalDetections.isEmpty()) {
      logger.warn("No SignalDetections found for ID = " + id);
      return Optional.empty();
    } else if (signalDetections.size() > 1) {
      throw new IllegalStateException(
          signalDetections.size() + " SignalDetections returned for ID = " + id);
    }

    return Optional.of(signalDetections.get(0));
  }

  /**
   * Internal method called when searching for SignalDetections to retrieve the associated
   * hypotheses.
   *
   * @param parentId The ID of the parent SignalDetection
   */
  private List<SignalDetectionHypothesis> findSignalDetectionHypothesesByParentSignalDetectionId(
      UUID parentId) {

    TypedQuery<SignalDetectionHypothesisDao> query = entityManagerFactory.createEntityManager()
        .createQuery(
            "SELECT s FROM SignalDetectionHypothesisDao s WHERE s.parentSignalDetection.signalDetectionId = :id",
            SignalDetectionHypothesisDao.class);
    query.setParameter("id", parentId);

    List<SignalDetectionHypothesisDao> signalDetectionHypothesisDaos = query.getResultList();

    return signalDetectionHypothesisDaos
        .stream()
        .map(SignalDetectionHypothesisDaoConverter::fromDao)
        .collect(Collectors.toList());
  }
}
