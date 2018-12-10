package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.datatransferobjects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.TestFixtures;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.RawStationDataFrame;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests the ability to serialize and deserialize the RawStationDataFrame
 * class using the ObjectSerializationUtility with the registered RawStationDataFrameDto mix-in.
 * Also check that malformed JSON (null fields) cause exceptions on deserialization.
 */
public class RawStationDataFrameDtoTest {

    private final RawStationDataFrame rawStationDataFrame = TestFixtures.rawStationDataFrame;
    private String rawStationDataFrameJson;

    @Before
    public void setup() throws Exception {
        assertNotNull(rawStationDataFrame);
        rawStationDataFrameJson = TestFixtures.objMapper.writeValueAsString(rawStationDataFrame);
        assertNotNull(rawStationDataFrameJson);
        assertTrue(rawStationDataFrameJson.length() > 0);
    }

    /**
     * Tests that a RawStationDataFrameDto created from a RawStationDataFrame can be serialized and
     * deserialized, and still remain equal to both the original RawStationDataFrameDto and the
     * RawStationDataFrame from which it was created.
     * @throws Exception
     */
    @Test
    public void deserializeTest() throws Exception {
        RawStationDataFrame deserializedRawStationDataFrame = callDeserializer(rawStationDataFrameJson);
        assertNotNull(deserializedRawStationDataFrame);
        assertEquals(deserializedRawStationDataFrame, rawStationDataFrame);
    }

    private static RawStationDataFrame callDeserializer(String json) throws Exception {
        return TestFixtures.objMapper.readValue(json, RawStationDataFrame.class);
    }

}
