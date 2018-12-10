package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.datatransferobjects;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.TestFixtures;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSohAnalog;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests the ability to serialize and deserialize the AcquiredChannelSohAnalog
 * class using the ObjectSerializationUtility with the registered AcquiredChannelSohAnalogDto mix-in.
 * Also check that malformed JSON (null fields) cause exceptions on deserialization.
 */
public class AcquiredChannelSohAnalogDtoTest {

    private final AcquiredChannelSohAnalog soh = TestFixtures.channelSohAnalog;
    private String sohJson;

    @Before
    public void setup() throws Exception {
        assertNotNull(soh);
        sohJson = TestFixtures.objMapper.writeValueAsString(soh);
        assertNotNull(sohJson);
        assertTrue(sohJson.length() > 0);
    }

    /**
     * Tests that a AcquiredChannelSohAnalog  can be serialized and deserialized,
     * and still remain equal to the original AcquiredChannelSohBoolean.
     * @throws Exception
     */
    @Test
    public void deserializeTest() throws Exception {
        AcquiredChannelSohAnalog deserializedSoh = callDeserializer(sohJson);
        assertNotNull(deserializedSoh);
        assertTrue(deserializedSoh.equals(soh));
    }

    private static AcquiredChannelSohAnalog callDeserializer(String json) throws Exception {
        return TestFixtures.objMapper.readValue(json, AcquiredChannelSohAnalog.class);
    }

}
