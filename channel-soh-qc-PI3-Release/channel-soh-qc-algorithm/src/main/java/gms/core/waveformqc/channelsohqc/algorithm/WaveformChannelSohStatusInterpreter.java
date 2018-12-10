package gms.core.waveformqc.channelsohqc.algorithm;

import gms.core.waveformqc.waveformqccontrol.objects.WaveformQcChannelSohStatus;
import gms.core.waveformqc.waveformqccontrol.objects.WaveformQcChannelSohStatus.Status;
import gms.core.waveformqc.waveformqccontrol.objects.WaveformQcChannelSohStatus.StatusState;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Processes {@link WaveformQcChannelSohStatus} and creates a {@link ChannelSohQcMask} for each
 * StatusState.SET status value.
 */
public class WaveformChannelSohStatusInterpreter {

  private WaveformChannelSohStatusInterpreter() {
  }

  /**
   * Generates {@link ChannelSohQcMask}s based on Channel SOH status in {@link
   * WaveformQcChannelSohStatus}
   *
   * @param waveformQcChannelSohStatus acquired Channel SOH status information, not null
   * @return list of {@link ChannelSohQcMask}
   */
  public static List<ChannelSohQcMask> createChannelSohQcMasks(
      WaveformQcChannelSohStatus waveformQcChannelSohStatus) {

    Objects.requireNonNull(waveformQcChannelSohStatus,
        "WaveformChannelSohStatusInterpreter createChannelSohQcMasks cannot accept null WaveformQcChannelSohStatus");

    final Function<Status, ChannelSohQcMask> channelSohQcMaskFromStatus =
        status -> ChannelSohQcMask.create(
            waveformQcChannelSohStatus.getQcMaskType(),
            waveformQcChannelSohStatus.getChannelSohSubtype(),
            status.getStartTime(),
            status.getEndTime());

    return waveformQcChannelSohStatus.getStatusChanges()
        .filter(status -> status.getStatus() == StatusState.SET)
        .map(channelSohQcMaskFromStatus)
        .collect(Collectors.toList());
  }
}
