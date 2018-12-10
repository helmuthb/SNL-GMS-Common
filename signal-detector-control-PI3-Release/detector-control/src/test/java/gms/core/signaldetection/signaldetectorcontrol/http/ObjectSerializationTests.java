package gms.core.signaldetection.signaldetectorcontrol.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import gms.core.signaldetection.signaldetectorcontrol.TestFixtures;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment.ChannelSegmentType;
import java.time.Instant;
import java.util.TreeSet;
import java.util.UUID;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ObjectSerializationTests {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  /**
   * A class that Jackson can't serialize.  Used to test exception conditions.
   */
  private class Foo {

    private final int param;

    private Foo(int param) {
      this.param = param;
    }
  }

  /**
   * Sample {@link StreamingDto} used in the serialization and deserialization tests
   */
  private StreamingDto streamingDto;

  /**
   * Sample {@link ClaimCheckDto} used in the serialization and deserialization tests
   */
  private ClaimCheckDto claimCheckDto;

  @Before
  public void setUp() {
    streamingDto = TestFixtures.getStreamingDto();
    claimCheckDto = TestFixtures.getClaimCheckDto();
  }

  @Test
  public void testStreamingDtoJsonSerialization() {
    byte[] json = ObjectSerialization.writeJson(streamingDto);
    assertNotNull(json);
    assertTrue(json.length > 0);

    StreamingDto deserialized = ObjectSerialization.readJson(json, StreamingDto.class);
    assertNotNull(deserialized);
    assertEquals(streamingDto, deserialized);
  }

  @Test
  public void testClaimCheckDtoJsonSerialization() {
    byte[] json = ObjectSerialization.writeJson(claimCheckDto);
    assertNotNull(json);
    assertTrue(json.length > 0);

    ClaimCheckDto deserialized = ObjectSerialization.readJson(json, ClaimCheckDto.class);
    assertNotNull(deserialized);
    assertEquals(claimCheckDto, deserialized);
  }

  @Test
  public void testJsonSerializationErrorExpectIllegalArgumentException() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Unable to serialize object to json");
    ObjectSerialization.writeJson(new Foo(99));
  }

  @Test
  public void testJsonSerializeNullExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("Unable to serialize null to json");
    ObjectSerialization.writeJson(null);
  }

  @Test
  public void testJsonParseErrorExpectIllegalArgumentException() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Unable to deserialize object from json");
    ObjectSerialization.readJson("{bad json}".getBytes(), Foo.class);
  }

  @Test
  public void testJsonParseNullExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("Unable to deserialize null json");
    ObjectSerialization.readJson(null, Foo.class);
  }

  @Test
  public void testJsonParseNullTypeExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("Unable to deserialize to null class type");
    ObjectSerialization.readJson("{-100}".getBytes(), null);
  }

  @Test
  public void testStreamingDtoMessagePackSerialization() {
    byte[] messagePack = ObjectSerialization.writeMessagePack(streamingDto);
    assertNotNull(messagePack);
    assertTrue(messagePack.length > 0);

    StreamingDto deserialized = ObjectSerialization
        .readMessagePack(messagePack, StreamingDto.class);
    assertNotNull(deserialized);
    assertEquals(streamingDto, deserialized);
  }

  @Test
  public void testMessagePackSerializationErrorExpectIllegalArgumentException() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Unable to serialize object to MessagePack");
    ObjectSerialization.writeMessagePack(new Foo(99));
  }

  @Test
  public void testMessagePackSerializeNullExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("Unable to serialize null to MessagePack");
    ObjectSerialization.writeMessagePack(null);
  }

  @Test
  public void testMessagePackParseErrorExpectIllegalArgumentException() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Unable to deserialize object from MessagePack");
    ObjectSerialization.readMessagePack(new byte[]{1}, Foo.class);
  }

  @Test
  public void testMessagePackParseNullExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("Unable to deserialize null MessagePack");
    ObjectSerialization.readMessagePack(null, Foo.class);
  }

  @Test
  public void testMessagePackParseNullTypeExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("Unable to deserialize to null class type");
    ObjectSerialization.readMessagePack(new byte[]{1}, null);
  }

  @Test
  public void testJsonClientObjectMapper() {
    final ChannelSegment channelSegment = createMockChannelSegment(UUID.randomUUID(), Instant.EPOCH,
        Instant.EPOCH.plusSeconds(300));

    com.mashape.unirest.http.ObjectMapper jsonMapper = ObjectSerialization
        .getJsonClientObjectMapper();

    assertEquals(channelSegment,
        jsonMapper.readValue(jsonMapper.writeValue(channelSegment), ChannelSegment.class));
  }

  private ChannelSegment createMockChannelSegment(UUID channelIdA, Instant startTime,
      Instant endTime) {
    return ChannelSegment
        .create(channelIdA, "mockSegment", ChannelSegmentType.ACQUIRED, startTime, endTime,
            new TreeSet<>(), CreationInfo.DEFAULT);
  }
}
