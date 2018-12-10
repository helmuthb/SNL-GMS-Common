package gms.core.waveformqc.waveformsignalqc.algorithm;

import static org.junit.Assert.assertEquals;

import java.time.Instant;
import java.util.UUID;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class WaveformSpike3PtQcMaskTests {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Test
  public void testCreate() throws Exception {
    Instant spikeTime = Instant.ofEpochSecond(2);

    UUID channelUuid = UUID.randomUUID();
    UUID channelSegmentUuid = UUID.randomUUID();
    WaveformSpike3PtQcMask mask = WaveformSpike3PtQcMask
        .create(channelUuid, channelSegmentUuid, Instant.ofEpochSecond(2));

    assertEquals(spikeTime, mask.getStartTime());
    assertEquals(spikeTime, mask.getEndTime());
    assertEquals(channelUuid, mask.getChannelId());
    assertEquals(channelSegmentUuid, mask.getChannelSegmentId());
  }

  @Test
  public void testNullMaskParameterExceptionChannelId() throws Exception {
    exception.expect(NullPointerException.class);
    exception.expectMessage("WaveformSpike3PtQcMask cannot have null channelId");
    WaveformSpike3PtQcMask
        .create(null, UUID.randomUUID(), Instant.ofEpochSecond(2));
  }

  @Test
  public void testNullMaskParameterExceptionChannelSegmentId() throws Exception {
    exception.expect(NullPointerException.class);
    exception.expectMessage("WaveformSpike3PtQcMask cannot have null channelSegmentId");
    WaveformSpike3PtQcMask
        .create(UUID.randomUUID(), null, Instant.ofEpochSecond(2));
  }

  @Test
  public void testNullMaskParameterExceptionSpikeTime() throws Exception {
    exception.expect(NullPointerException.class);
    exception.expectMessage("WaveformSpike3PtQcMask cannot have null spikeTime");
    WaveformSpike3PtQcMask
        .create(UUID.randomUUID(), UUID.randomUUID(), null);
  }

}
