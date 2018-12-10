package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.datatransferobjects;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.TestFixtures;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import org.junit.Test;


/**
 * Tests the ability to serialize and deserialize the Waveform
 * class using the ObjectSerializationUtility with the registered WaveformDto mix-in.
 * Also check that malformed JSON (null fields) cause exceptions on deserialization.
 */
public class WaveformDtoTest {

    /**
     * Tests that a WaveformDto created from a Waveform can be serialized and deserialized,
     * and still remain equal to both the original WaveformDto and the Waveform from which it was created.
     * @throws Exception
     */
    @Test
    public void serializationTest() throws Exception {
        String json = TestFixtures.objMapper.writeValueAsString(TestFixtures.waveform1);
        assertNotNull(json);
        assertTrue(json.length() > 0);

        Waveform deserializedWaveform = TestFixtures.objMapper.readValue(json, Waveform.class);
        assertNotNull(deserializedWaveform);
        assertTrue(deserializedWaveform.equals(TestFixtures.waveform1));
    }
}