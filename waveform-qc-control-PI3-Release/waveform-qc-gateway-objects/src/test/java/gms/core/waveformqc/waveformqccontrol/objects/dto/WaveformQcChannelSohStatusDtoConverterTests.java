package gms.core.waveformqc.waveformqccontrol.objects.dto;

import static org.junit.Assert.assertEquals;

import gms.core.waveformqc.waveformqccontrol.objects.ChannelSohSubtype;
import gms.core.waveformqc.waveformqccontrol.objects.WaveformQcChannelSohStatus;
import gms.core.waveformqc.waveformqccontrol.objects.WaveformQcChannelSohStatus.Builder;
import gms.core.waveformqc.waveformqccontrol.objects.WaveformQcChannelSohStatus.Status;
import gms.core.waveformqc.waveformqccontrol.objects.WaveformQcChannelSohStatus.StatusState;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskType;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Tests the WaveformQcChannelSohStatusDtoConverter
 */
public class WaveformQcChannelSohStatusDtoConverterTests {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  // Convenience fields used by multiple tests
  private final UUID processingChannelId = UUID.randomUUID();
  private final QcMaskType qcMaskType = QcMaskType.SENSOR_PROBLEM;
  private final ChannelSohSubtype channelSohSubtype = ChannelSohSubtype.CLIPPED;
  private final Instant[] startTimes = new Instant[]{Instant.now(),
      Instant.now().plusSeconds(10), Instant.now().plusSeconds(30), Instant.now().plusSeconds(40)};
  private final Instant[] endTimes = Arrays.stream(startTimes)
      .map(i -> i.plusSeconds(10)).toArray(Instant[]::new);

  // Different expected start and end times than provided times since there is a missing status
  private final Instant[] expectedStartTimes = new Instant[]{startTimes[0], startTimes[1],
      endTimes[1], startTimes[2], startTimes[3]};
  private final Instant[] expectedEndTimes = new Instant[]{endTimes[0], endTimes[1], startTimes[2],
      endTimes[2], endTimes[3]};

  private final Boolean[] status = new Boolean[]{true, false, true, false};
  private final StatusState[] expectedStatusState = new StatusState[]{StatusState.SET,
      StatusState.UNSET, StatusState.MISSING, StatusState.SET, StatusState.UNSET};

  private final Duration threshold = Duration.ofSeconds(5);

  // Constructed before each test
  private WaveformQcChannelSohStatus waveformQcChannelSohStatus;

  @Before
  public void setUp() throws Exception {
    Builder builder = WaveformQcChannelSohStatus
        .builder(processingChannelId, qcMaskType, channelSohSubtype, startTimes[0],
            endTimes[0], status[0], threshold);

    for (int i = 1; i < startTimes.length; ++i) {
      builder.addStatusChange(startTimes[i], endTimes[i], status[i]);
    }

    waveformQcChannelSohStatus = builder.build();
  }

  /**
   * Tests constructing a {@link WaveformQcChannelSohStatusDto} from a {@link
   * WaveformQcChannelSohStatus}
   */
  @Test
  public void testToDto() throws Exception {
    WaveformQcChannelSohStatusDto dto = WaveformQcChannelSohStatusDtoConverter
        .toDto(waveformQcChannelSohStatus);

    assertEquals(processingChannelId, dto.getProcessingChannelId());
    assertEquals(qcMaskType, dto.getQcMaskType());
    assertEquals(channelSohSubtype, dto.getChannelSohSubtype());

    List<WaveformQcChannelSohStatusDto.StatusChangeDto> statusChanges = dto.getStatusChanges();
    assertEquals(expectedStatusState.length, statusChanges.size());

    for (int i = 0; i < 4; ++i) {
      assertEquals(expectedStartTimes[i], statusChanges.get(i).getStartTime());
      assertEquals(expectedEndTimes[i], statusChanges.get(i).getEndTime());
      assertEquals(expectedStatusState[i], statusChanges.get(i).getStatus());
    }
  }

  @Test
  public void testToDtoNullExpectNullPointerException() throws Exception {
    exception.expect(NullPointerException.class);
    exception.expectMessage(
        "Cannot create WaveformQcChannelSohStatusDto from a null WaveformQcChannelSohStatus");
    WaveformQcChannelSohStatusDtoConverter.toDto(null);
  }

  /**
   * Tests constructing a {@link WaveformQcChannelSohStatus} from a {@link
   * WaveformQcChannelSohStatusDto}
   */
  @Test
  public void testFromDto() throws Exception {
    WaveformQcChannelSohStatusDto dto = new WaveformQcChannelSohStatusDto();
    dto.setProcessingChannelId(processingChannelId);
    dto.setQcMaskType(qcMaskType);
    dto.setChannelSohSubtype(channelSohSubtype);

    List<WaveformQcChannelSohStatusDto.StatusChangeDto> statusChanges = new ArrayList<>();
    for (int i = 0; i < expectedStatusState.length; ++i) {
      statusChanges
          .add(new WaveformQcChannelSohStatusDto.StatusChangeDto(expectedStartTimes[i],
              expectedEndTimes[i], expectedStatusState[i]));
    }
    dto.setStatusChanges(statusChanges);

    WaveformQcChannelSohStatus actualStatus = WaveformQcChannelSohStatusDtoConverter.fromDto(dto);

    assertEquals(processingChannelId, actualStatus.getProcessingChannelId());
    assertEquals(qcMaskType, actualStatus.getQcMaskType());
    assertEquals(channelSohSubtype, actualStatus.getChannelSohSubtype());
    assertEquals(expectedStatusState.length, actualStatus.getStatusChanges().count());

    Status[] actualStatusChanges = actualStatus.getStatusChanges().toArray(Status[]::new);
    for (int i = 0; i < expectedStatusState.length; ++i) {
      assertEquals(expectedStartTimes[i], actualStatusChanges[i].getStartTime());
      assertEquals(expectedEndTimes[i], actualStatusChanges[i].getEndTime());
      assertEquals(expectedStatusState[i], actualStatusChanges[i].getStatus());
    }
  }

  @Test
  public void testFromDtoNullExpectNullPointerException() throws Exception {
    exception.expect(NullPointerException.class);
    exception.expectMessage(
        "Cannot create WaveformQcChannelSohStatus from a null WaveformQcChannelSohStatusDto");
    WaveformQcChannelSohStatusDtoConverter.fromDto(null);
  }

  @Test
  public void testFromDtoNoStatusChangesExpectIllegalArgumentException() throws Exception {
    WaveformQcChannelSohStatusDto dto = new WaveformQcChannelSohStatusDto();
    dto.setProcessingChannelId(processingChannelId);
    dto.setQcMaskType(qcMaskType);
    dto.setChannelSohSubtype(channelSohSubtype);

    exception.expect(IllegalArgumentException.class);
    exception.expectMessage(
        "Cannot create WaveformQcChannelSohStatus from WaveformQcChannelSohStatusDto with no StatusChangeDto");
    WaveformQcChannelSohStatusDtoConverter.fromDto(dto);
  }
}
