
package gms.core.signalenhancement.waveformfiltering;

import gms.core.signalenhancement.waveformfiltering.objects.PluginVersion;
import gms.core.signalenhancement.waveformfiltering.plugin.FilterPlugin;
import gms.core.signalenhancement.waveformfiltering.plugin.PluginConfiguration;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FilterDefinition;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.util.Collection;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A component wrapping a {@link LinearWaveformFilterPlugin} by implementing the {@link
 * FilterPlugin} interface. Provides a name and version number but defers logic to the
 * LinearWaveformFilterPlugin.
 */
public class LinearWaveformFilterComponent implements FilterPlugin {

  private static final Logger logger = LoggerFactory
      .getLogger(LinearWaveformFilterComponent.class);

  private static final String PLUGIN_NAME = "linearWaveformFilterPlugin";

  private static final PluginVersion pluginVersion = PluginVersion.from(1, 0, 0);

  private final LinearWaveformFilterPlugin plugin;

  public LinearWaveformFilterComponent() {
    //TODO: Move plugin creation into initialize() and delete constructor if configuration ever required.
    plugin = new LinearWaveformFilterPlugin();
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
   * Obtains this plugin component's verion number
   *
   * @return {@link PluginVersion}, not null
   */
  @Override
  public PluginVersion getVersion() {
    return pluginVersion;
  }

  /**
   * Unimplemented - no configuration necessary
   * @param pluginConfiguration
   */
  @Override
  public void initialize(PluginConfiguration pluginConfiguration) {
    logger.info("Unimplemented - no configuration necessary at this time.");
  }

  /**
   * Uses the wrapped {@link LinearWaveformFilterPlugin} to generate a sequence of filtered waveforms
   *
   * @param channelSegment {@link ChannelSegment} containing waveforms to filter, not null
   * @param filterDefinition {@link FilterDefinition}, containing filter parameters, not null
   * @return New filtered waveforms
   * @throws NullPointerException if channelSegment or filterDefinition is null
   */
  @Override
  public Collection<Waveform> filter(ChannelSegment channelSegment,
      FilterDefinition filterDefinition) {

    Objects.requireNonNull(channelSegment,
        "LinearWaveformFilterPlugin cannot filter with null channelSegment");
    Objects.requireNonNull(filterDefinition,
        "LinearWaveformFilterPlugin cannot filter with null filterDefinition");

    return plugin.filter(channelSegment, filterDefinition);
  }
}