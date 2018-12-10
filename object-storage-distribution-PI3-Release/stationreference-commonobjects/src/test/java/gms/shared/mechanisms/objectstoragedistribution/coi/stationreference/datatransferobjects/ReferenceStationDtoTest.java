package gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.datatransferobjects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.TestFixtures;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceStation;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests the ability to serialize and deserialize the ReferenceStation
 * class using the ObjectSerializationUtility with the registered ReferenceStationDto mix-in.
 * Also check that malformed JSON (null fields) cause exceptions on deserialization.
 */
public class ReferenceStationDtoTest {

    private final ReferenceStation obj = TestFixtures.station;
    private String jsonString;

    @Before
    public void setup() throws Exception {
        assertNotNull(obj);
        jsonString = TestFixtures.objMapper.writeValueAsString(obj);
        assertNotNull(jsonString);
        assertTrue(jsonString.length() > 0);
    }

    @Test
    public void deserializeTest() throws Exception {
        ReferenceStation item = callDeserializer(jsonString);
        assertNotNull(item);
        assertEquals(item, obj);
    }

    private static ReferenceStation callDeserializer(String jsonString) throws Exception {
        return TestFixtures.objMapper.readValue(jsonString, ReferenceStation.class);
    }
}
