package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurement;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurementType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetection;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.SignalDetectionRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.persistence.EntityManagerFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class SignalDetectionRepositoryJpaTests {

  private EntityManagerFactory entityManagerFactory;

  private SignalDetectionRepository signalDetectionRepositoryJpa;

  private UUID id = UUID.randomUUID();
  private FeatureMeasurementType featureMeasurementType = FeatureMeasurementType.ARRIVAL_TIME;
  private double featureMeasurementValue = (double) Instant.now().toEpochMilli();
  private final UUID creationInfoId = UUID.randomUUID();
  private final FeatureMeasurement featureMeasurement = FeatureMeasurement.from(
      id, featureMeasurementType, featureMeasurementValue, creationInfoId);

  private String phase = "Pn";
  private List<FeatureMeasurement> featureMeasurements = List.of(featureMeasurement);

  private String monitoringOrganization = "CTBTO";
  private UUID stationId = UUID.randomUUID();

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Before
  public void setUp() {
    entityManagerFactory = SignalDetectionEntityManagerFactories.create("signaldetection-unitDB");
    signalDetectionRepositoryJpa = SignalDetectionRepositoryJpa.create(entityManagerFactory);
  }

  @After
  public void tearDown() {
    entityManagerFactory.close();
    entityManagerFactory = null;
    signalDetectionRepositoryJpa = null;
  }

  @Test
  public void testCreateNullEntityManagerFactoryExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage(
        "Cannot create SignalDetectionRepositoryJpa with a null EntityManagerFactory");
    SignalDetectionRepositoryJpa.create(null);
  }

  @Test
  public void testStoreNullSignalDetection() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("Cannot store a null SignalDetection");
    signalDetectionRepositoryJpa.store(null);
  }

  @Test
  public void testRetrieveAllExpectEmptyCollection() {
    Collection<SignalDetection> signalDetections = signalDetectionRepositoryJpa.retrieveAll();

    assertNotNull(signalDetections);
    assertEquals(0, signalDetections.size());
  }

  @Test
  public void testFindSignalDetectionByIdNullId() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("Cannot query using a null SignalDetection id");
    signalDetectionRepositoryJpa.findSignalDetectionById(null);
  }

  @Test
  public void testFindSignalDetectionByIdInvalidId() {
    Optional<SignalDetection> signalDetection =
        signalDetectionRepositoryJpa.findSignalDetectionById(UUID.randomUUID());

    assertEquals(false, signalDetection.isPresent());
  }

  /**
   * Test storing a SignalDetection with one SignalDetectionHypothesis, with the exact same object
   * count and values coming back from a query.
   *
   * @throws Exception any jpa exception
   */
  @Test
  public void testStoreSignalDetection() {
    storeAndRetrieveAll(1);
  }

  @Test
  public void testRetrieveAllSignalDetections() {
    storeAndRetrieveAll(2);
  }

  /**
   * Internal method to save "count" number of signal detections to the database, and then
   * retrieve all SignalDetections from the database and verify they matched what was saved.
   */
  private void storeAndRetrieveAll(int count) {
    // Loop through and save "count" number of SignalDetections
    int i = count;
    ArrayList<SignalDetection> signalDetections = new ArrayList<>(count);
    while (i > 0) {
      SignalDetection signalDetection = SignalDetection.create(
          monitoringOrganization, stationId, phase, featureMeasurements, creationInfoId);
      signalDetections.add(signalDetection);

      signalDetectionRepositoryJpa.store(signalDetection);

      i--;
    }

    // Query for all signal detections and make sure we get back same amount.
    Collection<SignalDetection> dbSignalDetections = signalDetectionRepositoryJpa.retrieveAll();
    assertEquals(count, dbSignalDetections.size());
    assertTrue(signalDetections.containsAll(dbSignalDetections));
  }

  /**
   * Test storing a SignalDetection with one hypothesis, adding another hypothesis and re-storing
   * the SignalDetection. This tests that previously persisted SignalDetections and hypotheses are
   * not duplicated in the database
   *
   * @throws Exception any jpa exception
   */
  @Test
  public void testStoreSignalDetectionNewSignalDetectionHypothesis() {
    SignalDetection signalDetection = SignalDetection.create(
        monitoringOrganization, stationId, phase, featureMeasurements, creationInfoId);

    signalDetectionRepositoryJpa.store(signalDetection);

    signalDetection
        .addSignalDetectionHypothesis("newPhase", featureMeasurements, UUID.randomUUID());

    signalDetectionRepositoryJpa.store(signalDetection);

    Optional<SignalDetection> signalDetectionOptional = signalDetectionRepositoryJpa
        .findSignalDetectionById(signalDetection.getId());
    assertTrue(signalDetectionOptional.isPresent());
    assertEquals(signalDetection, signalDetectionOptional.get());
  }

  @Test
  public void testFindSignalDetectionsById() {
    storeAndFindById(1);
    storeAndFindById(2);
  }

  /**
   * Internal method to save "count" number of signal detections to the database, and then
   * retrieve those SignalDetections based upon ID from the database and verify they matched
   * what was saved.
   */
  private void storeAndFindById(int count) {
    // Loop through and save "count" number of SignalDetections
    int i = count;
    ArrayList<SignalDetection> signalDetections = new ArrayList<>(count);
    while (i > 0) {
      SignalDetection signalDetection = SignalDetection.create(
          monitoringOrganization, stationId, phase, featureMeasurements, creationInfoId);
      signalDetections.add(signalDetection);

      signalDetectionRepositoryJpa.store(signalDetection);

      i--;
    }

    ArrayList<SignalDetection> dbSignalDetections = new ArrayList<>(count);
    for (SignalDetection signalDetection : signalDetections) {
      dbSignalDetections.add(
          signalDetectionRepositoryJpa.findSignalDetectionById(signalDetection.getId()).get());
    }

    assertEquals(count, dbSignalDetections.size());
    assertTrue(signalDetections.containsAll(dbSignalDetections));
  }

}
