package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.datatransferobjects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.TestFixtures;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Channel;
import org.junit.Before;
import org.junit.Test;


/**
 * Created by jwvicke on 10/12/17.
 */
public class ChannelDtoTest {

    private final Channel channel = TestFixtures.processingChannel01;
    private String channelJson;

    @Before
    public void setup() throws Exception {
        assertNotNull(channel);
        channelJson = TestFixtures.objMapper.writeValueAsString(channel);
        assertNotNull(channelJson);
        assertTrue(channelJson.length() > 0);
    }

    /**
     * Tests that a ChannelDto created from a ProcessingChannel can be serialized and deserialized,
     * and still remain equal to both the original ChannelDto and the
     * ProcessingChannel from which it was created.
     * @throws Exception
     */
    @Test
    public void deserializeTest() throws Exception {
        Channel deserializedChannel = callDeserializer(channelJson);
        assertNotNull(deserializedChannel);
        assertEquals(deserializedChannel, channel);
    }

    private static Channel callDeserializer(String json) throws Exception {
        return TestFixtures.objMapper.readValue(json, Channel.class);
    }
}
