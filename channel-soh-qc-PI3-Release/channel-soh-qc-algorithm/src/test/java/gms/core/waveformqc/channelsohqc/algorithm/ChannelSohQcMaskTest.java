package gms.core.waveformqc.channelsohqc.algorithm;

import gms.core.waveformqc.waveformqccontrol.objects.ChannelSohSubtype;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskType;
import java.time.Instant;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Tests the Class {@link ChannelSohQcMask}
 */
public class ChannelSohQcMaskTest {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Test
  public void testChannelSohQcMaskCreation() {
    ChannelSohQcMask testChannelSohQcMask = ChannelSohQcMask
        .create(QcMaskType.TIMING, ChannelSohSubtype.CLOCK_DIFFERENTIAL_OVER_THRESHOLD,
            Instant.ofEpochSecond(1), Instant.ofEpochSecond(2));

    Assert.assertEquals(QcMaskType.TIMING, testChannelSohQcMask.getQcMaskType());
    Assert.assertEquals(ChannelSohSubtype.CLOCK_DIFFERENTIAL_OVER_THRESHOLD,
        testChannelSohQcMask.getChannelSohSubtype());
    Assert.assertEquals(Instant.ofEpochSecond(1), testChannelSohQcMask.getStartTime());
    Assert.assertEquals(Instant.ofEpochSecond(2), testChannelSohQcMask.getEndTime());

  }

  @Test
  public void testNullMaskParameterExceptionMaskType() throws Exception {
    exception.expectMessage("Error creating ChannelSohQcMask: qcMaskType cannot be null");
    ChannelSohQcMask
        .create(null, ChannelSohSubtype.CLOCK_DIFFERENTIAL_OVER_THRESHOLD,
            Instant.ofEpochSecond(1), Instant.ofEpochSecond(2));
  }

  @Test
  public void testNullMaskParameterExceptionChannelSubType() throws Exception {
    exception.expectMessage("Error creating ChannelSohQcMask: channelSohSubtype cannot be null");
    ChannelSohQcMask
        .create(QcMaskType.TIMING, null,
            Instant.ofEpochSecond(1), Instant.ofEpochSecond(2));
  }

  @Test
  public void testNullMaskParameterExceptionStartTime() throws Exception {
    exception.expectMessage("Error creating ChannelSohQcMask: startTime cannot be null");
    ChannelSohQcMask
        .create(QcMaskType.TIMING, ChannelSohSubtype.CLOCK_DIFFERENTIAL_OVER_THRESHOLD,
            null, Instant.ofEpochSecond(2));
  }

  @Test
  public void testNullMaskParameterExceptionEndTime() throws Exception {
    exception.expectMessage("Error creating ChannelSohQcMask: endTime cannot be null");
    ChannelSohQcMask
        .create(QcMaskType.TIMING, ChannelSohSubtype.CLOCK_DIFFERENTIAL_OVER_THRESHOLD,
            Instant.ofEpochSecond(1), null);
  }

  @Test
  public void testEndBeforeStartTimeMaskParameterException() throws Exception {
    exception.expectMessage("Error creating ChannelSohQcMask: startTime must be before endTime");
    ChannelSohQcMask
        .create(QcMaskType.TIMING, ChannelSohSubtype.CLOCK_DIFFERENTIAL_OVER_THRESHOLD,
            Instant.ofEpochSecond(3), Instant.ofEpochSecond(2));
  }

}
