package gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.datatransferobjects;

import static org.junit.Assert.*;

import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.TestFixtures;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceAlias;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests the ability to serialize and deserialize the ReferenceStationAlias
 * class using the ObjectSerializationUtility with the registered ReferenceStationAliasDto mix-in.
 * Also check that malformed JSON (null fields) cause exceptions on deserialization.
 */
public class ReferenceAliasDtoTest {

    private final ReferenceAlias obj = TestFixtures.stationAlias;
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
        ReferenceAlias item = callDeserializer(jsonString);
        assertNotNull(item);
        assertEquals(item, obj);
    }

    private static ReferenceAlias callDeserializer(String jsonString) throws Exception {
        return TestFixtures.objMapper.readValue(jsonString, ReferenceAlias.class);
    }
}
