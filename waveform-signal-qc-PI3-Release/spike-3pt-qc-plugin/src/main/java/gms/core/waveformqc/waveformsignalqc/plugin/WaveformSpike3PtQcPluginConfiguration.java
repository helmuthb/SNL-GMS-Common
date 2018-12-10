package gms.core.waveformqc.waveformsignalqc.plugin;

import gms.core.waveformqc.waveformqccontrol.plugin.PluginConfiguration;
import java.util.Objects;

public class WaveformSpike3PtQcPluginConfiguration {

  private final static String SPIKE_3PT_THRESHOLD_KEY = "minConsecutiveSampleDifferenceSpikeThreshold";
  private final static String RMS_AMPLITUDE_RATIO_THRESHOLD = "rmsAmplitudeRatioThreshold";
  private final static String RMS_LEAD_SAMPLE_DEFFERENCES = "rmsLeadSampleDifferences";
  private final static String RMS_LAG_SAMPLE_DEFFERENCES = "rmsLagSampleDifferences";

  private double minConsecutiveSampleDifferenceSpikeThreshold;
  private double rmsAmplitudeRatioThreshold;
  private int rmsLeadSampleDifferences;
  private int rmsLagSampleDifferences;

  private WaveformSpike3PtQcPluginConfiguration(double minConsecutiveSampleDifferenceSpikeThreshold,
      double rmsAmplitudeRatioThreshold, int rmsLeadSampleDifferences, int rmsLagSampleDifferences) {
    this.minConsecutiveSampleDifferenceSpikeThreshold = minConsecutiveSampleDifferenceSpikeThreshold;
    this.rmsAmplitudeRatioThreshold = rmsAmplitudeRatioThreshold;
    this.rmsLeadSampleDifferences = rmsLeadSampleDifferences;
    this.rmsLagSampleDifferences = rmsLagSampleDifferences;
  }

  /**
   * Obtains a {@link WaveformSpike3PtQcPluginParameters} from this {@link
   * WaveformSpike3PtQcPluginConfiguration}
   *
   * @return WaveformSpike3PtQcPluginParameters, not null
   */
  public WaveformSpike3PtQcPluginParameters createParameters() {
    return WaveformSpike3PtQcPluginParameters.create(this.minConsecutiveSampleDifferenceSpikeThreshold,
        this.rmsAmplitudeRatioThreshold, this.rmsLeadSampleDifferences, this.rmsLagSampleDifferences);
  }

  /**
   * Obtain a specific {@link WaveformSpike3PtQcPluginConfiguration} from a generic {@link
   * PluginConfiguration}
   *
   * The PluginConfiguration must have a positive integer value for key  {@link
   * WaveformSpike3PtQcPluginConfiguration#minConsecutiveSampleDifferenceSpikeThreshold}
   * which is minimum ratio (min/max) between the difference of three consecutive data points that
   * won't generate a spike.
   *
   * @param pluginConfig PluginConfiguration, not null
   * @return WaveformSpike3PtQcPluginConfiguration, not null
   * @throws NullPointerException if pluginConfig is null
   * @throws IllegalArgumentException if the value for
   * {@link WaveformSpike3PtQcPluginConfiguration#minConsecutiveSampleDifferenceSpikeThreshold}
   * is not present, is not an integer, or is not positive
   */
  public static WaveformSpike3PtQcPluginConfiguration from(PluginConfiguration pluginConfig) {
    Objects.requireNonNull(pluginConfig,
        "WaveformSpike3PtQcPluginConfiguration.from requires non-null PluginConfiguration");

    double spike3PtThreshold;
    if (!pluginConfig.getParameter(SPIKE_3PT_THRESHOLD_KEY).isPresent()) {
      throw new IllegalArgumentException(
          "WaveformSpike3PtQcPluginConfiguration.from requires PluginConfiguration with a"
              + " configuration for (double) minConsecutiveSampleDifferenceSpikeThreshold");
    }

    try {
      spike3PtThreshold = (double) pluginConfig.getParameter(SPIKE_3PT_THRESHOLD_KEY).get();
    } catch (ClassCastException e) {
      throw new IllegalArgumentException(
          "WaveformSpike3PtQcPluginConfiguration.from requires (double) minConsecutiveSampleDifferenceSpikeThreshold", e);
    }

    if (spike3PtThreshold <= 0
        || spike3PtThreshold >= 1.0) {
      throw new IllegalArgumentException(
          "WaveformSpike3PtQcPluginParameters requires minConsecutiveSampleDifferenceSpikeThreshold > 0.0 and < 1.0");
    }

    double spikeRmsAmplitudeRatioThreshold;
    if (!pluginConfig.getParameter(RMS_AMPLITUDE_RATIO_THRESHOLD).isPresent()) {
      throw new IllegalArgumentException(
          "WaveformSpike3PtQcPluginConfiguration.from requires PluginConfiguration with a"
              + " configuration for (double) rmsAmplitudeRatioThreshold");
    }

    final Object value = pluginConfig.getParameter(RMS_AMPLITUDE_RATIO_THRESHOLD).get();

    if(Number.class.isInstance(value)) {
      spikeRmsAmplitudeRatioThreshold = ((Number)value).doubleValue();
    }
    else {
      throw new IllegalArgumentException(
          "WaveformSpike3PtQcPluginConfiguration.from requires (double) rmsAmplitudeRatioThreshold");
    }

    if (spikeRmsAmplitudeRatioThreshold <= 1.0) {
      throw new IllegalArgumentException(
          "WaveformSpike3PtQcPluginConfiguration.from requires rmsAmplitudeRatioThreshold > 1.0");
    }

    int spikeRmsLeadSampleDifferences;
    if (!pluginConfig.getParameter(RMS_LEAD_SAMPLE_DEFFERENCES).isPresent()) {
      throw new IllegalArgumentException(
          "WaveformSpike3PtQcPluginConfiguration.from requires PluginConfiguration with a"
              + " configuration for (int) rmsLeadSampleDifferences");
    }

    try {
      spikeRmsLeadSampleDifferences = (int) pluginConfig.getParameter(RMS_LEAD_SAMPLE_DEFFERENCES).get();
    } catch (ClassCastException e) {
      throw new IllegalArgumentException(
          "WaveformSpike3PtQcPluginConfiguration.from requires (int) rmsLeadSampleDifferences", e);
    }

    if (spikeRmsLeadSampleDifferences < 0) {
      throw new IllegalArgumentException(
          "WaveformSpike3PtQcPluginConfiguration.from requires rmsLeadSampleDifferences >= 0");
    }

    int spikeRmsLagSampleDifferences;
    if (!pluginConfig.getParameter(RMS_LAG_SAMPLE_DEFFERENCES).isPresent()) {
      throw new IllegalArgumentException(
          "WaveformSpike3PtQcPluginConfiguration.from requires PluginConfiguration with a"
              + " configuration for (int) rmsLagSampleDifferences");
    }
    try {
      spikeRmsLagSampleDifferences = (int) pluginConfig.getParameter(RMS_LAG_SAMPLE_DEFFERENCES).get();
    } catch (ClassCastException e) {
      throw new IllegalArgumentException(
          "WaveformSpike3PtQcPluginConfiguration.from requires (int) rmsLagSampleDifferences", e);
    }

    if (spikeRmsLagSampleDifferences < 0) {
      throw new IllegalArgumentException(
          "WaveformSpike3PtQcPluginConfiguration.from requires rmsLagSampleDifferences >= 0");
    }

    if (spikeRmsLeadSampleDifferences + spikeRmsLagSampleDifferences < 2) {
      throw new IllegalArgumentException(
          "WaveformSpike3PtQcPluginConfiguration.from requires (rmsLeadSampleDifferences + rmsLagSampleDifferences) >= 2");
    }

    return new WaveformSpike3PtQcPluginConfiguration(spike3PtThreshold,
        spikeRmsAmplitudeRatioThreshold, spikeRmsLeadSampleDifferences, spikeRmsLagSampleDifferences);
  }
}
