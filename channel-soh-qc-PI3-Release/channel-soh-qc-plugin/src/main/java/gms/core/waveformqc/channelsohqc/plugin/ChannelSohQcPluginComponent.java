package gms.core.waveformqc.channelsohqc.plugin;

import gms.core.waveformqc.waveformqccontrol.objects.PluginVersion;
import gms.core.waveformqc.waveformqccontrol.objects.WaveformQcChannelSohStatus;
import gms.core.waveformqc.waveformqccontrol.plugin.PluginConfiguration;
import gms.core.waveformqc.waveformqccontrol.plugin.WaveformQcPlugin;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMask;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSoh.AcquiredChannelSohType;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Channel SOH QC Plugin component with a name and version number. This plugin is used create QCMask
 * from channel SOH data.
 */
public class ChannelSohQcPluginComponent implements WaveformQcPlugin {

  private final String name = "channelSohQcPlugin";
  private final PluginVersion pluginVersion = PluginVersion.from(1, 0, 0);
  private ChannelSohQcPlugin channelSohQcPlugin;

  private static final Logger logger = LoggerFactory.getLogger(ChannelSohQcPluginComponent.class);

  /**
   * Channel SOH QC Plugin component with name and version that registers itself to the plugin
   * registry
   */
  public ChannelSohQcPluginComponent() {
  }

  /**
   * Method that gets the name of the plugin
   *
   * @return Plugin name
   */
  @Override
  public String getName() {
    return name;
  }

  /**
   * Method that gets the version of the plugin
   *
   * @return The major, minor, and patch version
   */
  @Override
  public PluginVersion getVersion() {
    return pluginVersion;
  }

  @Override
  public void initialize(PluginConfiguration pluginConfiguration) {
    Objects.requireNonNull(pluginConfiguration,
        "ChannelSohQcPluginComponent initialize cannot accept null pluginConfiguration");

    if (channelSohQcPlugin != null) {
      throw new IllegalStateException("ChannelSohQcPluginComponent cannot be initialized twice");
    }

    channelSohQcPlugin = ChannelSohQcPlugin
        .create(createChannelSohPluginConfiguration(pluginConfiguration));
  }

  /**
   * Parses {@link PluginConfiguration} to create a {@link ChannelSohPluginConfiguration}. Default
   * configuration always expected and overrides are optional.  Configuration schema is:
   *
   * "defaults" : Map("shouldCreateQcMasks":boolean, "mergeThreshold":Duration String) "overrides" :
   * Map("type":AcquiredChannelSohType String, "shouldCreateQcMasks":boolean,
   * "mergeThreshold":Duration String)
   */
  private static ChannelSohPluginConfiguration createChannelSohPluginConfiguration(
      PluginConfiguration pluginConfiguration) {

    // TODO: After moving to real configuration we need to test exception throwing on bad config

    Map<String, Object> defaults = (Map<String, Object>) pluginConfiguration
        .getParameter("defaults").orElseThrow(() -> new IllegalArgumentException("defaults"));

    final boolean defaultCreateQcMasks = (Boolean) defaults.get("shouldCreateQcMasks");
    final Duration defaultMergeThreshold = Duration.parse((String) defaults.get("mergeThreshold"));

    ChannelSohPluginConfiguration.Builder configBuilder = ChannelSohPluginConfiguration
        .builder(defaultCreateQcMasks, defaultMergeThreshold);

    if (pluginConfiguration.getParameter("overrides").isPresent()) {
      List<Map<String, Object>> overrides = (List<Map<String, Object>>) pluginConfiguration
          .getParameter("overrides").get();

      for (Map<String, Object> o : overrides) {
        if (!o.containsKey("type")) {
          throw new IllegalArgumentException("overrides");
        }

        AcquiredChannelSohType type = AcquiredChannelSohType.valueOf((String) o.get("type"));

        if (o.containsKey("shouldCreateQcMasks")) {
          configBuilder.setCreateQcMask(type, (Boolean) o.get("shouldCreateQcMasks"));
        }

        if (o.containsKey("mergeThreshold")) {
          configBuilder.setMergeThreshold(type, Duration.parse((String) o.get("mergeThreshold")));
        }
      }
    }

    return configBuilder.build();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Stream<QcMask> generateQcMasks(Collection<ChannelSegment> waveforms,
      Collection<WaveformQcChannelSohStatus> acquiredChannelSohs,
      Collection<QcMask> qcMasks,
      UUID creationInfoId) {

    if (channelSohQcPlugin == null) {
      throw new IllegalStateException(
          "ChannelSohQcPluginComponent cannot be used before it is initialized");
    }

    Objects.requireNonNull(waveforms,
        "ChannelSohQcPluginComponent generateQcMasks cannot accept null waveforms");
    Objects.requireNonNull(acquiredChannelSohs,
        "ChannelSohQcPluginComponent generateQcMasks cannot accept null acquiredChannelSohs");
    Objects.requireNonNull(qcMasks,
        "ChannelSohQcPluginComponent generateQcMasks cannot accept null existing QcMasks");
    Objects.requireNonNull(creationInfoId,
        "ChannelSohQcPluginComponent generateQcMasks cannot accept null existing creationInfoId");

    logger.info(
        "ChannelSohQcPluginComponent generateQcMasks invoked with Waveforms:{}, Statuses:{}, and QcMasks:{}",
        waveforms, acquiredChannelSohs, qcMasks);

    return channelSohQcPlugin.createQcMasks(acquiredChannelSohs, qcMasks, creationInfoId);
  }

}
