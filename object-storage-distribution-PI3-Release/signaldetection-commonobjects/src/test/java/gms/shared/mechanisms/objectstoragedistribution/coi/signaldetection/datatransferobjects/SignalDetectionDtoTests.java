package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.datatransferobjects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.TestFixtures;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurement;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurementTests;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetection;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetectionHypothesis;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetectionHypothesisTests;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Station;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.Test;

/**
 * Tests the serialization and deserialization of {@link SignalDetection} objects operations
 * to/from {@link SignalDetectionDto} objects.
 */
public class SignalDetectionDtoTests {

  private final UUID id = UUID.randomUUID();
  private String monitoringOrganization = "CTBTO";
  private UUID stationId = UUID.randomUUID();
  private final UUID creationInfoId = UUID.randomUUID();

  private String phase = "Pn";
  private List<FeatureMeasurement> featureMeasurements =
      Arrays.asList(new FeatureMeasurementTests().getTestFeatureMeasurement());

  @Test
  public void testSerialization() throws Exception {
    SignalDetection signalDetection = SignalDetection.from(
        id, monitoringOrganization, stationId, Collections.emptyList(), creationInfoId);

    signalDetection.addSignalDetectionHypothesis(phase, featureMeasurements, creationInfoId);

    String json = TestFixtures.objMapper.writeValueAsString(signalDetection);
    assertNotNull(json);
    assertTrue(json.length() > 0);

    SignalDetection deserializedSignalDetection = callDeserializer(json);
    assertNotNull(deserializedSignalDetection);
    assertEquals(signalDetection, deserializedSignalDetection);
  }

  private static SignalDetection callDeserializer(String json) throws Exception {
    return TestFixtures.objMapper.readValue(json, SignalDetection.class);
  }
}
