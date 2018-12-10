package gms.core.waveformqc.waveformsignalqc.plugin;

import gms.core.waveformqc.waveformsignalqc.algorithm.WaveformSpike3PtInterpreter;
import gms.core.waveformqc.waveformsignalqc.algorithm.WaveformSpike3PtQcMask;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMask;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskCategory;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskVersion;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Wraps the gap algorithm logic in {@link WaveformSpike3PtInterpreter} to
 * create new {@link QcMaskType#SPIKE} mask and update possible end-point spike masks with newly
 * acquired waveforms.
 *
 * Wrap WaveformSpike3PtQcPlugin in a plugin component implementing the {@link
 * gms.core.waveformqc.waveformqccontrol.plugin.WaveformQcPlugin} to create the actual spike plugin.
 */
public class WaveformSpike3PtQcPlugin {

  private WaveformSpike3PtQcPluginParameters waveformSpike3PtQcPluginParameters;
  private WaveformSpike3PtInterpreter waveformSpike3PtInterpreter;

  private WaveformSpike3PtQcPlugin(WaveformSpike3PtQcPluginConfiguration waveformSpike3PtQcPluginConfiguration,
      WaveformSpike3PtInterpreter waveformSpike3PtInterpreter) {
    this.waveformSpike3PtQcPluginParameters = waveformSpike3PtQcPluginConfiguration.createParameters();
    this.waveformSpike3PtInterpreter = waveformSpike3PtInterpreter;
  }

  /**
   * Obtains a new {@link WaveformSpike3PtQcPlugin} using the provided {@link
   * WaveformSpike3PtQcPluginConfiguration}
   *
   * @param waveformSpike3PtQcPluginConfiguration {@link WaveformSpike3PtQcPluginConfiguration}, not null
   * @return new {@link WaveformSpike3PtQcPlugin}, not null
   */
  public static WaveformSpike3PtQcPlugin create(
      WaveformSpike3PtQcPluginConfiguration waveformSpike3PtQcPluginConfiguration,
      WaveformSpike3PtInterpreter waveformSpike3PtInterpreter) {
    Objects.requireNonNull(waveformSpike3PtQcPluginConfiguration,
        "WaveformSpike3PtQcPlugin create cannot accept null pluginConfiguration");
    Objects.requireNonNull(waveformSpike3PtInterpreter,
        "WaveformSpike3PtQcPlugin create cannot accept null spike algorithm");

    return new WaveformSpike3PtQcPlugin(waveformSpike3PtQcPluginConfiguration,
        waveformSpike3PtInterpreter);
  }

  /**
   * Determines which spike {@link QcMask} exist in the provided {@link ChannelSegment} and updates as
   * necessary the existing QcMasks. Assumes the QcMasks and ChannelSegments occur in the same time
   * intervals.  Returns new QcMasks and any updated existingQcMasks.
   *
   * @param channelSegments {@link ChannelSegment} to check for spikes, not null
   * @param existingQcMasks existing {@link QcMask} that might be updated, not null
   * @param creationInfoId id to the {@link gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInformation}
   * associated with returned {@link QcMask}
   * @return Stream of new or updated {@link QcMask}, not null
   */
  public Stream<QcMask> createQcMasks(Collection<ChannelSegment> channelSegments,
      Collection<QcMask> existingQcMasks,
      UUID creationInfoId) {

    Objects.requireNonNull(channelSegments,
        "WaveformSpike3PtQcPlugin createQcMasks cannot accept null channelSegments");
    Objects.requireNonNull(existingQcMasks,
        "WaveformSpike3PtQcPlugin createQcMasks cannot accept null existing existingQcMasks");
    Objects.requireNonNull(creationInfoId,
        "WaveformSpike3PtQcPlugin createQcMasks cannot accept null creationInfoId");

    // Filter for valid non-rejected spike masks then group by processing channel id
    final Map<UUID, List<QcMask>> existingSpikeQcMasks = existingQcMasks.stream()
        .filter(WaveformSpike3PtQcPlugin::spikeType)
        .filter(q -> !q.getCurrentQcMaskVersion().isRejected())
        .collect(Collectors.groupingBy(QcMask::getProcessingChannelId));

    return channelSegments.stream().flatMap(
        c -> waveformSpike3PtInterpreter
            .createWaveformSpike3PtQcMasks(c,
                waveformSpike3PtQcPluginParameters.getMinConsecutiveSampleDifferenceSpikeThreshold(),
                waveformSpike3PtQcPluginParameters.getRmsLeadSampleDifferences(),
                waveformSpike3PtQcPluginParameters.getRmsLagSampleDifferences(),
                waveformSpike3PtQcPluginParameters.getRmsAmplitudeRatioThreshold()).stream()
                .filter(m -> spikeDifferentFromMasks(m, existingSpikeQcMasks.get(m.getChannelId())))
                .map(m -> qcMaskFromSpike3PtMask(m, creationInfoId)));
  }

  private static QcMask qcMaskFromSpike3PtMask(
          WaveformSpike3PtQcMask spike3PtQcMask, UUID creationInfoId) {

    final String rationale = "System created 3Pt Spike values mask";

    return QcMask.create(spike3PtQcMask.getChannelId(), List.of(),
            List.of(spike3PtQcMask.getChannelSegmentId()), QcMaskCategory.WAVEFORM_QUALITY,
            QcMaskType.SPIKE, rationale,
            spike3PtQcMask.getStartTime(), spike3PtQcMask.getEndTime(),
            creationInfoId);
  }

  /**
   * Determine if the provided {@link QcMask} is a spike QcMask (i.e. the mask's current {@link
   * gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskVersion}
   * has {@link QcMaskVersion#getType()} of {@link QcMaskType#SPIKE}
   *
   * @param qcMask {@link QcMask}, not null
   * @return true if the qcMask's current version is a spike and false otherwise
   */
  private static boolean spikeType(QcMask qcMask) {
    return qcMask.getCurrentQcMaskVersion().getType()
            .map(t -> QcMaskType.SPIKE.equals(t))
            .orElse(false);
  }
  
  /**
   * Determine if the spike does not already exist in the collection of existing QcMasks
   *
   * @param spike {@link WaveformSpike3PtQcMask}, not null
   * @param qcMasks list of {@link QcMask}, not null
   * @return true if the spike is different in time from each mask, false otherwise
   */
  private static boolean spikeDifferentFromMasks(WaveformSpike3PtQcMask spike, List<QcMask> qcMasks) {
    return (qcMasks == null) ? true: qcMasks.stream()
            .map(QcMask::getCurrentQcMaskVersion)
            .noneMatch(m -> spike.getStartTime().equals(m.getStartTime().get())
                    && spike.getEndTime().equals(m.getEndTime().get()));
  }
}
