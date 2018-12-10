package gms.core.waveformqc.waveformsignalqc.plugin;

import gms.core.waveformqc.waveformqccontrol.plugin.PluginConfiguration;
import java.util.Objects;

/**
 * Configuration for the {@link WaveformGapQcPlugin}
 *
 * Configuration is the threshold number of samples separating long and repairable data gaps
 */
public class WaveformGapQcPluginConfiguration {

  private final static String LONG_GAP_LENGTH_KEY = "minLongGapLengthInSamples";

  private int minLongGapLengthInSamples;

  private WaveformGapQcPluginConfiguration(int minLongGapLengthInSamples) {
    this.minLongGapLengthInSamples = minLongGapLengthInSamples;
  }

  /**
   * Obtains a {@link WaveformGapQcPluginParameters} from this {@link
   * WaveformGapQcPluginConfiguration}
   *
   * @return WaveformGapQcPluginParameters, not null
   */
  public WaveformGapQcPluginParameters createParameters() {
    return WaveformGapQcPluginParameters.create(this.minLongGapLengthInSamples);
  }

  /**
   * Obtain a specific {@link WaveformGapQcPluginConfiguration} from a generic {@link
   * PluginConfiguration}
   *
   * The PluginConfiguration must have a positive integer value for key  {@link
   * WaveformGapQcPluginConfiguration#LONG_GAP_LENGTH_KEY} which is a long gap's minimum length.
   *
   * @param pluginConfig PluginConfiguration, not null
   * @return WaveformGapQcPluginConfiguration, not null
   * @throws NullPointerException if pluginConfig is null
   * @throws IllegalArgumentException if the value for {@link WaveformGapQcPluginConfiguration#LONG_GAP_LENGTH_KEY}
   * is not present, is not an integer, or is not positive
   */
  public static WaveformGapQcPluginConfiguration from(PluginConfiguration pluginConfig) {
    Objects.requireNonNull(pluginConfig,
        "WaveformGapQcPluginConfiguration.from requires non-null PluginConfiguration");

    if (!pluginConfig.getParameter(LONG_GAP_LENGTH_KEY).isPresent()) {
      throw new IllegalArgumentException(
          "WaveformGapQcPluginConfiguration.from requires PluginConfiguration with a configuration for minLongGapLengthInSamples");
    }

    int longGapLength;
    try {
      longGapLength = (int) pluginConfig.getParameter(LONG_GAP_LENGTH_KEY).get();
    } catch (ClassCastException e) {
      throw new IllegalArgumentException(
          "WaveformGapQcPluginConfiguration.from requires integer minLongGapLengthInSamples", e);
    }

    if (longGapLength <= 0) {
      throw new IllegalArgumentException(
          "WaveformGapQcPluginConfiguration.from requires positive minLongGapLengthInSamples");
    }

    return new WaveformGapQcPluginConfiguration(longGapLength);
  }
}
