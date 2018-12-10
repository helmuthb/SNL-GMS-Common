package gms.core.waveformqc.waveformsignalqc.algorithm;

import static org.junit.Assert.assertEquals;

import java.time.Instant;
import java.util.UUID;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class WaveformRepeatedAmplitudeQcMaskTests {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Test
  public void testCreate() throws Exception {
    Instant start = Instant.EPOCH;
    Instant end = Instant.MAX;
    UUID channelUuid = UUID.randomUUID();
    UUID channelSegmentUuid = UUID.randomUUID();
    WaveformRepeatedAmplitudeQcMask mask = WaveformRepeatedAmplitudeQcMask
        .create(start, end, channelUuid, channelSegmentUuid);

    assertEquals(start, mask.getStartTime());
    assertEquals(end, mask.getEndTime());
    assertEquals(channelUuid, mask.getChannelId());
    assertEquals(channelSegmentUuid, mask.getChannelSegmentId());
  }

  @Test
  public void testNullMaskParameterExceptionStartTime() throws Exception {
    exception.expect(NullPointerException.class);
    exception.expectMessage("WaveformRepeatedAmplitudeQcMask cannot have null startTime");
    WaveformRepeatedAmplitudeQcMask
        .create(null, Instant.ofEpochSecond(2), UUID.randomUUID(), UUID.randomUUID());
  }

  @Test
  public void testNullMaskParameterExceptionEndTime() throws Exception {
    exception.expect(NullPointerException.class);
    exception.expectMessage("WaveformRepeatedAmplitudeQcMask cannot have null endTime");
    WaveformRepeatedAmplitudeQcMask
        .create(Instant.ofEpochSecond(1), null, UUID.randomUUID(), UUID.randomUUID());
  }

  @Test
  public void testNullMaskParameterExceptionChannelId() throws Exception {
    exception.expect(NullPointerException.class);
    exception.expectMessage("WaveformRepeatedAmplitudeQcMask cannot have null channelId");
    WaveformRepeatedAmplitudeQcMask
        .create(Instant.ofEpochSecond(1), Instant.ofEpochSecond(2), null, UUID.randomUUID());
  }

  @Test
  public void testNullMaskParameterExceptionChannelSegmentId() throws Exception {
    exception.expect(NullPointerException.class);
    exception.expectMessage("WaveformRepeatedAmplitudeQcMask cannot have null channelSegmentId");
    WaveformRepeatedAmplitudeQcMask
        .create(Instant.ofEpochSecond(1), Instant.ofEpochSecond(2), UUID.randomUUID(), null);
  }

  @Test
  public void testStartTimeAfterEndTimeExpectIllegalArgumentException() throws Exception {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("WaveformRepeatedAmplitudeQcMask startTime must be before endTime");
    WaveformRepeatedAmplitudeQcMask
        .create(Instant.ofEpochSecond(2), Instant.ofEpochSecond(1), UUID.randomUUID(),
            UUID.randomUUID());
  }
}
