package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.datatransferobjects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.TestFixtures;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FilterCausality;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FilterDefinition;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FilterPassBandType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FilterSource;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FilterType;
import org.junit.Test;

/**
 * Tests the serialization and deserialization of {@link FilterDefinition} objects operations
 * to/from {@link FilterDefinitionDto} objects.
 */
public class FilterDefinitionDtoTests {

  @Test
  public void testSerialization() throws Exception {
    FilterDefinition filterDefinition = FilterDefinition
        .createFir("filter name string", "filter description", FilterType.FIR_HAMMING,
            FilterPassBandType.BAND_PASS, 12.34, 99.99, 12, FilterSource.USER,
            FilterCausality.CAUSAL, false, 42.42, 24.24, new double[]{-12.34, 57.89, 64.0},
            4687.3574);

    String json = TestFixtures.objMapper.writeValueAsString(filterDefinition);
    assertNotNull(json);
    assertTrue(json.length() > 0);

    FilterDefinition deserialized = TestFixtures.objMapper.readValue(json, FilterDefinition.class);
    assertNotNull(deserialized);
    assertEquals(filterDefinition, deserialized);
  }

}
