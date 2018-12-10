package gms.core.waveformqc.waveformqccontrol.osdgateway.client;

import gms.core.waveformqc.waveformqccontrol.objects.WaveformQcChannelSohStatus;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Channel;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMask;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Collects the processing data required by WaveformQcControl's invoke operation.  The Waveform QC
 * Control OSD Gateway provides an operation to the controller to load information from the OSD and
 * populate this object.
 */
public class InvokeInputDataMap {

  private final Map<UUID, Set<QcMask>> qcMasksByProcessingChannel;
  private final Map<UUID, Set<ChannelSegment>> channelSegmentsByProcessingChannel;
  private final Map<UUID, Set<WaveformQcChannelSohStatus>> waveformQcChannelSohStatusByProcessingChannel;

  /**
   * Constructs a {@link InvokeInputDataMap} from the provided {@link ChannelSegment}, {@link
   * QcMask}, and {@link WaveformQcChannelSohStatus}.
   *
   * @param channelSegments Set of ChannelSegment, not null
   * @param qcMasks Set of QcMask, not null
   * @param waveformQcChannelSohStatuses Set of WaveformQcChannelSohStatus, not null
   * @return an InvokeInputData, not null
   * @throws NullPointerException if channelSegments, qcMasks, waveformQcChannelSohStatuses, or
   * waveformQcParameters is null
   */
  public static InvokeInputDataMap create(Set<ChannelSegment> channelSegments, Set<QcMask> qcMasks,
      Set<WaveformQcChannelSohStatus> waveformQcChannelSohStatuses) {

    Objects
        .requireNonNull(channelSegments, "InvokeInputDataMap cannot accept null ChannelSegments");
    Objects.requireNonNull(qcMasks, "InvokeInputDataMap cannot accept null QcMasks");
    Objects.requireNonNull(waveformQcChannelSohStatuses,
        "InvokeInputDataMap cannot accept null WaveformQcChannelSohStatuses");

    return new InvokeInputDataMap(
        groupByChannelId(qcMasks, QcMask::getProcessingChannelId),
        groupByChannelId(channelSegments, ChannelSegment::getProcessingChannelId),
        groupByChannelId(waveformQcChannelSohStatuses,
            WaveformQcChannelSohStatus::getProcessingChannelId));
  }

  /**
   * Constructs a map of processingChannel {@link UUID} to set of T from a provided set of T.
   * Uses the lookupChannelId to find the processingChannel UUID for each T.
   *
   * @param flat flat collection of T's, not null
   * @param getChannelId function to get the processingChannel UUID from a T, not null
   * @param <T> type of object being mapped by processingChannel UUID
   * @return Sets of T's mapped by processingChannel UUID, not null
   */
  private static <T> Map<UUID, Set<T>> groupByChannelId(Set<T> flat,
      Function<T, UUID> getChannelId) {

    return flat.stream()
        .collect(Collectors.groupingBy(getChannelId, Collectors.toSet()));
  }

  /**
   * Constructs a map of processingChannel {@link UUID} to T from a provided set of T.  Uses the
   * lookupChannelId to find the processingChannel UUID for each T.
   *
   * @param flat flat collection of T's, not null
   * @param getChannelId function to get the processingChannel UUID from a T, not null
   * @param <T> type of object being mapped by processingChannel UUID
   * @return Values of T mapped by processingChannel UUID, not null
   */
  private static <T> Map<UUID, T> mapByChannelId(Set<T> flat,
      Function<T, UUID> getChannelId) {
    return flat.stream().collect(Collectors.toMap(getChannelId, Function.identity()));
  }

  /**
   * Constructs the {@link InvokeInputDataMap} from the provided maps
   *
   * @param qcMasksByProcessingChannel {@link QcMask} by processingChannel {@link UUID}, not
   * null
   * @param channelSegmentsByProcessingChannel {@link ChannelSegment} by processingChannel UUID,
   * not null
   * @param waveformQcChannelSohStatusByProcessingChannel {@link WaveformQcChannelSohStatus} by
   * processingChannel UUID, not null
   */
  private InvokeInputDataMap(
      Map<UUID, Set<QcMask>> qcMasksByProcessingChannel,
      Map<UUID, Set<ChannelSegment>> channelSegmentsByProcessingChannel,
      Map<UUID, Set<WaveformQcChannelSohStatus>> waveformQcChannelSohStatusByProcessingChannel) {

    this.qcMasksByProcessingChannel = qcMasksByProcessingChannel;
    this.channelSegmentsByProcessingChannel = channelSegmentsByProcessingChannel;
    this.waveformQcChannelSohStatusByProcessingChannel = waveformQcChannelSohStatusByProcessingChannel;
  }

  /**
   * Obtains all of the {@link QcMask}s contained in this object for the provided {@link UUID}
   * to a {@link Channel}
   *
   * @param processingChannelId UUID to a ProcessingChannel, not null
   * @return {@link Optional} Set of QcMask mapped by the processingChannel, not null
   */
  public Optional<Set<QcMask>> getQcMasks(UUID processingChannelId) {
    Objects.requireNonNull(processingChannelId,
        "Cannot get QcMasks from InvokeInputDataMap for a null ProcessingChannel identifier");
    return Optional.ofNullable(qcMasksByProcessingChannel.get(processingChannelId));
  }

  /**
   * Obtains all of the {@link ChannelSegment}s contained in this object for the provided {@link
   * UUID} to a {@link Channel}
   *
   * @param processingChannelId UUID to a ProcessingChannel, not null
   * @return {@link Optional} Set of ChannelSegment mapped by the processingChannel, not null
   */
  public Optional<Set<ChannelSegment>> getChannelSegments(UUID processingChannelId) {
    Objects.requireNonNull(processingChannelId,
        "Cannot get ChannelSegments from InvokeInputDataMap for a null ProcessingChannel identifier");
    return Optional.ofNullable(channelSegmentsByProcessingChannel.get(processingChannelId));
  }

  /**
   * Obtains all of the {@link WaveformQcChannelSohStatus}s contained in this cbject for the
   * provided {@link UUID} to a {@link Channel}
   *
   * @param processingChannelId UUID to a ProcessingChannel, not null
   * @return {@link Optional} Set of WaveformQcChannelSohStatus mapped by the processingChannel, not
   * null
   */
  public Optional<Set<WaveformQcChannelSohStatus>> getWaveformQcChannelSohStatuses(
      UUID processingChannelId) {
    Objects.requireNonNull(processingChannelId,
        "Cannot get WaveformQcChannelSohStatuses from InvokeInputDataMap for a null ProcessingChannel identifier");
    return Optional
        .ofNullable(waveformQcChannelSohStatusByProcessingChannel.get(processingChannelId));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    InvokeInputDataMap that = (InvokeInputDataMap) o;

    return (qcMasksByProcessingChannel != null ? qcMasksByProcessingChannel
        .equals(that.qcMasksByProcessingChannel) : that.qcMasksByProcessingChannel == null) && (
        channelSegmentsByProcessingChannel != null ? channelSegmentsByProcessingChannel
            .equals(that.channelSegmentsByProcessingChannel)
            : that.channelSegmentsByProcessingChannel == null) && (
        waveformQcChannelSohStatusByProcessingChannel != null
            ? waveformQcChannelSohStatusByProcessingChannel
            .equals(that.waveformQcChannelSohStatusByProcessingChannel)
            : that.waveformQcChannelSohStatusByProcessingChannel == null);
  }

  @Override
  public int hashCode() {
    int result = qcMasksByProcessingChannel != null ? qcMasksByProcessingChannel.hashCode() : 0;
    result =
        31 * result + (channelSegmentsByProcessingChannel != null
            ? channelSegmentsByProcessingChannel
            .hashCode() : 0);
    result = 31 * result + (waveformQcChannelSohStatusByProcessingChannel != null
        ? waveformQcChannelSohStatusByProcessingChannel.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "InvokeInputDataMap{" +
        "qcMasksByProcessingChannel=" + qcMasksByProcessingChannel +
        ", channelSegmentsByProcessingChannel=" + channelSegmentsByProcessingChannel +
        ", waveformQcChannelSohStatusByProcessingChannel="
        + waveformQcChannelSohStatusByProcessingChannel +
        '}';
  }
}
