package gms.core.waveformqc.waveformsignalqc.plugin;

import gms.core.waveformqc.waveformqccontrol.objects.PluginVersion;
import gms.core.waveformqc.waveformqccontrol.objects.WaveformQcChannelSohStatus;
import gms.core.waveformqc.waveformqccontrol.plugin.PluginConfiguration;
import gms.core.waveformqc.waveformqccontrol.plugin.WaveformQcPlugin;
import gms.core.waveformqc.waveformsignalqc.algorithm.WaveformSpike3PtInterpreter;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMask;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import java.util.Collection;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A component wrapping a {@link WaveformSpike3PtQcPlugin} by implementing the {@link
 * WaveformSpike3PtQcPlugin} interface. Provides a name and version number but defers logic to the
 * WaveformGapQcPlugin.
 */
public class WaveformSpike3PtQcPluginComponent implements WaveformQcPlugin {

  private static final Logger logger = LoggerFactory.getLogger(WaveformSpike3PtQcPluginComponent.class);

  private static final String PLUGIN_NAME = "waveformSpike3PtQcPlugin";

  private static final PluginVersion pluginVersion = PluginVersion.from(1, 0, 0);

  private WaveformSpike3PtQcPlugin plugin = null;

  /**
   * Obtains this plugin component's name
   *
   * @return String, not null
   */
  @Override
  public String getName() {
    return PLUGIN_NAME;
  }

  /**
   * Obtains this plugin component's verion number
   *
   * @return {@link PluginVersion}, not null
   */
  @Override
  public PluginVersion getVersion() {
    return pluginVersion;
  }

  /**
   * Initialize using the provided {@link PluginConfiguration}.  Creates the wrapped {@link
   * WaveformSpike3PtQcPlugin}.  Must only be called once and must be called before the first call to
   * {@link WaveformSpike3PtQcPluginComponent#generateQcMasks(Collection, Collection, Collection, UUID)}
   *
   * @param pluginConfiguration generic PluginConfiguration, not null
   * @throws NullPointerException if pluginConfiguration is null
   * @throws IllegalStateException if this operation is called more than once
   */
  @Override
  public void initialize(PluginConfiguration pluginConfiguration) {
    Objects.requireNonNull(pluginConfiguration,
        "WaveformSpike3PtQcPluginComponent cannot be initialized with null PluginConfiguration");

    if (null != plugin) {
      throw new IllegalStateException("WaveformSpike3PtQcPluginComponent cannot be initialized twice");
    }

    plugin = WaveformSpike3PtQcPlugin.create(WaveformSpike3PtQcPluginConfiguration.from(pluginConfiguration),
        new WaveformSpike3PtInterpreter());
  }

  /**
   * Uses the wrapped {@link WaveformSpike3PtQcPlugin} to create spike {@link QcMask}s.
   * Must only be called after {@link WaveformSpike3PtQcPluginComponent#initialize(PluginConfiguration)}
   *
   * @param channelSegments {@link ChannelSegment}s to check for spikes, not null
   * @param waveformQcChannelSohStatuses {@link WaveformQcChannelSohStatus}, not null
   * @param existingQcMasks Previously created {@link QcMask}s that can affect processing, not null
   * @param creationInfoId {@link UUID} to a {@link gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo}
   * to associate with new QcMasks, not null
   * @return Stream of new or updated QcMasks, not null
   * @throws NullPointerException if channelSegments, waveformQcChannelSohStatuses, existingQcMasks,
   * or creationInfoId are null
   * @throws IllegalStateException if this operation is called prior to {@link
   * WaveformSpike3PtQcPluginComponent#initialize(PluginConfiguration)}
   */
  @Override
  public Stream<QcMask> generateQcMasks(Collection<ChannelSegment> channelSegments,
      Collection<WaveformQcChannelSohStatus> waveformQcChannelSohStatuses,
      Collection<QcMask> existingQcMasks, UUID creationInfoId) {
    logger.info("WaveformSpike3PtQcPluginComponent generateQcMasks invoked with Channel Segments:{},"
        + "Soh:{}, QcMasks:{}", channelSegments, waveformQcChannelSohStatuses, existingQcMasks);

    Objects.requireNonNull(channelSegments,
        "WaveformSpike3PtQcPluginComponent cannot generateQcMasks with null channelSegments");
    Objects.requireNonNull(waveformQcChannelSohStatuses,
        "WaveformSpike3PtQcPluginComponent cannot generateQcMasks with null waveformQcChannelSohStatuses");
    Objects.requireNonNull(existingQcMasks,
        "WaveformSpike3PtQcPluginComponent cannot generateQcMasks with null existingQcMasks");
    Objects.requireNonNull(creationInfoId,
        "WaveformSpike3PtQcPluginComponent cannot generateQcMasks with null creationInfoId");

    if (null == plugin) {
      throw new IllegalStateException(
          "WaveformSpike3PtQcPluginComponent cannot be used before it is initialized");
    }

    return plugin.createQcMasks(channelSegments, existingQcMasks, creationInfoId);
  }

}
