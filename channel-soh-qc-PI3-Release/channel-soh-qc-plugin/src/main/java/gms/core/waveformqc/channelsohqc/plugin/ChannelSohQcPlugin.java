package gms.core.waveformqc.channelsohqc.plugin;

import gms.core.waveformqc.channelsohqc.algorithm.ChannelSohQcMask;
import gms.core.waveformqc.waveformqccontrol.plugin.util.MergeQcMasks;
import gms.core.waveformqc.channelsohqc.algorithm.WaveformChannelSohStatusInterpreter;
import gms.core.waveformqc.waveformqccontrol.objects.WaveformQcChannelSohStatus;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMask;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskCategory;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskVersion;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An intermediary between {@link ChannelSohQcPluginComponent} and the algorithm implementation in
 * {@link WaveformChannelSohStatusInterpreter}  Contains logic that can be shared by any plugin
 * deployment and therefore doesn't belong in ChannelSohQcPluginComponent but which uses the COI
 * business objects so also doesn't belong in WaveformChannelSohStatusInterpreter.
 */
public class ChannelSohQcPlugin {

  private static final Logger logger = LoggerFactory.getLogger(ChannelSohQcPlugin.class);

  /**
   * {@link ChannelSohPluginParameters} controlling this {@link ChannelSohQcPlugin}'s {@link QcMask}
   * creation
   */
  private ChannelSohPluginParameters channelSohPluginParameters;

  /**
   * Constructs a {@link ChannelSohQcPlugin} using {@link ChannelSohPluginParameters} created from
   * the provided {@link ChannelSohPluginConfiguration}.
   *
   * @param channelSohPluginConfiguration configuration for the ChannelSohQcPlugin, not null
   */
  private ChannelSohQcPlugin(ChannelSohPluginConfiguration channelSohPluginConfiguration) {
    this.channelSohPluginParameters = channelSohPluginConfiguration.createParameters();
  }

  /**
   * Obtains a {@link ChannelSohQcPlugin} that uses the provided {@link
   * ChannelSohPluginConfiguration}
   *
   * @param channelSohPluginConfiguration ChannelSohPluginConfiguration used by the
   * ChannelSohQcPlugin, not null
   * @return a ChannelSohQcPlugin, not null
   * @throws NullPointerException if channelSohPluginConfiguration is null
   */
  public static ChannelSohQcPlugin create(
      ChannelSohPluginConfiguration channelSohPluginConfiguration) {

    Objects.requireNonNull(channelSohPluginConfiguration,
        "ChannelSohQcPlugin create cannot accept null pluginConfiguration");

    return new ChannelSohQcPlugin(channelSohPluginConfiguration);
  }

  /**
   * Uses {@link WaveformChannelSohStatusInterpreter} to create new {@link QcMask}s from the {@link
   * WaveformQcChannelSohStatus}, merges the new masks with the existingQcMasks, and returns any
   * created or updated QcMasks.
   *
   * @param acquiredChannelSohs collection of WaveformQcChannelSohStatus to process for creating new
   * QcMasks, not null
   * @param existingQcMasks existing QcMasks that might be affected by any newly created masks, not
   * null
   * @param creationInfoId identity to the creation information for any created qcMasks
   * @return stream of created or updated QcMasks, not null
   */
  public Stream<QcMask> createQcMasks(
      Collection<WaveformQcChannelSohStatus> acquiredChannelSohs,
      Collection<QcMask> existingQcMasks,
      UUID creationInfoId) {

    Objects.requireNonNull(acquiredChannelSohs,
        "ChannelSohQcPlugin createQcMasks cannot accept null acquiredChannelSohs");
    Objects.requireNonNull(existingQcMasks,
        "ChannelSohQcPlugin createQcMasks cannot accept null existing QcMasks");

    // Filter rejected existing masks and then group by ProcessingChannel identity and Type
    final Map<UUID, Map<Type, List<QcMask>>> existingByIdByType = existingQcMasks.stream()
        .filter(q -> !q.getCurrentQcMaskVersion().isRejected())
        .collect(Collectors
            .groupingBy(QcMask::getProcessingChannelId,
                Collectors.groupingBy(ChannelSohQcPlugin::extractCurrentTypeOrThrow)));

    return acquiredChannelSohs.stream()
        .filter(channelSohPluginParameters::shouldCreateQcMask)
        .flatMap(soh -> createOutputQcMasks(soh, existingByIdByType, channelSohPluginParameters,
            creationInfoId));
  }

  /**
   * Creates the combination of new, existing, and merged {@link QcMask}s for the given {@link
   * WaveformQcChannelSohStatus}
   *
   * @param sohStatus Waveform state of health status information
   * @param existingByIdByType Map organizing existing QcMasks by processing channel id and {@link
   * Type}
   * @param sohPluginParameters Configuration parameters used to determine merge threshold
   * @param creationInfoId identity to the new qcMasks' creation information
   * @return A Stream of output QcMasks, collected by another function.
   */
  private static Stream<QcMask> createOutputQcMasks(WaveformQcChannelSohStatus sohStatus,
      Map<UUID, Map<Type, List<QcMask>>> existingByIdByType,
      ChannelSohPluginParameters sohPluginParameters, UUID creationInfoId) {

    List<QcMask> newQcMasks = createNewQcMasks(sohStatus, creationInfoId);

    return newQcMasks.isEmpty() ? Stream.empty() :
        MergeQcMasks.merge(
            newQcMasks,
            lookupBySimilarQcMask(existingByIdByType, newQcMasks.get(0)),
            sohPluginParameters.getMergeThreshold(sohStatus.getChannelSohSubtype()),
            creationInfoId).stream();
  }

  /**
   * Creates new {@link ChannelSohQcMask}s and converts them into COI {@link QcMask}s.
   *
   * @param sohStatus Waveform state of health status information
   * @param creationInfoId identity to the new qcMasks' creation information
   * @return New QcMasks created by the plugin algorithm.
   */
  private static List<QcMask> createNewQcMasks(WaveformQcChannelSohStatus sohStatus,
      UUID creationInfoId) {

    List<ChannelSohQcMask> sohQcMasks = WaveformChannelSohStatusInterpreter
        .createChannelSohQcMasks(sohStatus);

    return sohQcMasks.stream()
        .map(m -> createQcMask(sohStatus.getProcessingChannelId(), m, creationInfoId))
        .collect(Collectors.toList());
  }

  /**
   * Obtains an Optional Type (QcMaskType, Rationale) for the current version of a QcMask
   *
   * @param qcMask QcMask with type and rationale information.
   * @return The type, or throws the provided RuntimeException if no type is available.
   */
  private static Type extractCurrentTypeOrThrow(QcMask qcMask) {
    QcMaskVersion currentVersion = qcMask.getCurrentQcMaskVersion();
    return new Type(currentVersion.getType()
        .orElseThrow(() -> new IllegalArgumentException("Cannot extract null type.")),
        currentVersion.getRationale());
  }

  /**
   * Obtains all QcMasks from the 2d Map that shares a processing channel id and {@link Type} with
   * the input {@link QcMask}.
   *
   * @param masksByIdByType Map organizing QcMasks by processing channel id and {@link Type}
   * @param similarMask Mask we are finding similar Masks for.
   * @return All QcMasks sharing a processing channel id and Type with the input.
   */
  private static List<QcMask> lookupBySimilarQcMask(
      Map<UUID, Map<Type, List<QcMask>>> masksByIdByType,
      QcMask similarMask) {
    return lookupByIdAndType(masksByIdByType, similarMask.getProcessingChannelId(),
        extractCurrentTypeOrThrow(similarMask));
  }

  /**
   * Obtains QcMasks from masksByIdByType corresponding to the provided id and type. If
   * masksByIdByType does not contain any entries for id and type return an empty collection.
   *
   * @param masksByIdByType lookup QcMasks from this map
   * @param id id to use in the lookup, not null
   * @param type type to use in the lookup, not null
   * @return Collection of QcMask, not null
   */
  private static List<QcMask> lookupByIdAndType(
      Map<UUID, Map<Type, List<QcMask>>> masksByIdByType, UUID id, Type type) {

    return masksByIdByType.getOrDefault(id, Collections.emptyMap())
        .getOrDefault(type, Collections.emptyList());
  }

  /**
   * Creates a {@link QcMask} from the info in {@link ChannelSohQcMask}
   *
   * @param channelSohQcMask the info needed to make the mask, generated from algorithm.
   * @return {@link QcMask}
   */
  private static QcMask createQcMask(UUID processingChannelId,
      ChannelSohQcMask channelSohQcMask, UUID creationInfoId) {

    return QcMask.create(processingChannelId, Collections.emptyList(), Collections.emptyList(),
        QcMaskCategory.STATION_SOH, channelSohQcMask.getQcMaskType(),
        channelSohQcMask.getChannelSohSubtype().getRationale(), channelSohQcMask.getStartTime(),
        channelSohQcMask.getEndTime(), creationInfoId);
  }

  /**
   * Utility class containing a (QcMaskType, Rationale String) tuple.
   */
  private static class Type {

    final QcMaskType type;
    final String rationale;

    Type(QcMaskType type, String rationale) {
      this.type = type;
      this.rationale = rationale;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      Type type1 = (Type) o;
      return type == type1.type && rationale.equals(type1.rationale);
    }

    @Override
    public int hashCode() {
      int result = type.hashCode();
      result = 31 * result + rationale.hashCode();
      return result;
    }
  }
}
