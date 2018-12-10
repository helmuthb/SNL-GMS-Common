package gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.datatransferobjects;

import static gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.TestFixtures.network;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.TestFixtures;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceDigitizer;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests the ability to serialize and deserialize the ReferenceDigitizer
 * class using the ObjectSerializationUtility with the registered ReferenceDigitizerDto mix-in.
 * Also check that malformed JSON (null fields) cause exceptions on deserialization.
 */
public class ReferenceDigitizerDtoTest {

    private final ReferenceDigitizer obj = TestFixtures.digitizer;
    private String segmentJson;

    @Before
    public void setup() throws Exception {
        assertNotNull(obj);
        segmentJson = TestFixtures.objMapper.writeValueAsString(obj);
        System.out.println(segmentJson);
        assertNotNull(segmentJson);
        assertTrue(segmentJson.length() > 0);
    }


    @Test
    public void deserializeTest() throws Exception {
        ReferenceDigitizer item = callDeserializer(segmentJson);
        assertNotNull(item);
        assertEquals(item, obj);
    }

    private static ReferenceDigitizer callDeserializer(String json) throws Exception {
        return TestFixtures.objMapper.readValue(json, ReferenceDigitizer.class);
    }
}
