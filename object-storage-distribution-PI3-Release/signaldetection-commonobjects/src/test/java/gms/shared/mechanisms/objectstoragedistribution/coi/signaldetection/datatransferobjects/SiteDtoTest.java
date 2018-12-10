package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.datatransferobjects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.TestFixtures;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Site;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;


/**
 * Test the Site DTO.
 */
public class SiteDtoTest {

    private final Site obj = TestFixtures.site;
    private String json;

    @Before
    public void setup() throws Exception {
        assertNotNull(obj);
        json = TestFixtures.objMapper.writeValueAsString(obj);
        assertNotNull(json);
        assertTrue(json.length() > 0);
    }

    /**
     * Tests that a COI object can be serialized and deserialized.
     * @throws Exception
     */
    @Ignore
    public void deserializeTest() throws Exception {
        Site deserialObj = callDeserializer(json);
        assertNotNull(deserialObj);
        assertEquals(deserialObj, obj);
    }

    private static Site callDeserializer(String json) throws Exception {
        return TestFixtures.objMapper.readValue(json, Site.class);
    }
}
