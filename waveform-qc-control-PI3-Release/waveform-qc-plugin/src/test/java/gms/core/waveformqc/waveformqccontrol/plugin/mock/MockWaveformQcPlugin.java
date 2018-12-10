package gms.core.waveformqc.waveformqccontrol.plugin.mock;

import gms.core.waveformqc.waveformqccontrol.objects.PluginVersion;
import gms.core.waveformqc.waveformqccontrol.objects.WaveformQcChannelSohStatus;
import gms.core.waveformqc.waveformqccontrol.plugin.WaveformQcPlugin;
import gms.core.waveformqc.waveformqccontrol.plugin.PluginConfiguration;
import gms.core.waveformqc.waveformqccontrol.plugin.util.PluginUtility;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMask;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskCategory;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskVersion;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mock Waveform QC plugin with name and version number. This mock plugin is used to test the Plugin
 * registry
 */
public class MockWaveformQcPlugin implements WaveformQcPlugin {

  private final String name;
  private final PluginVersion pluginVersion;

  /**
   * Logging
   */
  private static final Logger logger = LoggerFactory.getLogger(MockWaveformQcPlugin.class);

  /**
   * Mock Waveform QC plugin with name and version
   *
   * @param name The name of the plugin
   * @param pluginVersion The plugin version (Major, Minor, Patch)
   */
  private MockWaveformQcPlugin(String name, PluginVersion pluginVersion) {
    this.name = name;
    this.pluginVersion = pluginVersion;
  }

  /**
   * Checks to see if the registry is empty. If it is then an error is thrown and logged in the
   * logger
   *
   * @param name The plugin name
   * @param pluginVersion The plugin version (Major, Minor, Patch)
   * @return A new mock plugin from the registry information
   */
  public static MockWaveformQcPlugin from(String name, PluginVersion pluginVersion) {
    PluginUtility.checkNotNull(name, logger, "Error instantiating plugin, name cannot be null");
    PluginUtility.checkNotNull(pluginVersion, logger,
        "Error instantiating plugin, version cannot be null");

    return new MockWaveformQcPlugin(name, pluginVersion);
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
  public void initialize(PluginConfiguration configuration) {

  }

  /**
   * Generates a mock stream of waveform QC
   *
   * @param channelSegments The collection of waveforms to be masked
   * @param waveformQcChannelSohStatuses The collection of channel SOH data from the waveform data
   * @return An empty list of generated qc mask data
   */
  @Override
  public Stream<QcMask> generateQcMasks(Collection<ChannelSegment> channelSegments,
      Collection<WaveformQcChannelSohStatus> waveformQcChannelSohStatuses,
      Collection<QcMask> qcMasks, UUID creationInfoId) {
    return Stream.of(mockQcMask());
  }

  public static QcMask mockQcMask() {
    final Instant begin = Instant.parse("2017-09-25T10:00:00.00Z");
    final Instant end = begin.plusSeconds(60);

    return QcMask
        .from(new UUID(0L, 0L), new UUID(0L, 0L), Collections.singletonList(QcMaskVersion.from(
            0L, Collections.emptyList(), Collections.emptyList(),
            QcMaskCategory.STATION_SOH, QcMaskType.CALIBRATION, "", begin, end,
            new UUID(0L, 0L))));
  }

}
