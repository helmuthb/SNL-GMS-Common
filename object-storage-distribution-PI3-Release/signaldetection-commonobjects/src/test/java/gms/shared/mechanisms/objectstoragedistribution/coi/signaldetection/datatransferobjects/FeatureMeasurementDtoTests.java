package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.datatransferobjects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.TestFixtures;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurement;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurementType;
import java.time.Instant;
import java.util.UUID;
import org.junit.Test;

public class FeatureMeasurementDtoTests {

  private UUID id = UUID.randomUUID();
  private FeatureMeasurementType featureMeasurementType = FeatureMeasurementType.ARRIVAL_TIME;
  private double featureMeasurementValue = (double) Instant.now().toEpochMilli();
  private final UUID creationInfoId = UUID.randomUUID();

  @Test
  public void testSerialization() throws Exception {
    FeatureMeasurement featureMeasurement = FeatureMeasurement.from(
        id, featureMeasurementType, featureMeasurementValue, creationInfoId);

    String json = TestFixtures.objMapper.writeValueAsString(featureMeasurement);
    assertNotNull(json);
    assertTrue(json.length() > 0);

    FeatureMeasurement deserializedFeatureMeasurement = callDeserializer(json);
    assertNotNull(deserializedFeatureMeasurement);
    assertEquals(featureMeasurement, deserializedFeatureMeasurement);
  }

  private static FeatureMeasurement callDeserializer(String json) throws Exception {
    return TestFixtures.objMapper.readValue(json, FeatureMeasurement.class);
  }

}
