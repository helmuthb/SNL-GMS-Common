package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.datatransferobjects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.TestFixtures;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import org.junit.Before;
import org.junit.Test;


/**
 * Tests the ability to serialize and deserialize the ChannelSegment
 * class using the ObjectSerializationUtility with the registered ChannelSegmentDto mix-in.
 * Also check that malformed JSON (null fields) cause exceptions on deserialization.
 */
public class ChannelSegmentDtoTest {

    private final ChannelSegment segment = TestFixtures.channelSegment;
    private String segmentJson;

    @Before
    public void setup() throws Exception {
        assertNotNull(segment);
        segmentJson = TestFixtures.objMapper.writeValueAsString(segment);
        assertNotNull(segmentJson);
        assertTrue(segmentJson.length() > 0);
    }

    /**
     * Tests that a ChannelSegmentDto created from a ChannelSegment can be serialized and deserialized,
     * and still remain equal to both the original ChannelSegmentDto and the ChannelSegment from which it was created.
     * @throws Exception
     */
    @Test
    public void deserializeTest() throws Exception {
        ChannelSegment deserializedSegment = callDeserializer(segmentJson);
        assertNotNull(deserializedSegment);
        assertEquals(deserializedSegment, segment);
    }

    private static ChannelSegment callDeserializer(String json) throws Exception {
        return TestFixtures.objMapper.readValue(json, ChannelSegment.class);
    }
}
