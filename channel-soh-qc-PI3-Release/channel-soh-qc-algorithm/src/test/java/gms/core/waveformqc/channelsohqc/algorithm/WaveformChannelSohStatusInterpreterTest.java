package gms.core.waveformqc.channelsohqc.algorithm;

import gms.core.waveformqc.waveformqccontrol.objects.ChannelSohSubtype;
import gms.core.waveformqc.waveformqccontrol.objects.WaveformQcChannelSohStatus;
import gms.core.waveformqc.waveformqccontrol.objects.WaveformQcChannelSohStatus.Status;
import gms.core.waveformqc.waveformqccontrol.objects.WaveformQcChannelSohStatus.StatusState;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskType;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Tests the Class {@link WaveformChannelSohStatusInterpreter}
 */
public class WaveformChannelSohStatusInterpreterTest {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  private static final int statusLengthSecs = 10;
  private static final QcMaskType maskType = QcMaskType.TIMING;
  private static final ChannelSohSubtype maskSubtype = ChannelSohSubtype.CLOCK_DIFFERENTIAL_OVER_THRESHOLD;

  @Test
  public void testCreateMasksStartsFalse() throws Exception {
    WaveformQcChannelSohStatus status = createMockWaveformQcChannelSohStatus(false, 10,
        new UUID( 0L , 0L )
    );

    List<ChannelSohQcMask> sohQcMasks = WaveformChannelSohStatusInterpreter
        .createChannelSohQcMasks(status);

    Assert.assertEquals(5, sohQcMasks.size());

    for (int i = 0; i < 5; i++) {
      ChannelSohQcMask sohQcMask = sohQcMasks.get(i);
      verifyChannelSohQcMask(sohQcMask,
          Instant.ofEpochSecond(statusLengthSecs * (2 * i + 1)),
          Instant.ofEpochSecond(statusLengthSecs * (2 * i + 2)));
    }
  }

  @Test
  public void testCreateMasksStartsTrue() throws Exception {
    WaveformQcChannelSohStatus status = createMockWaveformQcChannelSohStatus(true, 10,
        new UUID( 0L , 0L )
    );

    List<ChannelSohQcMask> sohQcMasks = WaveformChannelSohStatusInterpreter
        .createChannelSohQcMasks(status);

    Assert.assertEquals(5, sohQcMasks.size());

    for (int i = 0; i < 5; i++) {
      ChannelSohQcMask sohQcMask = sohQcMasks.get(i);
      verifyChannelSohQcMask(sohQcMask,
          Instant.ofEpochSecond(statusLengthSecs * (2 * i)),
          Instant.ofEpochSecond(statusLengthSecs * (2 * i + 1)));
    }
  }

  @Test
  public void testCreateMasksStartsTrueOnlyOneStatus() throws Exception {
    WaveformQcChannelSohStatus status = createMockWaveformQcChannelSohStatus(true, 1,
        new UUID( 0L , 0L )
    );

    List<ChannelSohQcMask> sohQcMasks = WaveformChannelSohStatusInterpreter
        .createChannelSohQcMasks(status);

    Assert.assertEquals(1, sohQcMasks.size());
    ChannelSohQcMask sohQcMask = sohQcMasks.get(0);
    verifyChannelSohQcMask(sohQcMask, Instant.ofEpochSecond(0),
        Instant.ofEpochSecond(statusLengthSecs));
  }

  @Test
  public void testCreateMasksStartsFalseOnlyOneStatus() throws Exception {
    WaveformQcChannelSohStatus status = createMockWaveformQcChannelSohStatus(false, 1,
        new UUID( 0L , 0L )
    );

    List<ChannelSohQcMask> sohQcMasks = WaveformChannelSohStatusInterpreter
        .createChannelSohQcMasks(status);

    Assert.assertEquals(0, sohQcMasks.size());
  }

  @Test
  public void testCreateMasksIgnoresMissingStatus() throws Exception {
    List<Status> statusChanges = Arrays.asList(
        Status.create(Instant.ofEpochSecond(0), Instant.ofEpochSecond(statusLengthSecs),
            StatusState.SET),
        Status.create(Instant.ofEpochSecond(statusLengthSecs),
            Instant.ofEpochSecond(2 * statusLengthSecs), StatusState.MISSING),
        Status.create(Instant.ofEpochSecond(2 * statusLengthSecs),
            Instant.ofEpochSecond(3 * statusLengthSecs), StatusState.UNSET),
        Status.create(Instant.ofEpochSecond(3 * statusLengthSecs),
            Instant.ofEpochSecond(4 * statusLengthSecs), StatusState.MISSING),
        Status.create(Instant.ofEpochSecond(4 * statusLengthSecs),
            Instant.ofEpochSecond(5 * statusLengthSecs), StatusState.SET)
    );
    WaveformQcChannelSohStatus status = WaveformQcChannelSohStatus
        .from(new UUID( 0L , 0L ), maskType, maskSubtype, statusChanges);

    List<ChannelSohQcMask> sohQcMasks = WaveformChannelSohStatusInterpreter
        .createChannelSohQcMasks(status);

    Assert.assertEquals(2, sohQcMasks.size());
    verifyChannelSohQcMask(sohQcMasks.get(0), Instant.ofEpochSecond(0),
        Instant.ofEpochSecond(statusLengthSecs));
    verifyChannelSohQcMask(sohQcMasks.get(1), Instant.ofEpochSecond(4 * statusLengthSecs),
        Instant.ofEpochSecond(5 * statusLengthSecs));
  }

  @Test
  public void testCreateMasksNullExpectNullPointerExceptions() throws Exception {
    exception.expect(NullPointerException.class);
    exception.expectMessage(
        "WaveformChannelSohStatusInterpreter createChannelSohQcMasks cannot accept null WaveformQcChannelSohStatus");
    WaveformChannelSohStatusInterpreter.createChannelSohQcMasks(null);
  }

  private static void verifyChannelSohQcMask(ChannelSohQcMask sohQcMask, Instant startTime,
      Instant endTime) {

    Assert.assertEquals(maskType, sohQcMask.getQcMaskType());
    Assert.assertEquals(maskSubtype, sohQcMask.getChannelSohSubtype());
    Assert.assertEquals(startTime, sohQcMask.getStartTime());
    Assert.assertEquals(endTime, sohQcMask.getEndTime());
  }

  private static WaveformQcChannelSohStatus createMockWaveformQcChannelSohStatus(
      boolean startingStatus, int numStatuses, UUID processingChannelId) {

    boolean currentStatus = startingStatus;

    WaveformQcChannelSohStatus.Builder builder = WaveformQcChannelSohStatus
        .builder(processingChannelId, maskType, maskSubtype, Instant.ofEpochSecond(0),
            Instant.ofEpochSecond(statusLengthSecs), currentStatus, Duration.ofSeconds(5));

    currentStatus = !currentStatus;
    for (int i = 1; i < numStatuses; i++) {
      builder.addStatusChange(Instant.ofEpochSecond(i * statusLengthSecs),
          Instant.ofEpochSecond((i + 1) * statusLengthSecs), currentStatus);
      currentStatus = !currentStatus;
    }

    return builder.build();
  }
}