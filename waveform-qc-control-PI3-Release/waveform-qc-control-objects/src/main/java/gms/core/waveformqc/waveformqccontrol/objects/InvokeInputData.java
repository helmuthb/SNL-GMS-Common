package gms.core.waveformqc.waveformqccontrol.objects;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMask;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Collects the processing data required by WaveformQcControl's invoke operation.  The Waveform QC
 * Control OSD Gateway provides an operation to the controller to load information from the OSD and
 * populate this object.
 */
public class InvokeInputData {

  private final Set<ChannelSegment> channelSegments;
  private final Set<QcMask> qcMasks;
  private final Set<WaveformQcChannelSohStatus> waveformQcChannelSohStatuses;

  /**
   * Constructs the {@link InvokeInputData} from the provided sets
   *
   * @param channelSegments {@link ChannelSegment}, not null
   * @param qcMasks {@link QcMask}, not null
   * @param waveformQcChannelSohStatuses {@link WaveformQcChannelSohStatus}, not null
   */
  private InvokeInputData(
      Set<ChannelSegment> channelSegments,
      Set<QcMask> qcMasks,
      Set<WaveformQcChannelSohStatus> waveformQcChannelSohStatuses) {
    this.channelSegments = channelSegments;
    this.qcMasks = qcMasks;
    this.waveformQcChannelSohStatuses = waveformQcChannelSohStatuses;
  }

  /**
   * Constructs a {@link InvokeInputData} from the provided {@link ChannelSegment}, {@link QcMask},
   * and {@link WaveformQcChannelSohStatus}.
   *
   * @param channelSegments Set of ChannelSegment, not null
   * @param qcMasks Set of QcMask, not null
   * @param waveformQcChannelSohStatuses Set of WaveformQcChannelSohStatus, not null
   * @return an InvokeInputData, not null
   * @throws NullPointerException if channelSegments, qcMasks, or waveformQcChannelSohStatuses is
   * null
   */
  public static InvokeInputData create(Set<ChannelSegment> channelSegments, Set<QcMask> qcMasks,
      Set<WaveformQcChannelSohStatus> waveformQcChannelSohStatuses) {

    Objects.requireNonNull(channelSegments, "InvokeInputData cannot accept null ChannelSegments");
    Objects.requireNonNull(qcMasks, "InvokeInputData cannot accept null QcMasks");
    Objects.requireNonNull(waveformQcChannelSohStatuses,
        "InvokeInputData cannot accept null WaveformQcChannelSohStatuses");

    return new InvokeInputData(new HashSet<>(channelSegments),
        new HashSet<>(qcMasks), new HashSet<>(waveformQcChannelSohStatuses));
  }

  public Set<ChannelSegment> getChannelSegments() {
    return channelSegments;
  }

  public Stream<ChannelSegment> channelSegments() {
    return channelSegments.stream();
  }

  public Set<QcMask> getQcMasks() {
    return qcMasks;
  }

  public Stream<QcMask> qcMasks() {
    return qcMasks.stream();
  }

  public Set<WaveformQcChannelSohStatus> getWaveformQcChannelSohStatuses() {
    return waveformQcChannelSohStatuses;
  }

  public Stream<WaveformQcChannelSohStatus> waveformQcChannelSohStatuses() {
    return waveformQcChannelSohStatuses.stream();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    InvokeInputData that = (InvokeInputData) o;

    return (channelSegments != null ? channelSegments.equals(that.channelSegments)
        : that.channelSegments == null) && (qcMasks != null ? qcMasks.equals(that.qcMasks)
        : that.qcMasks == null) && (waveformQcChannelSohStatuses != null
        ? waveformQcChannelSohStatuses.equals(that.waveformQcChannelSohStatuses)
        : that.waveformQcChannelSohStatuses == null);
  }

  @Override
  public int hashCode() {
    int result = channelSegments != null ? channelSegments.hashCode() : 0;
    result = 31 * result + (qcMasks != null ? qcMasks.hashCode() : 0);
    result =
        31 * result + (waveformQcChannelSohStatuses != null ? waveformQcChannelSohStatuses
            .hashCode()
            : 0);
    return result;
  }

  @Override
  public String toString() {
    return "InvokeInputData{" +
        "channelSegments=" + channelSegments +
        ", qcMasks=" + qcMasks +
        ", waveformQcChannelSohStatuses=" + waveformQcChannelSohStatuses +
        '}';
  }
}
