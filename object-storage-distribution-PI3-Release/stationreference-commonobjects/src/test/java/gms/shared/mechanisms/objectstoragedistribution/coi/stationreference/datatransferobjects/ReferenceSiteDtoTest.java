package gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.datatransferobjects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.TestFixtures;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceSite;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceStation;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests the ability to serialize and deserialize the ReferenceSite
 * class using the ObjectSerializationUtility with the registered ReferenceSiteDto mix-in.
 * Also check that malformed JSON (null fields) cause exceptions on deserialization.
 */
public class ReferenceSiteDtoTest {

    private final ReferenceSite obj = TestFixtures.site;
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
        ReferenceSite item = callDeserializer(jsonString);
        assertNotNull(item);
        assertEquals(item, obj);
    }

    private static ReferenceSite callDeserializer(String jsonString) throws Exception {
        return TestFixtures.objMapper.readValue(jsonString, ReferenceSite.class);
    }
}
