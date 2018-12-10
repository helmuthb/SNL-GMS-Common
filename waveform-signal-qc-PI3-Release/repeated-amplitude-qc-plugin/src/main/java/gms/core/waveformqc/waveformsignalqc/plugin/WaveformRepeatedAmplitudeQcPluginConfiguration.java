package gms.core.waveformqc.waveformsignalqc.plugin;

import gms.core.waveformqc.waveformqccontrol.plugin.PluginConfiguration;
import java.util.Objects;

/**
 * Configuration for the {@link WaveformRepeatedAmplitudeQcPlugin}
 *
 * Configuration is the minimum length of a repeated adjacent amplitude values mask and the maximum
 * amplitude deviation a sample in a mask can have from the initial amplitude value in the mask.
 */
public class WaveformRepeatedAmplitudeQcPluginConfiguration {

  private final static String MIN_SERIES_LENGTH_SAMPLES_KEY = "minSeriesLengthInSamples";
  private final static String MAX_DELTA_FROM_START_AMPLITUDE_KEY = "maxDeltaFromStartAmplitude";
  private final static String MERGE_THRESHOLD_SECONDS_KEY = "maskMergeThresholdSeconds";

  private final int minSeriesLengthInSamples;
  private final double maxDeltaFromStartAmplitude;
  private final double maskMergeThresholdSeconds;

  private WaveformRepeatedAmplitudeQcPluginConfiguration(int minSeriesLengthInSamples,
      double maxDeltaFromStartAmplitude, double maskMergeThresholdSeconds) {
    this.minSeriesLengthInSamples = minSeriesLengthInSamples;
    this.maxDeltaFromStartAmplitude = maxDeltaFromStartAmplitude;
    this.maskMergeThresholdSeconds = maskMergeThresholdSeconds;
  }

  /**
   * Obtains a {@link WaveformRepeatedAmplitudeQcPluginParameters} from this {@link
   * WaveformRepeatedAmplitudeQcPluginConfiguration}
   *
   * @return WaveformGapQcPluginParameters, not null
   */
  public WaveformRepeatedAmplitudeQcPluginParameters createParameters() {
    return WaveformRepeatedAmplitudeQcPluginParameters
        .create(this.minSeriesLengthInSamples, this.maxDeltaFromStartAmplitude,
            this.maskMergeThresholdSeconds);
  }

  /**
   * Obtain a specific {@link WaveformRepeatedAmplitudeQcPluginConfiguration} from a generic {@link
   * PluginConfiguration}
   *
   * The PluginConfiguration must have: 1) an integer value >= 2 for key {@link
   * WaveformRepeatedAmplitudeQcPluginConfiguration#MIN_SERIES_LENGTH_SAMPLES_KEY} which is the
   * minimum length of a repeated adjacent amplitude values mask
   *
   * 2) a double value >= 0.0 for key {@link WaveformRepeatedAmplitudeQcPluginConfiguration#MAX_DELTA_FROM_START_AMPLITUDE_KEY}
   * which is how far an amplitude in a repeated adjacent amplitude values mask can be from the
   * first value in the mask
   *
   * 3) a double value >= 0.0 for key {@link WaveformRepeatedAmplitudeQcPluginConfiguration#MERGE_THRESHOLD_SECONDS_KEY}
   * which is the exclusive duration between two repeated adjacent amplitude value QcMasks that
   * should be merged into a single mask.  Since the duration is exclusive two masks exactly this
   * duration apart will not be merged.
   *
   * @param pluginConfig PluginConfiguration, not null
   * @return WaveformRepeatedAmplitudeQcPluginConfiguration, not null
   * @throws NullPointerException if pluginConfig is null
   * @throws IllegalArgumentException if the value for {@link WaveformRepeatedAmplitudeQcPluginConfiguration#MIN_SERIES_LENGTH_SAMPLES_KEY}
   * is not present, is not an integer, or is <= 1; if the value for {@link
   * WaveformRepeatedAmplitudeQcPluginConfiguration#MAX_DELTA_FROM_START_AMPLITUDE_KEY} is not
   * present, is not a double, or is < 0.0; if the value for {@link WaveformRepeatedAmplitudeQcPluginConfiguration#MERGE_THRESHOLD_SECONDS_KEY}
   * is not present, is not a double, or is < 0.0
   */
  public static WaveformRepeatedAmplitudeQcPluginConfiguration from(
      PluginConfiguration pluginConfig) {

    Objects.requireNonNull(pluginConfig,
        "WaveformRepeatedAmplitudeQcPluginConfiguration.from requires non-null PluginConfiguration");

    checkConfigurationPresent(pluginConfig, MIN_SERIES_LENGTH_SAMPLES_KEY);
    checkConfigurationPresent(pluginConfig, MAX_DELTA_FROM_START_AMPLITUDE_KEY);
    checkConfigurationPresent(pluginConfig, MERGE_THRESHOLD_SECONDS_KEY);

    final int minSeriesLengthInSamples = parseInt(pluginConfig, MIN_SERIES_LENGTH_SAMPLES_KEY);
    if (minSeriesLengthInSamples <= 1) {
      throw new IllegalArgumentException(
          "WaveformRepeatedAmplitudeQcPluginConfiguration.from requires minSeriesLengthInSamples >= 2");
    }

    final double maxDeltaFromStartAmplitude = parseDouble(pluginConfig,
        MAX_DELTA_FROM_START_AMPLITUDE_KEY);
    if (maxDeltaFromStartAmplitude < 0.0) {
      throw new IllegalArgumentException(
          "WaveformRepeatedAmplitudeQcPluginConfiguration.from requires maxDeltaFromStartAmplitude >= 0.0");
    }

    final double maskMergeThresholdSeconds = parseDouble(pluginConfig, MERGE_THRESHOLD_SECONDS_KEY);
    if (maskMergeThresholdSeconds < 0.0) {
      throw new IllegalArgumentException(
          "WaveformRepeatedAmplitudeQcPluginConfiguration.from requires maskMergeThresholdSeconds >= 0.0");
    }

    return new WaveformRepeatedAmplitudeQcPluginConfiguration(minSeriesLengthInSamples,
        maxDeltaFromStartAmplitude, maskMergeThresholdSeconds);
  }

  /**
   * Determines if the key is available in the pluginConfig
   *
   * @param pluginConfig {@link PluginConfiguration}, not null
   * @param key check for presence of this key in pluginConfig, not null
   */
  private static void checkConfigurationPresent(PluginConfiguration pluginConfig, String key) {
    if (!pluginConfig.getParameter(key).isPresent()) {
      throw new IllegalArgumentException(
          "WaveformRepeatedAmplitudeQcPluginConfiguration.from requires PluginConfiguration with a configuration for "
              + key);
    }
  }

  /**
   * Obtain an integer value from the provided key in the {@link PluginConfiguration}.  Assumes
   * the key is present in the pluginConfig
   *
   * @param pluginConfig {@link PluginConfiguration}, not null
   * @param key lookup integer value from this key, not null
   * @return integer value from the configuration
   * @throws IllegalArgumentException if the value associated with the key is not an integer
   */
  private static int parseInt(PluginConfiguration pluginConfig, String key) {
    int value;
    try {
      value = (int) pluginConfig.getParameter(key).get();
    } catch (ClassCastException e) {
      throw new IllegalArgumentException(
          "WaveformRepeatedAmplitudeQcPluginConfiguration.from requires integer value for " + key,
          e);
    }

    return value;
  }

  /**
   * Obtain a double value from the provided key in the {@link PluginConfiguration}.  Assumes
   * the key is present in the pluginConfig
   *
   * @param pluginConfig {@link PluginConfiguration}, not null
   * @param key lookup integer value from this key, not null
   * @return double value from the configuration
   * @throws IllegalArgumentException if the value associated with the key is not a double
   */
  private static double parseDouble(PluginConfiguration pluginConfig, String key) {
    double value;
    try {
      value = (double) pluginConfig.getParameter(key).get();
    } catch (ClassCastException e) {
      throw new IllegalArgumentException(
          "WaveformRepeatedAmplitudeQcPluginConfiguration.from requires double value for " + key,
          e);
    }

    return value;
  }
}
