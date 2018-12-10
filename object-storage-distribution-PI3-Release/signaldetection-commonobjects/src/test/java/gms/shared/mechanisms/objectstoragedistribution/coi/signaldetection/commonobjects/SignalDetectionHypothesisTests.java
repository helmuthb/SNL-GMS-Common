package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.TestUtilities;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Tests {@link SignalDetectionHypothesis} factory creation
 */
public class SignalDetectionHypothesisTests {

  private final UUID id = UUID.randomUUID();
  private String phase = "Pn";
  private boolean rejected = false;
  private List<FeatureMeasurement> featureMeasurements =
      Arrays.asList(new FeatureMeasurementTests().getTestFeatureMeasurement());
  private final UUID creationInfoId = UUID.randomUUID();

  private String monitoringOrganization = "CTBTO";
  private UUID stationId = UUID.randomUUID();
  private SignalDetection signalDetection = SignalDetection.from(id, monitoringOrganization, stationId,
      Collections.emptyList(), creationInfoId);

  private SignalDetectionHypothesis signalDetectionHypothesis;

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Before
  public void createSignalDetectionHypothesis() {
    signalDetection.addSignalDetectionHypothesis(phase, featureMeasurements, creationInfoId);
    signalDetectionHypothesis = signalDetection.getSignalDetectionHypotheses().get(0);
  }

  @Test
  public void testFromNullParameters() throws Exception {
    TestUtilities.checkStaticMethodValidatesNullArguments(SignalDetectionHypothesis.class, "from",
        id, signalDetection.getId(), phase, rejected, featureMeasurements, creationInfoId);
  }

  @Test
  public void testFrom() {
    SignalDetectionHypothesis signalDetectionHypothesis = SignalDetectionHypothesis.from(
        id, signalDetection.getId(), phase, rejected, featureMeasurements, creationInfoId);
    assertEquals(signalDetection.getId(), signalDetectionHypothesis.getParentSignalDetectionId());
    assertEquals(phase, signalDetectionHypothesis.getPhase());
    assertEquals(rejected, signalDetectionHypothesis.isRejected());
    assertArrayEquals(featureMeasurements.toArray(),
                      signalDetectionHypothesis.getFeatureMeasurements().toArray());
    assertEquals(creationInfoId, signalDetectionHypothesis.getCreationInfoId());
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testUnmodifiableFeatureMeasurement() throws Exception {
    signalDetectionHypothesis.getFeatureMeasurements().add(featureMeasurements.get(0));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNoArrivalTimeFeatureMeasurement() throws Exception {
    FeatureMeasurement featureMeasurement = FeatureMeasurement.from(
        UUID.randomUUID(), FeatureMeasurementType.AMPLITUDE, 0, UUID.randomUUID());
    List<FeatureMeasurement> featureMeasurements2 = new ArrayList<FeatureMeasurement>();

    SignalDetectionHypothesis signalDetectionHypothesis2 =
        SignalDetectionHypothesis.from(UUID.randomUUID(), UUID.randomUUID(), phase, rejected, featureMeasurements2, creationInfoId);
  }

  @Test
  public void testEqualsHashCode() {

    SignalDetectionHypothesis sdh2 =
        SignalDetectionHypothesis.from(signalDetectionHypothesis.getId(),
            signalDetectionHypothesis.getParentSignalDetectionId(),
            signalDetectionHypothesis.getPhase(), signalDetectionHypothesis.isRejected(),
            signalDetectionHypothesis.getFeatureMeasurements(), signalDetectionHypothesis.getCreationInfoId());

    assertEquals(signalDetectionHypothesis, sdh2);
    assertEquals(sdh2, signalDetectionHypothesis);
    assertEquals(signalDetectionHypothesis.hashCode(), sdh2.hashCode());
  }

  @Test
  public void testEqualsExpectInequality() {

    signalDetection.addSignalDetectionHypothesis(phase, featureMeasurements, creationInfoId);

    assertEquals(2, signalDetection.getSignalDetectionHypotheses().size());

    List<SignalDetectionHypothesis> sdh = signalDetection.getSignalDetectionHypotheses();
    final SignalDetectionHypothesis sdh1 = sdh.get(0);
    final SignalDetectionHypothesis sdh2 = sdh.get(1);

    // Different id
    assertNotEquals(sdh1, sdh2);
  }
}
