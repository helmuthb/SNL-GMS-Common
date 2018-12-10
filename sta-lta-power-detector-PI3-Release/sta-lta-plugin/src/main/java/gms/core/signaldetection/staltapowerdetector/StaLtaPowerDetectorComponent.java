package gms.core.signaldetection.staltapowerdetector;

import gms.core.signaldetection.signaldetectorcontrol.objects.PluginVersion;
import gms.core.signaldetection.signaldetectorcontrol.plugin.PluginConfiguration;
import gms.core.signaldetection.signaldetectorcontrol.plugin.SignalDetectorPlugin;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import java.time.Instant;
import java.util.Collection;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A component wrapping a {@link StaLtaPowerDetectorPlugin} by implementing the {@link
 * SignalDetectorPlugin} interface. Provides a name and version number but defers logic to the
 * StaLtaPowerDetectorPlugin.
 */
public class StaLtaPowerDetectorComponent implements SignalDetectorPlugin {

  private static final Logger logger = LoggerFactory.getLogger(StaLtaPowerDetectorComponent.class);

  private static final String PLUGIN_NAME = "staLtaPowerDetectorPlugin";

  private static final PluginVersion pluginVersion = PluginVersion.from(1, 0, 0);

  private StaLtaPowerDetectorPlugin plugin;

  private boolean initialized = false;

  public StaLtaPowerDetectorComponent() {
  }

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
   * Obtains this plugin component's version number
   *
   * @return {@link PluginVersion}, not null
   */
  @Override
  public PluginVersion getVersion() {
    return pluginVersion;
  }

  /**
   * Initialize this plugin using the provided {@link PluginConfiguration}. Used to configure how
   * the plugin should run.
   *
   * @param pluginConfiguration {@link PluginConfiguration}
   */
  @Override
  public void initialize(PluginConfiguration pluginConfiguration) {
    Objects.requireNonNull(pluginConfiguration,
        "StaLtaPowerDetectorComponent cannot be initialized with null PluginConfiguration");

    if (initialized) {
      throw new IllegalStateException("StaLtaPowerDetectorComponent cannot be initialized twice");
    }

    logger
        .info("Initializing StaLtaPowerDetectorPlugin with configuration {}", pluginConfiguration);

    plugin = StaLtaPowerDetectorPlugin
        .create(new Algorithm(), StaLtaConfiguration.from(pluginConfiguration));

    initialized = true;
  }

  /**
   * Detects signal arrival times on the input {@link ChannelSegment}.
   *
   * @param channelSegment detect signal arrivals in this ChannelSegment, not null
   * @return Collection of {@link Instant} signal arrival times, not null
   */
  @Override
  public Collection<Instant> detectSignals(ChannelSegment channelSegment) {

    if (!initialized) {
      throw new IllegalStateException(
          "StaLtaPowerDetectorComponent cannot be used before it is initialized");
    }

    return this.plugin.detect(channelSegment);
  }
}
