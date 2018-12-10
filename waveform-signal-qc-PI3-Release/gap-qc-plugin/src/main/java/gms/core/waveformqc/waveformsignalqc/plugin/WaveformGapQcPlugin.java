package gms.core.waveformqc.waveformsignalqc.plugin;

import gms.core.waveformqc.waveformsignalqc.algorithm.WaveformGapInterpreter;
import gms.core.waveformqc.waveformsignalqc.algorithm.WaveformGapQcMask;
import gms.core.waveformqc.waveformsignalqc.algorithm.WaveformGapUpdater;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMask;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskVersion;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Wraps the gap algorithm logic in {@link WaveformGapInterpreter} and {@link WaveformGapUpdater} to
 * create new {@link QcMaskType#REPAIRABLE_GAP} and {@link QcMaskType#LONG_GAP} masks and update
 * existing gap masks with newly acquired waveforms.
 *
 * Wrap WaveformGapQcPlugin in a plugin component implementing the {@link
 * gms.core.waveformqc.waveformqccontrol.plugin.WaveformQcPlugin} to create the actual gap plugin.
 */
public class WaveformGapQcPlugin {

  private WaveformGapQcPluginParameters waveformGapQcPluginParameters;

  private WaveformGapQcPlugin(WaveformGapQcPluginConfiguration waveformGapQcPluginConfiguration) {
    this.waveformGapQcPluginParameters = waveformGapQcPluginConfiguration.createParameters();
  }

  /**
   * Obtains a new {@link WaveformGapQcPlugin} using the provided {@link
   * WaveformGapQcPluginConfiguration}
   *
   * @param waveformGapQcPluginConfiguration {@link WaveformGapQcPluginConfiguration}, not null
   * @return new {@link WaveformGapQcPlugin}, not null
   */
  public static WaveformGapQcPlugin create(
      WaveformGapQcPluginConfiguration waveformGapQcPluginConfiguration) {
    Objects.requireNonNull(waveformGapQcPluginConfiguration,
        "WaveformGapQcPlugin create cannot accept null pluginConfiguration");

    return new WaveformGapQcPlugin(waveformGapQcPluginConfiguration);
  }

  /**
   * Determines which gap {@link QcMask} exist in the provided {@link ChannelSegment} and updates as
   * necessary the existing QcMasks (i.e. rejecting because they are filled, shortening or splitting
   * due to new data acquisition). Assumes the QcMasks and ChannelSegments occur in the same time
   * intervals.  Returns new QcMasks and any updated existingQcMasks.
   *
   * @param channelSegments {@link ChannelSegment} to check for gaps, not null
   * @param existingQcMasks existing {@link QcMask} that might be updated, not null
   * @param creationInfoId id to the {@link gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInformation}
   * associated with returned {@link QcMask}
   * @return Stream of new or updated {@link QcMask}, not null
   */
  public Stream<QcMask> createQcMasks(Collection<ChannelSegment> channelSegments,
      Collection<QcMask> existingQcMasks,
      UUID creationInfoId) {

    Objects.requireNonNull(channelSegments,
        "ChannelSohQcPlugin createQcMasks cannot accept null acquiredChannelSohs");
    Objects.requireNonNull(existingQcMasks,
        "ChannelSohQcPlugin createQcMasks cannot accept null existing QcMasks");

    // Filter for valid gap masks then group by processing channel id
    final Map<UUID, List<QcMask>> existingGapQcMasks = existingQcMasks.stream()
        .filter(WaveformGapQcPlugin::gapType)
        .collect(Collectors.groupingBy(QcMask::getProcessingChannelId));

    return channelSegments.stream()
        .flatMap(cs ->
            createOutputQcMasks(cs,
                lookupExistingQcMasks(existingGapQcMasks, cs.getProcessingChannelId()),
                creationInfoId));
  }

  /**
   * Utility using the {@link WaveformGapInterpreter} and {@link WaveformGapUpdater} to create new
   * and update existing qcMasks for a single {@link ChannelSegment}
   *
   * @param channelSegment {@link ChannelSegment} to check for gaps, not null
   * @param existingQcMasks existing {@link QcMask} that might be updated, not null
   * @param creationInfoId id to the {@link gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInformation}
   * associated with returned {@link QcMask}
   * @return Stream of new or updated {@link QcMask}, not null
   */
  private Stream<QcMask> createOutputQcMasks(ChannelSegment channelSegment,
      List<QcMask> existingQcMasks, UUID creationInfoId) {

    List<WaveformGapQcMask> newGaps = WaveformGapInterpreter
        .createWaveformGapQcMasks(channelSegment,
            waveformGapQcPluginParameters.getMinLongGapLengthInSamples());

    return WaveformGapUpdater.updateQcMasks(newGaps, existingQcMasks, channelSegment.getId(),
        creationInfoId).stream();
  }

  /**
   * Lookup the existingByChannelId {@link QcMask} occurring on the provided {@link
   * gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Channel}.
   * Returns an empty list if there are no such masks.
   *
   * @param existingByChannelId existing {@link QcMask} organized by ProcessingChannel id, not null
   * @param uuid processingChannel id, not null
   * @return list of {@link QcMask}, not null
   */
  private List<QcMask> lookupExistingQcMasks(Map<UUID, List<QcMask>> existingByChannelId,
      UUID uuid) {
    List<QcMask> existingForId = existingByChannelId.get(uuid);
    return existingForId != null ? existingForId : List.of();
  }

  /**
   * Determine if the provided {@link QcMask} is a gap QcMask (i.e. the mask's current {@link
   * gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskVersion}
   * has {@link QcMaskVersion#getType()} of either {@link QcMaskType#REPAIRABLE_GAP} and {@link
   * QcMaskType#LONG_GAP}
   *
   * @param qcMask {@link QcMask}, not null
   * @return true if the qcMask's current version is a gap and false otherwise
   */
  private static boolean gapType(QcMask qcMask) {
    return qcMask.getCurrentQcMaskVersion().getType()
        .map(t -> QcMaskType.LONG_GAP.equals(t) || QcMaskType.REPAIRABLE_GAP.equals(t))
        .orElse(false);
  }
}