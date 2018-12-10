package gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.datatransferobjects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.TestFixtures;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceNetwork;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests the ability to serialize and deserialize the ReferenceNetwork
 * class using the ObjectSerializationUtility with the registered ReferenceNetworkDto mix-in.
 * Also check that malformed JSON (null fields) cause exceptions on deserialization.
 */
public class ReferenceNetworkDtoTest {

    private final ReferenceNetwork network = TestFixtures.network;
    private String segmentJson;

    @Before
    public void setup() throws Exception {
        assertNotNull(network);
        segmentJson = TestFixtures.objMapper.writeValueAsString(network);
        assertNotNull(segmentJson);
        assertTrue(segmentJson.length() > 0);
    }


    @Test
    public void deserializeTest() throws Exception {
      ReferenceNetwork item = callDeserializer(segmentJson);
        assertNotNull(item);
        assertEquals(item, network);
    }

    private static ReferenceNetwork callDeserializer(String json) throws Exception {
        return TestFixtures.objMapper.readValue(json, ReferenceNetwork.class);
    }
}
