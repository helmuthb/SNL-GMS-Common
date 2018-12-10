package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.TestUtilities;
import java.time.Instant;
import java.util.Collections;
import java.util.UUID;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Tests {@link FeatureMeasurement} factory creation
 */
public class FeatureMeasurementTests {

  private UUID id = UUID.randomUUID();
  private FeatureMeasurementType featureMeasurementType = FeatureMeasurementType.ARRIVAL_TIME;
  private double featureMeasurementValue = (double)Instant.now().toEpochMilli();
  private final UUID creationInfoId = UUID.randomUUID();

  private final FeatureMeasurement featureMeasurement = FeatureMeasurement.from(
      id, featureMeasurementType, featureMeasurementValue, creationInfoId);

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Test
  public void testFromNullParameters() throws Exception {

    TestUtilities.checkStaticMethodValidatesNullArguments(FeatureMeasurement.class, "from",
        id, featureMeasurementType, featureMeasurementValue, creationInfoId);
  }

  @Test
  public void testCreateNullParameters() throws Exception {

    TestUtilities.checkStaticMethodValidatesNullArguments(FeatureMeasurement.class, "create",
        featureMeasurementType, featureMeasurementValue, creationInfoId);
  }

  @Test
  public void testFrom() {
    assertEquals(id, featureMeasurement.getId());
    assertEquals(featureMeasurementType, featureMeasurement.getFeatureMeasurementType());
    assertEquals(featureMeasurementValue, featureMeasurement.getFeatureMeasurementValue(), 0.001);
    assertEquals(creationInfoId, featureMeasurement.getCreationInfoId());
  }

  @Test
  public void testCreate() {
    FeatureMeasurement featureMeasurement2 = FeatureMeasurement.create(
        featureMeasurementType, featureMeasurementValue, creationInfoId);

    assertEquals(featureMeasurementType, featureMeasurement2.getFeatureMeasurementType());
    assertEquals(featureMeasurementValue, featureMeasurement2.getFeatureMeasurementValue(), 0.001);
    assertEquals(creationInfoId, featureMeasurement2.getCreationInfoId());
  }

  @Test
  public void testEqualsHashCode() {

    final FeatureMeasurement fm2 = FeatureMeasurement.from(featureMeasurement.getId(),
        featureMeasurement.getFeatureMeasurementType(), featureMeasurement.getFeatureMeasurementValue(), featureMeasurement.getCreationInfoId());

    assertEquals(featureMeasurement, fm2);
    assertEquals(fm2, featureMeasurement);
    assertEquals(featureMeasurement.hashCode(), fm2.hashCode());
  }

  @Test
  public void testEqualsExpectInequality() {

    FeatureMeasurement fm2 = FeatureMeasurement.create(featureMeasurement.getFeatureMeasurementType(),
        featureMeasurement.getFeatureMeasurementValue(), featureMeasurement.getCreationInfoId());

    // Different id
    assertNotEquals(featureMeasurement, fm2);

    // Different measurement type.
    fm2 = FeatureMeasurement.from(featureMeasurement.getId(), FeatureMeasurementType.AMPLITUDE,
        featureMeasurement.getFeatureMeasurementValue(), featureMeasurement.getCreationInfoId());
    assertNotEquals(featureMeasurement, fm2);

    // Different measurement value.
    fm2 = FeatureMeasurement.from(featureMeasurement.getId(), featureMeasurement.getFeatureMeasurementType(),
        123.456, featureMeasurement.getCreationInfoId());
    assertNotEquals(featureMeasurement, fm2);

    // Different creation id.
    fm2 = FeatureMeasurement.from(featureMeasurement.getId(), featureMeasurement.getFeatureMeasurementType(),
        featureMeasurement.getFeatureMeasurementValue(), UUID.randomUUID());
    assertNotEquals(featureMeasurement, fm2);
  }

  public FeatureMeasurement getTestFeatureMeasurement() {
    return featureMeasurement;
  }
}
